package hospicloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Migre les anciens noms de forfaits (Starter/Growth/Enterprise) vers Basic/Professionnel/Entreprise.
 */
@Component
public class SaasPlanMigration {

    private static final Logger log = LoggerFactory.getLogger(SaasPlanMigration.class);

    private final JdbcTemplate jdbcTemplate;

    public SaasPlanMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void migrateLegacyPlanNames() {
        try {
            int starter = jdbcTemplate.update("UPDATE abonnements SET plan_nom = 'Basic' WHERE plan_nom = 'Starter'");
            int growth = jdbcTemplate.update("UPDATE abonnements SET plan_nom = 'Professionnel' WHERE plan_nom = 'Growth'");
            int enterprise = jdbcTemplate.update(
                    "UPDATE abonnements SET plan_nom = 'Entreprise' WHERE plan_nom = 'Enterprise'");
            if (starter + growth + enterprise > 0) {
                log.info("Migration forfaits SaaS: Basic={}, Professionnel={}, Entreprise={}", starter, growth, enterprise);
            }
        } catch (Exception e) {
            log.warn("Migration forfaits SaaS ignorée: {}", e.getMessage());
        }
    }
}
