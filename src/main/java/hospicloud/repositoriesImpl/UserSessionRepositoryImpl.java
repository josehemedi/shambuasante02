package hospicloud.repositoriesImpl;

import hospicloud.repositories.UserSessionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class UserSessionRepositoryImpl implements UserSessionRepository {

    private final JdbcTemplate jdbcTemplate;
    private volatile boolean schemaEnsured = false;

    public UserSessionRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void ensureSchema() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS sessions_utilisateurs (
                    id_session INT NOT NULL AUTO_INCREMENT,
                    id_utilisateur INT NOT NULL,
                    token_session VARCHAR(255) NOT NULL,
                    date_connexion TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                    date_expiration TIMESTAMP NULL DEFAULT NULL,
                    derniere_activite TIMESTAMP NULL DEFAULT NULL,
                    adresse_ip VARCHAR(45) DEFAULT NULL,
                    PRIMARY KEY (id_session),
                    UNIQUE KEY uk_session_jti (token_session),
                    KEY idx_session_user (id_utilisateur),
                    KEY idx_session_expiration (date_expiration),
                    KEY idx_session_activite (derniere_activite)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
        addColumnIfMissing("sessions_utilisateurs", "derniere_activite", "TIMESTAMP NULL DEFAULT NULL");
        schemaEnsured = true;
    }

    private void addColumnIfMissing(String table, String column, String definition) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*) FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?
                    """,
                    Integer.class, table, column);
            if (count == null || count == 0) {
                jdbcTemplate.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
            }
        } catch (Exception ignored) {
            // Schéma déjà à jour ou droits insuffisants — ignoré
        }
    }

    private void ensureSchemaOnce() {
        if (!schemaEnsured) {
            synchronized (this) {
                if (!schemaEnsured) {
                    ensureSchema();
                }
            }
        }
    }

    @Override
    public boolean hasActiveSession(Integer idUtilisateur) {
        ensureSchemaOnce();
        purgeExpired();
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(1) FROM sessions_utilisateurs
                WHERE id_utilisateur = ? AND date_expiration > CURRENT_TIMESTAMP
                """,
                Integer.class,
                idUtilisateur);
        return count != null && count > 0;
    }

    @Override
    public boolean hasRecentActiveSession(Integer idUtilisateur, int inactivityThresholdSeconds) {
        ensureSchemaOnce();
        purgeExpired();
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(1) FROM sessions_utilisateurs
                WHERE id_utilisateur = ?
                  AND date_expiration > CURRENT_TIMESTAMP
                  AND COALESCE(derniere_activite, date_connexion)
                      > DATE_SUB(CURRENT_TIMESTAMP, INTERVAL ? SECOND)
                """,
                Integer.class,
                idUtilisateur,
                inactivityThresholdSeconds);
        return count != null && count > 0;
    }

    @Override
    public void createSession(Integer idUtilisateur, String jti, String adresseIp, LocalDateTime expiresAt) {
        ensureSchemaOnce();
        invalidateAllForUser(idUtilisateur);
        jdbcTemplate.update(
                """
                INSERT INTO sessions_utilisateurs (id_utilisateur, token_session, date_expiration, adresse_ip)
                VALUES (?, ?, ?, ?)
                """,
                idUtilisateur,
                jti,
                Timestamp.valueOf(expiresAt),
                adresseIp);
    }

    @Override
    public boolean isSessionActive(String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }
        ensureSchemaOnce();
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(1) FROM sessions_utilisateurs
                WHERE token_session = ? AND date_expiration > CURRENT_TIMESTAMP
                """,
                Integer.class,
                jti);
        return count != null && count > 0;
    }

    @Override
    public void touchSession(String jti) {
        if (jti == null || jti.isBlank()) {
            return;
        }
        ensureSchemaOnce();
        jdbcTemplate.update(
                "UPDATE sessions_utilisateurs SET derniere_activite = CURRENT_TIMESTAMP WHERE token_session = ?",
                jti);
    }

    @Override
    public void invalidateByJti(String jti) {
        if (jti == null || jti.isBlank()) {
            return;
        }
        ensureSchemaOnce();
        jdbcTemplate.update("DELETE FROM sessions_utilisateurs WHERE token_session = ?", jti);
    }

    @Override
    public void invalidateAllForUser(Integer idUtilisateur) {
        ensureSchemaOnce();
        jdbcTemplate.update("DELETE FROM sessions_utilisateurs WHERE id_utilisateur = ?", idUtilisateur);
    }

    @Override
    public void purgeExpired() {
        ensureSchemaOnce();
        jdbcTemplate.update("DELETE FROM sessions_utilisateurs WHERE date_expiration <= CURRENT_TIMESTAMP");
    }

    @Override
    public void purgeInactiveSessions(int inactivityThresholdSeconds) {
        ensureSchemaOnce();
        jdbcTemplate.update(
                """
                DELETE FROM sessions_utilisateurs
                WHERE date_expiration <= CURRENT_TIMESTAMP
                   OR COALESCE(derniere_activite, date_connexion)
                      <= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL ? SECOND)
                """,
                inactivityThresholdSeconds);
    }

    @Override
    public Optional<String> findActiveJtiForUser(Integer idUtilisateur) {
        ensureSchemaOnce();
        purgeExpired();
        try {
            String jti = jdbcTemplate.queryForObject(
                    """
                    SELECT token_session FROM sessions_utilisateurs
                    WHERE id_utilisateur = ? AND date_expiration > CURRENT_TIMESTAMP
                    ORDER BY date_connexion DESC
                    LIMIT 1
                    """,
                    String.class,
                    idUtilisateur);
            return Optional.ofNullable(jti);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
