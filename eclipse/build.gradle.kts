plugins {
    id("saros.gradle.eclipse.plugin")
    id("com.diffplug.p2.asmaven") // Provides the class FeaturesAndBundlesPublisher
}

val versionQualifier = ext.get("versionQualifier") as String

configurations {
    val testConfig by getting {}
    getByName("testImplementation") {
        extendsFrom(testConfig)
    }
}

sarosEclipse {
    manifest = file("META-INF/MANIFEST.MF")
    excludeManifestDependencies = listOf("saros.core", "org.junit", "org.eclipse.gef")
    isAddPdeNature = true
    isCreateBundleJar = true
    isAddDependencies = true
    pluginVersionQualifier = versionQualifier
}

sourceSets {
    main {
        java.srcDirs("src", "ext-src")
        resources.srcDirs("src")
        resources.exclude("**/*.java")
    }
    test {
        java.srcDirs("test/junit")
    }
}

dependencies {
    implementation(project(":saros.core"))
    testImplementation(project(path = ":saros.core", configuration = "testing"))
}

tasks {

    jar {
        into("assets") {
            from("assets")
        }
        into("icons") {
            from("icons")
        }
        from(".") {
            include("*.properties")
            include("readme.html")
            include("plugin.xml")
            include("LICENSE")
            include("CHANGELOG")
        }
    }

    val testJar by registering(Jar::class) {
        classifier = "tests"
        from(sourceSets["test"].output)
    }

    artifacts {
        add("testing", testJar)
    }


    /* Eclipse release tasks
     *
     * The following tasks provide the functionality of creating
     * an eclipse update-site (via "updateSite") or dropin (via "dropin").
     * The creation of the update-site uses as recommended the eclipse's
     * P2Directory. You can find a how-to here:
     * http://maksim.sorokin.dk/it/2010/11/26/creating-a-p2-repository-from-features-and-plugins/
     *
     * Instead of calling the P2Director via CLI we use the goomph
     * plugin's classes that provide an abstraction layer of the P2Director.
     */

    val updateSiteDirPath = "build/update-site"

    /* Step 0 of update-site creation
     *
     * Creates the structure:
     * update-site/
     *  |- features/
     *    |- feature.xml
     *  |- plugins/
     *    |- saros.core.jar
     *    |- saros.eclipse.jar
     *  |- category.xml
     */
    val updateSitePreparation by registering(Copy::class) {
        dependsOn(":saros.core:jar", ":saros.eclipse:jar")

        from("feature/feature.xml") {
            into("features")
        }
        into("plugins") {
            from(project.tasks.findByName("jar"))
            from(project(":saros.core").tasks.findByName("jar"))
        }
        from("update/site.xml")
        into(updateSiteDirPath)
    }

    /* Step 1 of update-site creation
     *
     * Creates the basic p2-Repository, but it is
     * not usable as update-site, because the plugins
     * are not visible to users.
     *
     * equivalent to P2Director call with:
     * <code>
     * org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher \
     *   -source <project dir>/build/update-site \
     *   -compress \
     *   -inplace \
     *   -append \
     *   -publishArtifacts
     * </code>
     */
    val updateSiteFeaturesAndBundlesPublishing by registering {
        dependsOn(updateSitePreparation)
        doLast {
            with(com.diffplug.gradle.p2.FeaturesAndBundlesPublisher()) {
                source(project.file(updateSiteDirPath))
                compress()
                inplace()
                append()
                publishArtifacts()
                runUsingBootstrapper()
            }
        }
    }

    /* Step 2 of update-site creation
     *
     * Adds the meta-data to the repository to make
     * the plugin accessible for users.
     *
     * equivalent to P2Director call with:
     * <code>
     * org.eclipse.equinox.p2.publisher.CategoryPublisher \
     *   -metadataRepository file:<project dir>/build/update-site \
     *   -categoryDefinition file:<project dir>/build/update-site/site.xml
     * </code>
     */
    val updateSiteCategoryPublishing by registering {
        dependsOn(updateSiteFeaturesAndBundlesPublishing)
        doLast {
            with(saros.gradle.eclipse.CategoryPublisher("4.8.0")) {
                metadataRepository(project.file(updateSiteDirPath))
                categoryDefinition(project.file("$updateSiteDirPath/site.xml"))
                runUsingPdeInstallation()
            }
        }
    }

    /* Step 3 of update-site creation
     *
     * The creation-process is already completed after
     * step 2, but this task removes the meta-data which
     * were necessary for update-site creation but are
     * not part of the update-site.
     */
    val updateSite by registering(Delete::class) {
        dependsOn(updateSiteCategoryPublishing)

        delete("$updateSiteDirPath/features/feature.xml")
        delete(fileTree("$updateSiteDirPath/plugins") {
            // Remove all bundles without version (which is appended during update-site build)
            exclude("*_*.jar")
        })
        delete("$updateSiteDirPath/site.xml")
    }

    /* Creates a dropin at build/dropin
     *
     * Creates an update-site, removes the superfluous meta-data
     * and creates a zip.
     */
    register("dropin", Zip::class) {
        dependsOn(updateSite)

        archiveFileName.set("saros-dropin.zip")
        destinationDirectory.set(project.file("build/dropin"))

        from(updateSiteDirPath) {
            exclude("*.jar")
        }
    }
}
