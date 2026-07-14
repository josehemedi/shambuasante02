package hospicloud.services;

import hospicloud.dtos.patient.PatientDashboardDTO;
import hospicloud.dtos.patient.UpcomingAppointmentDTO;

import java.util.List;

public interface PatientDashboardService {
    PatientDashboardDTO getDashboardData(Integer idPatient);

    List<UpcomingAppointmentDTO> getTeleconsultations(Integer idPatient);
}
