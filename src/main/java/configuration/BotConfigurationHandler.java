package configuration;

import log.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class BotConfigurationHandler {

    private final File configFile;
    private final Properties defaults;
    private final Properties config;

    public BotConfigurationHandler() {
        defaults = new Properties();
        defaults.setProperty("zGPB_token", "");
        defaults.setProperty("zGPB_owner", "");
        defaults.setProperty("zGPB_idm_username", "");
        defaults.setProperty("zGPB_idm_password", "");
        defaults.setProperty("zGPB_log_content", "false");
        defaults.setProperty("zGPB_idm_enabled", "false");
        defaults.setProperty("zGPB_idm_refresh", "10");
        defaults.setProperty("zGPB_dict_api", "https://api.urbandictionary.com/v0/define?term=[X]");
        defaults.setProperty("zGPB_dict_main", "https://www.urbandictionary.com/define.php?term=[X]");
        defaults.setProperty("zGPB_sql_database", "");
        defaults.setProperty("zGPB_sql_user", "");
        defaults.setProperty("zGPB_sql_password", "");


        config = new Properties();
        configFile = new File("config.settings");
        if (!configFile.exists()) {
            Logger.logDebugMessage("Could not find previous configuration");
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try (final FileInputStream fis = new FileInputStream(configFile)) {
                config.load(fis);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // fill up new values
        defaults.forEach((k, v) -> config.putIfAbsent(k, v));
    }

    public String getConfigValue(String key) {
        if (System.getenv().containsKey(key))
            return System.getenv(key);

        return config.getProperty(key);
    }

    public boolean getConfigValueBoolean(String key) {
        return Boolean.parseBoolean(getConfigValue(key));
    }

    public int getConfigValueInteger(String key) {
        return Integer.parseInt(getConfigValue(key));
    }

    public void saveProperties() {
        try (final FileOutputStream fos = new FileOutputStream(configFile)) {
            config.store(fos, "[]");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
