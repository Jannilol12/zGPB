package discord;

import discord.listeners.GeneralListener;
import discord.listeners.GuildListener;
import discord.listeners.MessageListener;
import discord.listeners.SlashCommandListener;
import log.Logger;
import main.zGPB;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;

public class DiscordHandler {

    private JDA localJDA;

    public void createConnection() {
        try {
            localJDA = JDABuilder.createDefault(zGPB.INSTANCE.botConfigurationHandler.getConfigValue("zGPB_token"))
                    .addEventListeners(new MessageListener(), new GuildListener(), new SlashCommandListener(), new GeneralListener())
                    .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES)
                    .build();
        } catch (LoginException e) {
            Logger.logException(e);
        }

        statusRotate();

        Logger.logDebugMessage("Logging in as " + localJDA.getSelfUser().getAsTag());
    }

    public JDA getLocalJDA() {
        return localJDA;
    }

    private void statusRotate() {
        localJDA.getPresence().setActivity(Activity.listening("!help"));
    }

}
