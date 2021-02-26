package main;

import console.ConsoleHandler;
import database.DatabaseHandler;
import discord.DiscordHandler;
import discord.command.CommandHandler;
import log.Logger;

public class JADB {

    public static final JADB INSTANCE = new JADB();

    public final CommandHandler commandHandler;
    public final DatabaseHandler databaseHandler;
    public final DiscordHandler discordHandler;
    public final ConsoleHandler consoleHandler;


    public JADB() {
        Logger.logDebugMessage("reached pre init");
        commandHandler = new CommandHandler();
        databaseHandler = new DatabaseHandler();
        discordHandler = new DiscordHandler();
        consoleHandler = new ConsoleHandler();
    }

    public static void main(String[] args) {
        INSTANCE.init();
    }

    private void init() {
        Logger.logDebugMessage("Reached init");
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        databaseHandler.initiateDatabase();
        discordHandler.createConnection();

        new Thread(consoleHandler::checkInput);

    }

    private void shutdown() {
        discordHandler.getLocalJDA().shutdown();
        databaseHandler.closeDatabaseConnection();
        Logger.logDebugMessage("caught shutdown");
    }

}
