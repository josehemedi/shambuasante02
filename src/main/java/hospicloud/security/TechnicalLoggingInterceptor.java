package hospicloud.security;

import hospicloud.dtos.events.TechnicalLogEvent;
import hospicloud.services.TechnicalLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class TechnicalLoggingInterceptor implements HandlerInterceptor {

    private final TechnicalLogService technicalLogService;

    public TechnicalLoggingInterceptor(TechnicalLogService technicalLogService) {
        this.technicalLogService = technicalLogService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestId = request.getHeader("X-Request-Id");
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        RequestContext.setRequestId(requestId);
        response.setHeader("X-Request-Id", requestId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        try {
            int status = response.getStatus();
            if (status >= 400 || ex != null) {
                String uri = request.getRequestURI();
                if (shouldSkip(uri)) {
                    return;
                }
                String module = resolveModule(uri);
                String action = "HTTP_" + status;
                String message = ex != null ? ex.getMessage() : "Réponse HTTP " + status;
                String errorDetails = ex != null ? stackTrace(ex) : null;
                technicalLogService.recordApiError(
                        module,
                        action,
                        uri,
                        request.getMethod(),
                        status,
                        message,
                        errorDetails,
                        clientIp(request),
                        request.getHeader("User-Agent"));
            }
        } finally {
            RequestContext.clear();
        }
    }

    private static boolean shouldSkip(String uri) {
        return uri.startsWith("/actuator")
                || uri.startsWith("/ws/")
                || uri.equals("/api/auth/login");
    }

    static String resolveModule(String uri) {
        if (uri.contains("/tenant/cashier") || uri.contains("/v1/factures")) {
            return "caisse";
        }
        if (uri.contains("/auth")) {
            return "auth";
        }
        if (uri.contains("/patients")) {
            return "patients";
        }
        if (uri.contains("/rendezvous")) {
            return "rendezvous";
        }
        if (uri.contains("/consultations")) {
            return "consultations";
        }
        if (uri.contains("/v1/lab")) {
            return "laboratoire";
        }
        if (uri.contains("/v1/reception")) {
            return "reception";
        }
        if (uri.contains("/tenant-admin/pharmacy")) {
            return "pharmacie";
        }
        if (uri.contains("/hopitaux")) {
            return "plateforme";
        }
        if (uri.contains("/subscriptions")) {
            return "abonnements";
        }
        if (uri.contains("/audit") || uri.contains("/support")) {
            return "support";
        }
        return "api";
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static String stackTrace(Exception ex) {
        StringBuilder sb = new StringBuilder();
        sb.append(ex.getClass().getSimpleName()).append(": ").append(ex.getMessage());
        for (StackTraceElement el : ex.getStackTrace()) {
            if (sb.length() > 4000) {
                break;
            }
            sb.append("\n at ").append(el);
        }
        return sb.toString();
    }
}
