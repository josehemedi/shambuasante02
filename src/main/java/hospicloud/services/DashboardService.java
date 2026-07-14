package hospicloud.services;

import hospicloud.dtos.DashboardDTO;
import hospicloud.dtos.DashboardStatsDTO;
import hospicloud.dtos.DoctorWorkspaceDTO;
import hospicloud.dtos.MrrSeriesPointDTO;
import hospicloud.dtos.PlanDistributionItemDTO;
import hospicloud.dtos.TenantOverviewDTO;

import java.util.List;

public interface DashboardService {
    DashboardDTO getDashboardData();
    DashboardStatsDTO getDashboardStats();
    DoctorWorkspaceDTO getDoctorWorkspaceData();
    List<MrrSeriesPointDTO> getMrrSeries(int months);
    List<PlanDistributionItemDTO> getPlanDistribution();
    List<TenantOverviewDTO> getTenantsOverview();
}