package discord.listeners;

import database.DataHandler;
import discord.command.commands.guild.ChannelCommand;
import main.zGPB;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GuildListener extends ListenerAdapter {

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        zGPB.INSTANCE.guildConfigurationHandler.updateGuild(event.getGuild().getIdLong());

        if (zGPB.INSTANCE.guildConfigurationHandler.getConfigBoolean(event.getGuild(), "mute_enabled")) {
            List<Role> mutedRole = event.getGuild().getRolesByName("muted", false);
            if (mutedRole.size() == 1) {
                for (GuildChannel tc : event.getGuild().getTextChannels()) {
                    if (!tc.getPermissionOverride(mutedRole.get(0)).getDenied().contains(Permission.MESSAGE_WRITE))
                        tc.createPermissionOverride(mutedRole.get(0)).setDeny(Permission.MESSAGE_WRITE).queue();
                }
            }

            event.getGuild().getRoles().stream().filter(r -> r.getName().startsWith("multicast-")).forEach(r -> r.delete().queue());

        }

    }

    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        DataHandler.handleRoleFixOnRoleAdd(event);
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        if (zGPB.INSTANCE.guildConfigurationHandler.getConfigBoolean(event.getGuild(), "temporary_channel_allowed")) {
            if (ChannelCommand.isTemporaryChannel(event.getChannelLeft().getIdLong())) {
                ChannelCommand.scheduleChannelDeletion(event.getChannelLeft().getIdLong());
            }
        }
        DataHandler.handleAssignment(event.getGuild(), event.getChannelJoined(), event.getMember());
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        DataHandler.handleAssignment(event.getGuild(), event.getChannelJoined(), event.getMember());
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        if (zGPB.INSTANCE.guildConfigurationHandler.getConfigBoolean(event.getGuild(), "temporary_channel_allowed")) {
            if (ChannelCommand.isTemporaryChannel(event.getChannelLeft().getIdLong())) {
                ChannelCommand.scheduleChannelDeletion(event.getChannelLeft().getIdLong());
            }
        }
    }

    @Override
    public void onVoiceChannelDelete(@NotNull VoiceChannelDeleteEvent event) {
        if (ChannelCommand.isTemporaryChannel(event.getChannel().getIdLong())) {
            DataHandler.removeTemporaryChannel(event.getChannel().getIdLong());
        }
    }
}
