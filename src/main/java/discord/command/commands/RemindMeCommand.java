package discord.command.commands;

import database.DataHandler;
import discord.command.Command;
import discord.command.CommandType;
import main.zGPB;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import timing.Event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class RemindMeCommand extends Command {

    public RemindMeCommand() {
        super("remindme", "remindme <x (y/M/w/d/h/m/s) / yyyy-MM-dd.hh:mm:ss>", "reminds you at the given time", 2, CommandType.BOTH);
    }

    @Override
    protected boolean onCommand(MessageReceivedEvent mre, String givenCommand, String[] splitCommand) {
        if (!super.onCommand(mre, givenCommand, splitCommand))
            return false;

        // TODO: 02/04/2021 fix unsafe string manipulation
        if (String.valueOf(splitCommand[1].charAt(splitCommand[1].length() - 1)).matches("[yMwdhms]")) {
            char unit = splitCommand[1].charAt(splitCommand[1].length() - 1);
            long cleanTime = Long.parseLong(splitCommand[1].replace("" + unit, ""));
            LocalDateTime remindTime;

            switch (unit) {
                case 'y' -> remindTime = LocalDateTime.now().plusYears(cleanTime);
                case 'M' -> remindTime = LocalDateTime.now().plusMonths(cleanTime);
                case 'w' -> remindTime = LocalDateTime.now().plusWeeks(cleanTime);
                case 'd' -> remindTime = LocalDateTime.now().plusDays(cleanTime);
                case 'h' -> remindTime = LocalDateTime.now().plusHours(cleanTime);
                case 'm' -> remindTime = LocalDateTime.now().plusMinutes(cleanTime);
                case 's' -> remindTime = LocalDateTime.now().plusSeconds(cleanTime);
                default -> {
                    mre.getMessage().reply("unknown format").mentionRepliedUser(false).queue();
                    return true;
                }
            }

            Event remindEvent = new Event(mre.getChannel().getIdLong(), mre.getMessageIdLong(), remindTime);
            DataHandler.saveReminder(remindEvent);
            mre.getMessage().reply("you will be reminded at " + remindTime.toString().substring(0, remindTime.toString().indexOf("."))).mentionRepliedUser(false).queue();
            zGPB.INSTANCE.reminderHandler.runTaskAtDateTime(remindTime, () -> {
                mre.getMessage().reply("here is your reminder :)").mentionRepliedUser(true).queue();
                DataHandler.removeReminder(remindEvent);
            });
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

            LocalDateTime remindTime = null;
            if (date != null && time != null) {
                remindTime = LocalDateTime.of(date, time);
            } else if (date != null) {
                remindTime = date.atStartOfDay();
            } else if (time != null) {
                remindTime = time.atDate(LocalDate.now());
            }

            if (remindTime != null) {
                Event remindEvent = new Event(mre.getChannel().getIdLong(), mre.getMessageIdLong(), remindTime);
                DataHandler.saveReminder(remindEvent);
                zGPB.INSTANCE.reminderHandler.runTaskAtDateTime(remindTime, () -> {
                    mre.getMessage().reply("here is your reminder :)").mentionRepliedUser(true).queue();
                    DataHandler.removeReminder(remindEvent);
                });
                mre.getMessage().reply("you will be reminded at the given time").mentionRepliedUser(false).queue();
            }

        }

        return true;
    }
}
