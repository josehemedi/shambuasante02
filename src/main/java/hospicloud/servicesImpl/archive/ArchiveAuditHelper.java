package hospicloud.servicesImpl.archive;

import hospicloud.dtos.events.TechnicalLogEvent;
import hospicloud.security.CurrentUserService;
import hospicloud.security.TenantContext;
import hospicloud.services.TechnicalLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class ArchiveAuditHelper {

    private static final String MODULE = "ARCHIVAGE";

    private final TechnicalLogService technicalLogService;
    private final CurrentUserService currentUserService;

    public ArchiveAuditHelper(TechnicalLogService technicalLogService,
                              CurrentUserService currentUserService) {
        this.technicalLogService = technicalLogService;
        this.currentUserService = currentUserService;
    }

    public void log(String action, String status, String message, Long archiveId,
                    String ancienStatut, String nouveauStatut, String motif) {
        TechnicalLogEvent event = new TechnicalLogEvent();
        event.setHopitalId(TenantContext.getHopitalId());
        Integer userId = currentUserService.getCurrentUtilisateurId();
        if (userId != null) event.setUserId(userId.longValue());
        event.setUserEmail(currentUserService.getCurrentUsername());
        if (currentUserService.getCurrentRole() != null) {
            event.setUserRole(currentUserService.getCurrentRole().name());
        }
        event.setModule(MODULE);
        event.setAction(action);
        event.setStatus(status);
        String details = "archiveId=" + archiveId;
        if (ancienStatut != null) details += ";ancien=" + ancienStatut;
        if (nouveauStatut != null) details += ";nouveau=" + nouveauStatut;
        if (motif != null) details += ";motif=" + motif;
        event.setMessage(message);
        event.setErrorDetails(details);
        resolveHttpContext(event);
        technicalLogService.record(event);
    }

    private void resolveHttpContext(TechnicalLogEvent event) {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return;
        HttpServletRequest request = attrs.getRequest();
        event.setEndpoint(request.getRequestURI());
        event.setHttpMethod(request.getMethod());
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            event.setIpAddress(forwarded.split(",")[0].trim());
        } else {
            event.setIpAddress(request.getRemoteAddr());
        }
        event.setUserAgent(request.getHeader("User-Agent"));
    }
}
