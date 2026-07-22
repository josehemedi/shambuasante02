package hospicloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Étend patients_documents pour le partage médecin → patient.
 */
@Component
public class PatientDocumentsSchemaMigration {

    private static final Logger log = LoggerFactory.getLogger(PatientDocumentsSchemaMigration.class);

    private final JdbcTemplate jdbcTemplate;

    public PatientDocumentsSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void migrate() {
        try {
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS patients_documents (
                      id_document INT NOT NULL AUTO_INCREMENT,
                      id_hopital INT NULL,
                      id_patient INT NOT NULL,
                      nom_fichier VARCHAR(255) NOT NULL,
                      type_document VARCHAR(50) NULL,
                      url_fichier VARCHAR(255) NULL,
                      contenu_resume TEXT NULL,
                      partage_patient TINYINT(1) NOT NULL DEFAULT 0,
                      envoye_par INT NULL,
                      reference_type VARCHAR(40) NULL,
                      reference_id BIGINT NULL,
                      date_upload TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
                      date_envoi TIMESTAMP NULL,
                      PRIMARY KEY (id_document),
                      KEY idx_pd_patient (id_patient),
                      KEY idx_pd_hopital (id_hopital)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """);
            ensureColumn("id_hopital", "INT NULL");
            ensureColumn("contenu_resume", "TEXT NULL");
            ensureColumn("partage_patient", "TINYINT(1) NOT NULL DEFAULT 0");
            ensureColumn("envoye_par", "INT NULL");
            ensureColumn("reference_type", "VARCHAR(40) NULL");
            ensureColumn("reference_id", "BIGINT NULL");
            ensureColumn("date_envoi", "TIMESTAMP NULL");
            // url_fichier peut être NULL pour partages textuels (labo)
            try {
                jdbcTemplate.execute("ALTER TABLE patients_documents MODIFY COLUMN url_fichier VARCHAR(255) NULL");
            } catch (Exception ignored) {
            }
            log.info("Table patients_documents prête (partage patient)");
        } catch (Exception e) {
            log.warn("Migration patients_documents ignorée: {}", e.getMessage());
        }
    }

    private void ensureColumn(String column, String definition) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*) FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'patients_documents'
                      AND COLUMN_NAME = ?
                    """,
                    Integer.class,
                    column
            );
            if (count == null || count == 0) {
                jdbcTemplate.execute("ALTER TABLE patients_documents ADD COLUMN " + column + " " + definition);
            }
        } catch (Exception ex) {
            log.debug("Colonne {}.{} : {}", "patients_documents", column, ex.getMessage());
        }
    }
}
