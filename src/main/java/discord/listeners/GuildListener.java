package discord.listeners;

import main.JADB;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class GuildListener extends ListenerAdapter {

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        JADB.INSTANCE.configurationHandler.createGuildProperties(event.getGuild().getIdLong());
    }

}
