package discord.command;

import discord.command.slashcommands.RemindMeSlashCommand;
import main.zGPB;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.HashSet;
import java.util.Set;

public class SlashCommandHandler {

    private final Set<SlashCommand> commands;

    public SlashCommandHandler() {
        this.commands = new HashSet<>();
        registerCommands();
    }

    private void registerCommands() {
        commands.add(new RemindMeSlashCommand());

        for(SlashCommand slashCommand : commands) {
            zGPB.INSTANCE.discordHandler.getLocalJDA().getGuildById(814890317538394152L).upsertCommand(slashCommand.getAsCommandData()).queue();
        }
    }

    public void onCommand(SlashCommandEvent sce) {
        for(SlashCommand sc :commands) {
            if(sc.getName().equals(sce.getName()))
                sc.onCommand(sce, sce.getCommandString());
        }
    }

}
