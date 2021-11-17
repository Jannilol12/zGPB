package timing;

import java.time.ZonedDateTime;

public record Event(long id, long channelID, long messageID, long userID, ZonedDateTime time, String content) {

    @Override
    public String toString() {
        return "Event{" +
               "id=" + id +
               ",channelID=" + channelID +
               ", messageID=" + messageID +
               ", userID=" + userID +
               ", time=" + time +
               ", content='" + content + '\'' +
               '}';
    }
    
}
