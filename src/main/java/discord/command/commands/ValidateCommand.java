package discord.command.commands;

import discord.Validator;
import discord.command.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ValidateCommand extends Command {

    public ValidateCommand() {
        super("validate", "validate id token", "Validates a command", 2, "verify");
    }

    @Override
    protected boolean onCommand(MessageReceivedEvent mre, String givenCommand, String[] splitCommand) {
        super.onCommand(mre, givenCommand, splitCommand);

        if (Validator.validate(mre, splitCommand[1], splitCommand[2])) {
            return true;
        } else
            mre.getMessage().reply("validation failed, are you sure the id and token is correct?").mentionRepliedUser(false).queue();

        return false;
    }
}
