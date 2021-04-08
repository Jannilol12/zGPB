package discord.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.EnumSet;

public abstract class GuildCommand extends Command {

    private EnumSet<Permission> permissions;

    public GuildCommand(String name, String usage, String description, int argCount, Permission... permission) {
        super(name, usage, description, argCount);
        permissions = EnumSet.noneOf(Permission.class);
        permissions.addAll(Arrays.asList(permission));
    }

    @Override
    protected boolean onCommand(MessageReceivedEvent mre, String givenCommand, String[] splitCommand) {
        if (!super.onCommand(mre, givenCommand, splitCommand))
            return false;

        if (!mre.getMember().hasPermission(permissions)) {
            mre.getMessage().reply("insufficient permissions, " + permissions.toString() + " needed").mentionRepliedUser(false).queue();
            return false;
        }

        return true;
    }
}
