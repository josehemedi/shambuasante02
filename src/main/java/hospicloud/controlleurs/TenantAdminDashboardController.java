package hospicloud.controlleurs;

import hospicloud.dtos.HospitalAdminDashboardDTO;
import hospicloud.services.HospitalAdminDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant-admin/dashboard")
public class TenantAdminDashboardController {

    private final HospitalAdminDashboardService hospitalAdminDashboardService;

    public TenantAdminDashboardController(HospitalAdminDashboardService hospitalAdminDashboardService) {
        this.hospitalAdminDashboardService = hospitalAdminDashboardService;
    }

    @GetMapping
    public ResponseEntity<HospitalAdminDashboardDTO> getDashboard() {
        return ResponseEntity.ok(hospitalAdminDashboardService.getDashboard());
    }
}
