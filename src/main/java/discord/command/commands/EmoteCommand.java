package discord.command.commands;

import discord.command.GuildCommand;
import main.zGPB;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import network.NetworkUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class EmoteCommand extends GuildCommand {

    public EmoteCommand() {
        super("emote", "emote <name>", "adds the emote that is attached to the corresponding message", 2);
    }

    @Override
    protected boolean onCommand(MessageReceivedEvent mre, String givenCommand, String[] splitCommand) {
        if (!super.onCommand(mre, givenCommand, splitCommand))
            return false;

        if (!zGPB.INSTANCE.guildConfigurationHandler.getConfigBoolean(mre, "emote_enabled")) {
            mre.getMessage().reply("this guild does not support adding emotes").mentionRepliedUser(false).queue();
            return true;
        }

        if (mre.getMessage().getAttachments().isEmpty() || mre.getMessage().getAttachments().size() > 1) {
            mre.getMessage().reply("there is no file attached / there are too many files attached").mentionRepliedUser(false).queue();
            return true;
        }

        if (!mre.getGuild().getSelfMember().hasPermission(Permission.MANAGE_EMOTES)) {
            mre.getMessage().reply("the bot does not have sufficient permissions").mentionRepliedUser(false).queue();
            return true;
        }

        if (!mre.getGuild().getEmotesByName(splitCommand[1], false).isEmpty()) {
            mre.getMessage().reply("this name is already being used").mentionRepliedUser(false).queue();
            return true;
        }

        List<Message.Attachment> messageAttachments = mre.getMessage().getAttachments();


        if (messageAttachments.size() == 0) {
            mre.getMessage().reply("no image attached").mentionRepliedUser(false).queue();
            return true;
        }

        Message.Attachment imageAttachment = mre.getMessage().getAttachments().get(0);

        if (!(imageAttachment.getFileExtension().equals("png") || imageAttachment.getFileExtension().equals("jpg"))) {
            mre.getMessage().reply("png/jpg only").mentionRepliedUser(false).queue();
            return true;
        }

        byte[] base64Image = NetworkUtil.getBytesFromURL(imageAttachment.getUrl());
        if (base64Image == null) {
            mre.getMessage().reply("couldn't fetch image").mentionRepliedUser(false).queue();
            return true;
        }

        Emote e = mre.getGuild().createEmote(splitCommand[1],
                Icon.from(base64Image, imageAttachment.getFileExtension().equals("png") ? Icon.IconType.PNG : Icon.IconType.JPEG)).
                complete();
        mre.getMessage().reply("emote successfully created").mentionRepliedUser(false).queue();
        mre.getMessage().addReaction(e).queueAfter(500, TimeUnit.MILLISECONDS);
        return true;
    }
}
