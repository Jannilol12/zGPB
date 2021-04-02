package timing;

import java.time.LocalDateTime;

public record Event(long channelID, long messageID, LocalDateTime time) {

}
