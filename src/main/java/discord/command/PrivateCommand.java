package discord.command;

@Deprecated
public abstract class PrivateCommand extends Command {

    public PrivateCommand(String name, String usage, String description, int argCount, String... aliases) {
        super(name, usage, description, argCount, aliases);
    }

    public PrivateCommand(String name, String usage, String description, int argCount) {
        super(name, usage, description, argCount);
    }
}
