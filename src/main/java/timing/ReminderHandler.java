package timing;

import database.DataHandler;
import log.Logger;
import main.zGPB;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReminderHandler {

    public void runTaskAtDateTime(ZonedDateTime end, Runnable task) {
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.schedule(task, ZonedDateTime.now().until(end, ChronoUnit.SECONDS), TimeUnit.SECONDS);
    }

    public void registerOldReminders() {
        Set<Event> reminders = zGPB.INSTANCE.databaseHandler.getReminders();
        Logger.logDebugMessage("Registering " + reminders.size() + " past reminders");
        reminders.forEach(event -> {
            // fix database inconsistencies on the fly
            if(event.time().isBefore(ZonedDateTime.now())) {
                DataHandler.removeReminder(event);
            } else {
                runTaskAtDateTime(event.time(), () -> {
                    zGPB.INSTANCE.discordHandler.getLocalJDA().
                            getTextChannelById(event.channelID()).
                            retrieveMessageById(event.messageID()).queue(m -> m.reply("here is your reminder :)").queue());
                    DataHandler.removeReminder(event);
                });
            }
        });

    }

}