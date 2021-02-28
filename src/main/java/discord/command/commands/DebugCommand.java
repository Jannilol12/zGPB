package discord.command.commands;

import discord.command.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.StringJoiner;

public class DebugCommand extends Command {

    public DebugCommand() {
        super("debug", "debug", "debug", 0);
    }

    @Override
    protected void onCommand(MessageReceivedEvent mre, String givenCommand, String[] splitCommand) {

        if (mre.isFromGuild()) {
            Guild g = mre.getGuild();
            for (Role r : g.getRoles()) {
                StringJoiner out = new StringJoiner(System.lineSeparator());
                out.add(r.getName());

                for (Permission p : r.getPermissions()) {
                    out.add(p.getName());
                }
                mre.getChannel().sendMessage(out.toString()).queue();
            }
        }

    }
}
