package discord.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.HashSet;

public abstract class Command {

    private String name, usage, description;
    private int argCount;
    private HashSet<String> aliases;

    public Command(String name, String usage, String description, int argCount, String... aliases) {
        this.name = name;
        this.usage = usage;
        this.description = description;
        this.argCount = argCount;
        this.aliases = createAliasSetFromString(aliases);
    }

    public Command(String name, String usage, String description, int argCount) {
        this.name = name;
        this.usage = usage;
        this.description = description;
        this.argCount = argCount;
    }

    protected void onCommand(MessageReceivedEvent mre, String givenCommand, String[] splitCommand) {
        if (!isSyntaxCorrect(givenCommand)) {
            System.out.println(usage);
            return;
        }
    }

    private HashSet<String> createAliasSetFromString(String... alias) {
        HashSet<String> aliasCrate = new HashSet<>();
        aliasCrate.addAll(Arrays.asList(alias));
        return aliasCrate;
    }

    protected boolean isSyntaxCorrect(String command) {
        if (command.split(" ").length - 1 != argCount)
            return false;
        return true;
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