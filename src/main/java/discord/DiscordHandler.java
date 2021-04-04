package discord;

import discord.command.commands.ChannelCommand;
import discord.listeners.GuildListener;
import discord.listeners.MessageListener;
import log.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscordHandler {

    private JDA localJDA;

    public void createConnection() {
        try {
            localJDA = JDABuilder.createDefault(System.getenv("zGPB_token"))
                    .addEventListeners(new MessageListener(), new GuildListener())
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES)
                    .build();
        } catch (LoginException e) {
            Logger.logException(e);
        }

        // Create temporary channel cleaner that runs every day at 5 am
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
        ses.scheduleAtFixedRate(this::cleanTemporaryChannels,
                LocalTime.now().until(LocalTime.now().plus(1, ChronoUnit.SECONDS), ChronoUnit.SECONDS),
                TimeUnit.HOURS.toSeconds(1), TimeUnit.SECONDS);
        Logger.logDebugMessage("Started temporary cleanup executor service");
    }

    private void cleanTemporaryChannels() {
        ChannelCommand.deleteUnusedChannels();
    }

    public JDA getLocalJDA() {
        return localJDA;
    }

}
