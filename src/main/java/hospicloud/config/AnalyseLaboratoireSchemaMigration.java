package hospicloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class AnalyseLaboratoireSchemaMigration {

    private static final Logger log = LoggerFactory.getLogger(AnalyseLaboratoireSchemaMigration.class);
    private final JdbcTemplate jdbcTemplate;

    public AnalyseLaboratoireSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ensureColumns() {
        try {
            ensureColumn("resultat_texte", "TEXT NULL");
            ensureColumn("interpretation", "VARCHAR(80) NULL");
            ensureColumn("valeurs_reference", "VARCHAR(120) NULL");
            ensureColumn("id_consultation", "INT NULL");
            fixConsultationForeignKey();
            log.info("Schéma analyses_laboratoire prêt");
        } catch (Exception e) {
            log.warn("Migration analyses_laboratoire ignorée: {}", e.getMessage());
        }
    }

    /**
     * L'app utilise consultations_medicales ; l'ancienne FK pointait vers consultations.
     */
    private void fixConsultationForeignKey() {
        try {
            Integer wrongFk = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*) FROM information_schema.KEY_COLUMN_USAGE
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'analyses_laboratoire'
                      AND COLUMN_NAME = 'id_consultation'
                      AND REFERENCED_TABLE_NAME = 'consultations'
                    """,
                    Integer.class
            );
            if (wrongFk != null && wrongFk > 0) {
                jdbcTemplate.execute(
                        "ALTER TABLE analyses_laboratoire DROP FOREIGN KEY analyses_laboratoire_ibfk_5");
                log.info("FK analyses_laboratoire_ibfk_5 (consultations) supprimée");
            }
        } catch (Exception e) {
            log.warn("Drop FK consultation: {}", e.getMessage());
            // Essayer d'autres noms de contrainte
            tryDropConstraint("fk_analyses_consultation");
            tryDropConstraint("analyses_laboratoire_ibfk_5");
        }

        try {
            Integer correctFk = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*) FROM information_schema.KEY_COLUMN_USAGE
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'analyses_laboratoire'
                      AND COLUMN_NAME = 'id_consultation'
                      AND REFERENCED_TABLE_NAME = 'consultations_medicales'
                    """,
                    Integer.class
            );
            if (correctFk == null || correctFk == 0) {
                // Alignement type (consultations_medicales.id_consultation = BIGINT)
                try {
                    jdbcTemplate.execute(
                            "ALTER TABLE analyses_laboratoire MODIFY COLUMN id_consultation BIGINT NULL");
                    log.info("Colonne analyses_laboratoire.id_consultation passée en BIGINT");
                } catch (Exception typeEx) {
                    log.warn("MODIFY id_consultation: {}", typeEx.getMessage());
                }
                // Nettoyer les IDs orphelins avant d'ajouter la FK
                jdbcTemplate.update(
                        """
                        UPDATE analyses_laboratoire a
                        LEFT JOIN consultations_medicales c ON a.id_consultation = c.id_consultation
                        SET a.id_consultation = NULL
                        WHERE a.id_consultation IS NOT NULL AND c.id_consultation IS NULL
                        """
                );
                jdbcTemplate.execute(
                        """
                        ALTER TABLE analyses_laboratoire
                        ADD CONSTRAINT fk_analyses_consultations_medicales
                        FOREIGN KEY (id_consultation)
                        REFERENCES consultations_medicales(id_consultation)
                        ON DELETE SET NULL
                        """
                );
                log.info("FK analyses_laboratoire → consultations_medicales ajoutée");
            }
        } catch (Exception e) {
            log.warn("FK consultations_medicales non ajoutée (lien logique conservé): {}", e.getMessage());
        }
    }

    private void tryDropConstraint(String name) {
        try {
            jdbcTemplate.execute("ALTER TABLE analyses_laboratoire DROP FOREIGN KEY " + name);
            log.info("FK {} supprimée", name);
        } catch (Exception ignored) {
            // ignore
        }
    }

    private void ensureColumn(String column, String definition) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*) FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'analyses_laboratoire'
                      AND COLUMN_NAME = ?
                    """,
                    Integer.class,
                    column
            );
            if (count != null && count == 0) {
                jdbcTemplate.execute("ALTER TABLE analyses_laboratoire ADD COLUMN " + column + " " + definition);
                log.info("Colonne analyses_laboratoire.{} ajoutée", column);
            }
        } catch (Exception e) {
            log.warn("Colonne analyses_laboratoire.{}: {}", column, e.getMessage());
        }
    }
}
