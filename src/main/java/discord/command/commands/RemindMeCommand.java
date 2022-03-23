package discord.command.commands;

import database.DataHandler;
import discord.command.Command;
import main.DateUtil;
import main.zGPB;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import timing.Event;

import java.time.ZonedDateTime;
import java.util.StringJoiner;
import java.util.concurrent.ThreadLocalRandom;

public class RemindMeCommand extends Command {

    public RemindMeCommand() {
        super("remindme", "remindme <x (y/M/w/d/h/m/s) / any date format you wish for> <text>", "reminds you at the given time with the given text", 2, "rm");
    }

    @Override
    protected boolean isSyntaxCorrect(String command) {
        int l = command.split(" ").length;
        return l > 2;
    }

    @Override
    protected boolean onCommand(MessageReceivedEvent mre, String givenCommand, String[] splitCommand) {
        if (!super.onCommand(mre, givenCommand, splitCommand))
            return false;

        String timeString = splitCommand[1];


        if (!DateUtil.isDynamicTimeString(splitCommand[1])) {
            timeString = getDateString(givenCommand.split(" "));
        }

        String content = splitCommand[splitCommand.length - 1];
        if (content.length() >= 1000) {
            mre.getMessage().reply("content too long").mentionRepliedUser(false).queue();
            return true;
        }

        if (mre.isFromGuild()) {
            if (content.contains("@everyone")
                || mre.getMessage().getContentRaw().contains("@here")
                || mre.getMessage().getMentionedMembers().size() != 0
                || mre.getMessage().getMentionedRoles().size() != 0) {
                if (!(mre.getMessage().getReferencedMessage() != null && mre.getMessage().getMentionedMembers().size() <= 1)) {
                    mre.getMessage().reply("you are not allowed to ping users/roles in reminders").mentionRepliedUser(true).queue();
                    return true;
                }
            }
        }

        ZonedDateTime remindTime = DateUtil.getAdjustedDateByInput(timeString);

        if (remindTime == null) {
            mre.getMessage().reply("could not parse date/time format :(").mentionRepliedUser(true).queue();
            return true;
        }

        Event remindEvent = new Event(ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE),
                mre.getChannel().getIdLong(), mre.getMessageIdLong(), mre.getAuthor().getIdLong(), remindTime, content);
        DataHandler.saveReminder(remindEvent);
        zGPB.INSTANCE.reminderHandler.remindMessage(remindEvent);
        mre.getMessage().addReaction("U+2795").queue();

        return true;
    }

    private String getDateString(String[] input) {
        StringJoiner sb = new StringJoiner(" ");

        for (String subString : input) {
            if (subString.equals("remindme"))
                continue;
            if (!subString.chars().allMatch(c -> Character.isDigit(c) || c == ':'))
                break;
            sb.add(subString);
        }

        return sb.toString().stripTrailing();
    }

}
