package discord.command;

import discord.command.commands.*;
import log.Logger;
import main.JADB;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
        commands.add(new RelayCommand());
        commands.add(new ChannelCommand());
        commands.add(new HelpCommand());

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
                // TODO: Permissions could be handled here
                c.onCommand(mre, msg, split);
                break;
            }
        }
    }

    public Set<Command> getRegisteredCommands() {
        return Collections.unmodifiableSet(commands);
    }

    private boolean isValidPrefix(char localPrefix, MessageReceivedEvent mre) {
        if (mre.isFromGuild())
            return localPrefix == JADB.INSTANCE.configurationHandler.getConfigCharValueForGuildByEvent(mre, "prefix");
        return localPrefix == PREFIX;
    }

}
