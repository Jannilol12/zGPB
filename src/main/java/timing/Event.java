package timing;

import java.time.ZonedDateTime;

public record Event(long channelID, long messageID, ZonedDateTime time) {

}
