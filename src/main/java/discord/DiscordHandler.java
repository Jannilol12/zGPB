package discord;

import discord.listeners.MessageListener;
import log.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;

public class DiscordHandler {

    private JDA localJDA;

    public void createConnection() {
        try {
            localJDA = JDABuilder.createDefault(System.getenv("jadb_token"))
                    .addEventListeners(new MessageListener())
                    .build();
        } catch (LoginException e) {
            Logger.logException(e);
        }
    }

    public JDA getLocalJDA() {
        return localJDA;
    }

}
