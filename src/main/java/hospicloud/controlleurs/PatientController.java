package hospicloud.controlleurs;

import hospicloud.async.AsyncJobResponse;
import hospicloud.async.AsyncJobType;
import hospicloud.dtos.PatientDossierDTO;
import hospicloud.model.Patient;
import hospicloud.services.AsyncJobService;
import hospicloud.services.AsyncReportGateway;
import hospicloud.services.PatientService;
import hospicloud.servicesImpl.PatientDossierReportService;
import hospicloud.servicesImpl.PatientsListReportService;
import hospicloud.security.TenantContext;
import hospicloud.security.UtilisateurPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/patients", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class PatientController {

    private final PatientService patientService;
    private final PatientDossierReportService patientDossierReportService;
    private final PatientsListReportService patientsListReportService;
    private final AsyncJobService asyncJobService;
    private final AsyncReportGateway asyncReportGateway;

    @Autowired
    public PatientController(PatientService patientService,
                             PatientDossierReportService patientDossierReportService,
                             PatientsListReportService patientsListReportService,
                             AsyncJobService asyncJobService,
                             AsyncReportGateway asyncReportGateway) {
        this.patientService = patientService;
        this.patientDossierReportService = patientDossierReportService;
        this.patientsListReportService = patientsListReportService;
        this.asyncJobService = asyncJobService;
        this.asyncReportGateway = asyncReportGateway;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createPatient(
            @Valid @RequestBody Patient patient,
            @RequestParam(defaultValue = "false") boolean async) {
        if (async) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("nom", patient.getNom());
            payload.put("prenom", patient.getPrenom());
            payload.put("sexe", patient.getSexe());
            payload.put("dateNaissance", patient.getDateNaissance() != null ? patient.getDateNaissance().toString() : null);
            payload.put("telephone", patient.getTelephone());
            payload.put("email", patient.getEmail());
            payload.put("adresse", patient.getAdresse());
            AsyncJobResponse job = asyncJobService.enqueueEnregistrement(
                    AsyncJobType.ENREGISTREMENT_PATIENT,
                    currentHopitalId(),
                    currentUserId(),
                    payload);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .header(HttpHeaders.LOCATION, job.getStatusUrl())
                    .body(job);
        }

        patientService.enregisterPatient(patient);
        if (patient.getIdPatient() != null) {
            URI location = URI.create(String.format("/api/patients/%d", patient.getIdPatient()));
            return ResponseEntity.created(location).body(patient);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(patient);
    }

    /** Enregistrement patient 100 % asynchrone (file RabbitMQ). */
    @PostMapping(path = "/async", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createPatientAsync(@Valid @RequestBody Patient patient) {
        return createPatient(patient, true);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updatePatient(@PathVariable("id") Long id, @Valid @RequestBody Patient patient) {
        patient.setIdPatient(id);
        patientService.modifierPatient(patient);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable("id") Long id) {
        patientService.supprimerPatient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable("id") Long id) {
        return patientService.trouverPatientParId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(path = "/{id}/dossier")
    public ResponseEntity<PatientDossierDTO> getPatientDossier(@PathVariable("id") Long id) {
        return ResponseEntity.ok(patientService.obtenirDossierComplet(id));
    }

    @GetMapping(path = "/{id}/dossier/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadPatientDossierPdf(@PathVariable("id") Long id) {
        try {
            byte[] pdf = patientDossierReportService.genererPdf(patientService.obtenirDossierComplet(id));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=dossier_patient_" + id + ".pdf")
                    .body(pdf);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(PatientController.class)
                    .error("Erreur génération PDF dossier patient {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(path = "/rapport/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadPatientsListPdf(
            @RequestParam(value = "mine", required = false) Boolean mine) {
        try {
            byte[] pdf = patientsListReportService.genererPdf(mine);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=rapport_patients.pdf")
                    .body(pdf);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(PatientController.class)
                    .error("Erreur génération PDF liste patients: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Patient>> getAllPatients(
            @RequestParam(value = "mine", required = false) Boolean mine) {
        List<Patient> list = patientService.trouverTousLesPatients(mine);
        return ResponseEntity.ok(list);
    }

    @GetMapping(path = "/by-code/{code}")
    public ResponseEntity<Patient> getPatientByCode(@PathVariable("code") String code) {
        return patientService.trouverPatientParNumero(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(path = "/search")
    public ResponseEntity<List<Patient>> searchByName(@RequestParam(value = "nom", required = false) String nom,
                                                      @RequestParam(value = "prenom", required = false) String prenom) {
        List<Patient> list = patientService.rechercherParNomEtPrenom(nom, prenom);
        return ResponseEntity.ok(list);
    }

    private Integer currentHopitalId() {
        Integer fromCtx = TenantContext.getHopitalId();
        if (fromCtx != null) return fromCtx;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UtilisateurPrincipal p) {
            return p.getIdHopital();
        }
        return null;
    }

    private Integer currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UtilisateurPrincipal p) {
            return p.getIdUtilisateur();
        }
        return null;
    }

}