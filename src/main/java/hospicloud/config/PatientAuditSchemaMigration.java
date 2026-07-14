package hospicloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Colonnes de traçabilité utilisateur (multi-tenant SaaS).
 */
@Component
public class PatientAuditSchemaMigration {

    private static final Logger log = LoggerFactory.getLogger(PatientAuditSchemaMigration.class);
    private final JdbcTemplate jdbcTemplate;

    public PatientAuditSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void migrate() {
        addColumnIfMissing("patients", "cree_par", "INT NULL");
        addColumnIfMissing("patients", "modifie_par", "INT NULL");
        addColumnIfMissing("admission", "cree_par", "INT NULL");
        addColumnIfMissing("admission", "check_in_par", "INT NULL");
        addColumnIfMissing("bons_sortie", "delivre_par", "INT NULL");
        addIndexIfMissing("patients", "idx_patients_hopital_cree_par", "id_hopital, cree_par");
        addIndexIfMissing("rendez_vous01", "idx_rdv_hopital_cree_par", "id_hopital, cree_par");
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

    private void addIndexIfMissing(String table, String indexName, String columns) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.STATISTICS "
                            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND INDEX_NAME = ?",
                    Integer.class, table, indexName);
            if (count == null || count == 0) {
                jdbcTemplate.execute("CREATE INDEX " + indexName + " ON " + table + " (" + columns + ")");
                log.info("Index {} sur {} créé", indexName, table);
            }
        } catch (Exception e) {
            log.warn("Index {} ignoré: {}", indexName, e.getMessage());
        }
    }
}
