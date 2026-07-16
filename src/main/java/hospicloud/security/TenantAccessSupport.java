package hospicloud.security;

import hospicloud.exceptions.ForbiddenException;
import hospicloud.model.Role;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Résolution du tenant (id_hopital) depuis le JWT — jamais depuis un paramètre client
 * pour les rôles liés à un établissement. SUPER_ADMIN peut opérer via TenantContext
 * (X-Hopital-Id validé uniquement par TenantResolverFilter).
 */
public final class TenantAccessSupport {

    private TenantAccessSupport() {}

    public static Integer requireHopitalId(Role... allowedRoles) {
        UtilisateurPrincipal principal = requirePrincipal(allowedRoles);
        return resolveHopitalId(principal);
    }

    public static UtilisateurPrincipal requirePrincipal(Role... allowedRoles) {
        Set<Role> allowed = Arrays.stream(allowedRoles).collect(Collectors.toSet());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UtilisateurPrincipal principal)) {
            throw new AccessDeniedException("Authentification requise");
        }
        if (!allowed.contains(principal.getAppRole())) {
            throw new ForbiddenException("Accès non autorisé pour votre rôle");
        }
        return principal;
    }

    /**
     * Tenant effectif : JWT pour le staff ; TenantContext (header) pour SUPER_ADMIN.
     */
    public static Integer resolveHopitalId(UtilisateurPrincipal principal) {
        if (principal.getAppRole() == Role.SUPER_ADMIN) {
            Integer fromContext = TenantContext.getHopitalId();
            if (fromContext != null) {
                return fromContext;
            }
            throw new ForbiddenException(
                    "SUPER_ADMIN : précisez X-Hopital-Id pour cibler un établissement.");
        }
        Integer hopitalId = principal.getIdHopital();
        if (hopitalId == null) {
            throw new ForbiddenException("Aucun établissement associé à votre compte");
        }
        return hopitalId;
    }
}
