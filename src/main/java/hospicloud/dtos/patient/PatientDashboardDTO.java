package hospicloud.dtos.patient;

import java.util.List;

public class PatientDashboardDTO {
    private DashboardStatsDTO stats;
    private List<UpcomingAppointmentDTO> upcomingAppointments;
    private List<RecentActivityDTO> recentActivities;

    public PatientDashboardDTO() {}

    public PatientDashboardDTO(DashboardStatsDTO stats, List<UpcomingAppointmentDTO> upcomingAppointments, List<RecentActivityDTO> recentActivities) {
        this.stats = stats;
        this.upcomingAppointments = upcomingAppointments;
        this.recentActivities = recentActivities;
    }

    public DashboardStatsDTO getStats() { return stats; }
    public void setStats(DashboardStatsDTO stats) { this.stats = stats; }

    public List<UpcomingAppointmentDTO> getUpcomingAppointments() { return upcomingAppointments; }
    public void setUpcomingAppointments(List<UpcomingAppointmentDTO> upcomingAppointments) { this.upcomingAppointments = upcomingAppointments; }

    public List<RecentActivityDTO> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<RecentActivityDTO> recentActivities) { this.recentActivities = recentActivities; }
}
