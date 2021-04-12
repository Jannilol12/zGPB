package timing;

import java.time.ZonedDateTime;

public record Event(long channelID, long messageID, ZonedDateTime time, String content) {

    @Override
    public String toString() {
        return "Event{" +
               "channelID=" + channelID +
               ", messageID=" + messageID +
               ", time=" + time +
               ", content='" + content + '\'' +
               '}';
    }
    
}
