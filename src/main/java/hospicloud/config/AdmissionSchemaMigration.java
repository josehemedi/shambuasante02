package hospicloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class AdmissionSchemaMigration {

    private static final Logger log = LoggerFactory.getLogger(AdmissionSchemaMigration.class);
    private final JdbcTemplate jdbcTemplate;

    public AdmissionSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ensureAdmissionTable() {
        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS admission (
                    id_admission INT AUTO_INCREMENT PRIMARY KEY,
                    id_hopital INT NOT NULL,
                    id_patient INT NOT NULL,
                    id_medecin INT NULL,
                    id_rendez_vous INT NULL,
                    niveau_priorite INT NOT NULL DEFAULT 3,
                    temps_arrivee DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    statut VARCHAR(30) NOT NULL DEFAULT 'EN_ATTENTE',
                    INDEX idx_admission_hopital_statut (id_hopital, statut),
                    INDEX idx_admission_hopital_arrivee (id_hopital, temps_arrivee)
                )
                """);
            ensureColumn("numero_passage", "INT NULL");
            ensureColumn("salle", "VARCHAR(80) NULL");
            ensureColumn("appele_a", "DATETIME NULL");
            ensureColumn("cree_par", "INT NULL");
            ensureColumn("check_in_par", "INT NULL");
            log.info("Table admission prête");
        } catch (Exception e) {
            log.warn("Migration admission ignorée: {}", e.getMessage());
        }
    }

    private void ensureColumn(String column, String definition) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*) FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'admission'
                      AND COLUMN_NAME = ?
                    """,
                    Integer.class,
                    column
            );
            if (count != null && count == 0) {
                jdbcTemplate.execute("ALTER TABLE admission ADD COLUMN " + column + " " + definition);
                log.info("Colonne admission.{} ajoutée", column);
            }
        } catch (Exception e) {
            log.warn("Colonne admission.{}: {}", column, e.getMessage());
        }
    }
}
