package hospicloud.controlleurs;

import hospicloud.dtos.ChangeSubscriptionPlanRequest;
import hospicloud.dtos.HospitalPlanCatalogDTO;
import hospicloud.dtos.TenantSubscriptionDTO;
import hospicloud.dtos.TenantSubscriptionHistoryDTO;
import hospicloud.services.TenantSubscriptionService;
import hospicloud.servicesImpl.TenantSubscriptionPaymentReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenant-admin/subscription")
public class TenantSubscriptionController {

    private final TenantSubscriptionService tenantSubscriptionService;
    private final TenantSubscriptionPaymentReportService tenantSubscriptionPaymentReportService;

    public TenantSubscriptionController(
            TenantSubscriptionService tenantSubscriptionService,
            TenantSubscriptionPaymentReportService tenantSubscriptionPaymentReportService) {
        this.tenantSubscriptionService = tenantSubscriptionService;
        this.tenantSubscriptionPaymentReportService = tenantSubscriptionPaymentReportService;
    }

    @GetMapping
    public ResponseEntity<TenantSubscriptionDTO> getCurrent() {
        return ResponseEntity.ok(tenantSubscriptionService.getCurrentSubscription());
    }

    @GetMapping("/plans")
    public ResponseEntity<List<HospitalPlanCatalogDTO>> listPlans() {
        return ResponseEntity.ok(tenantSubscriptionService.listAvailablePlans());
    }

    @GetMapping("/history")
    public ResponseEntity<List<TenantSubscriptionHistoryDTO>> getHistory(
            @RequestParam(defaultValue = "12") int limit) {
        return ResponseEntity.ok(tenantSubscriptionService.getHistory(limit));
    }

    @PostMapping("/repay")
    public ResponseEntity<TenantSubscriptionDTO> repay() {
        return ResponseEntity.ok(tenantSubscriptionService.repaySubscription());
    }

    @PostMapping("/change-plan")
    public ResponseEntity<TenantSubscriptionDTO> changePlan(@Valid @RequestBody ChangeSubscriptionPlanRequest request) {
        return ResponseEntity.ok(tenantSubscriptionService.changePlan(request));
    }

    @GetMapping(value = "/payments-report/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadPaymentsReportPdf() {
        byte[] pdf = tenantSubscriptionPaymentReportService.genererPdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=rapport_paiements_abonnement.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
