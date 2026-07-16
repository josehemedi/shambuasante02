package hospicloud.security;

import hospicloud.model.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class TenantResolverFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            Integer hopitalId = resolveTenantId(request);
            if (hopitalId != null) {
                TenantContext.setHopitalId(hopitalId);
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private Integer resolveTenantId(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UtilisateurPrincipal principal) {
            if (principal.getAppRole() == Role.SUPER_ADMIN) {
                String headerId = request.getHeader("X-Hopital-Id");
                if (headerId != null && !headerId.isBlank()) {
                    try {
                        return Integer.parseInt(headerId);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid X-Hopital-Id header: " + headerId);
                    }
                }
                return null;
            }

            Integer hopitalId = principal.getIdHopital();
            if (hopitalId == null) {
                throw new IllegalStateException("Tenant context not initialized. L'utilisateur n'est associé à aucun hôpital.");
            }
            return hopitalId;
        }

        // SaaS multi-tenant : sans JWT, jamais initialiser le tenant via X-Hopital-Id.
        return null;
    }
}
