package discord.command;

import discord.command.commands.ConfigCommand;
import discord.command.commands.InternalCommand;
import discord.command.commands.StatusCommand;
import discord.command.commands.ValidateCommand;
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
        commands.add(new ValidateCommand());

        Logger.logDebugMessage("Registered " + commands.size() + " commands");
    }

    public void handleMessage(MessageReceivedEvent mre) {
        String msg = mre.getMessage().getContentRaw();

        if (!isValidPrefix(msg.charAt(0), mre))
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

    private boolean isValidPrefix(char localPrefix, MessageReceivedEvent mre) {
        if (mre.isFromGuild())
            return localPrefix == JADB.INSTANCE.configurationHandler.getConfigCharValueForGuildByEvent(mre, "prefix");
        return localPrefix == PREFIX;
    }

}
