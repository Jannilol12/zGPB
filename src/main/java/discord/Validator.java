package discord;

import discord.command.Command;
import log.Logger;
import main.Util;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class Validator {

    private static final HashMap<String, CachedCommand> cachedCommands = new HashMap<>();

    private static final HashSet<String> tokenSet = new HashSet<>();

    public static void generateTokens() {
        for (int i = 0; i < 16; i++) {
            String curUUID = UUID.randomUUID().toString();
            tokenSet.add(curUUID);
            Logger.logDebugMessage("Created token " + i + " = " + curUUID);
        }
    }

    public static void revokeTokens() {
        Logger.logDebugMessage("Revoked " + tokenSet.size() + " tokens");
        cachedCommands.clear();
    }

    public static String cacheCommand(Command cache, MessageReceivedEvent mre, String command, String[] split) {
        String id = Util.createRandomString(12);
        cachedCommands.put(id, new CachedCommand(cache, mre, command, split));
        return id;
    }

    public static boolean validate(MessageReceivedEvent mre, String id, String token) {
        if (cachedCommands.containsKey(id) && tokenSet.contains(token)) {
            tokenSet.remove(token);
            mre.getMessage().reply("validation successful, used token " + token).mentionRepliedUser(false).queue();
            CachedCommand cur = cachedCommands.get(id);
            cur.cache.onValidate(cur.mre, cur.givenCommand, cur.splitCommand);
            cachedCommands.remove(id);
            return true;
        }
        return false;
    }

    private static class CachedCommand {

        private Command cache;
        private MessageReceivedEvent mre;
        private String givenCommand;
        private String[] splitCommand;

        public CachedCommand(Command c, MessageReceivedEvent mre, String givenCommand, String[] splitCommand) {
            this.cache = c;
            this.mre = mre;
            this.givenCommand = givenCommand;
            this.splitCommand = splitCommand;
        }
    }

}
