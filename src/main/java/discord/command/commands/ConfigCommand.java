package discord.command.commands;

import discord.MessageCrafter;
import discord.command.Command;
import main.JADB;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ConfigCommand extends Command {

    public ConfigCommand() {
        super("config", "config <key=value | show>", "Sets configuration values for current guild", 1, "configuration");
    }

    @Override
    protected boolean onCommand(MessageReceivedEvent mre, String givenCommand, String[] splitCommand) {
        if(!super.onCommand(mre, givenCommand, splitCommand))
            return false;

        if (splitCommand[1].equals("show")) {
            mre.getMessage().reply(MessageCrafter.craftCodeMessage
                    ("json", JADB.INSTANCE.configurationHandler.getPropertiesAsString(mre.getGuild().getIdLong()))).
                    mentionRepliedUser(false).queue();
        } else {
            String[] keyValueSplit = splitCommand[1].split("=");
            JADB.INSTANCE.configurationHandler.setConfigValueForGuild(mre, keyValueSplit[0], keyValueSplit[1]);
            mre.getMessage().reply("Successfully set config value").mentionRepliedUser(false).queue();
        }

        return true;
    }
}
