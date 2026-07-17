package hospicloud.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import hospicloud.exceptions.ApiError;
import hospicloud.model.Role;
import hospicloud.saas.SaasPlanFeature;
import hospicloud.services.SaasPlanService;
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
import java.util.List;

/**
 * Bloque l'accès aux modules non inclus dans le forfait SaaS de l'établissement.
 */
public class PlanFeatureEnforcementFilter extends OncePerRequestFilter {

    private record PathRule(String prefix, SaasPlanFeature feature) {}

    private static final List<PathRule> RULES = List.of(
            new PathRule("/api/tenant-admin/laboratory", SaasPlanFeature.LAB),
            new PathRule("/api/v1/lab", SaasPlanFeature.LAB),
            new PathRule("/api/tenant-admin/pharmacy", SaasPlanFeature.PHARMACY),
            new PathRule("/api/tenant-admin/billing", SaasPlanFeature.BILLING),
            new PathRule("/api/tenant/cashier", SaasPlanFeature.BILLING),
            new PathRule("/api/v1/factures", SaasPlanFeature.BILLING),
            new PathRule("/api/tenant-admin/reports", SaasPlanFeature.REPORTS),
            new PathRule("/api/consultations/teleconsultation", SaasPlanFeature.TELECONSULTATION),
            new PathRule("/api/ai", SaasPlanFeature.AI_ASSISTANT),
            new PathRule("/api/rag", SaasPlanFeature.AI_ASSISTANT)
    );

    private final SaasPlanService saasPlanService;
    private final ObjectMapper objectMapper;

    public PlanFeatureEnforcementFilter(SaasPlanService saasPlanService, ObjectMapper objectMapper) {
        this.saasPlanService = saasPlanService;
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

        String path = request.getRequestURI();
        if (path == null) {
            filterChain.doFilter(request, response);
            return;
        }

        for (PathRule rule : RULES) {
            if (path.equals(rule.prefix()) || path.startsWith(rule.prefix() + "/")) {
                if (!saasPlanService.hasFeature(principal.getIdHopital(), rule.feature())) {
                    writeForbidden(response, request, rule.feature());
                    return;
                }
                break;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void writeForbidden(HttpServletResponse response,
                                HttpServletRequest request,
                                SaasPlanFeature feature) throws IOException {
        ApiError error = new ApiError(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "Module non disponible avec votre forfait actuel (" + feature.name() + ").",
                request.getRequestURI(),
                "PLAN_FEATURE_DENIED");
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
        return path.startsWith("/api/auth/")
                || path.startsWith("/api/public/")
                || path.startsWith("/api/tenant-admin/subscription")
                || path.startsWith("/actuator/")
                || path.startsWith("/ws/");
    }
}
