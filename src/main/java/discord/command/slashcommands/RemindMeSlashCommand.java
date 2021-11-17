package discord.command.slashcommands;

import database.DataHandler;
import discord.command.SlashCommand;
import main.DateUtil;
import main.zGPB;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import timing.Event;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class RemindMeSlashCommand extends SlashCommand {

    public RemindMeSlashCommand() {
        super("remindme", "creates a reminder", Set.of(
                new OptionData(OptionType.STRING, "time", "the time you will be reminded on", true),
                new OptionData(OptionType.STRING, "content", "what you want to be reminded of", false)
        ));
    }

    @Override
    protected boolean onCommand(SlashCommandEvent sce, String commandString) {
        sce.deferReply().queue();

        ZonedDateTime remindTime = DateUtil.getAdjustedDateByInput(sce.getOption("time").getAsString());

        sce.getHook().sendMessage("You will be reminded on " + remindTime.toString()).queue();

        Event remindEvent = new Event(ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE),
                sce.getChannel().getIdLong(), -1, sce.getUser().getIdLong(), remindTime,
                sce.getOption("content") == null ? "" : sce.getOption("content").getAsString());

        DataHandler.saveReminder(remindEvent);
        zGPB.INSTANCE.reminderHandler.remindMessage(remindEvent);

        return true;
    }

}
