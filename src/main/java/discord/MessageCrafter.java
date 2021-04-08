package discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Set;

public class MessageCrafter {

    public static MessageEmbed craftGenericEmbedMessage(String title, Set<EmbedField> fields) {
        return craftGenericEmbedMessage(title, fields.toArray(new EmbedField[]{}));
    }

    public static MessageEmbed craftGenericEmbedMessage(String title, EmbedField... fields) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setColor(new Color(100, 255, 80));
        eb.setTimestamp(Instant.now().atZone(ZoneId.systemDefault()));
        eb.setFooter("zGBP");
        eb.setTitle(title);

        for (EmbedField ef : fields) {
            String v = ef.value();
            if (v.length() > 1024)
                v = v.substring(0, 1019) + "[...]";
            eb.addField(ef.key(), v, ef.inline());
        }

        return eb.build();
    }

    public static String craftCodeMessage(String language, String content, boolean truncate) {
        int maxAllowedLength = 1999 - language.length() - 10;

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
