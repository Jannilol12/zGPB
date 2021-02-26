import console.ConsoleHandler;
import discord.DiscordHandler;
import log.Logger;

import java.io.Console;

public class JADB {

    public static final JADB INSTANCE = new JADB();

    public final DiscordHandler discordHandler;
    public final ConsoleHandler consoleHandler;

    public static void main(String[] args) {
        INSTANCE.init();
    }

    public JADB() {
        Logger.logDebugMessage("reached pre init");
        discordHandler = new DiscordHandler();
        consoleHandler = new ConsoleHandler();
    }

    private void init() {
        Logger.logDebugMessage("reached init");
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        discordHandler.createConnection();

        consoleHandler.checkInput();

    }

    private void shutdown() {
        Logger.logDebugMessage("shutdown");
    }

}
