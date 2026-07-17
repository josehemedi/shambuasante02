package hospicloud.controlleurs;

import hospicloud.async.AsyncJobResponse;
import hospicloud.async.AsyncJobType;
import hospicloud.dtos.OrdonnanceEnvoiResponse;
import hospicloud.dtos.OrdonnanceRequest;
import hospicloud.dtos.OrdonnanceResponse;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.model.Ordonnance;
import hospicloud.security.CurrentUserService;
import hospicloud.services.AsyncReportGateway;
import hospicloud.services.OrdonnanceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ordonnances")
public class OrdonnanceController {

    private final OrdonnanceService ordonnanceService;
    private final AsyncReportGateway asyncReportGateway;
    private final CurrentUserService currentUserService;

    @Autowired
    public OrdonnanceController(OrdonnanceService ordonnanceService,
                                AsyncReportGateway asyncReportGateway,
                                CurrentUserService currentUserService) {
        this.ordonnanceService = ordonnanceService;
        this.asyncReportGateway = asyncReportGateway;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public ResponseEntity<String> creer(@Valid @RequestBody OrdonnanceRequest request) {
        ordonnanceService.creerOrdonnance(request);
        return ResponseEntity.status(201).body("Ordonnance créée avec succès.");
    }

    /** Génération PDF asynchrone (RabbitMQ) — réponse 202 + jobId. */
    @PostMapping("/{id}/pdf/async")
    public ResponseEntity<AsyncJobResponse> telechargerPdfAsync(@PathVariable Long id) {
        Map<String, Object> params = Map.of("reportName", "Ordonnance.jasper", "idOrdonnance", id);
        return asyncReportGateway.submit(AsyncJobType.REPORT_ORDONNANCE, id, params);
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> telechargerPdf(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean async) {
        if (async) {
            return telechargerPdfAsync(id);
        }
        byte[] pdf = ordonnanceService.genererPdfOrdonnance(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=ordonnance_" + id + ".pdf")
                .body(pdf);
    }

    @GetMapping("/patient/{idPatient}")
    public ResponseEntity<List<OrdonnanceResponse>> listerParPatient(@PathVariable Integer idPatient) {
        List<OrdonnanceResponse> response = ordonnanceService.listerParPatient(idPatient)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /** Ordonnances prescrites par le médecin connecté. */
    @GetMapping("/medecin/me")
    public ResponseEntity<List<OrdonnanceResponse>> listerPourMedecinConnecte() {
        Integer idMedecin = currentUserService.getCurrentMedecinId();
        if (idMedecin == null) {
            throw new ForbiddenException(
                    "Aucun profil médecin n'est associé à votre compte. Contactez l'administrateur.");
        }
        List<OrdonnanceResponse> response = ordonnanceService.listerParMedecin(idMedecin)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdonnanceResponse> obtenirParId(@PathVariable Long id) {
        return ordonnanceService.trouverParId(id)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/renouveler")
    public ResponseEntity<String> renouveler(@PathVariable Long id,
                                             @Valid @RequestBody OrdonnanceRequest request) {
        ordonnanceService.renouvelerOrdonnance(id, request);
        return ResponseEntity.ok("Ordonnance renouvelée avec succès.");
    }

    @PatchMapping("/{id}/annuler")
    public ResponseEntity<String> annuler(@PathVariable Long id) {
        ordonnanceService.annulerOrdonnance(id);
        return ResponseEntity.ok("Ordonnance annulée avec succès.");
    }

    /**
     * Envoie l'ordonnance PDF au patient concerné (e-mail professionnel + pièce jointe).
     */
    @PostMapping("/{id}/envoyer-patient")
    public ResponseEntity<OrdonnanceEnvoiResponse> envoyerAuPatient(@PathVariable Long id) {
        Integer idMedecin = currentUserService.getCurrentMedecinId();
        if (idMedecin == null) {
            throw new ForbiddenException(
                    "Aucun profil médecin n'est associé à votre compte. Contactez l'administrateur.");
        }
        return ResponseEntity.ok(ordonnanceService.envoyerAuPatient(id, idMedecin));
    }

    private OrdonnanceResponse mapToResponse(Ordonnance o) {
        OrdonnanceResponse r = new OrdonnanceResponse();
        r.setIdOrdonnance(o.getIdOrdonnance());
        r.setNumeroOrdonnance(o.getNumeroOrdonnance());
        r.setIdPatient(o.getIdPatient());
        r.setNomPatient(o.getNomPatient());
        r.setIdMedecin(o.getIdMedecin());
        r.setDatePrescription(o.getDatePrescription());
        r.setDiagnostic(o.getDiagnostic());
        r.setContenuOrdonnance(o.getContenuOrdonnance());
        r.setObservations(o.getObservations());
        r.setStatut(o.getStatut());
        r.setDateExpiration(o.getDateExpiration());

        String qrPayload = "SHAMBUA|ORD|" + o.getHospitalId() + "|" + o.getIdOrdonnance()
                + "|ORD-" + o.getHospitalId() + "-" + o.getIdOrdonnance();
        r.setQrCodeImage(hospicloud.utils.QrCodeService.generateQrCodeBytes(qrPayload));
        return r;
    }
}
