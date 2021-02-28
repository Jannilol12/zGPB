package discord;

public class MessageCrafter {

    public static String craftCodeMessage(String language, String content, boolean truncate) {
        int maxAllowedLength = 1999 - language.length() - 6;

        if (!truncate && content.length() >= maxAllowedLength)
            return null;

        if (content.length() >= maxAllowedLength) {
            String truncated = content.substring(0, maxAllowedLength - 5) + "[...]";
            return "```" + language + System.lineSeparator() + truncated + "```";
        } else {
            return "```" + language + System.lineSeparator() + content + "```";
        }
    }

    public static String craftCodeMessage(String language, String content) {
        return craftCodeMessage(language, content, true);
    }

}
