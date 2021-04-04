package discord.command.commands;

import discord.MessageCrafter;
import discord.command.Command;
import discord.command.CommandType;
import main.zGPB;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ConfigCommand extends Command {

    public ConfigCommand() {
        super("config", "config <key=value | show>", "sets configuration values for current guild", 1, CommandType.GUILD);
    }

    @Override
    protected boolean onCommand(MessageReceivedEvent mre, String givenCommand, String[] splitCommand) {
        if (!super.onCommand(mre, givenCommand, splitCommand))
            return false;

        if (splitCommand[1].equals("show")) {
            mre.getMessage().reply(MessageCrafter.craftCodeMessage
                    ("json", zGPB.INSTANCE.configurationHandler.getPropertiesAsString(mre.getGuild().getIdLong()))).
                    mentionRepliedUser(false).queue();
        } else {
            if (mre.getMember() == null || !mre.getMember().hasPermission(Permission.MANAGE_ROLES)) {
                mre.getMessage().reply("insufficient permissions").mentionRepliedUser(false).queue();
            }

            String[] keyValueSplit = splitCommand[1].split("=");
            if (keyValueSplit.length <= 1) {
                mre.getMessage().reply("wrong syntax: `" + usage + "`").mentionRepliedUser(false).queue();
                return false;
            }
            zGPB.INSTANCE.configurationHandler.setConfigValueForGuild(mre, keyValueSplit[0], keyValueSplit[1]);
            mre.getMessage().reply("successfully set config value").mentionRepliedUser(false).queue();
        }

        return true;
    }
}
