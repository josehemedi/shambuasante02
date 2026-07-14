package hospicloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class AsyncJobsSchemaMigration {

    private static final Logger log = LoggerFactory.getLogger(AsyncJobsSchemaMigration.class);
    private final JdbcTemplate jdbcTemplate;

    public AsyncJobsSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void migrate() {
        try {
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS async_jobs (
                        job_id VARCHAR(64) PRIMARY KEY,
                        job_type VARCHAR(64) NOT NULL,
                        status VARCHAR(32) NOT NULL,
                        id_hopital INT NULL,
                        actor_user_id INT NULL,
                        entity_id BIGINT NULL,
                        payload_json MEDIUMTEXT NULL,
                        result_json MEDIUMTEXT NULL,
                        result_path VARCHAR(512) NULL,
                        error_message TEXT NULL,
                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        completed_at DATETIME NULL,
                        KEY idx_async_jobs_status (status),
                        KEY idx_async_jobs_hopital (id_hopital, created_at)
                    )
                    """);
            ensureStorageDir();
            log.info("Table async_jobs prête");
        } catch (Exception e) {
            log.warn("Migration async_jobs ignorée: {}", e.getMessage());
        }
    }

    private void ensureStorageDir() {
        try {
            Path dir = Path.of(System.getProperty("app.async.storage-dir", "/var/hospicloud/async"));
            Files.createDirectories(dir.resolve("reports"));
            Files.createDirectories(dir.resolve("enregistrements"));
        } catch (Exception e) {
            try {
                Path fallback = Path.of(System.getProperty("java.io.tmpdir"), "hospicloud-async");
                Files.createDirectories(fallback.resolve("reports"));
                System.setProperty("app.async.storage-dir", fallback.toString());
                log.info("Stockage async fallback: {}", fallback);
            } catch (Exception ignored) {
            }
        }
    }
}
