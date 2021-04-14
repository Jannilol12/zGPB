package timing;

import database.DataHandler;
import log.Logger;
import main.Util;
import main.zGPB;
import net.dv8tion.jda.api.entities.Message;
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

    private final ScheduledExecutorService executorService;

    public ReminderHandler() {
        executorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    }

    private void runTaskAtDateTime(ZonedDateTime end, Runnable task) {
        executorService.schedule(task, ZonedDateTime.now().until(end, ChronoUnit.SECONDS), TimeUnit.SECONDS);
    }

    public void remindMessage(Event remindEvent) {
        runTaskAtDateTime(remindEvent.time(), () -> {
            TextChannel tc = zGPB.INSTANCE.discordHandler.getLocalJDA().getTextChannelById(remindEvent.channelID());
            tc.retrieveMessageById(remindEvent.messageID()).queue(m -> sendReminder(m, remindEvent), new ErrorHandler().handle(
                    ErrorResponse.UNKNOWN_MESSAGE, (e) -> {
                        sendReminder(tc, DataHandler.getUserByMessage(remindEvent.messageID()), remindEvent);
                    }
            ));
            DataHandler.removeReminder(remindEvent);
        });
    }

    // TODO: 12/04/2021 doesn't work in private channels anymore
    private void sendReminder(Message m, Event e) {
        String remindText = "here is your reminder " + (e.content().trim().isEmpty() ? "" : "[" + e.content() + "]");
        if (m.isFromGuild()) {
            m.retrieveReactionUsers("\u2795").queue(users -> {
                if (users.size() <= 1) {
                    m.reply(remindText).queue();
                } else {
                    m.getGuild().createRole().setName("multicast-" + Util.createRandomString(3)).queue(r -> {
                        users.forEach(u -> m.getGuild().addRoleToMember(u.getId(), r).queue());
                        m.reply("<@&" + r.getId() + "> " + remindText).queue(
                                message -> message.editMessage(remindText).queue()
                        );
                        r.delete().queueAfter(1, TimeUnit.DAYS);
                    });
                }
            });
        } else {
            m.reply(remindText).queue();
        }
    }

    private void sendReminder(TextChannel tc, long user, Event e) {
        if (user != -1 && tc.canTalk()) {
            tc.sendMessage("<@" + user + "> here is your reminder" + (e.content().trim().isEmpty() ? "" : "[" + e.content() + "]")).queue();
        } else {
            System.err.println("hello " + e);
        }
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