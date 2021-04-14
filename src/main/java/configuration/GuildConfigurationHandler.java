package configuration;

import log.Logger;
import main.Util;
import main.zGPB;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GuildConfigurationHandler {

    private final Map<String, ConfigType> typeMappings;
    private final Map<String, String> defaults;

    private Map<Long, Map<String, String>> configMappings;

    public GuildConfigurationHandler() {
        configMappings = new HashMap<>();

        defaults = new HashMap<>();
        defaults.put("logging_enabled", "false");
        defaults.put("prefix", "!");
        defaults.put("allow_message_relay", "false");
        defaults.put("temporary_channel_allowed", "false");
        defaults.put("temporary_channel_category", "-1");
        defaults.put("temporary_channel_max", "1");
        defaults.put("emote_enabled", "false");
        defaults.put("grade_notification", "-1");
        defaults.put("fix_role_change", "false");
        defaults.put("fix_role_add", "");
        defaults.put("fix_role_remove", "no_role");
        defaults.put("fix_role_auto", "");
        defaults.put("fix_role_auto_msg", "");
        defaults.put("temporary_channel_assignment", "-1");
        defaults.put("mute_enabled", "true");

        typeMappings = new HashMap<>();
        typeMappings.put("logging_enabled", ConfigType.BOOLEAN);
        typeMappings.put("prefix", ConfigType.TEXT);
        typeMappings.put("allow_message_relay", ConfigType.BOOLEAN);
        typeMappings.put("temporary_channel_allowed", ConfigType.BOOLEAN);
        typeMappings.put("temporary_channel_category", ConfigType.DISCORD_ID);
        typeMappings.put("temporary_channel_max", ConfigType.NUMBER);
        typeMappings.put("emote_enabled", ConfigType.BOOLEAN);
        typeMappings.put("grade_notification", ConfigType.DISCORD_ID);
        typeMappings.put("fix_role_change", ConfigType.BOOLEAN);
        typeMappings.put("fix_role_add", ConfigType.TEXT);
        typeMappings.put("fix_role_remove", ConfigType.TEXT);
        typeMappings.put("fix_role_auto", ConfigType.DISCORD_ID);
        typeMappings.put("fix_role_auto_msg", ConfigType.DISCORD_ID);
        typeMappings.put("temporary_channel_assignment", ConfigType.DISCORD_ID);
        typeMappings.put("mute_enabled", ConfigType.BOOLEAN);

    }

    public void loadOldConfig() {
        configMappings = zGPB.INSTANCE.databaseHandler.getConfig();
        Logger.logDebugMessage("Loaded " + configMappings.size() + " configurations");
    }

    public void saveConfig() {
        configMappings.forEach((l, m) -> m.forEach((k, v) -> zGPB.INSTANCE.databaseHandler.insertConfig(l, k, v)));
    }

    public void updateGuild(long id) {
        if (!configMappings.containsKey(id))
            configMappings.put(id, new HashMap<>());

        defaults.forEach((k, v) -> configMappings.get(id).putIfAbsent(k, v));
    }

    public boolean setConfig(long guild, String key, String value) {
        if (!defaults.containsKey(key))
            return false;

        if (!configMappings.containsKey(guild))
            updateGuild(guild);

        if (!checkValue(key, value))
            return false;

        configMappings.get(guild).put(key, value);
        return true;
    }

    public String getGuildConfig(long guild) {
        StringBuilder sb = new StringBuilder();
        sb.append("zGPB configuration").append(System.lineSeparator());
        for (String configKey : configMappings.get(guild).keySet()) {
            sb.append(String.format("%-14s %-28s = %-16s", "[" + typeMappings.get(configKey) + "]", configKey, configMappings.get(guild).get(configKey))).append(System.lineSeparator());
        }
        return sb.toString();
    }

    public Set<Long> getIDsForKey(String key) {
        Set<Long> out = new HashSet<>();
        for (long guildID : configMappings.keySet()) {
            long v = getConfigLong(guildID, key);
            if (checkValue(key, "" + v))
                out.add(getConfigLong(guildID, key));
        }
        return out;
    }

    protected boolean checkValue(String key, String value) {
        ConfigType ct = typeMappings.get(key);
        return switch (ct) {
            case BOOLEAN -> value.equalsIgnoreCase("false") || value.equalsIgnoreCase("true");
            case DISCORD_ID -> Util.isValidDiscordID(value);
            case NUMBER -> value.length() <= 19 && value.chars().allMatch(Character::isDigit);
            default -> !(value.length() > 100);
        };
    }

    public boolean getConfigBoolean(long guild, String key) {
        if (key == null || !configMappings.containsKey(guild) || !defaults.containsKey(key))
            throw new IllegalArgumentException(guild + " " + key);
        return Boolean.parseBoolean(configMappings.get(guild).get(key));
    }

    public long getConfigLong(long guild, String key) {
        if (key == null || !configMappings.containsKey(guild) || !defaults.containsKey(key))
            throw new IllegalArgumentException(guild + " " + key);
        if (configMappings.get(guild).get(key).trim().isEmpty())
            return -1;
        return Long.parseLong(configMappings.get(guild).get(key));
    }

    public String getConfigString(long guild, String key) {
        if (key == null || !configMappings.containsKey(guild) || !defaults.containsKey(key))
            throw new IllegalArgumentException(guild + " " + key);
        return configMappings.get(guild).get(key);
    }

    public boolean getConfigBoolean(Guild guild, String key) {
        return getConfigBoolean(guild.getIdLong(), key);
    }

    public long getConfigLong(Guild guild, String key) {
        return getConfigLong(guild.getIdLong(), key);
    }

    public String getConfigString(Guild guild, String key) {
        return getConfigString(guild.getIdLong(), key);
    }

    public boolean getConfigBoolean(Message message, String key) {
        return getConfigBoolean(message.getGuild().getIdLong(), key);
    }

    public long getConfigLong(Message message, String key) {
        return getConfigLong(message.getGuild().getIdLong(), key);
    }

    public String getConfigString(Message message, String key) {
        return getConfigString(message.getGuild().getIdLong(), key);
    }

    public boolean getConfigBoolean(GenericMessageEvent event, String key) {
        return getConfigBoolean(event.getGuild().getIdLong(), key);
    }

    public long getConfigLong(GenericMessageEvent event, String key) {
        return getConfigLong(event.getGuild().getIdLong(), key);
    }

    public String getConfigString(GenericMessageEvent event, String key) {
        return getConfigString(event.getGuild().getIdLong(), key);
    }

}
