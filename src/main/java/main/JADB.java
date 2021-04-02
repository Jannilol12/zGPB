package main;

import configuration.ConfigurationHandler;
import console.ConsoleHandler;
import database.DatabaseHandler;
import discord.DiscordHandler;
import discord.command.CommandHandler;
import external.GradeListener;
import log.Logger;
import timing.ReminderHandler;

public class JADB {

    public static final JADB INSTANCE = new JADB();

    public final ConfigurationHandler configurationHandler;
    public final CommandHandler commandHandler;
    public final DatabaseHandler databaseHandler;
    public final DiscordHandler discordHandler;
    public final ConsoleHandler consoleHandler;
    public final GradeListener gradeListener;
    public final ReminderHandler reminderHandler;

    public JADB() {
        Logger.logDebugMessage("reached pre init");
        configurationHandler = new ConfigurationHandler();
        commandHandler = new CommandHandler();
        databaseHandler = new DatabaseHandler();
        discordHandler = new DiscordHandler();
        consoleHandler = new ConsoleHandler();
        gradeListener = new GradeListener();
        reminderHandler = new ReminderHandler();
    }

    public static void main(String[] args) {
        INSTANCE.init();
    }

    private void init() {
        Logger.logDebugMessage("reached init");
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        databaseHandler.initiateDatabase();
        discordHandler.createConnection();
        consoleHandler.checkInput();

        Logger.logDebugMessage("reached post init");
        reminderHandler.registerOldReminders();
    }

    private void shutdown() {
        Logger.logDebugMessage("Shutdown initiated");
        discordHandler.getLocalJDA().shutdown();
        databaseHandler.closeDatabaseConnection();
        configurationHandler.saveProperties();
    }

}
