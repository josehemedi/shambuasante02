package hospicloud.controlleurs;

import hospicloud.async.AsyncJobResponse;
import hospicloud.async.AsyncJobType;
import hospicloud.exceptions.BadRequestException;
import hospicloud.security.TenantContext;
import hospicloud.security.UtilisateurPrincipal;
import hospicloud.services.AsyncJobService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * API unifiée des travaux asynchrones (rapports + enregistrements).
 */
@RestController
@RequestMapping("/api/async")
public class AsyncJobController {

    private final AsyncJobService asyncJobService;

    public AsyncJobController(AsyncJobService asyncJobService) {
        this.asyncJobService = asyncJobService;
    }

    @PostMapping("/reports")
    public ResponseEntity<AsyncJobResponse> submitReport(@RequestBody Map<String, Object> body) {
        AsyncJobType type = parseType(body.get("type"), AsyncJobType.REPORT_GENERIC);
        if (!type.name().startsWith("REPORT_")) {
            throw new BadRequestException("Type de rapport invalide");
        }
        Long entityId = body.get("entityId") != null ? ((Number) body.get("entityId")).longValue() : null;
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = body.get("payload") instanceof Map<?, ?> m
                ? (Map<String, Object>) m
                : body;
        AsyncJobResponse job = asyncJobService.enqueueReport(
                type, currentHopitalId(), currentUserId(), entityId, payload);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header(HttpHeaders.LOCATION, job.getStatusUrl())
                .body(job);
    }

    @PostMapping("/enregistrements")
    public ResponseEntity<AsyncJobResponse> submitEnregistrement(@RequestBody Map<String, Object> body) {
        AsyncJobType type = parseType(body.get("type"), AsyncJobType.ENREGISTREMENT_PATIENT);
        if (!type.name().startsWith("ENREGISTREMENT_")) {
            throw new BadRequestException("Type d'enregistrement invalide");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = body.get("payload") instanceof Map<?, ?> m
                ? (Map<String, Object>) m
                : body;
        AsyncJobResponse job = asyncJobService.enqueueEnregistrement(
                type, currentHopitalId(), currentUserId(), payload);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header(HttpHeaders.LOCATION, job.getStatusUrl())
                .body(job);
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<AsyncJobResponse> getJob(@PathVariable String jobId) {
        return ResponseEntity.ok(asyncJobService.getJob(jobId));
    }

    @GetMapping(value = "/jobs/{jobId}/download", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> download(@PathVariable String jobId) {
        byte[] pdf = asyncJobService.loadReportBytes(jobId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=rapport-" + jobId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private AsyncJobType parseType(Object raw, AsyncJobType fallback) {
        if (raw == null) return fallback;
        try {
            return AsyncJobType.valueOf(String.valueOf(raw).trim().toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Type de job inconnu: " + raw);
        }
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
