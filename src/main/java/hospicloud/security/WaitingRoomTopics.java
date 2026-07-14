package hospicloud.security;

import java.util.regex.Pattern;

public final class WaitingRoomTopics {

    public static final Pattern WAITING_ROOM_TOPIC =
            Pattern.compile("^/topic/waiting-room/(\\d+)$");

    private WaitingRoomTopics() {}

    public static String destination(Integer tenantId) {
        return "/topic/waiting-room/" + tenantId;
    }
}
