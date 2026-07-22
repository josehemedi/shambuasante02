package hospicloud.controlleurs;

import hospicloud.dtos.PatientDossierDTO;
import hospicloud.dtos.SupportTicketDTO;
import hospicloud.dtos.patient.PatientAppointmentRequestDTO;
import hospicloud.dtos.patient.PatientAssistanceRequestDTO;
import hospicloud.dtos.patient.PatientMessageConversationDTO;
import hospicloud.dtos.patient.PatientProfileUpdateDTO;
import hospicloud.model.Facture;
import hospicloud.model.Ordonnance;
import hospicloud.model.Patient;
import hospicloud.model.RendezVous;
import hospicloud.security.CurrentUserContext;
import hospicloud.services.ConsultationMedicaleService;
import hospicloud.services.PatientMessageService;
import hospicloud.services.PatientPortalService;
import hospicloud.services.PatientService;
import hospicloud.servicesImpl.PatientDossierReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/patients/me")
public class PatientSelfController {

    private final PatientService patientService;
    private final PatientPortalService patientPortalService;
    private final PatientMessageService patientMessageService;
    private final ConsultationMedicaleService consultationMedicaleService;
    private final PatientDossierReportService patientDossierReportService;

    public PatientSelfController(PatientService patientService,
                                 PatientPortalService patientPortalService,
                                 PatientMessageService patientMessageService,
                                 ConsultationMedicaleService consultationMedicaleService,
                                 PatientDossierReportService patientDossierReportService) {
        this.patientService = patientService;
        this.patientPortalService = patientPortalService;
        this.patientMessageService = patientMessageService;
        this.consultationMedicaleService = consultationMedicaleService;
        this.patientDossierReportService = patientDossierReportService;
    }

    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Patient> getProfile() {
        return ResponseEntity.ok(patientPortalService.getMyProfile());
    }

    @PutMapping(value = "/profile", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Patient> updateProfile(@Valid @RequestBody PatientProfileUpdateDTO request) {
        return ResponseEntity.ok(patientPortalService.updateMyProfile(request));
    }

    @GetMapping(value = "/appointments", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RendezVous>> listAppointments() {
        return ResponseEntity.ok(patientPortalService.listMyAppointments());
    }

    @PostMapping(value = "/appointments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RendezVous> requestAppointment(@Valid @RequestBody PatientAppointmentRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientPortalService.requestAppointment(request));
    }

    @PostMapping(value = "/appointments/{id}/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RendezVous> cancelAppointment(
            @PathVariable("id") Integer id,
            @RequestBody(required = false) Map<String, String> body) {
        String motif = body != null ? body.get("motif") : null;
        return ResponseEntity.ok(patientPortalService.cancelMyAppointment(id, motif));
    }

    @PostMapping(value = "/appointments/{id}/reschedule", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RendezVous> rescheduleAppointment(
            @PathVariable("id") Integer id,
            @RequestBody Map<String, String> body) {
        String date = body != null ? body.get("dateHeureRdv") : null;
        return ResponseEntity.ok(patientPortalService.rescheduleMyAppointment(id, date));
    }

    @GetMapping(value = "/doctors", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> listDoctors(
            @RequestParam(value = "specialite", required = false) String specialite) {
        return ResponseEntity.ok(patientPortalService.listDoctorsForBooking(specialite));
    }

    @GetMapping(value = "/invoices", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Facture>> listInvoices() {
        return ResponseEntity.ok(patientPortalService.listMyInvoices());
    }

    @GetMapping(value = "/prescriptions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Ordonnance>> listPrescriptions() {
        return ResponseEntity.ok(patientPortalService.listMyPrescriptions());
    }

    @GetMapping(value = "/lab-results", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> listLabResults() {
        return ResponseEntity.ok(patientPortalService.listMyLabResults());
    }

    @GetMapping(value = "/documents", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> listDocuments() {
        return ResponseEntity.ok(patientPortalService.listMyDocuments());
    }

    @GetMapping("/documents/{idDocument}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Integer idDocument) {
        Map<String, Object> file = patientPortalService.downloadMyDocument(idDocument);
        byte[] bytes = (byte[]) file.get("bytes");
        String fileName = String.valueOf(file.getOrDefault("fileName", "document"));
        String contentType = String.valueOf(file.getOrDefault("contentType", MediaType.APPLICATION_OCTET_STREAM_VALUE));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName.replace("\"", "") + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(bytes);
    }

    @PostMapping(value = "/assistance", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SupportTicketDTO> requestAssistance(@Valid @RequestBody PatientAssistanceRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientPortalService.requestAssistance(request));
    }

    @GetMapping(value = "/dossier", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientDossierDTO> getMonDossier() {
        Integer idPatient = CurrentUserContext.getPatientId();
        if (idPatient == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(patientService.obtenirMonDossier());
    }

    @GetMapping(value = "/dossier/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadMonDossierPdf() {
        Integer idPatient = CurrentUserContext.getPatientId();
        if (idPatient == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            byte[] pdf = patientDossierReportService.genererPdf(patientService.obtenirMonDossier());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=dossier_medical.pdf")
                    .body(pdf);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(PatientSelfController.class)
                    .error("Erreur génération PDF dossier patient {}: {}", idPatient, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/messages/conversations", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PatientMessageConversationDTO>> getMesConversations() {
        Integer idPatient = CurrentUserContext.getPatientId();
        if (idPatient == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(patientMessageService.listConversations());
    }

    @GetMapping(value = "/consultations/{idConsultation}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadConsultationPdf(@PathVariable Long idConsultation) {
        Integer idPatient = CurrentUserContext.getPatientId();
        if (idPatient == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            byte[] pdf = consultationMedicaleService.genererPdfFicheConsultation(idConsultation);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=fiche_consultation_" + idConsultation + ".pdf")
                    .body(pdf);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(PatientSelfController.class)
                    .error("Erreur génération PDF consultation {}: {}", idConsultation, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
