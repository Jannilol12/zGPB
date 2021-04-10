package discord;

import discord.command.commands.guild.ChannelCommand;
import discord.listeners.GuildListener;
import discord.listeners.MessageListener;
import log.Logger;
import main.zGPB;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscordHandler {

    private JDA localJDA;

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

        // Create temporary channel cleaner that runs every day at 5 am
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
        long timeUntil5AM = LocalTime.now().until(LocalTime.of(3, 38), ChronoUnit.SECONDS);
        ses.scheduleAtFixedRate(this::cleanTemporaryChannels, timeUntil5AM, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
        Logger.logDebugMessage("Started temporary cleanup executor service");
    }

    public JDA getLocalJDA() {
        return localJDA;
    }

    private void cleanTemporaryChannels() {
        ChannelCommand.deleteUnusedChannels();
    }

    private void statusRotate() {
        Activity[] dictionary = new Activity[7];
        dictionary[0] = (Activity.watching("my life going downhill"));
        dictionary[1] = (Activity.listening("elmasri navathe fundamentals of database systems part 37"));
        dictionary[2] = (Activity.playing("AuD Speedrun any%"));
        dictionary[3] = (Activity.competing("not crashing championship"));
        dictionary[4] = (Activity.playing("stay at home #2"));
        dictionary[5] = (Activity.playing("programming kenken"));
        dictionary[6] = (Activity.playing("rüge verfassen"));

        Random r = new Random();

        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleAtFixedRate(() -> {
            localJDA.getPresence().setActivity(dictionary[r.nextInt(dictionary.length)]);
        }, 10, 60, TimeUnit.SECONDS);

    }

}
