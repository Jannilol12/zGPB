package discord.command;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Set;

public abstract class SlashCommand {

    protected String name;
    protected String description;

    protected Set<OptionData> options;

    public SlashCommand(String name, String description, Set<OptionData> options) {
        this.name = name;
        this.description = description;
        this.options = options;
    }

    protected CommandData getAsCommandData() {
        return new CommandData(name, description).addOptions(options);
    }

    protected boolean onCommand(SlashCommandEvent sce, String commandString) {

        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<OptionData> getOptions() {
        return options;
    }

    public void setOptions(Set<OptionData> options) {
        this.options = options;
    }
}
