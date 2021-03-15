package discord.command.commands;

import discord.EmbedField;
import discord.MessageCrafter;
import discord.command.Command;
import main.JADB;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashSet;

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
            // TODO: Add documentation for commands
        } else {
            HashSet<EmbedField> commandFields = new HashSet<>();
            for(Command c : JADB.INSTANCE.commandHandler.getRegisteredCommands()) {
                commandFields.add(new EmbedField(c.getName(), c.getDescription(), false));
            }
            mre.getMessage().reply(MessageCrafter.craftGenericEmbedMessage("help",commandFields)).mentionRepliedUser(false).queue();
        }

        return true;

    }
}
