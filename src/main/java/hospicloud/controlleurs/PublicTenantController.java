package hospicloud.controlleurs;

import hospicloud.dtos.TenantPublicDTO;
import hospicloud.services.TenantPublicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/tenant")
public class PublicTenantController {

    private final TenantPublicService tenantPublicService;

    public PublicTenantController(TenantPublicService tenantPublicService) {
        this.tenantPublicService = tenantPublicService;
    }

    @GetMapping
    public ResponseEntity<TenantPublicDTO> getBySubdomain(@RequestParam("subdomain") String subdomain) {
        return ResponseEntity.ok(tenantPublicService.getBySubdomain(subdomain));
    }
}
