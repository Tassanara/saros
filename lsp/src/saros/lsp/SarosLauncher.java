package saros.lsp;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.lsp4j.jsonrpc.Launcher;

import saros.lsp.extensions.client.SarosLanguageClient;
import saros.lsp.log.LanguageClientAppender;
import saros.lsp.log.LogOutputStream;

/**
 * Entry point for the Saros LSP server.
 */
public class SarosLauncher {

    private static final Logger LOG = Logger.getLogger(SarosLauncher.class);
    private static final String LOGGING_CONFIG_FILE = "/log4j.properties";

    
    /** 
     * Starts the server.
     * 
     * @param args command-line arguments
     * @throws Exception on critical failures
     */
    public static void main(String[] args) throws Exception {

        if (args.length > 1) {
            throw new IllegalArgumentException("wrong number of arguments");
        } else if (args.length != 1) {
            throw new IllegalArgumentException("port parameter not supplied");
        }

        URL log4jProperties = SarosLauncher.class.getResource(LOGGING_CONFIG_FILE);
        PropertyConfigurator.configure(log4jProperties);

        LogOutputStream los = new LogOutputStream(LOG, Level.DEBUG);
        PrintStream ps = new PrintStream(los);
        System.setOut(ps);

        int port = Integer.parseInt(args[0]);
        Socket socket = new Socket("localhost", port);
        
        LOG.info("listening on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    socket.close();
                } catch (IOException e) {
                    // NOP
                }
            } 
        }); 
        
        SarosLanguageServerImpl langSvr = new SarosLanguageServerImpl();
        Launcher<SarosLanguageClient> l = Launcher.createLauncher(langSvr, SarosLanguageClient.class, socket.getInputStream(), socket.getOutputStream());
       
        SarosLanguageClient langClt = l.getRemoteProxy();
        LOG.addAppender(new LanguageClientAppender(langClt));
        langSvr.connect(langClt);
        
        l.startListening();
    }
}