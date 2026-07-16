package hospicloud.controlleurs;

import hospicloud.dtos.TenantPublicDTO;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.model.Role;
import hospicloud.security.TenantContext;
import hospicloud.security.UtilisateurPrincipal;
import hospicloud.services.TenantPublicService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tenant courant résolu depuis le compte authentifié (JWT idHopital),
 * jamais depuis le sous-domaine navigateur.
 */
@RestController
@RequestMapping("/api/tenant")
public class TenantCurrentController {

    private final TenantPublicService tenantPublicService;

    public TenantCurrentController(TenantPublicService tenantPublicService) {
        this.tenantPublicService = tenantPublicService;
    }

    @GetMapping("/current")
    public ResponseEntity<TenantPublicDTO> getCurrentTenant() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UtilisateurPrincipal principal)) {
            throw new ForbiddenException("Authentification requise");
        }

        Integer hopitalId = principal.getIdHopital();
        if (hopitalId == null && principal.getAppRole() == Role.SUPER_ADMIN) {
            hopitalId = TenantContext.getHopitalId();
        }
        if (hopitalId == null) {
            throw new ResourceNotFoundException("Aucun établissement associé à votre compte");
        }

        return ResponseEntity.ok(tenantPublicService.getByHopitalId(hopitalId));
    }
}
