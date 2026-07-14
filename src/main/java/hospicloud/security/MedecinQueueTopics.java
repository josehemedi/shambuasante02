package hospicloud.security;

import java.util.regex.Pattern;

/**
 * Topic live de la file d'attente d'un médecin.
 * /topic/medecin-queue/{tenantId}/{medecinId}
 */
public final class MedecinQueueTopics {

    public static final Pattern MEDECIN_QUEUE_TOPIC =
            Pattern.compile("^/topic/medecin-queue/(\\d+)/(\\d+)$");

    private MedecinQueueTopics() {}

    public static String destination(Integer tenantId, Integer medecinId) {
        return "/topic/medecin-queue/" + tenantId + "/" + medecinId;
    }
}
