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

    private static final Map<String, String> dateMappings = new HashMap<>() {{
        put("^\\d{8}$", "yyyyMMdd");

        put("^\\d{1,2}-\\d{1,2}-\\d{4}$", "d-M-y");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "y-M-d");

        put("^\\d{1,2}\\.\\d{1,2}\\.\\d{4}$", "d.M.y");
        put("^\\d{4}\\.\\d{1,2}\\.\\d{1,2}$", "y.M.d");

        put("^\\d{1,2}/\\d{1,2}/\\d{4}$", "d/M/y");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "y/M/d");

        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}$", "d MMM y");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}$", "d MMMM y");

        put("^\\d{12}$", "yyyyMMddHHmm");
        put("^\\d{8}\\s\\d{4}$", "yyyyMMdd HHmm");

        put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{1,2}$", "d-M-y H:m");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{1,2}$", "y-M-d H:m");

        put("^\\d{1,2}\\.\\d{1,2}\\.\\d{4}\\s\\d{1,2}:\\d{1,2}$", "d.M.y H:m");
        put("^\\d{4}\\.\\d{1,2}\\.\\d{1,2}\\s\\d{1,2}:\\d{1,2}$", "y.M.d H:m");

        put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{1,2}$", "d/M/y H:m");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{1,2}$", "y/M/d H:m");

        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{1,2}$", "d MMM y H:m");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{1,2}$", "d MMMM y H:m");

        put("^\\d{14}$", "yyyyMMddHHmmss");
        put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss");

        put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}$", "d-M-y H:m:s");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}$", "y-M-d H:m:s");

        put("^\\d{1,2}\\.\\d{1,2}\\.\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}$", "d.M.y H:m:s");
        put("^\\d{4}\\.\\d{1,2}\\.\\d{1,2}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}$", "y.M.d H:m:s");

        put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}$", "d/M/y H:m:s");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}$", "y/M/d H:m:s");

        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}$", "d MMM y H:m:s");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{1,2}:\\d{1,2}$", "d MMMM y H:m:s");

        put("^\\d{1,2}.\\d{1,2}$", "d.M");
        put("^\\d{1,2}:\\d{1,2}$", "H:m");
    }};

    private DateUtil() {
    }

    public static String determineDateFormat(String str) {
        for (String regExp : dateMappings.keySet()) {
            if (str.matches(regExp))
                return dateMappings.get(regExp);
        }

        return null;
    }

    public static ZonedDateTime getDateByInput(String rawInput) {
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


        if (localTime == null && localDate == null)
            return null;

        return ZonedDateTime.of(localDate == null ? LocalDate.now(ZoneId.systemDefault()) : localDate,
                localTime == null ? LocalTime.now(ZoneId.systemDefault()) : localTime, ZoneId.systemDefault());
    }
}
