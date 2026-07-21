package hospicloud.controlleurs;

import hospicloud.dtos.HospitalPlanCatalogDTO;
import hospicloud.dtos.SubscriptionInvoiceDTO;
import hospicloud.dtos.SubscriptionKpisDTO;
import hospicloud.dtos.SubscriptionTimelineEventDTO;
import hospicloud.services.SubscriptionManagementService;
import hospicloud.servicesImpl.PlatformSubscriptionInvoicesReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionManagementController {

    private final SubscriptionManagementService subscriptionManagementService;
    private final PlatformSubscriptionInvoicesReportService platformSubscriptionInvoicesReportService;

    public SubscriptionManagementController(
            SubscriptionManagementService subscriptionManagementService,
            PlatformSubscriptionInvoicesReportService platformSubscriptionInvoicesReportService) {
        this.subscriptionManagementService = subscriptionManagementService;
        this.platformSubscriptionInvoicesReportService = platformSubscriptionInvoicesReportService;
    }

    @GetMapping("/kpis")
    public ResponseEntity<SubscriptionKpisDTO> getKpis() {
        return ResponseEntity.ok(subscriptionManagementService.getKpis());
    }

    @GetMapping("/plans")
    public ResponseEntity<List<HospitalPlanCatalogDTO>> getPlans() {
        return ResponseEntity.ok(subscriptionManagementService.getPlans());
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<SubscriptionInvoiceDTO>> getInvoices(
            @RequestParam(value = "limit", defaultValue = "50") int limit) {
        return ResponseEntity.ok(subscriptionManagementService.getInvoices(limit));
    }

    @GetMapping(value = "/invoices/export.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportInvoicesPdf() {
        byte[] pdf = platformSubscriptionInvoicesReportService.genererPdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=factures_abonnements_plateforme.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/timeline")
    public ResponseEntity<List<SubscriptionTimelineEventDTO>> getTimeline(
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        return ResponseEntity.ok(subscriptionManagementService.getTimeline(limit));
    }
}
