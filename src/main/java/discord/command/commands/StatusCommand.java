package discord.command.commands;

import discord.command.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class StatusCommand extends Command {

    public StatusCommand() {
        super("status", "status", "prints basic bot information", 1, "healthcheck");
    }

    @Override
    protected boolean onCommand(MessageReceivedEvent mre, String givenCommand, String[] splitCommand) {
        if (!super.onCommand(mre, givenCommand, splitCommand))
            return false;

        long latency = mre.getMessage().getTimeCreated().until(ZonedDateTime.now(), ChronoUnit.MILLIS);
        mre.getChannel().sendMessage("Latency: " + latency + "ms; WebSocket: " + mre.getJDA().getGatewayPing() + "ms").queue();

        return true;
    }

}
