package log;

import java.time.Instant;

public final class Logger {

    public static void logDebugMessage(String msg) {
        System.out.println("[DEBUG, " + Instant.now().toString() + "] " + msg);
    }

    public static void logException(Exception e) {
        e.printStackTrace();
    }

}
