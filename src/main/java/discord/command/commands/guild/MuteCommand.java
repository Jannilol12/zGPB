package discord.command.commands.guild;

import discord.command.GuildCommand;
import main.Util;
import main.zGPB;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MuteCommand extends GuildCommand {

    private final static ScheduledExecutorService muteService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    public MuteCommand() {
        super("mute", "mute <member> <time>", "mutes the given member", 3, Permission.ADMINISTRATOR);
    }

    @Override
    protected boolean onCommand(MessageReceivedEvent mre, String givenCommand, String[] splitCommand) {
        if (!super.onCommand(mre, givenCommand, splitCommand))
            return false;

        if (!zGPB.INSTANCE.guildConfigurationHandler.getConfigBoolean(mre, "mute_enabled") || mre.getMember().getRoles().stream().noneMatch(r -> r.getName().toLowerCase().contains("admin"))) {
            mre.getMessage().reply("muting is not enabled on this server").mentionRepliedUser(false).queue();
            return true;
        }

        Member toMute;

        if (mre.getMessage().getMentionedMembers().size() != 1) {
            mre.getMessage().reply("you need to mention a member to mute").mentionRepliedUser(false).queue();
            return true;
        }

        toMute = mre.getMessage().getMentionedMembers().get(0);

        List<Role> mutedRole = mre.getGuild().getRolesByName("muted", false);
        if (mutedRole.size() != 1) {
            mre.getMessage().reply("muted role is not set up correctly").mentionRepliedUser(false).queue();
            return true;
        }

        handleRole(splitCommand[2], mre.getGuild(), toMute, mutedRole.get(0));

        mre.getMessage().addReaction("U+2714").queue();

        return true;

    }

    private void handleRole(String time, Guild g, Member m, Role r) {
        if (m == null) {
            System.out.println("time = " + time + ", g = " + g + ", m = " + m + ", r = " + r);
            return;
        }

        g.addRoleToMember(m, r).queue();

        muteService.schedule(() -> {
            g.removeRoleFromMember(m, r).queue();
        }, ZonedDateTime.now().until(Util.getTimeAdded(time), ChronoUnit.SECONDS), TimeUnit.SECONDS);
    }

}
