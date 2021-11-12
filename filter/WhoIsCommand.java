package discord.command.commands;

import database.DataHandler;
import discord.command.GuildCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.format.DateTimeFormatter;

public class WhoIsCommand extends GuildCommand {

    public WhoIsCommand() {
        super("whois", "whois <user>", "retrieves information about the user", 2, "resolve");
    }

    @Override
    protected boolean onCommand(MessageReceivedEvent mre, String givenCommand, String[] splitCommand) {
        if (!super.onCommand(mre, givenCommand, splitCommand))
            return false;

        if (mre.getMessage().getMentionedMembers().size() != 1) {
            mre.getMessage().reply("you need to mention a single member").mentionRepliedUser(false);
            return true;
        }

        Member mentioned = mre.getMessage().getMentionedMembers().get(0);

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(mentioned.getUser().getAsTag() + (mentioned.getNickname() != null ? " [" + mentioned.getNickname() + "]" : ""));

        eb.setThumbnail(mentioned.getUser().getAvatarUrl());
        eb.setFooter("id = " + mentioned.getId());

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

        eb.addField("creation date", mentioned.getTimeCreated().format(dtf), true);
        eb.addField("join date", mentioned.getTimeJoined().format(dtf), true);
        eb.addField("messages", "" + DataHandler.getMessageCountByUser(mentioned.getIdLong()), true);

        mre.getMessage().reply(eb.build()).mentionRepliedUser(false).queue();

        return true;
    }
}
