package discord.command;

import discord.command.commands.DirectCommand;
import discord.command.commands.InfoCommand;
import discord.command.commands.StatusCommand;
import log.Logger;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashSet;

public class CommandHandler {

    private final HashSet<Command> commands;
    // TODO: Add configuration management
    private final char PREFIX = '+';

    public CommandHandler() {
        this.commands = new HashSet<>();
        registerCommands();
    }

    private void registerCommands() {
        commands.add(new StatusCommand());
        commands.add(new InfoCommand());
        commands.add(new DirectCommand());

        Logger.logDebugMessage("Registererd " + commands.size() + " commands");
    }

    public void handleMessage(MessageReceivedEvent mre) {
        String msg = mre.getMessage().getContentRaw();
        if (msg.charAt(0) != PREFIX)
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
