package discord;

import main.zGPB;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.awt.*;
import java.nio.CharBuffer;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModerationHandler {

    private Set<String> filterSet;

    private Set<ModerationEntry> moderationEntries;

    public ModerationHandler() {
        filterSet = new HashSet<>();
        filterSet.add("badword");

        moderationEntries = new HashSet<>();
    }

    public boolean filterMessage(MessageReceivedEvent mre) {
        String filterList = zGPB.INSTANCE.guildConfigurationHandler.getConfigString(mre, "filter_list");

        if (!filterList.trim().equals("EMPTY")) {
            boolean textEval = evaluateMessageContent(mre.getMessage());
            if (textEval) {
                long moderationChannelID = zGPB.INSTANCE.guildConfigurationHandler.getConfigLong(mre.getGuild(), "filter_moderation_channel");
                TextChannel moderationChannel = zGPB.INSTANCE.discordHandler.getLocalJDA().getTextChannelById(moderationChannelID);

                MessageEmbed hitEmbed = new EmbedBuilder().setTitle("[Filter list] Hit").setColor(Color.RED).setThumbnail(mre.getAuthor().getAvatarUrl()).
                        addField("author", mre.getAuthor().getAsTag(), true).
                        addField("id", mre.getMessageId(), true).
                        addField("date", mre.getMessage().getTimeCreated().toString(), true).
                        addField("direct link", mre.getMessage().getJumpUrl(), false).
                        addField("content", mre.getMessage().getContentRaw(), false).
                        setTimestamp(Instant.now()).build();

                moderationChannel.sendMessage(hitEmbed).queue(evalMsg -> {
                    moderationEntries.add(new ModerationEntry(mre.getMessageIdLong(), evalMsg.getIdLong(), mre.getTextChannel().getIdLong()));
                });
                return true;
            }
        }
        return false;
    }


    public void handleMessageReaction(MessageReactionAddEvent mrae) {
        if (!mrae.getReactionEmote().isEmoji())
            return;

        if (!mrae.getReactionEmote().getAsCodepoints().equalsIgnoreCase("U+274c"))
            return;

        ModerationEntry me = moderationEntries.stream().filter(entry -> entry.evalID() == mrae.getMessageIdLong()).findFirst().orElse(null);
        if (me != null) {
            TextChannel tc = zGPB.INSTANCE.discordHandler.getLocalJDA().getTextChannelById(me.channelID);
            if (tc != null) {
                tc.retrieveMessageById(me.messageID).queue(message -> {
                    message.delete().queue();
                });
            }
        }
    }


    private boolean evaluateMessageContent(Message msg) {
        boolean filterCheck;
        String contentRaw = msg.getContentStripped().trim().replaceAll(" ", "").replaceAll(":", "").toLowerCase();
        contentRaw = regionalIndicatorReplace(contentRaw);
        filterCheck = filterSet.stream().anyMatch(contentRaw::contains);
        if (filterCheck)
            return true;

        Map<Integer, Integer> charMappings = new HashMap<>();

        CharBuffer.wrap(contentRaw.toCharArray()).chars().forEach(i -> {
            charMappings.computeIfAbsent(i, x -> 0);
            charMappings.put(i, charMappings.get(i) + 1);
        });

        int maxChar = 0,  maxCount = 0;

        for (int ch : charMappings.keySet()) {
            if (maxCount < charMappings.get(ch)) {
                maxChar = ch;
                maxCount = charMappings.get(ch);
            }
        }
        String delimiterFix = contentRaw.replace("" + (char) maxChar, "");
        return filterSet.stream().anyMatch(delimiterFix::contains);
    }

    private String regionalIndicatorReplace(String in) {
        return in.replace("\uD83C\uDDE6", "a").replace("\uD83C\uDDE7", "b")
                .replace("\uD83C\uDDE8", "c").replace("\uD83C\uDDE9", "d")
                .replace("\uD83C\uDDEA", "e").replace("\uD83C\uDDEB", "f")
                .replace("\uD83C\uDDEC", "g").replace("\uD83C\uDDED", "h")
                .replace("\uD83C\uDDEE", "i").replace("\uD83C\uDDEF", "j")
                .replace("\uD83C\uDDF0", "k").replace("\uD83C\uDDF1", "l")
                .replace("\uD83C\uDDF2", "m").replace("\uD83C\uDDF3", "n")
                .replace("\uD83C\uDDF4", "o").replace("\uD83C\uDDF5", "p")
                .replace("\uD83C\uDDF6", "q").replace("\uD83C\uDDF7", "r")
                .replace("\uD83C\uDDF8", "s").replace("\uD83C\uDDF9", "t")
                .replace("\uD83C\uDDFA", "u").replace("\uD83C\uDDFB", "v")
                .replace("\uD83C\uDDFC", "w").replace("\uD83C\uDDFD", "x")
                .replace("\uD83C\uDDFE", "y").replace("\uD83C\uDDFF", "z");
    }

    private boolean evaluateMessageAttachments(Message msg) {
        return true;
    }

    private enum FilterType {
        SLUR, HATESPEECH, NSFW
    }

    private record ModerationEntry(long messageID, long evalID, long channelID) {

    }

}
