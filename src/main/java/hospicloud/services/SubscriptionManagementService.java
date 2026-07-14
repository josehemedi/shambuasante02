package hospicloud.services;

import hospicloud.dtos.HospitalPlanCatalogDTO;
import hospicloud.dtos.SubscriptionInvoiceDTO;
import hospicloud.dtos.SubscriptionKpisDTO;
import hospicloud.dtos.SubscriptionTimelineEventDTO;

import java.util.List;

public interface SubscriptionManagementService {
    SubscriptionKpisDTO getKpis();

    List<HospitalPlanCatalogDTO> getPlans();

    List<SubscriptionInvoiceDTO> getInvoices(int limit);

    List<SubscriptionTimelineEventDTO> getTimeline(int limit);
}
