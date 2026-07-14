package hospicloud.controlleurs;

import hospicloud.model.Patient;
import hospicloud.services.PatientService;
import hospicloud.security.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/medecins/patients", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class MedecinPatientController {

    private final PatientService patientService;
    private final CurrentUserService currentUserService;

    @Autowired
    public MedecinPatientController(PatientService patientService, CurrentUserService currentUserService) {
        this.patientService = patientService;
        this.currentUserService = currentUserService;
    }

    /**
     * 📋 GET /api/medecins/patients
     */
    @GetMapping
    public ResponseEntity<List<Patient>> getPatientsByMedecin(
            @RequestParam(value = "mine", required = false) Boolean mine) {
        return ResponseEntity.ok(patientService.trouverTousLesPatients(mine));
    }

    /**
     * 🔍 GET /api/medecins/patients/search
     */
    @GetMapping(path = "/search")
    public ResponseEntity<List<Patient>> searchPatientsDuMedecin(
            @RequestParam(value = "nom", required = false) String nom,
            @RequestParam(value = "prenom", required = false) String prenom) {
        
        Integer idMedecin = currentUserService.getCurrentMedecinId();
        List<Patient> list = patientService.rechercherPatientsDuMedecin(idMedecin, nom, prenom);
        return ResponseEntity.ok(list);
    }

    /**
     * 🏥 GET /api/medecins/patients/{idPatient}/dossier
     */
    @GetMapping(path = "/{idPatient}/dossier")
    public ResponseEntity<Patient> getDossierPatientParMedecin(
            @PathVariable("idPatient") Long idPatient) {
        
        Integer idMedecin = currentUserService.getCurrentMedecinId();
        Patient patient = patientService.consulterDossierPatientParMedecin(idMedecin, idPatient);
        return ResponseEntity.ok(patient);
    }

    /**
     * ➕ POST /api/medecins/patients/{idPatient}/lier
     */
    @PostMapping(path = "/{idPatient}/lier")
    public ResponseEntity<Void> lierPatientAMedecin(
            @PathVariable("idPatient") Long idPatient) {
        
        Integer idMedecin = currentUserService.getCurrentMedecinId();
        patientService.lierPatientAMedecin(idMedecin, idPatient);
        return ResponseEntity.ok().build();
    }
}