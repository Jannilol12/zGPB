package timing;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// TODO: 02/04/2021 save reminders in db to prevent data loss
public class ReminderHandler {

    public void runTaskAtDateTime(LocalDateTime end, Runnable task) {
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.schedule(task, LocalDateTime.now().until(end, ChronoUnit.SECONDS), TimeUnit.SECONDS);
    }

}