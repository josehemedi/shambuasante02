package hospicloud.controlleurs;

import hospicloud.dtos.PatientDossierDTO;
import hospicloud.dtos.patient.PatientMessageConversationDTO;
import hospicloud.security.CurrentUserContext;
import hospicloud.services.ConsultationMedicaleService;
import hospicloud.services.PatientMessageService;
import hospicloud.services.PatientService;
import hospicloud.servicesImpl.PatientDossierReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients/me")
public class PatientSelfController {

    private final PatientService patientService;
    private final PatientMessageService patientMessageService;
    private final ConsultationMedicaleService consultationMedicaleService;
    private final PatientDossierReportService patientDossierReportService;

    public PatientSelfController(PatientService patientService,
                                 PatientMessageService patientMessageService,
                                 ConsultationMedicaleService consultationMedicaleService,
                                 PatientDossierReportService patientDossierReportService) {
        this.patientService = patientService;
        this.patientMessageService = patientMessageService;
        this.consultationMedicaleService = consultationMedicaleService;
        this.patientDossierReportService = patientDossierReportService;
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
