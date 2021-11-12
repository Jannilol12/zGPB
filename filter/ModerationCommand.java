package discord.command.commands.guild;

import discord.command.GuildCommand;
import main.zGPB;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ModerationCommand extends GuildCommand {

    public ModerationCommand() {
        super("moderation", "moderation reload", "control over moderation features", 2, Permission.ADMINISTRATOR);
    }

    @Override
    protected boolean onCommand(MessageReceivedEvent mre, String givenCommand, String[] splitCommand) {
        if(!super.onCommand(mre, givenCommand, splitCommand))
            return false;

        zGPB.INSTANCE.discordHandler.getModerationHandler().loadFilterList();
        mre.getMessage().reply("Refreshed filter list").mentionRepliedUser(false).queue();
        return true;
    }
}
