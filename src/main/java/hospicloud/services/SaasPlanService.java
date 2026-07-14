package hospicloud.services;

import hospicloud.dtos.HospitalPlanCatalogDTO;
import hospicloud.dtos.TenantSubscriptionDTO;
import hospicloud.saas.SaasPlanDefinition;
import hospicloud.saas.SaasPlanFeature;

import java.util.List;

public interface SaasPlanService {

    String normalizePlanName(String planName);

    SaasPlanDefinition getDefinition(String planName);

    SaasPlanDefinition getPlanForHospital(Integer hopitalId);

    boolean hasFeature(Integer hopitalId, SaasPlanFeature feature);

    void assertFeature(Integer hopitalId, SaasPlanFeature feature);

    int countStaffUsers(Integer hopitalId);

    void assertCanAddStaffUser(Integer hopitalId);

    int countTeleconsultationsThisMonth(Integer hopitalId);

    void assertTeleconsultationQuota(Integer hopitalId);

    TenantSubscriptionDTO enrichSubscription(TenantSubscriptionDTO dto);

    List<HospitalPlanCatalogDTO> buildCatalogPlans(long subscribersBasic, long subscribersPro, long subscribersEnterprise);
}
