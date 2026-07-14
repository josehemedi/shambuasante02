package hospicloud.services;

import hospicloud.dtos.ChangeSubscriptionPlanRequest;
import hospicloud.dtos.HospitalPlanCatalogDTO;
import hospicloud.dtos.TenantSubscriptionDTO;
import hospicloud.dtos.TenantSubscriptionHistoryDTO;

import java.util.List;

public interface TenantSubscriptionService {
    TenantSubscriptionDTO getCurrentSubscription();

    List<HospitalPlanCatalogDTO> listAvailablePlans();

    List<TenantSubscriptionHistoryDTO> getHistory(int limit);

    TenantSubscriptionDTO repaySubscription();

    TenantSubscriptionDTO changePlan(ChangeSubscriptionPlanRequest request);
}
