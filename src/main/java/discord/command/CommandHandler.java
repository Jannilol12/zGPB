package discord.command;

import discord.command.commands.ConfigCommand;
import discord.command.commands.InternalCommand;
import discord.command.commands.StatusCommand;
import log.Logger;
import main.JADB;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashSet;

public class CommandHandler {

    private final HashSet<Command> commands;
    private final char PREFIX = '.';

    public CommandHandler() {
        this.commands = new HashSet<>();
        registerCommands();
    }

    private void registerCommands() {
        commands.add(new StatusCommand());
        commands.add(new InternalCommand());
        commands.add(new ConfigCommand());

        Logger.logDebugMessage("Registered " + commands.size() + " commands");
    }

    public void handleMessage(MessageReceivedEvent mre) {
        String msg = mre.getMessage().getContentRaw();
        char localPrefix = msg.charAt(0);

        if ((mre.isFromGuild() && localPrefix != JADB.INSTANCE.configurationHandler.getConfigCharValueForGuildByEvent(mre, "prefix")))
            return;
        else if (localPrefix != PREFIX)
            return;

        msg = msg.substring(1);
        String[] split = msg.split(" ");
        for (Command c : commands) {
            if (split[0].equals(c.getName()) || (c.getAliases() != null && c.getAliases().contains(split[0]))) {
                c.onCommand(mre, msg, split);
                break;
            }
        }
    }

}
