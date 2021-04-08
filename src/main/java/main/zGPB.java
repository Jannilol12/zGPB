package main;

import configuration.BotConfigurationHandler;
import configuration.GuildConfigurationHandler;
import console.ConsoleHandler;
import database.DatabaseHandler;
import discord.DiscordHandler;
import discord.command.CommandHandler;
import external.idm.GradeManager;
import log.Logger;
import timing.ReminderHandler;

public class zGPB {

    public static final zGPB INSTANCE = new zGPB();

    public final BotConfigurationHandler botConfigurationHandler;
    public final CommandHandler commandHandler;
    public final DatabaseHandler databaseHandler;
    public final GuildConfigurationHandler guildConfigurationHandler;
    public final DiscordHandler discordHandler;
    public final ConsoleHandler consoleHandler;
    public final GradeManager gradeManager;
    public final ReminderHandler reminderHandler;

    public zGPB() {
        Logger.logDebugMessage("Reached pre init");
        botConfigurationHandler = new BotConfigurationHandler();
        databaseHandler = new DatabaseHandler();
        guildConfigurationHandler = new GuildConfigurationHandler();
        commandHandler = new CommandHandler();
        discordHandler = new DiscordHandler();
        consoleHandler = new ConsoleHandler();
        gradeManager = new GradeManager();
        reminderHandler = new ReminderHandler();
    }

    public static void main(String[] args) {
        INSTANCE.init();
    }

    private void init() {
        Logger.logDebugMessage("Reached init");
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        databaseHandler.initiateDatabase();
        discordHandler.createConnection();
        consoleHandler.checkInput();

        Logger.logDebugMessage("Reached post init");
        reminderHandler.registerOldReminders();
    }

    private void shutdown() {
        Logger.logDebugMessage("Shutdown initiated");
        guildConfigurationHandler.saveConfig();
        discordHandler.getLocalJDA().shutdown();
        databaseHandler.closeDatabaseConnection();
        botConfigurationHandler.saveProperties();
    }

}
