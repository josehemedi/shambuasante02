package hospicloud.servicesImpl;

import io.livekit.server.AccessToken;
import io.livekit.server.CanPublish;
import io.livekit.server.CanSubscribe;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LiveKitService {

    private static final Logger logger = LoggerFactory.getLogger(LiveKitService.class);

    @Value("${livekit.api.key:}")
    private String apiKey;

    @Value("${livekit.api.secret:}")
    private String apiSecret;

    @Value("${livekit.url:wss://hospicloud-nz8gfvan.livekit.cloud}")
    private String liveKitUrl;

    public String generateToken(String roomName, String participantIdentity, String displayName) {
        validateConfiguration();

        AccessToken token = new AccessToken(apiKey.trim(), apiSecret.trim());

        token.setIdentity(participantIdentity);
        token.setName(displayName != null && !displayName.isBlank() ? displayName : participantIdentity);
        // TTL en millisecondes (2 h) — ne pas confondre avec des secondes
        token.setTtl(TimeUnit.HOURS.toMillis(2));

        token.addGrants(
            new RoomJoin(true),
            new RoomName(roomName),
            new CanPublish(true),
            new CanSubscribe(true)
        );

        return token.toJwt();
    }

    public String getLiveKitUrl() {
        validateConfiguration();
        return liveKitUrl.trim();
    }

    public String generateRoomName(Integer idHopital, Integer idRdv) {
        if (idHopital == null || idHopital <= 0) {
            throw new IllegalArgumentException("idHopital requis pour la salle LiveKit.");
        }
        if (idRdv == null || idRdv <= 0) {
            throw new IllegalArgumentException("idRendezVous requis pour la salle LiveKit.");
        }
        return "tenant-" + idHopital + "-teleconsultation-" + idRdv;
    }

    /** @deprecated Préférer {@link #generateRoomName(Integer, Integer)} pour l'isolation multi-tenant. */
    @Deprecated
    public String generateRoomName(Integer idRdv) {
        return "teleconsultation-" + idRdv;
    }

    private void validateConfiguration() {
        if (apiKey == null || apiKey.isBlank() || apiSecret == null || apiSecret.isBlank()) {
            throw new IllegalStateException(
                    "LiveKit non configuré : définissez livekit.api.key et livekit.api.secret "
                            + "(ou variables LIVEKIT_API_KEY / LIVEKIT_API_SECRET).");
        }
        if (liveKitUrl == null || liveKitUrl.isBlank()) {
            throw new IllegalStateException("LiveKit non configuré : livekit.url est requis.");
        }
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank()
                && apiSecret != null && !apiSecret.isBlank()
                && liveKitUrl != null && !liveKitUrl.isBlank();
    }

    public void logConfigurationHint() {
        if (!isConfigured()) {
            logger.warn("LiveKit : clés API absentes — la téléconsultation vidéo sera indisponible.");
            return;
        }
        logger.info("LiveKit configuré pour l'hôte {}", liveKitUrl.trim());
    }
}