package discord.listeners;

import main.zGPB;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class GeneralListener extends ListenerAdapter {

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        zGPB.INSTANCE.onReadyCallback();
    }
}
