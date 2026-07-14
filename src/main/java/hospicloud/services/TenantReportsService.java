package hospicloud.services;

import hospicloud.dtos.TenantReportsOverviewDTO;

import java.time.LocalDate;

public interface TenantReportsService {

    TenantReportsOverviewDTO getOverview(LocalDate from, LocalDate to);
}
