package discord.command.commands.guild;

import discord.command.GuildCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ScanCommand extends GuildCommand {

    public ScanCommand() {
        super("scan", "", "", 2, Permission.ADMINISTRATOR);
    }

    @Override
    protected boolean onCommand(MessageReceivedEvent mre, String givenCommand, String[] splitCommand) {
        return true;
    }

}
