package hospicloud.repositories;

import hospicloud.dtos.patient.DashboardStatsDTO;
import hospicloud.dtos.patient.UpcomingAppointmentDTO;
import hospicloud.dtos.patient.RecentActivityDTO;
import java.util.List;

public interface PatientDashboardRepository {
    
    DashboardStatsDTO getPatientDashboardStats(Integer idPatient);
    
    List<UpcomingAppointmentDTO> getUpcomingAppointments(Integer idPatient);
    
    List<RecentActivityDTO> getRecentActivities(Integer idPatient);

    List<UpcomingAppointmentDTO> getTeleconsultations(Integer idPatient);
}
