package hospicloud.controlleurs;

import hospicloud.dtos.patient.PatientDashboardDTO;
import hospicloud.dtos.patient.UpcomingAppointmentDTO;
import hospicloud.security.CurrentUserContext;
import hospicloud.security.TenantContext;
import hospicloud.services.PatientDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients/me/dashboard")
public class PatientDashboardController {

    private final PatientDashboardService patientDashboardService;

    public PatientDashboardController(PatientDashboardService patientDashboardService) {
        this.patientDashboardService = patientDashboardService;
    }

    @GetMapping
    public ResponseEntity<PatientDashboardDTO> getDashboard() {
        //Validation basique de sécurité (tenant et patient)
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        Integer idPatient = CurrentUserContext.getPatientId();

        if (idPatient == null) {
            return ResponseEntity.status(401).build(); // Unauthorized if no patient context
        }

        PatientDashboardDTO dashboardData = patientDashboardService.getDashboardData(idPatient);
        return ResponseEntity.ok(dashboardData);
    }

    @GetMapping("/teleconsultations")
    public ResponseEntity<List<UpcomingAppointmentDTO>> getTeleconsultations() {
        Integer idPatient = CurrentUserContext.getPatientId();
        if (idPatient == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(patientDashboardService.getTeleconsultations(idPatient));
    }
}
