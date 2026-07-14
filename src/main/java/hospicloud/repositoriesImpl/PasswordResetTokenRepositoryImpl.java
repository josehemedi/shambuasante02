package hospicloud.repositoriesImpl;

import hospicloud.model.PasswordResetToken;
import hospicloud.repositories.PasswordResetTokenRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class PasswordResetTokenRepositoryImpl implements PasswordResetTokenRepository {

    private final JdbcTemplate jdbcTemplate;
    private volatile boolean schemaEnsured = false;

    public PasswordResetTokenRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void ensureSchema() {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS password_reset_tokens (
                id BIGINT NOT NULL AUTO_INCREMENT,
                id_utilisateur INT NOT NULL,
                id_hopital INT NULL,
                token_hash VARCHAR(128) NOT NULL,
                expires_at TIMESTAMP NOT NULL,
                used_at TIMESTAMP NULL DEFAULT NULL,
                created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                PRIMARY KEY (id),
                UNIQUE KEY uk_password_reset_hash (token_hash),
                KEY idx_password_reset_user (id_utilisateur, created_at)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);
        schemaEnsured = true;
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
    public void invalidateAllForUser(Integer idUtilisateur) {
        ensureSchemaOnce();
        jdbcTemplate.update(
                "UPDATE password_reset_tokens SET used_at = CURRENT_TIMESTAMP WHERE id_utilisateur = ? AND used_at IS NULL",
                idUtilisateur
        );
    }

    @Override
    public void save(PasswordResetToken token) {
        ensureSchemaOnce();
        jdbcTemplate.update(
                """
                INSERT INTO password_reset_tokens
                (id_utilisateur, id_hopital, token_hash, expires_at, used_at, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                token.getIdUtilisateur(),
                token.getIdHopital(),
                token.getTokenHash(),
                Timestamp.valueOf(token.getExpiresAt()),
                token.getUsedAt() != null ? Timestamp.valueOf(token.getUsedAt()) : null,
                token.getCreatedAt() != null ? Timestamp.valueOf(token.getCreatedAt()) : Timestamp.valueOf(LocalDateTime.now())
        );
    }

    @Override
    public Optional<PasswordResetToken> findValidByHash(String tokenHash) {
        ensureSchemaOnce();
        String sql = """
            SELECT id, id_utilisateur, id_hopital, token_hash, expires_at, used_at, created_at
            FROM password_reset_tokens
            WHERE token_hash = ? AND used_at IS NULL AND expires_at > CURRENT_TIMESTAMP
            LIMIT 1
            """;
        return jdbcTemplate.query(sql, rs -> rs.next() ? Optional.of(mapRow(rs)) : Optional.empty(), tokenHash);
    }

    @Override
    public void markUsed(Long id) {
        ensureSchemaOnce();
        jdbcTemplate.update(
                "UPDATE password_reset_tokens SET used_at = CURRENT_TIMESTAMP WHERE id = ?",
                id
        );
    }

    private PasswordResetToken mapRow(ResultSet rs) throws SQLException {
        PasswordResetToken token = new PasswordResetToken();
        token.setId(rs.getLong("id"));
        token.setIdUtilisateur(rs.getInt("id_utilisateur"));
        int hopitalId = rs.getInt("id_hopital");
        if (!rs.wasNull()) {
            token.setIdHopital(hopitalId);
        }
        token.setTokenHash(rs.getString("token_hash"));
        Timestamp expiresAt = rs.getTimestamp("expires_at");
        if (expiresAt != null) {
            token.setExpiresAt(expiresAt.toLocalDateTime());
        }
        Timestamp usedAt = rs.getTimestamp("used_at");
        if (usedAt != null) {
            token.setUsedAt(usedAt.toLocalDateTime());
        }
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            token.setCreatedAt(createdAt.toLocalDateTime());
        }
        return token;
    }
}
