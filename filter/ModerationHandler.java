package discord;

import log.Logger;
import main.zGPB;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.util.*;

public class ModerationHandler {

    private Set<String> filterSet;

    private Set<ModerationEntry> moderationEntries;

    public ModerationHandler() {
        loadFilterList();
    }

    public void loadFilterList() {
        filterSet = new HashSet<>();
        moderationEntries = new HashSet<>();

        // TODO: Better file system
        File filterFolder = new File("filter" + File.separator);
        if (filterFolder.exists()) {
            Arrays.stream(filterFolder.listFiles()).forEach(file -> {
                try {
                    filterSet.addAll(Files.readAllLines(file.toPath()));
                } catch (IOException e) {
                    Logger.logException("Could not read filter file " + file.getAbsolutePath());
                }
            });
        }

    }

    public boolean filterMessage(MessageReceivedEvent mre) {
        if(!mre.isFromGuild())
            return false;

        String filterList = zGPB.INSTANCE.guildConfigurationHandler.getConfigString(mre, "filter_list");

        if (!filterList.trim().equals("EMPTY")) {
            String textEval = evaluateMessageContent(mre.getMessage());
            if (textEval == null)
                textEval = evaluateMessageEmbeds(mre.getMessage());

            if(textEval == null)
                textEval = evaluateMessageAttachments(mre.getMessage());

            if (textEval != null) {
                long moderationChannelID = zGPB.INSTANCE.guildConfigurationHandler.getConfigLong(mre.getGuild(), "filter_moderation_channel");
                TextChannel moderationChannel = zGPB.INSTANCE.discordHandler.getLocalJDA().getTextChannelById(moderationChannelID);

                MessageEmbed hitEmbed = new EmbedBuilder().setColor(Color.RED).
                        addField("author", mre.getAuthor().getAsTag(), true).
                        addField("detected word", textEval, true).
                        addField("direct link", mre.getMessage().getJumpUrl(), false).
                        addField("content", mre.getMessage().getContentRaw(), false).build();
                moderationChannel.sendMessage(hitEmbed).queue(evalMsg -> {
                    moderationEntries.add(new ModerationEntry(mre.getMessageIdLong(), evalMsg.getIdLong(), mre.getTextChannel().getIdLong()));
                    evalMsg.addReaction("U+2705").queue();
                    evalMsg.addReaction("U+274c").queue();
                });
                return true;
            }
        }
        return false;
    }


    public void handleMessageReaction(MessageReactionAddEvent mrae) {
        if(!mrae.isFromGuild())
            return;
        if (!mrae.getReactionEmote().isEmoji())
            return;
        if(mrae.getMember().getUser().isBot())
            return;

        boolean isApproval = mrae.getReactionEmote().getAsCodepoints().equalsIgnoreCase("U+2705");

        if (!(mrae.getReactionEmote().getAsCodepoints().equalsIgnoreCase("U+274c") || isApproval))
            return;

        ModerationEntry me = moderationEntries.stream().filter(entry -> entry.evalID() == mrae.getMessageIdLong()).findFirst().orElse(null);
        if (me != null) {
            TextChannel tc;
            if (!isApproval) {
                tc = zGPB.INSTANCE.discordHandler.getLocalJDA().getTextChannelById(me.channelID);
                if (tc != null) {
                    tc.retrieveMessageById(me.messageID).queue(message -> {
                        message.delete().queue();
                    });
                }
            }

            tc = zGPB.INSTANCE.discordHandler.getLocalJDA().getTextChannelById(zGPB.INSTANCE.guildConfigurationHandler.getConfigLong(mrae.getGuild(), "filter_moderation_channel"));
            tc.retrieveMessageById(me.evalID).queue(m -> m.delete().queue());
        }
    }

    private String evaluateMessageEmbeds(Message msg) {
        for (MessageEmbed me : msg.getEmbeds()) {
            String wordCheck = null;
            if (me.getDescription() != null)
                wordCheck = evaluateString(me.getDescription());
            if (wordCheck != null)
                return "[EMBED] " + wordCheck;


            if (me.getTitle() != null)
                wordCheck = evaluateString(me.getTitle());
            if (wordCheck != null)
                return "[EMBED] " + wordCheck;

            if (me.getFooter() != null) {
                if (me.getFooter().getText() != null) {
                    wordCheck = evaluateString(me.getFooter().getText());
                    if (wordCheck != null)
                        return "[EMBED] " + wordCheck;
                }
            }

            for (MessageEmbed.Field field : me.getFields()) {
                wordCheck = field.getName();
                if (wordCheck != null)
                    return "[EMBED] " + wordCheck;
                wordCheck = field.getValue();
                if (wordCheck != null)
                    return "[EMBED] " + wordCheck;
            }

        }
        return null;
    }

    private String evaluateMessageContent(Message msg) {
        return evaluateString(msg.getContentStripped());
    }

    private String evaluateMessageAttachments(Message msg) {
        for (Message.Attachment ma : msg.getAttachments()) {
            String eval = evaluateString(ma.getFileName());
            if (eval != null)
                return "[ATTACHMENT] " + eval;
        }
        return null;
    }

    private String evaluateString(String msg) {
        String filterCheck;
        String contentRaw = msg.trim().replaceAll(" ", "").replaceAll(":", "").toLowerCase();
        contentRaw = leetSpeakReplace(contentRaw);
        contentRaw = regionalIndicatorReplace(contentRaw);

        filterCheck = filterSet.stream().filter(contentRaw::contains).findFirst().orElse(null);
        if (filterCheck != null)
            return filterCheck;

        Map<Integer, Integer> charMappings = new HashMap<>();

        CharBuffer.wrap(contentRaw.toCharArray()).chars().forEach(i -> {
            charMappings.computeIfAbsent(i, x -> 0);
            charMappings.put(i, charMappings.get(i) + 1);
        });

        int maxChar = 0, maxCount = 0;

        for (int ch : charMappings.keySet()) {
            if (maxCount < charMappings.get(ch)) {
                maxChar = ch;
                maxCount = charMappings.get(ch);
            }
        }
        String delimiterFix = contentRaw.replace("" + (char) maxChar, "");
        filterCheck = filterSet.stream().filter(delimiterFix::contains).findFirst().orElse(null);
        if (filterCheck != null)
            return filterCheck;
        return null;
    }


    private String leetSpeakReplace(String in) {
        return in.replaceAll("0", "o").replaceAll("3", "e").replace("1", "i");
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


    private record ModerationEntry(long messageID, long evalID, long channelID) {

    }

}
