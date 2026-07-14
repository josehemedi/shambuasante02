package hospicloud.controlleurs;

import hospicloud.async.AsyncJobResponse;
import hospicloud.async.AsyncJobType;
import hospicloud.dtos.OrdonnanceRequest;
import hospicloud.dtos.OrdonnanceResponse;
import hospicloud.model.Ordonnance;
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

    @Autowired
    public OrdonnanceController(OrdonnanceService ordonnanceService, AsyncReportGateway asyncReportGateway) {
        this.ordonnanceService = ordonnanceService;
        this.asyncReportGateway = asyncReportGateway;
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
        // Prépare les params via génération sync légère : on laisse le listener utiliser Jasper
        // avec un payload minimal ; pour Ordonnance le listener REPORT_GENERIC a besoin des params.
        // On bascule sur type ORDONNANCE + entityId et on enrichit dans le gate si besoin.
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

    // =========================
    // LIST BY PATIENT (DTO + QR via controller)
    // =========================
    @GetMapping("/patient/{idPatient}")
    public ResponseEntity<List<OrdonnanceResponse>> listerParPatient(@PathVariable Integer idPatient) {

        List<OrdonnanceResponse> response = ordonnanceService.listerParPatient(idPatient)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // =========================
    // GET BY ID (DTO + QR)
    // =========================
    @GetMapping("/{id}")
    public ResponseEntity<OrdonnanceResponse> obtenirParId(@PathVariable Long id) {

        return ordonnanceService.trouverParId(id)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================
    // RENOUVELER
    // =========================
    @PostMapping("/{id}/renouveler")
    public ResponseEntity<String> renouveler(@PathVariable Long id,
                                             @Valid @RequestBody OrdonnanceRequest request) {
        ordonnanceService.renouvelerOrdonnance(id, request);
        return ResponseEntity.ok("Ordonnance renouvelée avec succès.");
    }

    // =========================
    // ANNULER
    // =========================
    @PatchMapping("/{id}/annuler")
    public ResponseEntity<String> annuler(@PathVariable Long id) {
        ordonnanceService.annulerOrdonnance(id);
        return ResponseEntity.ok("Ordonnance annulée avec succès.");
    }

    // =========================
    // MAPPING ENTITY → DTO
    // =========================
    private OrdonnanceResponse mapToResponse(Ordonnance o) {

        OrdonnanceResponse r = new OrdonnanceResponse();

        r.setIdOrdonnance(o.getIdOrdonnance());
        r.setNumeroOrdonnance(o.getNumeroOrdonnance());
        r.setIdPatient(o.getIdPatient());
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