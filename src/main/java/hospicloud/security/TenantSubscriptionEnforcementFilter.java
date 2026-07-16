package hospicloud.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import hospicloud.exceptions.ApiError;
import hospicloud.model.Role;
import hospicloud.services.TenantSubscriptionAccessService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Bloque l'accès API aux utilisateurs d'un établissement dont l'abonnement est expiré,
 * sauf pour l'administrateur d'hôpital qui conserve l'accès aux endpoints de renouvellement.
 */
public class TenantSubscriptionEnforcementFilter extends OncePerRequestFilter {

    private static final Set<String> TENANT_ADMIN_RENEWAL_PREFIXES = Set.of(
            "/api/tenant-admin/subscription",
            "/api/tenant/current",
            "/api/auth/me",
            "/api/auth/logout"
    );

    private final TenantSubscriptionAccessService tenantSubscriptionAccessService;
    private final ObjectMapper objectMapper;

    public TenantSubscriptionEnforcementFilter(
            TenantSubscriptionAccessService tenantSubscriptionAccessService,
            ObjectMapper objectMapper) {
        this.tenantSubscriptionAccessService = tenantSubscriptionAccessService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UtilisateurPrincipal principal)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (principal.getAppRole() == Role.SUPER_ADMIN || principal.getIdHopital() == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!tenantSubscriptionAccessService.isPlatformAccessRestricted(principal.getIdHopital())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (principal.getAppRole() == Role.TENANT_ADMIN && isTenantAdminRenewalPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        writeForbidden(response, request,
                principal.getAppRole() == Role.TENANT_ADMIN
                        ? "Accès limité au renouvellement d'abonnement. Utilisez la page Mon abonnement."
                        : "L'abonnement de votre établissement a expiré. Contactez l'administrateur de l'hôpital.");
    }

    private boolean isTenantAdminRenewalPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path == null) {
            return false;
        }
        for (String prefix : TENANT_ADMIN_RENEWAL_PREFIXES) {
            if (path.equals(prefix) || path.startsWith(prefix + "/")) {
                return true;
            }
        }
        return false;
    }

    private void writeForbidden(HttpServletResponse response,
                                HttpServletRequest request,
                                String message) throws IOException {
        ApiError error = new ApiError(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                message,
                request.getRequestURI(),
                "SUBSCRIPTION_LAPSED");
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), error);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path == null) {
            return false;
        }
        return path.startsWith("/api/auth/login")
                || path.startsWith("/api/auth/forgot-password")
                || path.startsWith("/api/auth/reset-password")
                || path.startsWith("/api/public/")
                || path.startsWith("/actuator/")
                || path.startsWith("/ws/");
    }
}
