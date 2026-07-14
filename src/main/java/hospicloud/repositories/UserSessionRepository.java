package hospicloud.repositories;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserSessionRepository {
    void ensureSchema();

    boolean hasActiveSession(Integer idUtilisateur);

    boolean hasRecentActiveSession(Integer idUtilisateur, int inactivityThresholdSeconds);

    void createSession(Integer idUtilisateur, String jti, String adresseIp, LocalDateTime expiresAt);

    boolean isSessionActive(String jti);

    void touchSession(String jti);

    void invalidateByJti(String jti);

    void invalidateAllForUser(Integer idUtilisateur);

    void purgeExpired();

    void purgeInactiveSessions(int inactivityThresholdSeconds);

    Optional<String> findActiveJtiForUser(Integer idUtilisateur);
}
