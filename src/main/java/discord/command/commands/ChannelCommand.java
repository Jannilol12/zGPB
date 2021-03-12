package discord.command.commands;

import discord.command.Command;
import main.JADB;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChannelCommand extends Command {

    private static final ArrayList<Map.Entry<Long, VoiceChannel>> channelMappings = new ArrayList<>();

    public ChannelCommand() {
        super("channel", "channel <create|modify|delete> name [size]", "creates a temporary channel", 2);
    }

    public static void deleteUnusedChannels() {
        // Error handling
        List<Long> tempChannels = JADB.INSTANCE.databaseHandler.getAllTemporaryChannels();
        if (tempChannels != null) {
            for (long channelID : JADB.INSTANCE.databaseHandler.getAllTemporaryChannels()) {
                // TODO: If currentVoice is null, that means that the database is inconsistent, so the entry should be removed
                VoiceChannel currentVoice = JADB.INSTANCE.discordHandler.getLocalJDA().getVoiceChannelById(channelID);
                if (currentVoice != null && currentVoice.getMembers().size() == 0) {
                    JADB.INSTANCE.databaseHandler.removeTemporaryChannel(channelID);
                    currentVoice.delete().queue();
                }
            }
        }

    }

    private static int getChannelCountByUser(long authorID) {
        int count = 0;
        for (Map.Entry<Long, VoiceChannel> e : channelMappings) {
            if (e.getKey() == authorID)
                count++;
        }
        return count;
    }

    private static VoiceChannel getChannelByUserAndChannel(long authorID, long channelID) {
        int count = 0;
        for (Map.Entry<Long, VoiceChannel> e : channelMappings) {
            if (e.getKey() == authorID && e.getValue().getIdLong() == channelID)
                return e.getValue();
        }
        return null;
    }

    @Override
    protected boolean onCommand(MessageReceivedEvent mre, String givenCommand, String[] splitCommand) {
        if (!super.onCommand(mre, givenCommand, splitCommand))
            return false;

        Category category = mre.getGuild().getCategoryById(JADB.INSTANCE.configurationHandler.
                getConfigLongValueForGuildByEvent(mre, "temporary_channel_category"));
        if (category == null) {
            mre.getMessage().reply("this guild does not have a temporary channel category set").mentionRepliedUser(false).queue();
            return true;
        }

        // Permission handling, maybe require roles
        String[] secondSplit = givenCommand.split(" ");

        int size = -1;
        if (secondSplit.length == 4) {

            try {
                size = Integer.parseInt(secondSplit[3]);
            } catch (NumberFormatException nfe) {
                mre.getMessage().reply("please enter a valid number").mentionRepliedUser(false).queue();
                return true;
            }
            if (size > 99) {
                mre.getMessage().reply("please enter a size below 100").mentionRepliedUser(false).queue();
                return true;
            }

        } else {
            size = 99;
        }

        if (splitCommand[1].equals("create")) {
            if (secondSplit.length > 4)
                return false;

            int maxGuildBitrate = mre.getGuild().getMaxBitrate();
            long authorId = mre.getAuthor().getIdLong();

            int maxChannelsPerUser = JADB.INSTANCE.configurationHandler.getConfigIntValueForGuildByEvent(mre, "temporary_channel_max");

            if (getChannelCountByUser(authorId) >= maxChannelsPerUser) {
                mre.getMessage().reply("you are only allowed to create " + maxChannelsPerUser + " channels").mentionRepliedUser(false).queue();
                return true;
            }

            try {
                category.createVoiceChannel(secondSplit[2]).setUserlimit(size).setBitrate(maxGuildBitrate).
                        queue(channel -> {
                            channelMappings.add(new AbstractMap.SimpleEntry<>(authorId, channel));
                            JADB.INSTANCE.databaseHandler.insertTemporaryChannel(channel.getIdLong(), mre.getAuthor().getIdLong(), mre.getGuild().getIdLong(), channel.getName());
                        });
                mre.getMessage().reply("channel created successfully").mentionRepliedUser(false).queue();
            } catch (InsufficientPermissionException ipe) {
                mre.getMessage().reply("the bot has insufficient permissions").mentionRepliedUser(false).queue();
            }

        } else if (splitCommand[1].equals("modify")) {
            if (secondSplit.length > 4)
                return false;

            long channelID = JADB.INSTANCE.databaseHandler.getTemporaryChannelIDByNameAndAuthor(mre.getAuthor().getIdLong(), secondSplit[2]);

            if (channelID == -1) {
                mre.getMessage().reply("channel name is ambiguous").mentionRepliedUser(false).queue();
                return false;
            } else if (channelID == -2) {
                mre.getMessage().reply("you can only modify temporary channels that you created").mentionRepliedUser(false).queue();
                return false;
            }

            VoiceChannel vc = getChannelByUserAndChannel(mre.getAuthor().getIdLong(), channelID);

            if (vc != null) {
                vc.getManager().setName(vc.getName()).setUserLimit(size == 99 ? vc.getUserLimit() : size).queue();
                mre.getMessage().reply("channel successfully modified").mentionRepliedUser(false).queue();
            } else {
                mre.getMessage().reply("the channel was not found, this is probably not your fault").mentionRepliedUser(false).queue();
            }

        } else if (splitCommand[1].equals("delete")) {

        }


        return true;
    }

}
