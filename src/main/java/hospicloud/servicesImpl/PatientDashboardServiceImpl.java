package hospicloud.servicesImpl;

import hospicloud.dtos.patient.DashboardStatsDTO;
import hospicloud.dtos.patient.PatientDashboardDTO;
import hospicloud.dtos.patient.RecentActivityDTO;
import hospicloud.dtos.patient.UpcomingAppointmentDTO;
import hospicloud.repositories.PatientDashboardRepository;
import hospicloud.services.PatientDashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PatientDashboardServiceImpl implements PatientDashboardService {

    private final PatientDashboardRepository patientDashboardRepository;

    public PatientDashboardServiceImpl(PatientDashboardRepository patientDashboardRepository) {
        this.patientDashboardRepository = patientDashboardRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PatientDashboardDTO getDashboardData(Integer idPatient) {
        if (idPatient == null) {
            throw new IllegalArgumentException("L'ID du patient ne peut pas être nul.");
        }

        DashboardStatsDTO stats = patientDashboardRepository.getPatientDashboardStats(idPatient);
        List<UpcomingAppointmentDTO> upcomingAppointments = patientDashboardRepository.getUpcomingAppointments(idPatient);
        List<RecentActivityDTO> recentActivities = patientDashboardRepository.getRecentActivities(idPatient);

        return new PatientDashboardDTO(stats, upcomingAppointments, recentActivities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UpcomingAppointmentDTO> getTeleconsultations(Integer idPatient) {
        if (idPatient == null) {
            throw new IllegalArgumentException("L'ID du patient ne peut pas être nul.");
        }
        return patientDashboardRepository.getTeleconsultations(idPatient);
    }
}
