package hospicloud.controlleurs;

import hospicloud.dtos.TenantBillingOverviewDTO;
import hospicloud.services.TenantBillingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant-admin/billing")
public class TenantBillingController {

    private final TenantBillingService tenantBillingService;

    public TenantBillingController(TenantBillingService tenantBillingService) {
        this.tenantBillingService = tenantBillingService;
    }

    @GetMapping("/overview")
    public ResponseEntity<TenantBillingOverviewDTO> getOverview() {
        return ResponseEntity.ok(tenantBillingService.getOverview());
    }
}
