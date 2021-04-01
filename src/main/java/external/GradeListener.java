package external;

import discord.EmbedField;
import discord.MessageCrafter;
import log.Logger;
import main.JADB;
import net.dv8tion.jda.api.entities.TextChannel;
import network.GradeEntry;
import network.NetworkUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class GradeListener {

    public boolean isEnabled = true;
    private Set<GradeEntry> current;
    private boolean successfulInit = false;

    public GradeListener() {
        if (isEnabled) {
            if (NetworkUtil.initializeCampusConnection()) {
                current = Collections.emptySet();
                successfulInit = true;
                waitForResults();
            } else {
                Logger.logException("Couldn't initialize campus connection");
            }

        }
    }

    public void waitForResults() {
        new Thread(() -> {

            while (true) {


                if (!isEnabled)
                    return;

                if (!successfulInit)
                    return;

                try {

                    Logger.logDebugMessage("Fetching new grades");
                    Set<GradeEntry> newEntries = Objects.requireNonNull(NetworkUtil.getGradesFromMyCampus());

                    if (newEntries.size() != current.size()) {
                        Set<GradeEntry> tempCopy = new HashSet<>(newEntries);
                        newEntries.removeAll(current);

                        for (long channelID : JADB.INSTANCE.configurationHandler.getChannelsForGradeNotification()) {
                            TextChannel tc = JADB.INSTANCE.discordHandler.getLocalJDA().getTextChannelById(channelID);

                            if (tc != null) {
                                for (GradeEntry ge : newEntries) {
                                    tc.sendMessage(MessageCrafter.craftGenericEmbedMessage("exam got updated",
                                            new EmbedField("name", ge.name(), false),
                                            new EmbedField("semester", ge.semester(), false),
                                            new EmbedField("id", ge.id(), true),
                                            new EmbedField("date", ge.date(), true),
                                            new EmbedField("ects", ge.ects(), true)
                                    )).queue();
                                }
                            } else {
                                Logger.logException("channel couldn't be found");
                            }

                        }

                        current = tempCopy;
                    }

                    TimeUnit.MINUTES.sleep(5);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }

}
