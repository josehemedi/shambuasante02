package hospicloud.services;

import hospicloud.dtos.LaboratoryOverviewDTO;

public interface TenantLaboratoryService {
    LaboratoryOverviewDTO getOverview();
}
