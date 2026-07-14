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
 * pour les rôles liés à un établissement.
 */
public final class TenantAccessSupport {

    private TenantAccessSupport() {}

    public static Integer requireHopitalId(Role... allowedRoles) {
        Set<Role> allowed = Arrays.stream(allowedRoles).collect(Collectors.toSet());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UtilisateurPrincipal principal)) {
            throw new AccessDeniedException("Authentification requise");
        }
        if (!allowed.contains(principal.getAppRole())) {
            throw new ForbiddenException("Accès non autorisé pour votre rôle");
        }
        Integer hopitalId = principal.getIdHopital();
        if (hopitalId == null) {
            throw new ForbiddenException("Aucun établissement associé à votre compte");
        }
        return hopitalId;
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
        if (principal.getIdHopital() == null) {
            throw new ForbiddenException("Aucun établissement associé à votre compte");
        }
        return principal;
    }
}
