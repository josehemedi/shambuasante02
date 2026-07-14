package hospicloud.security;

import java.util.regex.Pattern;

public final class ReceptionLiveTopics {

    public static final Pattern RECEPTION_TOPIC =
            Pattern.compile("^/topic/reception/(\\d+)$");

    private ReceptionLiveTopics() {}

    public static String destination(Integer tenantId) {
        return "/topic/reception/" + tenantId;
    }
}
