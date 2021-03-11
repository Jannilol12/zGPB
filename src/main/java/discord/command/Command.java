package discord.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.HashSet;

public abstract class Command {

    protected String name, usage, description;
    protected int argCount;
    protected HashSet<String> aliases;

    public Command(String name, String usage, String description, int argCount, String... aliases) {
        this(name, usage, description, argCount);
        this.aliases = createAliasSetFromString(aliases);
    }

    public Command(String name, String usage, String description, int argCount) {
        this.name = name;
        this.usage = usage;
        this.description = description;
        this.argCount = argCount;
    }

    protected boolean onCommand(MessageReceivedEvent mre, String givenCommand, String[] splitCommand) {
        if (!isSyntaxCorrect(givenCommand)) {
            mre.getMessage().reply("wrong syntax: `" + usage+"`").mentionRepliedUser(false).queue();
            return false;
        }
        return true;
    }

    private HashSet<String> createAliasSetFromString(String... alias) {
        return new HashSet<>(Arrays.asList(alias));
    }

    protected boolean isSyntaxCorrect(String command) {
        return command.split(" ",argCount).length == argCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getArgCount() {
        return argCount;
    }

    public void setArgCount(int argCount) {
        this.argCount = argCount;
    }

    public HashSet<String> getAliases() {
        return aliases;
    }

    public void setAliases(HashSet<String> aliases) {
        this.aliases = aliases;
    }
}
