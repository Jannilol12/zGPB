package discord.command.commands;

import discord.DictionaryEntry;
import discord.EmbedField;
import discord.MessageCrafter;
import discord.command.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import network.NetworkUtil;

public class DefineCommand extends Command {

    public DefineCommand() {
        super("define", "define <term>", "gets the definition from urban dictionary", 2);
    }

    @Override
    protected boolean onCommand(MessageReceivedEvent mre, String givenCommand, String[] splitCommand) {
        if (!super.onCommand(mre, givenCommand, splitCommand))
            return false;

        DictionaryEntry highest = NetworkUtil.getDefinitionForWord(splitCommand[1]);
        if (highest == null) {
            mre.getMessage().reply("couldn't resolve the given term").mentionRepliedUser(false).queue();
        } else {
            mre.getMessage().reply(MessageCrafter.craftGenericEmbedMessage("definition for " + splitCommand[1] + " from " + highest.author(),
                    new EmbedField("definition", highest.definition(), false),
                    new EmbedField("example", highest.example(), false),
                    new EmbedField("thumbs up", highest.thumbsUp() + "", true),
                    new EmbedField("thumbs down", highest.thumbsDown() + "", true),
                    new EmbedField("written on", highest.date().split("T")[0], true),
                    new EmbedField("link", highest.url(), false))).
                    mentionRepliedUser(false).queue();
        }

        return true;
    }
}
