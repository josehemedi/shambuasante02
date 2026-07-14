package hospicloud.controlleurs;

import hospicloud.dtos.HospitalPlanCatalogDTO;
import hospicloud.dtos.SubscriptionInvoiceDTO;
import hospicloud.dtos.SubscriptionKpisDTO;
import hospicloud.dtos.SubscriptionTimelineEventDTO;
import hospicloud.services.SubscriptionManagementService;
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

    public SubscriptionManagementController(SubscriptionManagementService subscriptionManagementService) {
        this.subscriptionManagementService = subscriptionManagementService;
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

    @GetMapping("/timeline")
    public ResponseEntity<List<SubscriptionTimelineEventDTO>> getTimeline(
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        return ResponseEntity.ok(subscriptionManagementService.getTimeline(limit));
    }
}
