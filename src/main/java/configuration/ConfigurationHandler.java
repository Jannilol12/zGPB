package configuration;

import log.Logger;
import main.Util;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

// TODO: 01/04/2021 redo configuration to ensure types
public class ConfigurationHandler {

    private final Path configFolder = Path.of("config/");
    private final HashMap<Long, Properties> guildPropertyMap;
    private final Properties defaultProperties;

    public ConfigurationHandler() {
        defaultProperties = new Properties();
        defaultProperties.setProperty("logging_enabled", "false");
        defaultProperties.setProperty("prefix", ".");
        // This can and will pose a security risk, so the user should be warned
        defaultProperties.setProperty("allow_message_relay", "false");
        defaultProperties.setProperty("temporary_channel_allowed", "false");
        defaultProperties.setProperty("temporary_channel_category", "-1");
        defaultProperties.setProperty("temporary_channel_max", "1");
        defaultProperties.setProperty("emote_enabled", "false");
        defaultProperties.setProperty("grade_notification", "none");
        defaultProperties.setProperty("fix_role_change", "false");
        defaultProperties.setProperty("fix_role_add", "");
        defaultProperties.setProperty("fix_role_remove", "no_role");

        // TODO: Think about using directory that is independent from working direction
        if (!Files.exists(configFolder, LinkOption.NOFOLLOW_LINKS)) {
            try {
                Files.createDirectory(configFolder);
            } catch (IOException e) {
                Logger.logException("Couldn't create config directory", e);
            }
        }

        guildPropertyMap = new HashMap<>();

        try {
            Files.list(configFolder).filter(path -> path.toString().endsWith(".config")).
                    forEach(configFile -> guildPropertyMap.put(getIDFromFileName(configFile), loadPropertiesFromPath(configFile)));
        } catch (Exception e) {
            Logger.logException("Couldn't list files", e);
        }

        Logger.logDebugMessage("Loaded " + guildPropertyMap.size() + " guild configs");
    }

    public void saveProperties() {
        Logger.logDebugMessage("Saving server properties");
        try {
            for (long id : guildPropertyMap.keySet()) {
                Path currentConfigFilePath = configFolder.resolve(id + ".config");
                if (!Files.exists(currentConfigFilePath)) {
                    Files.createFile(currentConfigFilePath);
                }
                guildPropertyMap.get(id).store(new FileOutputStream(currentConfigFilePath.toFile()), "zGPB configuration for guild=" + id);
            }
        } catch (Exception e) {
            guildPropertyMap.forEach((l, p) -> System.out.println(l + "   " + p.toString()));
            Logger.logException("Couldn't save properties", e);
        }
    }

    public void createGuildProperties(long idLong) {
        // Without condition so new configuration values won't break anything
        Logger.logDebugMessage("Updating or creating properties for " + idLong);
        // this should be done by passing the defaults in the guildProperties constructor, but somehow it's not working
        defaultProperties.forEach((k, v) -> guildPropertyMap.get(idLong).putIfAbsent(k.toString(), v.toString()));
    }

    public String getPropertiesAsString(long guildId) {
        StringWriter tempWriter = new StringWriter();
        try {
            guildPropertyMap.get(guildId).store(tempWriter, "zGPB configuration for guild=" + guildId);
        } catch (IOException e) {
            Logger.logException("Couldn't transform properties for " + guildId, e);
        }
        return tempWriter.getBuffer().toString();
    }

    public Set<Long> getChannelsForGradeNotification() {
        Set<Long> result = new HashSet<>();

        for (Long guild : guildPropertyMap.keySet()) {
            if (!guildPropertyMap.get(guild).getProperty("grade_notification").equals("none")) {
                String rawChannelID = guildPropertyMap.get(guild).getProperty("grade_notification");

                if (!Util.isValidDiscordID(rawChannelID)) {
                    Logger.logException("invalid channel id, skipping; " + rawChannelID + " - " + guild);
                } else {
                    try {
                        result.add(Long.parseLong(rawChannelID));
                    } catch (Exception e) {
                        Logger.logException(e);
                    }
                }
            }
        }

        return result;
    }

    public String getConfigValueForGuildByGuild(Guild g, String configKey) {
        return (guildPropertyMap.get(g.getIdLong()).getProperty(configKey));
    }

    public int getConfigIntValueForGuildByEvent(MessageReceivedEvent mre, String configKey) {
        return Integer.parseInt(guildPropertyMap.get(mre.getGuild().getIdLong()).getProperty(configKey));
    }

    public long getConfigLongValueForGuildByEvent(MessageReceivedEvent mre, String configKey) {
        return Long.parseLong(guildPropertyMap.get(mre.getGuild().getIdLong()).getProperty(configKey));
    }

    public char getConfigCharValueForGuildByEvent(MessageReceivedEvent mre, String configKey) {
        return guildPropertyMap.get(mre.getGuild().getIdLong()).getProperty(configKey).charAt(0);
    }

    public String getConfigValueForGuildByEvent(MessageReceivedEvent mre, String configKey) {
        return guildPropertyMap.get(mre.getGuild().getIdLong()).getProperty(configKey);
    }

    public boolean getConfigBooleanValueForGuildByEvent(MessageReceivedEvent mre, String configKey) {
        return Boolean.parseBoolean((guildPropertyMap.get(mre.getGuild().getIdLong()).getProperty(configKey)));
    }

    public boolean getConfigBooleanValueForGuildByGuild(Guild g, String configKey) {
        return Boolean.parseBoolean((guildPropertyMap.get(g.getIdLong()).getProperty(configKey)));
    }

    public boolean getConfigBooleanValueForGuildByMessage(Message m, String configKey) {
        return Boolean.parseBoolean(guildPropertyMap.get(m.getGuild().getIdLong()).getProperty(configKey));
    }

    public boolean getConfigBooleanValueForGuildByChannel(TextChannel c, String configKey) {
        return Boolean.parseBoolean((guildPropertyMap.get(c.getGuild().getIdLong()).getProperty(configKey)));
    }

    public void setConfigValueForGuild(MessageReceivedEvent mre, String configKey, String configValue) {
        if (!defaultProperties.containsKey(configKey)) {
            Logger.logDebugMessage("Adding new values is not allowed, " + mre.getMessage().getIdLong() + " : " + mre.getMessage().getContentRaw());
            return;
        }
        guildPropertyMap.get(mre.getGuild().getIdLong()).setProperty(configKey, configValue);
    }

    private Long getIDFromFileName(Path file) {
        return Long.parseLong(file.toString().replace(".config", "").replace("config\\", ""));
    }

    private Properties loadPropertiesFromPath(Path currentPath) {
        Properties current = new Properties();
        try {
            current.load(new FileInputStream(currentPath.toFile()));
        } catch (IOException e) {
            Logger.logException("Couldn't load config file " + currentPath.toString(), e);
        }
        return current;
    }

}
