package hospicloud.controlleurs;

import hospicloud.dtos.DashboardDTO;
import hospicloud.dtos.DashboardStatsDTO;
import hospicloud.dtos.MrrSeriesPointDTO;
import hospicloud.dtos.PlanDistributionItemDTO;
import hospicloud.dtos.TenantOverviewDTO;
import hospicloud.services.DashboardService;
import hospicloud.servicesImpl.DoctorDashboardReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final DoctorDashboardReportService doctorDashboardReportService;

    public DashboardController(
            DashboardService dashboardService,
            DoctorDashboardReportService doctorDashboardReportService) {
        this.dashboardService = dashboardService;
        this.doctorDashboardReportService = doctorDashboardReportService;
    }

    @GetMapping
    public ResponseEntity<DashboardDTO> getDashboard() {
        DashboardDTO dashboard = dashboardService.getDashboardData();
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/medecin")
    public ResponseEntity<DashboardDTO> getMedecinDashboard() {
        return getDashboard();
    }

    @GetMapping(value = "/medecin/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getMedecinDashboardPdf() {
        byte[] pdf = doctorDashboardReportService.genererPdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=tableau_bord_medecin.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        DashboardStatsDTO stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/mrr-series")
    public ResponseEntity<List<MrrSeriesPointDTO>> getMrrSeries(
            @RequestParam(value = "months", defaultValue = "6") int months) {
        return ResponseEntity.ok(dashboardService.getMrrSeries(months));
    }

    @GetMapping("/plan-distribution")
    public ResponseEntity<List<PlanDistributionItemDTO>> getPlanDistribution() {
        return ResponseEntity.ok(dashboardService.getPlanDistribution());
    }

    @GetMapping("/tenants")
    public ResponseEntity<List<TenantOverviewDTO>> getTenantsOverview() {
        return ResponseEntity.ok(dashboardService.getTenantsOverview());
    }
}