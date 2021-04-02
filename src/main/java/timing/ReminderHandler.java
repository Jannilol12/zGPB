package timing;

import discord.DataHandler;
import log.Logger;
import main.JADB;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReminderHandler {

    public void runTaskAtDateTime(LocalDateTime end, Runnable task) {
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.schedule(task, LocalDateTime.now().until(end, ChronoUnit.SECONDS), TimeUnit.SECONDS);
    }

    public void registerOldReminders() {
        Set<Event> reminders = JADB.INSTANCE.databaseHandler.getReminders();
        Logger.logDebugMessage("Registering " + reminders.size() + " past reminders");
        reminders.forEach(event -> {
            // fix database inconsistencies on the fly
            if(event.time().isBefore(LocalDateTime.now())) {
                DataHandler.removeReminder(event);
            } else {
                runTaskAtDateTime(event.time(), () -> {
                    JADB.INSTANCE.discordHandler.getLocalJDA().
                            getTextChannelById(event.channelID()).
                            retrieveMessageById(event.messageID()).queue(m -> m.reply("here is your reminder :)").queue());
                    DataHandler.removeReminder(event);
                });
            }
        });

    }

}