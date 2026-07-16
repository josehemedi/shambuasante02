package hospicloud.controlleurs;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Ancien endpoint de résolution tenant par sous-domaine — désactivé.
 * Le tenant SaaS est désormais résolu via le compte authentifié ({@code GET /api/tenant/current}).
 */
@RestController
@RequestMapping("/api/public/tenant")
public class PublicTenantController {

    @GetMapping
    public ResponseEntity<Map<String, String>> getBySubdomain(
            @RequestParam(value = "subdomain", required = false) String subdomain) {
        return ResponseEntity.status(HttpStatus.GONE).body(Map.of(
                "error", "SUBDOMAIN_TENANT_DISABLED",
                "message",
                "La détection d'établissement par sous-domaine est désactivée. "
                        + "Connectez-vous : le tenant est déterminé par votre compte (idHopital)."));
    }
}
