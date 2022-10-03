package CCDetect.lsp.server;

import CCDetect.lsp.utils.JSONUtility;

/**
 * Configuration
 */
public class Configuration {
    private static Configuration instance;
    // Create some sort of map for getting initialization options
    private String language;

    public String getLanguage() {
        return language;
    }

    public static void createInstanceFromJson(String json) {
        instance = JSONUtility.toModel(json, Configuration.class);
    }

    public static Configuration getInstance() {
        return instance;
    }
}
