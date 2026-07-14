package hospicloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class RendezVousSchemaMigration {

    private static final Logger log = LoggerFactory.getLogger(RendezVousSchemaMigration.class);
    private final JdbcTemplate jdbcTemplate;

    public RendezVousSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ensureUrlVisioColumn() {
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE rendez_vous01 ADD COLUMN url_visio VARCHAR(500) NULL AFTER statut_rdv");
            log.info("Colonne url_visio ajoutée à rendez_vous01");
        } catch (Exception e) {
            log.debug("Colonne url_visio déjà présente ou migration ignorée: {}", e.getMessage());
        }
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE rendez_vous01 ADD COLUMN rappel_30min_envoye_at DATETIME NULL AFTER url_visio");
            log.info("Colonne rappel_30min_envoye_at ajoutée à rendez_vous01");
        } catch (Exception e) {
            log.debug("Colonne rappel_30min_envoye_at déjà présente ou migration ignorée: {}", e.getMessage());
        }
        ensureColumn("salle_physique", "VARCHAR(80) NULL");
    }

    private void ensureColumn(String column, String definition) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    """
                    SELECT COUNT(*) FROM information_schema.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = 'rendez_vous01'
                      AND COLUMN_NAME = ?
                    """,
                    Integer.class,
                    column
            );
            if (count != null && count == 0) {
                jdbcTemplate.execute("ALTER TABLE rendez_vous01 ADD COLUMN " + column + " " + definition);
                log.info("Colonne rendez_vous01.{} ajoutée", column);
            }
        } catch (Exception e) {
            log.warn("Colonne rendez_vous01.{}: {}", column, e.getMessage());
        }
    }
}
