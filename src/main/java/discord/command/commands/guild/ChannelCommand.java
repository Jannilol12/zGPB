package discord.command.commands.guild;

import database.DataHandler;
import database.TemporaryChannel;
import discord.command.GuildCommand;
import main.zGPB;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ChannelCommand extends GuildCommand {

    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    private final static Map<Long, ScheduledFuture<?>> deletionFutures = new HashMap<>();

    public ChannelCommand() {
        super("channel", "channel <create|modify|delete> name [size]", "creates a temporary channel", 2);
    }

    public static void deleteUnusedChannels() {
        Set<TemporaryChannel> tempChannels = DataHandler.getTemporaryChannels();
        if (tempChannels != null) {
            for (TemporaryChannel tc : tempChannels) {
                VoiceChannel currentVoice = zGPB.INSTANCE.discordHandler.getLocalJDA().getVoiceChannelById(tc.id());
                if (currentVoice != null) {
                    if (currentVoice.getMembers().size() == 0) {
                        DataHandler.removeTemporaryChannel(tc.id());
                        currentVoice.delete().queue();
                    }
                }
            }
        }
    }

    public static void scheduleChannelDeletion(long channelID) {
        if (deletionFutures.containsKey(channelID))
            return;
        deletionFutures.put(channelID,
                executorService.scheduleAtFixedRate(new DeletionTask(channelID), 30, 30, TimeUnit.SECONDS));
    }

    public static int getChannelCountByUser(long authorID) {
        int count = 0;
        for (TemporaryChannel tc : DataHandler.getTemporaryChannels()) {
            if (tc.owner() == authorID)
                count++;
        }
        return count;
    }

    public static boolean isTemporaryChannel(long channelID) {

        for (TemporaryChannel tc : DataHandler.getTemporaryChannels()) {
            if (tc.id() == channelID)
                return true;
        }

        return false;
    }

    private static VoiceChannel getChannelByUserAndChannel(long authorID, long channelID) {
        for (TemporaryChannel tc : DataHandler.getTemporaryChannels()) {
            if (tc.owner() == authorID && tc.id() == channelID)
                return zGPB.INSTANCE.discordHandler.getLocalJDA().getVoiceChannelById(tc.id());
        }
        return null;
    }

    @Override
    @Deprecated
    protected boolean onCommand(MessageReceivedEvent mre, String givenCommand, String[] splitCommand) {
        if (!super.onCommand(mre, givenCommand, splitCommand))
            return false;

        Category category = mre.getGuild().getCategoryById(zGPB.INSTANCE.guildConfigurationHandler.
                getConfigLong(mre, "temporary_channel_category"));
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
                long maxChannelsPerUser = zGPB.INSTANCE.guildConfigurationHandler.getConfigLong(mre, "temporary_channel_max");
                if (getChannelCountByUser(authorId) >= maxChannelsPerUser) {
                    mre.getMessage().reply("you are only allowed to create " + maxChannelsPerUser + " channels").mentionRepliedUser(false).queue();
                    return true;
                }
                try {
                    category.createVoiceChannel(secondSplit[2]).setUserlimit(size).setBitrate(maxGuildBitrate).
                            queue(channel -> {
                                DataHandler.addTemporaryChannel(channel, mre);
                                scheduleChannelDeletion(channel.getIdLong());
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

    // this exists so the task can end itself after deleting the channel
    private static class DeletionTask implements Runnable {

        private final long channelID;

        public DeletionTask(long id) {
            this.channelID = id;
        }

        @Override
        public void run() {
            VoiceChannel currentVoice = zGPB.INSTANCE.discordHandler.getLocalJDA().getVoiceChannelById(channelID);
            if (currentVoice != null) {
                if (currentVoice.getMembers().size() == 0) {
                    DataHandler.removeTemporaryChannel(channelID);
                    currentVoice.delete().queue(s -> {
                    }, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_CHANNEL));
                    deletionFutures.get(channelID).cancel(false);
                }
            } else {
                DataHandler.removeTemporaryChannel(channelID);
            }
        }
    }

}
