package discord.listeners;

import database.DataHandler;
import discord.command.commands.ChannelCommand;
import main.zGPB;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class GuildListener extends ListenerAdapter {

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        zGPB.INSTANCE.guildConfigurationHandler.updateGuild(event.getGuild().getIdLong());
    }

    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        DataHandler.handleRoleFixOnRoleAdd(event);
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        if (zGPB.INSTANCE.guildConfigurationHandler.getConfigBoolean(event.getGuild(), "temporary_channel_allowed")) {
            if (ChannelCommand.isTemporaryChannel(event.getChannelLeft().getIdLong())) {
                ChannelCommand.scheduleChannelDeletion(event.getChannelLeft().getIdLong(), true);
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
                ChannelCommand.scheduleChannelDeletion(event.getChannelLeft().getIdLong(), true);
            }
        }
    }

}
