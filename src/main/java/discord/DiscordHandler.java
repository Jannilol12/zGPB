package discord;

import discord.listeners.GuildListener;
import discord.listeners.MessageListener;
import log.Logger;
import main.zGPB;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscordHandler {

    private JDA localJDA;
    private ModerationHandler moderationHandler;

    public DiscordHandler() {
        moderationHandler = new ModerationHandler();
    }

    public void createConnection() {
        try {
            localJDA = JDABuilder.createDefault(zGPB.INSTANCE.botConfigurationHandler.getConfigValue("zGPB_token"))
                    .addEventListeners(new MessageListener(), new GuildListener())
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES)
                    .build();
            statusRotate();
        } catch (LoginException e) {
            Logger.logException(e);
        }

        Logger.logDebugMessage("Logging in as " + localJDA.getSelfUser().getAsTag());

    }

    public JDA getLocalJDA() {
        return localJDA;
    }

    private void statusRotate() {
        Activity[] dictionary = new Activity[6];
        dictionary[0] = Activity.playing("!help");

        Random r = new Random();

        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleAtFixedRate(() -> localJDA.getPresence().setActivity(dictionary[r.nextInt(dictionary.length)]), 1, 120, TimeUnit.SECONDS);

    }

    public ModerationHandler getModerationHandler() {
        return moderationHandler;
    }
}
