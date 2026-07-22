package hospicloud.controlleurs;

import hospicloud.dtos.patient.PatientRegistrationRequestDTO;
import hospicloud.dtos.patient.PatientRegistrationResponseDTO;
import hospicloud.dtos.patient.PublicHospitalDTO;
import hospicloud.services.PatientPortalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/public", produces = MediaType.APPLICATION_JSON_VALUE)
public class PublicPatientController {

    private final PatientPortalService patientPortalService;

    public PublicPatientController(PatientPortalService patientPortalService) {
        this.patientPortalService = patientPortalService;
    }

    /** Liste / recherche d'établissements actifs pour l'inscription patient. */
    @GetMapping("/hospitals")
    public ResponseEntity<List<PublicHospitalDTO>> searchHospitals(
            @RequestParam(value = "q", required = false) String q) {
        return ResponseEntity.ok(patientPortalService.searchHospitals(q));
    }

    /** Inscription patient : choix d'hôpital puis création du compte lié. */
    @PostMapping(path = "/patients/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientRegistrationResponseDTO> register(
            @Valid @RequestBody PatientRegistrationRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientPortalService.register(request));
    }
}
