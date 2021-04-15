package discord;

import database.DataHandler;
import database.TemporaryChannel;
import main.zGPB;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

// TODO: 15/04/2021 maybe not static unsure
public class ChannelHandler {

    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    private final static Map<Long, ScheduledFuture<?>> deletionFutures = new HashMap<>();

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
