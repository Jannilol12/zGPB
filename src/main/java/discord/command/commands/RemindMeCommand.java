package discord.command.commands;

import database.DataHandler;
import discord.command.Command;
import main.Util;
import main.zGPB;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import timing.Event;

import java.time.*;

public class RemindMeCommand extends Command {

    public RemindMeCommand() {
        super("remindme", "remindme <x (y/M/w/d/h/m/s) / yyyy-MM-dd.hh:mm:ss> [text]", "reminds you at the given time with the given text", 2);
    }

    @Override
    protected boolean isSyntaxCorrect(String command) {
        int l = command.split(" ", 3).length;
        return l > 1 && l <= 3;
    }

    @Override
    protected boolean onCommand(MessageReceivedEvent mre, String givenCommand, String[] splitCommand) {
        if (!super.onCommand(mre, givenCommand, splitCommand))
            return false;

        if (splitCommand[1].length() < 2) {
            mre.getMessage().reply("couldn't parse format").mentionRepliedUser(false).queue();
            return true;
        }

        String content = "";
        if (splitCommand.length >= 3) {
            splitCommand = givenCommand.split(" ", 3);
            content = splitCommand[2];
        }
        if (content.length() >= 1000) {
            mre.getMessage().reply("content too long").mentionRepliedUser(false).queue();
            return true;
        }

        if (content.contains("@everyone")
            || mre.getMessage().getContentRaw().contains("@here")
            || mre.getMessage().getMentionedMembers().size() != 0
            || mre.getMessage().getMentionedRoles().size() != 0) {
            if(!(mre.getMessage().getReferencedMessage() != null && mre.getMessage().getMentionedMembers().size() <= 1)) {
                mre.getMessage().reply("you are not allowed to ping users/roles in reminders").mentionRepliedUser(true).queue();
                return true;
            }
        }

        // TODO: 12/04/2021 deduplicate
        // TODO: 15/04/2021 check substrings
        if (String.valueOf(splitCommand[1].charAt(splitCommand[1].length() - 1)).matches("[yMwdhms]")) {
            ZonedDateTime remindTime = Util.getTimeAdded(splitCommand[1]);

            if (remindTime == null) {
                mre.getMessage().reply("unknown format").mentionRepliedUser(false).queue();
                return true;
            }

            Event remindEvent = new Event(mre.getChannel().getIdLong(), mre.getMessageIdLong(), remindTime, content);

            DataHandler.saveReminder(remindEvent);
//            mre.getMessage().addReaction("U+2714").queue();
            mre.getMessage().addReaction("U+2795").queue();
            zGPB.INSTANCE.reminderHandler.remindMessage(remindEvent);
        } else {
            LocalDate date = null;
            LocalTime time = null;

            if (splitCommand[1].contains(".")) {
                String[] timedate = splitCommand[1].split("\\.");

                try {
                    date = LocalDate.parse(timedate[0]);
                    time = LocalTime.parse(timedate[1]);
                } catch (Exception e) {
                    mre.getMessage().reply("couldn't parse datetime").mentionRepliedUser(false).queue();
                    return true;
                }

            } else {
                if (splitCommand[1].contains("-")) {
                    try {
                        date = LocalDate.parse(splitCommand[1]);
                    } catch (Exception e) {
                        mre.getMessage().reply("couldn't parse date").mentionRepliedUser(false).queue();
                        return true;
                    }
                } else {
                    try {
                        time = LocalTime.parse(splitCommand[1]);
                    } catch (Exception e) {
                        mre.getMessage().reply("couldn't parse time").mentionRepliedUser(false).queue();
                        return true;
                    }
                }
            }

            ZonedDateTime remindTime = null;
            if (date != null && time != null) {
                remindTime = ZonedDateTime.of(LocalDateTime.of(date, time), ZoneId.systemDefault());
            } else if (date != null) {
                remindTime = date.atStartOfDay(ZoneId.systemDefault());
            } else if (time != null) {
                remindTime = time.atDate(LocalDate.now()).atZone(ZoneId.systemDefault());
            }

            if (remindTime != null) {
                Event remindEvent = new Event(mre.getChannel().getIdLong(), mre.getMessageIdLong(), remindTime, content);
                DataHandler.saveReminder(remindEvent);
                zGPB.INSTANCE.reminderHandler.remindMessage(remindEvent);
//                mre.getMessage().addReaction("U+2714").queue();
                mre.getMessage().addReaction("U+2795").queue();
            }

        }

        return true;
    }
}
