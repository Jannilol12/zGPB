package discord.command.commands.guild;

import discord.MessageCrafter;
import discord.command.GuildCommand;
import main.zGPB;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ConfigCommand extends GuildCommand {

    public ConfigCommand() {
        super("config", "config <key=value | show>", "sets configuration values for current guild", 1, Permission.MANAGE_ROLES);
    }

    @Override
    protected boolean onCommand(MessageReceivedEvent mre, String givenCommand, String[] splitCommand) {
        if (!super.onCommand(mre, givenCommand, splitCommand))
            return false;

        if (splitCommand[1].equals("show")) {
            mre.getMessage().reply(MessageCrafter.craftCodeMessage
                    ("json", zGPB.INSTANCE.guildConfigurationHandler.getGuildConfig(mre.getGuild().getIdLong()))).
                    mentionRepliedUser(false).queue();
        } else {
            if (mre.getMember() == null || !mre.getMember().hasPermission(Permission.MANAGE_ROLES)) {
                mre.getMessage().reply("insufficient permissions, MANAGE_ROLES needed").mentionRepliedUser(false).queue();
                return true;
            }

            if(!splitCommand[1].contains(" "))
                return true;

            String[] keyValueSplit = splitCommand[1].split("=");
            if (keyValueSplit.length <= 1) {
                mre.getMessage().reply("wrong syntax: `" + usage + "`").mentionRepliedUser(false).queue();
                return true;
            }
            if (zGPB.INSTANCE.guildConfigurationHandler.setConfig(mre.getGuild().getIdLong(), keyValueSplit[0], keyValueSplit[1]))
                mre.getMessage().reply("successfully set config value").mentionRepliedUser(false).queue();
            else
                mre.getMessage().reply("invalid config value, is the type correct?").mentionRepliedUser(false).queue();
        }

        return true;
    }
}
