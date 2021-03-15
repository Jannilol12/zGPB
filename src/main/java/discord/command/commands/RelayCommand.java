package discord.command.commands;

import discord.command.Command;
import discord.command.CommandType;
import main.JADB;
import main.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Instant;
import java.util.List;

public class RelayCommand extends Command {

    public RelayCommand() {
        super("relay", "relay <channel_name | channel_id> message", "relays a message to a given channel", 2, CommandType.PRIVATE);
    }

    @Override
    protected boolean onCommand(MessageReceivedEvent mre, String givenCommand, String[] splitCommand) {
        if (!super.onCommand(mre, givenCommand, splitCommand))
            return false;

        if (splitCommand[1].chars().allMatch(Character::isDigit)) {
            relayMessage(mre, JADB.INSTANCE.discordHandler.getLocalJDA().getTextChannelById(Long.parseLong(splitCommand[1])), splitCommand[2]);
        } else {
            List<TextChannel> channels = JADB.INSTANCE.discordHandler.getLocalJDA().getTextChannelsByName(splitCommand[1], false);
            if (channels.size() == 0) {
                mre.getMessage().reply("There was no channel found that matches the given name").mentionRepliedUser(false).queue();
                return false;
            } else if (channels.size() > 1) {
                mre.getMessage().reply("This channel name is ambiguous, please provide the channel id").mentionRepliedUser(false).queue();
                return false;
            }
            relayMessage(mre, channels.get(0), splitCommand[2]);
        }


        return true;
    }

    private void relayMessage(MessageReceivedEvent mre, TextChannel channel, String message) {

        if (!JADB.INSTANCE.configurationHandler.getConfigBooleanValueForGuildByChannel(channel, "allow_message_relay")) {
            mre.getMessage().reply("This guild does not allow relaying messages").mentionRepliedUser(false).queue();
            return;
        }

        if (!channel.canTalk()) {
            mre.getMessage().reply("The bot can't interact with this channel").mentionRepliedUser(false).queue();
        } else {
            channel.sendMessage(
                    new EmbedBuilder().addField("Message relay", message, true).setAuthor("Anonymous").
                            setTimestamp(Instant.now()).setFooter(Util.createRandomString(8)).build()).
                    queue();
        }
    }

}
