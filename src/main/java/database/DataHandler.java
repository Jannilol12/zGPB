package database;

import discord.ChannelHandler;
import log.Logger;
import main.Util;
import main.zGPB;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import timing.Event;

import java.util.EnumSet;
import java.util.Set;

public class DataHandler {

    public static Set<TemporaryChannel> getTemporaryChannels() {
        return zGPB.INSTANCE.databaseHandler.getAllTemporaryChannels();
    }

    public static void addTemporaryChannel(VoiceChannel vc, MessageReceivedEvent mre) {
        zGPB.INSTANCE.databaseHandler.insertTemporaryChannel(vc.getIdLong(), mre.getAuthor().getIdLong(), mre.getGuild().getIdLong(), vc.getName());
    }

    public static void addTemporaryChannel(VoiceChannel vc, Member m) {
        zGPB.INSTANCE.databaseHandler.insertTemporaryChannel(vc.getIdLong(), m.getIdLong(), vc.getGuild().getIdLong(), vc.getName());
    }

    public static long getTemporaryChannelByNameAndID(long authorID, String channelName) {
        return zGPB.INSTANCE.databaseHandler.getTemporaryChannelIDByNameAndAuthor(authorID, channelName);
    }

    public static void removeTemporaryChannel(long channelID) {
        zGPB.INSTANCE.databaseHandler.removeTemporaryChannel(channelID);
    }

    public static void saveReminder(Event e) {
        zGPB.INSTANCE.databaseHandler.insertReminder(e);
    }

    public static void removeReminder(Event e) {
        zGPB.INSTANCE.databaseHandler.removeReminder(e);
    }

    // TODO: Move to ChannelHandler
    public static void handleAssignment(Guild guild, VoiceChannel channelJoined, Member member) {
        if (zGPB.INSTANCE.guildConfigurationHandler.getConfigBoolean(guild, "temporary_channel_allowed")) {
            long channelID = zGPB.INSTANCE.guildConfigurationHandler.getConfigLong(guild, "temporary_channel_assignment");
            if (channelJoined.getIdLong() == channelID) {
                Category category = guild.getCategoryById(zGPB.INSTANCE.guildConfigurationHandler.
                        getConfigLong(guild, "temporary_channel_category"));

                if (category != null) {
                    long maxChannelsPerUser = zGPB.INSTANCE.guildConfigurationHandler.getConfigLong(guild, "temporary_channel_max");
                    if (ChannelHandler.getChannelCountByUser(member.getIdLong()) >= maxChannelsPerUser) {
                        zGPB.INSTANCE.discordHandler.getLocalJDA().
                                retrieveUserById(member.getId()).queue(u -> u.openPrivateChannel().
                                queue(p -> {
                                    p.sendMessage("you are only allowed to create " + maxChannelsPerUser + " voice channels in this guild, try deleting old ones first").queue();
                                    guild.kickVoiceMember(member).queue();
                                }));
                    } else {
                        category.createVoiceChannel("channel_" + Util.createRandomString(2)).
                                setUserlimit(16).setBitrate(guild.getMaxBitrate()).addMemberPermissionOverride(member.getIdLong(),
                                EnumSet.of(Permission.MANAGE_CHANNEL), null).queue(v -> {
                            DataHandler.addTemporaryChannel(v, member);
                            try {
                                // Catch unknown user
                                if (member.getVoiceState().inVoiceChannel()) {
                                    guild.moveVoiceMember(member, v).queue();
                                }
                            } catch (Exception e) {
                                Logger.logException("illegal voice state, " + e.getMessage());
                            }
                            ChannelHandler.scheduleChannelDeletion(v.getIdLong());
                        });
                    }
                }
            }
        }
    }

}
