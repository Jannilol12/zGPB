package discord.command;

import discord.command.commands.InternalCommand;
import discord.command.commands.DebugCommand;
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
        commands.add(new DebugCommand());
        commands.add(new InternalCommand());

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
