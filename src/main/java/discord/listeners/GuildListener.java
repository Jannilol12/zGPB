package discord.listeners;

import discord.DataHandler;
import discord.command.commands.ChannelCommand;
import main.Util;
import main.zGPB;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.EnumSet;
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
        // TODO: 04/04/2021 fix after new role change
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

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        if (zGPB.INSTANCE.configurationHandler.getConfigBooleanValueForGuildByGuild(event.getGuild(), "temporary_channel_allowed")) {
            if(ChannelCommand.isTemporaryChannel(event.getChannelLeft().getIdLong())) {
                ChannelCommand.scheduleChannelDeletion(event.getChannelLeft().getIdLong());
            }
        }
        handleAssignment(event.getGuild(), event.getChannelJoined(), event.getMember());
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        handleAssignment(event.getGuild(), event.getChannelJoined(), event.getMember());
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        if (zGPB.INSTANCE.configurationHandler.getConfigBooleanValueForGuildByGuild(event.getGuild(), "temporary_channel_allowed")) {
            if(ChannelCommand.isTemporaryChannel(event.getChannelLeft().getIdLong())) {
                ChannelCommand.scheduleChannelDeletion(event.getChannelLeft().getIdLong());
            }
        }
    }

    // TODO: 04/04/2021 move to better place
    private void handleAssignment(Guild guild, VoiceChannel channelJoined, Member member) {
        if (zGPB.INSTANCE.configurationHandler.getConfigBooleanValueForGuildByGuild(guild, "temporary_channel_allowed")) {
            long channelID = zGPB.INSTANCE.configurationHandler.getConfigLongValueForGuildByGuild(guild, "temporary_channel_assignment");
            if (channelJoined.getIdLong() == channelID) {
                Category category = guild.getCategoryById(zGPB.INSTANCE.configurationHandler.
                        getConfigLongValueForGuildByGuild(guild, "temporary_channel_category"));
                if (category != null) {

                    long maxChannelsPerUser = zGPB.INSTANCE.configurationHandler.getConfigLongValueForGuildByGuild(guild, "temporary_channel_max");
                    if (ChannelCommand.getChannelCountByUser(member.getIdLong()) >= maxChannelsPerUser) {
                        zGPB.INSTANCE.discordHandler.getLocalJDA().
                                retrieveUserById(member.getId()).queue(u -> u.openPrivateChannel().
                                queue(p -> p.sendMessage("you are only allowed to create " + maxChannelsPerUser + " voice channels in this guild, try deleting old ones first").queue()));
                    } else {
                        category.createVoiceChannel("channel_" + Util.createRandomString(2)).
                                setUserlimit(16).setBitrate(guild.getMaxBitrate()).addMemberPermissionOverride(member.getIdLong(),
                                EnumSet.of(Permission.MANAGE_CHANNEL), null).queue(v -> {
                            DataHandler.addTemporaryChannel(v, member);
                            ChannelCommand.channelMappings.add(new AbstractMap.SimpleEntry<>(member.getIdLong(), v));
                            guild.moveVoiceMember(member, v).queue();
                        });
                    }
                }
            }

        }
    }

}
