package hospicloud.security;

import java.util.regex.Pattern;

public final class RealtimeNotificationTopics {

    public static final Pattern USER_TOPIC = Pattern.compile("^/topic/tenant/(\\d+)/user/(\\d+)/notifications$");

    private RealtimeNotificationTopics() {}

    public static String destination(Integer tenantId, Integer userId) {
        if (tenantId == null || userId == null) {
            throw new IllegalArgumentException("tenantId et userId requis pour la notification temps réel");
        }
        return "/topic/tenant/" + tenantId + "/user/" + userId + "/notifications";
    }
}
