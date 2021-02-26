package log;

import java.time.Instant;

public final class Logger {

    public static void logDebugMessage(String msg) {
        System.out.println("[DEBUG, " + Instant.now().toString() + "] " + msg);
    }

    public static void logException(String msg, Exception e) {
        System.err.println("[ERROR, " + Instant.now().toString() + "] " + msg);
        e.printStackTrace();
    }

    public static void logException(Exception e) {
        e.printStackTrace();
    }

}
