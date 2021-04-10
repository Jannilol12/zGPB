package log;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public final class Logger {

    private static ArrayList<String> logEntries = new ArrayList<>();

    public static void logDebugMessage(String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("[DEBUG, ").append(
                DateTimeFormatter.ofPattern("dd/MM/yyyy - hh:mm").format(
                        Instant.now().atZone(ZoneId.systemDefault()))).append("] ").append(msg);
        logEntries.add(sb.toString());
        System.out.println(sb);
    }

    public static void logException(String msg, Exception e) {
        logException(msg);
        e.printStackTrace();
        logEntries.add(getExceptionTest(e));
    }

    public static void logException(String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("[ERROR, ").append(DateTimeFormatter.ofPattern("dd/MM/yyyy - hh:mm").format(
                Instant.now().atZone(ZoneId.systemDefault()))).append("] ").append(msg);
        System.out.println(sb);
        logEntries.add(sb.toString());
    }

    public static void logException(Exception e) {
        logEntries.add(getExceptionTest(e));
        e.printStackTrace();
    }

    public static String getLastMessages() {
        StringBuilder sb = new StringBuilder();
        for (int i = logEntries.size() - 1; i >= 0; i--) {
            sb.append(logEntries.get(i));
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    private static String getExceptionTest(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.close();
        try {
            sw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sw.toString();
    }

}
