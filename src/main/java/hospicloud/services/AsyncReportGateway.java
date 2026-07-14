package hospicloud.services;

import hospicloud.async.AsyncJobResponse;
import hospicloud.async.AsyncJobType;
import hospicloud.security.TenantContext;
import hospicloud.security.UtilisateurPrincipal;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Aide pour soumettre rapidement un rapport en mode asynchrone (HTTP 202).
 */
@Component
public class AsyncReportGateway {

    private final AsyncJobService asyncJobService;

    public AsyncReportGateway(AsyncJobService asyncJobService) {
        this.asyncJobService = asyncJobService;
    }

    public ResponseEntity<AsyncJobResponse> submit(
            AsyncJobType type, Long entityId, Map<String, Object> payload) {
        AsyncJobResponse job = asyncJobService.enqueueReport(
                type, currentHopitalId(), currentUserId(), entityId, payload);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .header(HttpHeaders.LOCATION, job.getStatusUrl())
                .body(job);
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
