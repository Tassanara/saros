package saros.stf;

import java.io.File;
import java.net.URL;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.status.StatusLogger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/** The activator class controls the plug-in life cycle */
public class Activator extends AbstractUIPlugin {

  private static final String LOG4J2_CONFIG_FILENAME = "saros_log4j2.xml";
  // The plug-in ID
  public static final String PLUGIN_ID = "saros.stf";

  // The shared instance
  private static Activator plugin;

  /** The constructor */
  public Activator() {}

  /*
   * (non-Javadoc)
   *
   * @see
   * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
   * )
   */
  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
    // Configure logging before StartupSarosSTF is started
    setupLoggers();
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
   * )
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    super.stop(context);
  }

  /**
   * Returns the shared instance
   *
   * @return the shared instance
   */
  public static Activator getDefault() {
    return plugin;
  }

  /**
   * Returns an image descriptor for the image file at the given plug-in relative path
   *
   * @param path the path
   * @return the image descriptor
   */
  public static ImageDescriptor getImageDescriptor(String path) {
    return imageDescriptorFromPlugin(PLUGIN_ID, path);
  }

  private void setupLoggers() {
    try {
      final File mainLogDir = getStateLocation().toFile();
      final String logDir = mainLogDir + File.separator + "log";

      final URL configFileU = getBundle().getResource(LOG4J2_CONFIG_FILENAME);
      if (configFileU == null) {
        throw new RuntimeException(
            "Failed to get the log4j2 config file from the bundle class loader");
      }
      final String configFileUri = configFileU.toString();

      // set config file to be used
      System.setProperty("log4j.configurationFile", configFileUri);
      // set properties to be used in the config file
      System.setProperty("logging.logDir", logDir);
      System.setProperty("logging.logLevel", Level.TRACE.name());

      // trigger reconfiguration with new properties and config file
      final LoggerContext context = (LoggerContext) LogManager.getContext(false);
      context.reconfigure();
    } catch (RuntimeException e) {
      StatusLogger.getLogger().error("initializing loggers failed", e);
      e.printStackTrace();
    }
  }
}
