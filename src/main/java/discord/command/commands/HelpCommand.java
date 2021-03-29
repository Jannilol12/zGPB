package discord.command.commands;

import discord.EmbedField;
import discord.MessageCrafter;
import discord.command.Command;
import main.JADB;
import main.Util;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("help", "help [command]", "shows all possible commands or how to use a command", 1);
    }

    @Override
    protected boolean onCommand(MessageReceivedEvent mre, String givenCommand, String[] splitCommand) {
        if (!super.onCommand(mre, givenCommand, splitCommand))
            return false;

        if (splitCommand.length > 2) {
            mre.getMessage().reply("wrong usage, usage: `" + usage + "`").mentionRepliedUser(false).queue();
        } else if (splitCommand.length == 2) {
            List<Command> a = JADB.INSTANCE.commandHandler.getRegisteredCommands().
                    stream().filter(c -> c.getName().equals(splitCommand[1])).collect(Collectors.toList());

            Command cur = null;
            if (a.size() == 0) {
                cur = Util.getFuzzyMatchedCommand(splitCommand[1]);
                if (cur == null) {
                    mre.getMessage().reply("command wasn't found and couldn't be fuzzy matched").mentionRepliedUser(false).queue();
                    return true;
                }
            }

            if (cur == null)
                cur = a.get(0);

            mre.getMessage().reply(MessageCrafter.craftGenericEmbedMessage("help [" + splitCommand[1] + "]",
                    new EmbedField("name", cur.getName(), true),
                    new EmbedField("usage", cur.getUsage(), true),
                    new EmbedField("description", cur.getDescription(), false),
                    new EmbedField("aliases", cur.getAliases() == null ? "[]" : cur.getAliases().toString(), true),
                    new EmbedField("type", cur.getCommandType().toString(), true)))
                    .mentionRepliedUser(false).queue();
        } else {
            HashSet<EmbedField> commandFields = new HashSet<>();
            for (Command c : JADB.INSTANCE.commandHandler.getRegisteredCommands()) {
                commandFields.add(new EmbedField(c.getName(), c.getDescription(), false));
            }
            mre.getMessage().reply(MessageCrafter.craftGenericEmbedMessage("help", commandFields)).mentionRepliedUser(false).queue();
        }

        return true;

    }
}
