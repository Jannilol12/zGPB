package discord.command.commands;

import discord.MessageCrafter;
import discord.command.Command;
import log.Logger;
import main.zGPB;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class InternalCommand extends Command {

    public InternalCommand() {
        super("internal", "internal <db|database|bot|log> query", "provides a raw interface to interact with internal bot components", 3);
    }

    @Override
    protected boolean isSyntaxCorrect(String command) {
        String[] partSplit = command.split(" ", 3);
        return partSplit.length == getArgCount();
    }

    @Override
    protected boolean onCommand(MessageReceivedEvent mre, String givenCommand, String[] splitCommand) {
        if (!super.onCommand(mre, givenCommand, splitCommand))
            return false;

        if (mre.getAuthor().getId().equals(zGPB.INSTANCE.botConfigurationHandler.getConfigValue("zGPB_owner"))) {
            if ((splitCommand[1].equals("db") || splitCommand[1].equals("database"))) {
                // Manual split because query can contain whitespaces
                String actualQuery = givenCommand.split(" ", 3)[2];
                String result = zGPB.INSTANCE.databaseHandler.getResultFromQuery(actualQuery).replace("`", "´");
                mre.getMessage().reply(MessageCrafter.craftCodeMessage("sql", result)).mentionRepliedUser(false).queue();
            } else if (splitCommand[1].equals("bot")) {
                if (splitCommand[2].equals("shutdown")) {
                    mre.getMessage().reply("goodbye").complete();
                    System.exit(0);
                } else if (splitCommand[2].equals("reinit")) {
                    mre.getMessage().reply("trying to reinitialize").queue();
                    zGPB.INSTANCE.gradeManager.startMonitoring();
                }
            } else if (splitCommand[1].equals("log")) {
                mre.getMessage().reply(MessageCrafter.craftCodeMessage("yml", Logger.getLastMessages())).mentionRepliedUser(false).queue();
            } else if (splitCommand[1].equals("mappings")) {

            }
        } else {
            mre.getMessage().reply("security violation, are you sure your environment variables are set correctly?").mentionRepliedUser(false).queue();
        }

        return true;
    }

}
