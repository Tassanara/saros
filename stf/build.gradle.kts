plugins {
  id("saros.gradle.eclipse.plugin")
}

val versionQualifier = (ext.get("versionQualifier") ?: "") as String
val junitVersion = ext.get("junitVersion")

sarosEclipse {
    manifest = file("META-INF/MANIFEST.MF")
    excludeManifestDependencies = listOf("saros.core", "saros.eclipse", "org.junit", "org.eclipse.gef")
    isCreateBundleJar = true
    isAddDependencies = true
    pluginVersionQualifier = versionQualifier
}

configurations {
    val releaseDep by getting {}
    val compile by getting {
        extendsFrom(releaseDep)
    }
}

dependencies {
    if (junitVersion != null) {
        compile(junitVersion)
    }
    compile(project(":saros.core"))
    compile(project(":saros.eclipse"))

    compile(project(path = ":saros.eclipse", configuration = "testing"))

    releaseDep(fileTree("libs"))
}

sourceSets {
    main {
        java.srcDirs("src")
        resources.srcDirs("src")
        resources.exclude("**/*.java")
    }
    test {
        java.srcDirs("test")
    }
}

tasks {
    jar {
        from("plugin.xml")
        into("test/resources") {
            from("test/resources")
        }
    }

    val testJar by registering(Jar::class) {
        classifier = "tests"
        from(sourceSets["test"].output)
    }

    artifacts {
        add("testing", testJar)
    }
    /*
     * Copy the log4j2 files into the eclipse project dir
     * to make them available for PDE.
     */
    register("copyLogFiles", Copy::class) {
      into("${project.projectDir}/")
      from(rootProject.file("log4j2.xml"))
      from(rootProject.file("saros_log4j2.xml"))
    }
}
