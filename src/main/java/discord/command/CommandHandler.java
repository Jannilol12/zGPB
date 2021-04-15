package discord.command;

import discord.command.commands.*;
import discord.command.commands.direct.RelayCommand;
import discord.command.commands.guild.*;
import log.Logger;
import main.Util;
import main.zGPB;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CommandHandler {

    private final HashSet<Command> commands;
    private final char PREFIX = '!';

    public CommandHandler() {
        this.commands = new HashSet<>();
        registerCommands();
    }

    private void registerCommands() {
        commands.add(new StatusCommand());
        commands.add(new InternalCommand());
        commands.add(new ConfigCommand());
        commands.add(new RelayCommand());
        commands.add(new HelpCommand());
        commands.add(new ScanCommand());
        commands.add(new EmoteCommand());
        commands.add(new DefineCommand());
        commands.add(new RemindMeCommand());
        commands.add(new MuteCommand());

        Logger.logDebugMessage("Registered " + commands.size() + " commands");
    }

    public void handleMessage(MessageReceivedEvent mre) {
        String msg = mre.getMessage().getContentRaw();

        if (msg.trim().isEmpty() || msg.trim().length() < 2)
            return;

        if (!isValidPrefix(msg.charAt(0), mre))
            return;

        msg = msg.substring(1);
        String[] split = msg.split(" ");
        boolean wasFound = false;
        for (Command c : commands) {
            if (split[0].equals(c.getName()) || (c.getAliases() != null && c.getAliases().contains(split[0]))) {
                c.onCommand(mre, msg, split);
                wasFound = true;
                break;
            }
        }

        if (!wasFound) {
            Command fuzz = Util.getFuzzyMatchedCommand(split[0]);
            if (fuzz != null)
                fuzz.onCommand(mre, msg, split);
        }

    }

    public Set<Command> getRegisteredCommands() {
        return Collections.unmodifiableSet(commands);
    }

    private boolean isValidPrefix(char localPrefix, MessageReceivedEvent mre) {
        if (mre.isFromGuild())
            return localPrefix == zGPB.INSTANCE.guildConfigurationHandler.getConfigString(mre, "prefix").charAt(0);
        return localPrefix == PREFIX;
    }

}
