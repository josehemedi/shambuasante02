package hospicloud.controlleurs;

import hospicloud.dtos.LaboratoryOverviewDTO;
import hospicloud.services.TenantLaboratoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant-admin/laboratory")
public class TenantLaboratoryController {

    private final TenantLaboratoryService tenantLaboratoryService;

    public TenantLaboratoryController(TenantLaboratoryService tenantLaboratoryService) {
        this.tenantLaboratoryService = tenantLaboratoryService;
    }

    @GetMapping("/overview")
    public ResponseEntity<LaboratoryOverviewDTO> getOverview() {
        return ResponseEntity.ok(tenantLaboratoryService.getOverview());
    }
}
