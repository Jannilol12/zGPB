package discord.listeners;

import main.zGPB;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class GuildListener extends ListenerAdapter {

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        zGPB.INSTANCE.configurationHandler.createGuildProperties(event.getGuild().getIdLong());
    }

    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        // TODO: 03/04/2021 move to better place
        if (zGPB.INSTANCE.configurationHandler.getConfigBooleanValueForGuildByGuild(event.getGuild(), "fix_role_change")) {
            AtomicBoolean foundConflicting = new AtomicBoolean(false);
            String rawRoles = zGPB.INSTANCE.configurationHandler.getConfigValueForGuildByGuild(event.getGuild(), "fix_role_add");
            String[] conflicting = rawRoles.contains(",") ? rawRoles.split(",") : new String[]{rawRoles};

            if (conflicting.length == 0)
                return;

            event.getRoles().forEach(r -> {
                if (!foundConflicting.get())
                    foundConflicting.set(Arrays.stream(conflicting).anyMatch(v -> v.equals(r.getName())));
            });

            if (foundConflicting.get()) {
                String fixRemove = zGPB.INSTANCE.configurationHandler.getConfigValueForGuildByGuild(event.getGuild(), "fix_role_remove");
                if (fixRemove.trim().isEmpty())
                    return;
                List<Role> fixRole = zGPB.INSTANCE.discordHandler.getLocalJDA().getRolesByName(fixRemove, false);
                if (fixRole.isEmpty())
                    return;
                Role guildRole = fixRole.stream().filter(r -> r.getGuild().getIdLong() == event.getGuild().getIdLong()).findFirst().get();
                if (event.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES))
                    event.getGuild().removeRoleFromMember(event.getMember(), guildRole).queue();
            }
        }
    }
}
