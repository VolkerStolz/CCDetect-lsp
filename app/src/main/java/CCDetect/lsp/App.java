/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package CCDetect.lsp;

import java.io.IOException;
import java.util.logging.Logger;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;

import CCDetect.lsp.server.CCLanguageServer;
import CCDetect.lsp.utils.CCFileStateLogger;
import CCDetect.lsp.utils.CCGeneralLogger;

public class App {

    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    public static Launcher<LanguageClient> createLauncher(LanguageServer server) {
        Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(
                server,
                System.in,
                System.out);
        LanguageClient client = launcher.getRemoteProxy();
        ((LanguageClientAware) server).connect(client);

        return launcher;
    }

    public static void setupLogging() {
        try {
            CCGeneralLogger.setup();
            CCFileStateLogger.setup();
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
        }
    }

    public static void main(String[] args) {
        setupLogging();
        LanguageServer server = CCLanguageServer.getInstance();
        Launcher<LanguageClient> launcher = createLauncher(server);
        launcher.startListening();
    }
}
