package hospicloud.servicesImpl;

import hospicloud.dtos.HospitalPlanCatalogDTO;
import hospicloud.dtos.TenantSubscriptionDTO;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.repositories.AbonnementRepository;
import hospicloud.saas.SaasPlanDefinition;
import hospicloud.saas.SaasPlanFeature;
import hospicloud.saas.SaasPlanRegistry;
import hospicloud.services.SaasPlanService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SaasPlanServiceImpl implements SaasPlanService {

    private final AbonnementRepository abonnementRepository;
    private final JdbcTemplate jdbcTemplate;

    public SaasPlanServiceImpl(AbonnementRepository abonnementRepository, JdbcTemplate jdbcTemplate) {
        this.abonnementRepository = abonnementRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String normalizePlanName(String planName) {
        return SaasPlanRegistry.normalize(planName);
    }

    @Override
    public SaasPlanDefinition getDefinition(String planName) {
        return SaasPlanRegistry.require(planName);
    }

    @Override
    public SaasPlanDefinition getPlanForHospital(Integer hopitalId) {
        String planName = resolvePlanName(hopitalId);
        return SaasPlanRegistry.require(planName);
    }

    @Override
    public boolean hasFeature(Integer hopitalId, SaasPlanFeature feature) {
        return getPlanForHospital(hopitalId).hasFeature(feature);
    }

    @Override
    public void assertFeature(Integer hopitalId, SaasPlanFeature feature) {
        if (!hasFeature(hopitalId, feature)) {
            throw new ForbiddenException(
                    "Cette fonctionnalité n'est pas incluse dans votre forfait "
                            + getPlanForHospital(hopitalId).canonicalName()
                            + ". Passez à un forfait supérieur depuis Mon abonnement.");
        }
    }

    @Override
    public int countStaffUsers(Integer hopitalId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM utilisateurs
                        WHERE id_hopital = ?
                          AND est_actif = 1
                          AND UPPER(role) <> 'PATIENT'
                        """,
                Integer.class,
                hopitalId);
        return count != null ? count : 0;
    }

    @Override
    public void assertCanAddStaffUser(Integer hopitalId) {
        SaasPlanDefinition plan = getPlanForHospital(hopitalId);
        if (plan.isUnlimitedStaff()) {
            return;
        }
        int current = countStaffUsers(hopitalId);
        if (current >= plan.maxStaffUsers()) {
            throw new ForbiddenException(
                    "Limite du forfait " + plan.canonicalName() + " atteinte ("
                            + plan.maxStaffUsers() + " employés maximum). Passez au forfait Professionnel ou Entreprise.");
        }
    }

    @Override
    public int countTeleconsultationsThisMonth(Integer hopitalId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM rendez_vous r
                        INNER JOIN medecins m ON m.id_medecin = r.id_medecin
                        WHERE m.id_hopital = ?
                          AND UPPER(r.canal) = 'TELECONSULTATION'
                          AND YEAR(r.date_heure) = YEAR(CURRENT_DATE)
                          AND MONTH(r.date_heure) = MONTH(CURRENT_DATE)
                        """,
                Integer.class,
                hopitalId);
        return count != null ? count : 0;
    }

    @Override
    public void assertTeleconsultationQuota(Integer hopitalId) {
        SaasPlanDefinition plan = getPlanForHospital(hopitalId);
        if (!plan.hasFeature(SaasPlanFeature.TELECONSULTATION)) {
            assertFeature(hopitalId, SaasPlanFeature.TELECONSULTATION);
            return;
        }
        if (plan.isUnlimitedTeleconsultation()) {
            return;
        }
        int used = countTeleconsultationsThisMonth(hopitalId);
        if (used >= plan.teleconsultationMonthlyLimit()) {
            throw new ForbiddenException(
                    "Quota téléconsultation mensuel atteint ("
                            + plan.teleconsultationMonthlyLimit()
                            + " sessions). Passez au forfait Entreprise pour un accès illimité.");
        }
    }

    @Override
    public TenantSubscriptionDTO enrichSubscription(TenantSubscriptionDTO dto) {
        if (dto == null || dto.getIdHopital() == null) {
            return dto;
        }
        String normalizedPlan = normalizePlanName(dto.getPlanNom());
        SaasPlanDefinition definition = SaasPlanRegistry.require(normalizedPlan);
        dto.setPlanNom(normalizedPlan);
        dto.setMaxUsers(definition.maxStaffUsers());
        dto.setCurrentUserCount(countStaffUsers(dto.getIdHopital()));
        dto.setFeatures(SaasPlanRegistry.featureKeys(definition));
        dto.setTeleconsultationMonthlyLimit(definition.teleconsultationMonthlyLimit());
        dto.setTeleconsultationUsedThisMonth(countTeleconsultationsThisMonth(dto.getIdHopital()));
        dto.setTargetAudienceFr(definition.targetAudienceFr());
        dto.setTargetAudienceEn(definition.targetAudienceEn());
        return dto;
    }

    @Override
    public List<HospitalPlanCatalogDTO> buildCatalogPlans(long subscribersBasic,
                                                          long subscribersPro,
                                                          long subscribersEnterprise) {
        return SaasPlanRegistry.catalog().stream()
                .map(definition -> {
                    long subscribers = switch (definition.canonicalName()) {
                        case SaasPlanRegistry.ENTREPRISE -> subscribersEnterprise;
                        case SaasPlanRegistry.PROFESSIONNEL -> subscribersPro;
                        default -> subscribersBasic;
                    };
                    HospitalPlanCatalogDTO dto = new HospitalPlanCatalogDTO();
                    dto.setName(definition.canonicalName());
                    dto.setPrice(definition.monthlyPrice());
                    dto.setSubscribers(subscribers);
                    dto.setPopular(definition.popular());
                    dto.setMaxUsers(definition.maxStaffUsers());
                    dto.setTeleconsultationMonthlyLimit(definition.teleconsultationMonthlyLimit());
                    dto.setFeatureKeys(SaasPlanRegistry.featureKeys(definition));
                    dto.setTargetAudienceFr(definition.targetAudienceFr());
                    dto.setTargetAudienceEn(definition.targetAudienceEn());
                    dto.setFeatures(definition.marketingFeaturesFr());
                    dto.setFeaturesEn(definition.marketingFeaturesEn());
                    return dto;
                })
                .toList();
    }

    private String resolvePlanName(Integer hopitalId) {
        Optional<TenantSubscriptionDTO> subscription = abonnementRepository.findActiveSubscription(hopitalId);
        if (subscription.isPresent() && subscription.get().getPlanNom() != null) {
            return normalizePlanName(subscription.get().getPlanNom());
        }
        return SaasPlanRegistry.BASIC;
    }
}
