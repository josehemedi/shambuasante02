package hospicloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class AuditSupportSchemaMigration {

    private static final Logger log = LoggerFactory.getLogger(AuditSupportSchemaMigration.class);
    private final JdbcTemplate jdbcTemplate;

    public AuditSupportSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void migrate() {
        createSupportTicketsIfMissing();
    }

    private void createSupportTicketsIfMissing() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.TABLES "
                            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'support_tickets'",
                    Integer.class);
            if (count != null && count > 0) {
                return;
            }
            jdbcTemplate.execute("""
                    CREATE TABLE support_tickets (
                      id BIGINT NOT NULL AUTO_INCREMENT,
                      hopital_id INT NOT NULL,
                      created_by_user_id INT DEFAULT NULL,
                      created_by_email VARCHAR(150) DEFAULT NULL,
                      created_by_role VARCHAR(50) DEFAULT NULL,
                      subject VARCHAR(255) NOT NULL,
                      description TEXT NOT NULL,
                      module VARCHAR(100) DEFAULT NULL,
                      priority ENUM('LOW','MEDIUM','HIGH','CRITICAL') NOT NULL DEFAULT 'MEDIUM',
                      status ENUM('OPEN','IN_PROGRESS','RESOLVED','CLOSED') NOT NULL DEFAULT 'OPEN',
                      request_id VARCHAR(100) DEFAULT NULL,
                      assigned_to VARCHAR(150) DEFAULT NULL,
                      resolution_notes TEXT,
                      created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
                      PRIMARY KEY (id),
                      KEY idx_support_tickets_hopital_id (hopital_id),
                      KEY idx_support_tickets_status (status),
                      KEY idx_support_tickets_module (module),
                      KEY idx_support_tickets_request_id (request_id),
                      KEY idx_support_tickets_created_at (created_at),
                      CONSTRAINT support_tickets_ibfk_1 FOREIGN KEY (hopital_id) REFERENCES hopitaux (id_hopital)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
                    """);
            log.info("Table support_tickets créée");
        } catch (Exception e) {
            log.warn("Migration support_tickets ignorée: {}", e.getMessage());
        }
    }
}
