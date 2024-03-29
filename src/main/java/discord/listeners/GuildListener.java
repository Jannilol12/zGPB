package discord.listeners;

import database.DataHandler;
import discord.ChannelHandler;
import main.zGPB;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
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

        event.getGuild().getRoles().stream().filter(r -> r.getName().startsWith("multicast-")).forEach(r -> r.delete().queue());
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        String role = zGPB.INSTANCE.guildConfigurationHandler.getConfigString(event.getGuild(), "join_role");
        if (!role.isBlank()) {
            List<Role> roles = event.getGuild().getRolesByName(role, false);
            if (roles.size() == 1) {
                event.getGuild().addRoleToMember(event.getMember(), roles.get(0)).queue();
            }
        }
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        if (zGPB.INSTANCE.guildConfigurationHandler.getConfigBoolean(event.getGuild(), "temporary_channel_allowed")) {
            if (ChannelHandler.isTemporaryChannel(event.getChannelLeft().getIdLong())) {
                ChannelHandler.scheduleChannelDeletion(event.getChannelLeft().getIdLong());
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
            if (ChannelHandler.isTemporaryChannel(event.getChannelLeft().getIdLong())) {
                ChannelHandler.scheduleChannelDeletion(event.getChannelLeft().getIdLong());
            }
        }
    }

    @Override
    public void onVoiceChannelDelete(@NotNull VoiceChannelDeleteEvent event) {
        if (ChannelHandler.isTemporaryChannel(event.getChannel().getIdLong())) {
            DataHandler.removeTemporaryChannel(event.getChannel().getIdLong());
        }
    }
}
