package hospicloud.security;

import java.util.regex.Pattern;

public final class TeleconsultationChatTopics {

    public static final Pattern CHAT_TOPIC =
            Pattern.compile("^/topic/tenant/(\\d+)/teleconsultation/(\\d+)/chat$");

    private TeleconsultationChatTopics() {}

    public static String destination(Integer tenantId, Integer idRdv) {
        if (tenantId == null || idRdv == null) {
            throw new IllegalArgumentException("tenantId et idRdv requis pour le chat téléconsultation");
        }
        return "/topic/tenant/" + tenantId + "/teleconsultation/" + idRdv + "/chat";
    }
}
