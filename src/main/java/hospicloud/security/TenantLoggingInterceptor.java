package hospicloud.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

public class TenantLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Integer hopitalId = TenantContext.getHopitalId();
        if (hopitalId != null) {
            MDC.put("hopitalId", String.valueOf(hopitalId));
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // Très important pour éviter les fuites de mémoire et les erreurs de logs entre requêtes
        MDC.remove("hopitalId");
    }
}