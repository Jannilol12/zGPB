package main;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DateUtil {

    private static final Map<String, String> DATE_FORMAT_MAPPINGS = new HashMap<>() {{
        put("^\\d{1,2}.\\d{1,2}$", "d.M");
        put("^\\d{1,2}:\\d{1,2}$", "H:m");

        put("^\\d{8}$", "yyyyMMdd");

        put("^\\d{12}$", "yyyyMMddHHmm");
        put("^\\d{8}\\s\\d{4}$", "yyyyMMdd HHmm");

        put("^\\d{14}$", "yyyyMMddHHmmss");
        put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss");


        put("^\\d{1,2}-\\d{1,2}-\\d{4}$", "d-M-y");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "y-M-d");


        put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{1,2}$", "d-M-y H:m");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{1,2}$", "y-M-d H:m");

        put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}$", "d-M-y H:m:s");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}$", "y-M-d H:m:s");


        put("^\\d{1,2}\\.\\d{1,2}\\.\\d{4}$", "d.M.y");
        put("^\\d{4}\\.\\d{1,2}\\.\\d{1,2}$", "y.M.d");

        put("^\\d{1,2}\\.\\d{1,2}\\.\\d{4}\\s\\d{1,2}:\\d{1,2}$", "d.M.y H:m");
        put("^\\d{4}\\.\\d{1,2}\\.\\d{1,2}\\s\\d{1,2}:\\d{1,2}$", "y.M.d H:m");

        put("^\\d{1,2}\\.\\d{1,2}\\.\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}$", "d.M.y H:m:s");
        put("^\\d{4}\\.\\d{1,2}\\.\\d{1,2}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}$", "y.M.d H:m:s");


        put("^\\d{1,2}/\\d{1,2}/\\d{4}$", "d/M/y");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "y/M/d");

        put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{1,2}$", "d/M/y H:m");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{1,2}$", "y/M/d H:m");

        put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}$", "d/M/y H:m:s");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}$", "y/M/d H:m:s");


        put("^\\d{1,2}\\s\\d{1,2}\\s\\d{4}$", "d M y");

        put("^\\d{1,2}\\s\\d{1,2}\\s\\d{4}\\s\\d{1,2}:\\d{1,2}$", "d M y H:m");

        put("^\\d{1,2}\\s\\d{1,2}\\s\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}$", "d M y H:m:s");
    }};

    private DateUtil() {
    }

    private static String determineDateFormat(String str) {
        for (String regExp : DATE_FORMAT_MAPPINGS.keySet()) {
            if (str.matches(regExp))
                return DATE_FORMAT_MAPPINGS.get(regExp);
        }

        return null;
    }

    public static boolean isDynamicTimeString(String in) {
        if (in.length() < 2)
            return false;
        return String.valueOf(in.charAt(in.length() - 1)).matches("[yMwdhms]");
    }

    public static ZonedDateTime getAdjustedDateByInput(String raw) {
        if (isDynamicTimeString(raw)) {
            return getTimeAdded(raw);
        } else {
            return getDateByInput(raw);
        }
    }

    public static ZonedDateTime getAdjustedDateByInputPreChecked(String raw, boolean pre) {
        if (pre) {
            return getTimeAdded(raw);
        } else {
            return getDateByInput(raw);
        }
    }

    private static ZonedDateTime getDateByInput(String rawInput) {
        String filteredInput = rawInput.trim().toLowerCase(Locale.ROOT);

        String dateFormat = determineDateFormat(filteredInput);
        if (dateFormat == null)
            return null;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(dateFormat);


        LocalDate localDate = null;
        LocalTime localTime = null;

        try {
            localDate = LocalDate.parse(filteredInput, dtf);
        } catch (DateTimeParseException ignored) {
        }

        try {
            localTime = LocalTime.parse(filteredInput, dtf);
        } catch (DateTimeParseException ignored) {
        }

        if (localTime == null)
            localTime = LocalTime.now(ZoneId.systemDefault());

        if (localDate == null) {
            localDate = LocalDate.now(ZoneId.systemDefault());

            if (localTime.isBefore(LocalTime.now()))
                localDate = localDate.plusDays(1);
        }

        return ZonedDateTime.of(localDate, localTime, ZoneId.systemDefault());
    }

    public static ZonedDateTime getTimeAdded(String time) {
        char unit = time.charAt(time.length() - 1);
        long cleanTime = Long.parseLong(time.replace(unit + "", ""));
        ZonedDateTime resultTime = ZonedDateTime.now();
        switch (time.substring(time.length() - 1).charAt(0)) {
            case 'y' -> resultTime = resultTime.plusYears(cleanTime);
            case 'M' -> resultTime = resultTime.plusMonths(cleanTime);
            case 'w' -> resultTime = resultTime.plusWeeks(cleanTime);
            case 'd' -> resultTime = resultTime.plusDays(cleanTime);
            case 'h' -> resultTime = resultTime.plusHours(cleanTime);
            case 'm' -> resultTime = resultTime.plusMinutes(cleanTime);
            case 's' -> resultTime = resultTime.plusSeconds(cleanTime);
            default -> {
                return null;
            }
        }
        return resultTime;
    }

}
