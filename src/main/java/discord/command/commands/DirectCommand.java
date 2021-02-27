package discord.command.commands;

import discord.command.Command;
import main.JADB;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class DirectCommand extends Command {

    public DirectCommand() {
        super("direct", "direct <db|database|bot> query", "Provides a raw interface to interact with internal bot components", 3);
    }

    @Override
    protected boolean isSyntaxCorrect(String command) {
        String[] partSplit = command.split(" ", 3);
        return partSplit.length == getArgCount();
    }

    @Override
    protected void onCommand(MessageReceivedEvent mre, String givenCommand, String[] splitCommand) {
        super.onCommand(mre, givenCommand, splitCommand);

        // TODO: Permission handling
        if (mre.getAuthor().getId().equals(System.getenv("jadb_owner"))) {
            if ((splitCommand[1].equals("db") || splitCommand[1].equals("database"))) {
                // Manual split because query can contain whitespaces
                String actualQuery = givenCommand.split(" ", 3)[2];
                String result = JADB.INSTANCE.databaseHandler.getResultFromQuery(actualQuery).replace("`", "´");
                if (result.length() >= 2000) {
                    result = result.substring(0, 1980);
                    result += "...";
                }
                mre.getChannel().sendMessage(
                        "```java" + System.lineSeparator() + result + "```").queue();
            }
        }

    }
}
