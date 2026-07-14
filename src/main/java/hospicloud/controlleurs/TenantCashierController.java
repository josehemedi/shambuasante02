package hospicloud.controlleurs;

import hospicloud.async.AsyncJobResponse;
import hospicloud.async.AsyncJobType;
import hospicloud.dtos.BillingAdvanceRequestDTO;
import hospicloud.dtos.BillingComposeRequestDTO;
import hospicloud.dtos.TenantCashierPaymentRequestDTO;
import hospicloud.dtos.TenantCashierWorkspaceDTO;
import hospicloud.services.AsyncReportGateway;
import hospicloud.services.BillingCompositionService;
import hospicloud.services.TenantCashierService;
import hospicloud.servicesImpl.CashierDashboardReportService;
import hospicloud.servicesImpl.CashierInvoiceReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/tenant/cashier")
public class TenantCashierController {

    private final TenantCashierService tenantCashierService;
    private final BillingCompositionService billingCompositionService;
    private final CashierDashboardReportService cashierDashboardReportService;
    private final CashierInvoiceReportService cashierInvoiceReportService;
    private final AsyncReportGateway asyncReportGateway;

    public TenantCashierController(
            TenantCashierService tenantCashierService,
            BillingCompositionService billingCompositionService,
            CashierDashboardReportService cashierDashboardReportService,
            CashierInvoiceReportService cashierInvoiceReportService,
            AsyncReportGateway asyncReportGateway) {
        this.tenantCashierService = tenantCashierService;
        this.billingCompositionService = billingCompositionService;
        this.cashierDashboardReportService = cashierDashboardReportService;
        this.cashierInvoiceReportService = cashierInvoiceReportService;
        this.asyncReportGateway = asyncReportGateway;
    }

    @GetMapping("/workspace")
    public ResponseEntity<TenantCashierWorkspaceDTO> getWorkspace() {
        return ResponseEntity.ok(tenantCashierService.getWorkspace());
    }

    @PostMapping("/rapport/pdf/async")
    public ResponseEntity<AsyncJobResponse> getDashboardPdfAsync() {
        return asyncReportGateway.submit(
                AsyncJobType.REPORT_CAISSE_DASHBOARD,
                null,
                Map.of("reportName", "Dashboard_Caissier.jrxml"));
    }

    @GetMapping(value = "/rapport/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> getDashboardPdf(@RequestParam(defaultValue = "false") boolean async) {
        if (async) {
            return getDashboardPdfAsync();
        }
        byte[] pdf = cashierDashboardReportService.genererPdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=tableau_bord_caissier.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping("/factures/{idFacture}/pdf/async")
    public ResponseEntity<AsyncJobResponse> getInvoicePdfAsync(@PathVariable Integer idFacture) {
        return asyncReportGateway.submit(
                AsyncJobType.REPORT_CAISSE_FACTURE,
                idFacture.longValue(),
                Map.of("idFacture", idFacture));
    }

    @GetMapping(value = "/factures/{idFacture}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> getInvoicePdf(
            @PathVariable Integer idFacture,
            @RequestParam(defaultValue = "false") boolean async) {
        if (async) {
            return getInvoicePdfAsync(idFacture);
        }
        byte[] pdf = cashierInvoiceReportService.genererPdf(idFacture);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=facture_" + idFacture + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping("/payments")
    public ResponseEntity<Map<String, Object>> collectPayment(@RequestBody TenantCashierPaymentRequestDTO request) {
        return ResponseEntity.ok(tenantCashierService.collectPayment(request));
    }

    @PostMapping("/invoices/compose")
    public ResponseEntity<Map<String, Object>> composeInvoice(@RequestBody BillingComposeRequestDTO request) {
        return ResponseEntity.ok(billingCompositionService.composeInvoice(request));
    }

    @PostMapping("/invoices/refresh")
    public ResponseEntity<Map<String, Object>> refreshOpenInvoices() {
        return ResponseEntity.ok(billingCompositionService.refreshOpenInvoices());
    }

    @PostMapping("/advances")
    public ResponseEntity<Map<String, Object>> recordAdvance(@RequestBody BillingAdvanceRequestDTO request) {
        return ResponseEntity.ok(billingCompositionService.recordAdvance(request));
    }
}
