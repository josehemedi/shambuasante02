package hospicloud.controlleurs;

import hospicloud.dtos.TenantReportsOverviewDTO;
import hospicloud.services.TenantReportsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/tenant-admin/reports")
public class TenantAdminReportsController {

    private final TenantReportsService tenantReportsService;

    public TenantAdminReportsController(TenantReportsService tenantReportsService) {
        this.tenantReportsService = tenantReportsService;
    }

    @GetMapping("/overview")
    public ResponseEntity<TenantReportsOverviewDTO> getOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(tenantReportsService.getOverview(from, to));
    }
}
