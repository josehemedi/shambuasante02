package hospicloud.saas;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class SaasPlanRegistry {

    public static final String BASIC = "Basic";
    public static final String PROFESSIONNEL = "Professionnel";
    public static final String ENTREPRISE = "Entreprise";

    private static final Set<SaasPlanFeature> BASIC_FEATURES = Set.of(
            SaasPlanFeature.PATIENTS,
            SaasPlanFeature.APPOINTMENTS,
            SaasPlanFeature.CONSULTATIONS,
            SaasPlanFeature.STAFF_MANAGEMENT
    );

    private static final Set<SaasPlanFeature> PROFESSIONNEL_FEATURES = Set.of(
            SaasPlanFeature.PATIENTS,
            SaasPlanFeature.APPOINTMENTS,
            SaasPlanFeature.CONSULTATIONS,
            SaasPlanFeature.STAFF_MANAGEMENT,
            SaasPlanFeature.LAB,
            SaasPlanFeature.PHARMACY,
            SaasPlanFeature.BILLING,
            SaasPlanFeature.TELECONSULTATION,
            SaasPlanFeature.REPORTS
    );

    private static final Set<SaasPlanFeature> ENTREPRISE_FEATURES = Set.of(
            SaasPlanFeature.PATIENTS,
            SaasPlanFeature.APPOINTMENTS,
            SaasPlanFeature.CONSULTATIONS,
            SaasPlanFeature.STAFF_MANAGEMENT,
            SaasPlanFeature.LAB,
            SaasPlanFeature.PHARMACY,
            SaasPlanFeature.BILLING,
            SaasPlanFeature.TELECONSULTATION,
            SaasPlanFeature.REPORTS,
            SaasPlanFeature.AI_ASSISTANT,
            SaasPlanFeature.PRIORITY_SUPPORT,
            SaasPlanFeature.CUSTOMIZATION,
            SaasPlanFeature.ENHANCED_BACKUPS,
            SaasPlanFeature.INTEGRATIONS
    );

    private static final Map<String, SaasPlanDefinition> PLANS = new LinkedHashMap<>();

    static {
        PLANS.put(BASIC, new SaasPlanDefinition(
                BASIC,
                BigDecimal.valueOf(499),
                10,
                null,
                false,
                "Petite clinique",
                "Small clinic",
                BASIC_FEATURES,
                List.of(
                        "Jusqu'à 10 employés",
                        "Gestion des patients",
                        "Rendez-vous & consultations",
                        "Tableau de bord essentiel",
                        "Support standard"
                ),
                List.of(
                        "Up to 10 staff members",
                        "Patient management",
                        "Appointments & consultations",
                        "Essential dashboard",
                        "Standard support"
                )
        ));

        PLANS.put(PROFESSIONNEL, new SaasPlanDefinition(
                PROFESSIONNEL,
                BigDecimal.valueOf(1499),
                50,
                50,
                true,
                "Hôpital moyen",
                "Medium hospital",
                PROFESSIONNEL_FEATURES,
                List.of(
                        "Jusqu'à 50 utilisateurs",
                        "Laboratoire & pharmacie",
                        "Facturation intégrée",
                        "Téléconsultation (50 sessions/mois)",
                        "Rapports statistiques",
                        "Support e-mail"
                ),
                List.of(
                        "Up to 50 users",
                        "Laboratory & pharmacy",
                        "Integrated billing",
                        "Teleconsultation (50 sessions/month)",
                        "Statistical reports",
                        "Email support"
                )
        ));

        PLANS.put(ENTREPRISE, new SaasPlanDefinition(
                ENTREPRISE,
                BigDecimal.valueOf(3999),
                null,
                null,
                false,
                "Grand hôpital & entreprises",
                "Large hospital & enterprises",
                ENTREPRISE_FEATURES,
                List.of(
                        "Utilisateurs illimités",
                        "Tous les modules Shambua Santé",
                        "Téléconsultation illimitée",
                        "Assistant IA clinique",
                        "Support prioritaire 24/7",
                        "Personnalisation & intégrations",
                        "Sauvegardes renforcées"
                ),
                List.of(
                        "Unlimited users",
                        "All Shambua Santé modules",
                        "Unlimited teleconsultation",
                        "Clinical AI assistant",
                        "24/7 priority support",
                        "Customization & integrations",
                        "Enhanced backups"
                )
        ));
    }

    private SaasPlanRegistry() {
    }

    public static SaasPlanDefinition require(String planName) {
        SaasPlanDefinition definition = PLANS.get(normalize(planName));
        if (definition == null) {
            return PLANS.get(BASIC);
        }
        return definition;
    }

    public static SaasPlanDefinition get(String planName) {
        return PLANS.get(normalize(planName));
    }

    public static List<SaasPlanDefinition> catalog() {
        return List.of(PLANS.get(BASIC), PLANS.get(PROFESSIONNEL), PLANS.get(ENTREPRISE));
    }

    public static Set<String> allowedPlanNames() {
        return PLANS.keySet();
    }

    public static String normalize(String planName) {
        if (planName == null || planName.isBlank()) {
            return BASIC;
        }
        String trimmed = planName.trim();
        return switch (trimmed) {
            case "Starter", "starter", "Démarrage", "Demarrage" -> BASIC;
            case "Growth", "growth", "Croissance", "Pro", "Professional" -> PROFESSIONNEL;
            case "Enterprise", "enterprise", "Entreprise" -> ENTREPRISE;
            default -> PLANS.containsKey(trimmed) ? trimmed : BASIC;
        };
    }

    public static int planRank(String planName) {
        return switch (normalize(planName)) {
            case ENTREPRISE -> 3;
            case PROFESSIONNEL -> 2;
            default -> 1;
        };
    }

    public static List<String> marketingFeatures(String planName, Locale locale) {
        SaasPlanDefinition definition = require(planName);
        return locale != null && "fr".equalsIgnoreCase(locale.getLanguage())
                ? definition.marketingFeaturesFr()
                : definition.marketingFeaturesEn();
    }

    public static List<String> featureKeys(SaasPlanDefinition definition) {
        return definition.features().stream()
                .map(Enum::name)
                .sorted()
                .toList();
    }
}
