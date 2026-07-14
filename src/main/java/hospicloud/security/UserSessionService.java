package hospicloud.security;

import hospicloud.repositories.UserSessionRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Duration;

@Service
public class UserSessionService {

    /** Fenêtre d'activité : au-delà, la session est considérée comme fermée (navigateur fermé sans logout). */
    private static final int INACTIVITY_THRESHOLD_SECONDS = 180;

    private final UserSessionRepository userSessionRepository;
    private final JwtProperties jwtProperties;

    public UserSessionService(UserSessionRepository userSessionRepository, JwtProperties jwtProperties) {
        this.userSessionRepository = userSessionRepository;
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    public void purgeStaleSessionsOnStartup() {
        purgeExpired();
        purgeInactiveSessions();
    }

    public boolean hasActiveSession(Integer idUtilisateur) {
        return userSessionRepository.hasActiveSession(idUtilisateur);
    }

    /**
     * Session réellement utilisée (activité API récente), pas seulement une entrée JWT non expirée.
     */
    public boolean hasRecentActiveSession(Integer idUtilisateur) {
        purgeExpired();
        return userSessionRepository.hasRecentActiveSession(idUtilisateur, INACTIVITY_THRESHOLD_SECONDS);
    }

    public void registerSession(Integer idUtilisateur, String jti, String adresseIp) {
        LocalDateTime expiresAt = LocalDateTime.now().plus(Duration.ofMillis(jwtProperties.getExpirationMs()));
        userSessionRepository.createSession(idUtilisateur, jti, adresseIp, expiresAt);
        userSessionRepository.touchSession(jti);
    }

    public boolean isSessionActive(String jti) {
        return userSessionRepository.isSessionActive(jti);
    }

    public void touchSession(String jti) {
        userSessionRepository.touchSession(jti);
    }

    public void invalidateSession(String jti) {
        userSessionRepository.invalidateByJti(jti);
    }

    public void invalidateAllForUser(Integer idUtilisateur) {
        userSessionRepository.invalidateAllForUser(idUtilisateur);
    }

    public void purgeExpired() {
        userSessionRepository.purgeExpired();
    }

    public void purgeInactiveSessions() {
        userSessionRepository.purgeInactiveSessions(INACTIVITY_THRESHOLD_SECONDS);
    }
}
