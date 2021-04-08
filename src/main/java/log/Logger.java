package log;

import java.time.Instant;
import java.time.ZoneId;

public final class Logger {

    public static void logDebugMessage(String msg) {
        System.out.println("[DEBUG, " + Instant.now().atZone(ZoneId.systemDefault()).toString() + "] " + msg);
    }

    public static void logException(String msg, Exception e) {
        System.err.println("[ERROR, " + Instant.now().atZone(ZoneId.systemDefault()).toString() + "] " + msg);
        e.printStackTrace();
    }

    public static void logException(String msg) {
        System.err.println("[ERROR, " + Instant.now().atZone(ZoneId.systemDefault()).toString() + "] " + msg);
    }

    public static void logException(Exception e) {
        e.printStackTrace();
    }

}
