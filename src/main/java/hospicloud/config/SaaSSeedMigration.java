package hospicloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import hospicloud.saas.SaasPlanRegistry;

/**
 * Initialise les abonnements SaaS à partir des hôpitaux existants si la table est vide.
 */
@Component
public class SaaSSeedMigration {

    private static final Logger log = LoggerFactory.getLogger(SaaSSeedMigration.class);
    private static final String[] PLANS = {
            SaasPlanRegistry.BASIC,
            SaasPlanRegistry.PROFESSIONNEL,
            SaasPlanRegistry.ENTREPRISE
    };
    private static final BigDecimal[] PRICES = {
            SaasPlanRegistry.require(SaasPlanRegistry.BASIC).monthlyPrice(),
            SaasPlanRegistry.require(SaasPlanRegistry.PROFESSIONNEL).monthlyPrice(),
            SaasPlanRegistry.require(SaasPlanRegistry.ENTREPRISE).monthlyPrice()
    };

    private final JdbcTemplate jdbcTemplate;

    public SaaSSeedMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void seed() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM abonnements", Integer.class);
            if (count != null && count > 0) {
                return;
            }
            List<Map<String, Object>> hopitaux = jdbcTemplate.queryForList(
                    "SELECT id_hopital, est_actif, date_creation FROM hopitaux");
            if (hopitaux.isEmpty()) {
                return;
            }
            for (Map<String, Object> row : hopitaux) {
                int id = ((Number) row.get("id_hopital")).intValue();
                boolean actif = row.get("est_actif") == null || Boolean.TRUE.equals(row.get("est_actif"))
                        || (row.get("est_actif") instanceof Number n && n.intValue() == 1);
                int planIdx = id % PLANS.length;
                String statut = actif ? "actif" : "suspendu";
                jdbcTemplate.update(
                        "INSERT INTO abonnements (id_hopital, plan_nom, montant_mensuel, statut, date_debut) VALUES (?, ?, ?, ?, ?)",
                        id, PLANS[planIdx], PRICES[planIdx], statut, row.get("date_creation"));
            }
            log.info("Abonnements SaaS initialisés pour {} hôpital(aux)", hopitaux.size());
        } catch (Exception e) {
            log.warn("Seed abonnements ignoré: {}", e.getMessage());
        }
    }
}
