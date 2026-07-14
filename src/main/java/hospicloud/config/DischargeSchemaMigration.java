package hospicloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DischargeSchemaMigration {

    private static final Logger log = LoggerFactory.getLogger(DischargeSchemaMigration.class);
    private final JdbcTemplate jdbcTemplate;

    public DischargeSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void migrate() {
        addColumnIfMissing("patients", "statut_clinique",
                "VARCHAR(30) NOT NULL DEFAULT 'AMBULATOIRE'");
        addColumnIfMissing("bons_sortie", "statut_workflow",
                "VARCHAR(40) NOT NULL DEFAULT 'AUTORISE_MEDICALEMENT'");
        addColumnIfMissing("bons_sortie", "id_ordonnance", "BIGINT NULL");
        addColumnIfMissing("bons_sortie", "id_admission", "INT NULL");
    }

    private void addColumnIfMissing(String table, String column, String definition) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS "
                            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                    Integer.class, table, column);
            if (count == null || count == 0) {
                jdbcTemplate.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
                log.info("Colonne {}.{} ajoutée", table, column);
            }
        } catch (Exception e) {
            log.warn("Migration {}.{} ignorée: {}", table, column, e.getMessage());
        }
    }
}
