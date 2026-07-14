package hospicloud.services;

import hospicloud.dtos.TenantBillingOverviewDTO;

public interface TenantBillingService {
    TenantBillingOverviewDTO getOverview();
}
