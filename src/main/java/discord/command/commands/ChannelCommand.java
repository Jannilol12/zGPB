package discord.command.commands;

import discord.DataHandler;
import discord.command.Command;
import discord.command.CommandType;
import main.zGPB;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChannelCommand extends Command {

    public static final ArrayList<Map.Entry<Long, VoiceChannel>> channelMappings = new ArrayList<>();

    public ChannelCommand() {
        super("channel", "channel <create|modify|delete> name [size]", "creates a temporary channel", 2, CommandType.GUILD);
    }

    public static void deleteUnusedChannels() {
        List<Long> tempChannels = DataHandler.getTemporaryChannels();
        if (tempChannels != null) {
            for (long channelID : tempChannels) {
                VoiceChannel currentVoice = zGPB.INSTANCE.discordHandler.getLocalJDA().getVoiceChannelById(channelID);
                if (currentVoice != null) {
                    if (currentVoice.getMembers().size() == 0) {
                        DataHandler.removeTemporaryChannel(channelID);
                        currentVoice.delete().queue();
                    }
                } else {
                    DataHandler.removeTemporaryChannel(channelID);
                }
            }
        }
    }

    public static void scheduleChannelDeletion(long channelID) {
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.schedule(() -> {
            VoiceChannel currentVoice = zGPB.INSTANCE.discordHandler.getLocalJDA().getVoiceChannelById(channelID);
            if (currentVoice != null) {
                if (currentVoice.getMembers().size() == 0) {
                    DataHandler.removeTemporaryChannel(channelID);
                    currentVoice.delete().queue();
                } else {
                    scheduleChannelDeletion(channelID);
                }
            } else {
                DataHandler.removeTemporaryChannel(channelID);
            }
        }, LocalDateTime.now().until(LocalTime.now().plus(5, ChronoUnit.MINUTES), ChronoUnit.SECONDS), TimeUnit.SECONDS);
    }

    public static int getChannelCountByUser(long authorID) {
        int count = 0;
        for (Map.Entry<Long, VoiceChannel> e : channelMappings) {
            if (e.getKey() == authorID)
                count++;
        }
        return count;
    }

    public static boolean isTemporaryChannel(long channelID) {
        for (Map.Entry<Long, VoiceChannel> e : channelMappings) {
            if (e.getValue().getIdLong() == channelID)
                return true;
        }
        return false;
    }

    private static VoiceChannel getChannelByUserAndChannel(long authorID, long channelID) {
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

        Category category = mre.getGuild().getCategoryById(zGPB.INSTANCE.configurationHandler.
                getConfigLongValueForGuildByEvent(mre, "temporary_channel_category"));
        if (category == null) {
            mre.getMessage().reply("this guild does not have a temporary channel category set").mentionRepliedUser(false).queue();
            return true;
        }

        // Permission handling, maybe require roles
        String[] secondSplit = givenCommand.split(" ");

        int size;
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

        long authorId = mre.getAuthor().getIdLong();

        if (secondSplit.length > 4) {
            mre.getMessage().reply("too many arguments, usage: `" + usage + "`").queue();
            return false;
        }

        switch (splitCommand[1]) {
            case "create" -> {
                int maxGuildBitrate = mre.getGuild().getMaxBitrate();
                int maxChannelsPerUser = zGPB.INSTANCE.configurationHandler.getConfigIntValueForGuildByEvent(mre, "temporary_channel_max");
                if (getChannelCountByUser(authorId) >= maxChannelsPerUser) {
                    mre.getMessage().reply("you are only allowed to create " + maxChannelsPerUser + " channels").mentionRepliedUser(false).queue();
                    return true;
                }
                try {
                    category.createVoiceChannel(secondSplit[2]).setUserlimit(size).setBitrate(maxGuildBitrate).
                            queue(channel -> {
                                channelMappings.add(new AbstractMap.SimpleEntry<>(authorId, channel));
                                DataHandler.addTemporaryChannel(channel, mre);
                            });
                    mre.getMessage().reply("channel created successfully").mentionRepliedUser(false).queue();
                } catch (InsufficientPermissionException ipe) {
                    mre.getMessage().reply("the bot has insufficient permissions").mentionRepliedUser(false).queue();
                }
            }
            case "modify" -> {
                long channelID = transformAndGetChannelID(mre, authorId, secondSplit[2], "you can only modify temporary channels that you created");

                if (channelID == -1)
                    return true;

                VoiceChannel vc = getChannelByUserAndChannel(authorId, channelID);

                if (vc != null) {
                    vc.getManager().setName(vc.getName()).setUserLimit(size == 99 ? vc.getUserLimit() : size).queue();
                    mre.getMessage().reply("channel successfully modified").mentionRepliedUser(false).queue();
                } else {
                    mre.getMessage().reply("the channel was not found, this is probably not your fault").mentionRepliedUser(false).queue();
                }

            }
            case "delete" -> {
                long channelID = transformAndGetChannelID(mre, authorId, secondSplit[2], "you can only delete temporary channels that you created");

                if (channelID == -1)
                    return true;

                VoiceChannel vc = getChannelByUserAndChannel(authorId, channelID);

                if (vc != null) {
                    vc.delete().queue();
                    mre.getMessage().reply("channel successfully deleted").mentionRepliedUser(false).queue();
                } else {
                    mre.getMessage().reply("couldn't delete channel, this is probably not your fault").mentionRepliedUser(false).queue();
                }
            }
        }

        return true;
    }

    private long transformAndGetChannelID(MessageReceivedEvent mre, long authorID, String name, String specificText) {
        long tempID = DataHandler.getTemporaryChannelByNameAndID(authorID, name);

        if (tempID == -1) {
            mre.getMessage().reply("channel name is ambiguous").mentionRepliedUser(false).queue();
            return -1;
        } else if (tempID == -2) {
            mre.getMessage().reply(specificText).mentionRepliedUser(false).queue();
            return -1;
        } else {
            return tempID;
        }
    }

}
