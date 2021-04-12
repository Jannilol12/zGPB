package timing;

import database.DataHandler;
import log.Logger;
import main.zGPB;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReminderHandler {

    private ScheduledExecutorService executorService;

    public ReminderHandler() {
        executorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    }

    private void runTaskAtDateTime(ZonedDateTime end, Runnable task) {
        executorService.schedule(task, ZonedDateTime.now().until(end, ChronoUnit.SECONDS), TimeUnit.SECONDS);
    }

    public void remindMessage(Event remindEvent) {
        runTaskAtDateTime(remindEvent.time(), () -> {
            TextChannel tc = zGPB.INSTANCE.discordHandler.getLocalJDA().getTextChannelById(remindEvent.channelID());
            tc.retrieveMessageById(remindEvent.messageID()).queue(m -> m.reply("here is your reminder :)").queue(), new ErrorHandler().handle(
                    ErrorResponse.UNKNOWN_MESSAGE, (e) -> {
                        long user = DataHandler.getUserByMessage(remindEvent.messageID());
                        if (user != -1 && tc.canTalk()) {
                            tc.sendMessage("<@" + user + "> here is your reminder :)").queue();
                        }
                    }
            ));
            DataHandler.removeReminder(remindEvent);
        });
    }

    public void registerOldReminders() {
        Set<Event> reminders = zGPB.INSTANCE.databaseHandler.getReminders();
        Logger.logDebugMessage("Registering " + reminders.size() + " past reminders");
        reminders.forEach(event -> {
            // fix database inconsistencies on the fly
            if (event.time().isBefore(ZonedDateTime.now())) {
                DataHandler.removeReminder(event);
            } else {
                remindMessage(event);
            }
        });

    }

}