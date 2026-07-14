package hospicloud.servicesImpl;

import hospicloud.dtos.TenantCashierHistoryItemDTO;
import hospicloud.dtos.TenantCashierKpisDTO;
import hospicloud.dtos.TenantCashierQueueItemDTO;
import hospicloud.dtos.TenantCashierWorkspaceDTO;
import hospicloud.dtos.reporting.CashierHistoryReportRowDTO;
import hospicloud.dtos.reporting.CashierQueueReportRowDTO;
import hospicloud.dtos.reporting.ReportChartRowDTO;
import hospicloud.model.Hopital;
import hospicloud.model.Role;
import hospicloud.repositories.HopitalRepository;
import hospicloud.security.CurrentUserService;
import hospicloud.security.TenantAccessSupport;
import hospicloud.services.TenantCashierService;
import hospicloud.services.reporting.ReportGenerator;
import hospicloud.utils.BarcodeService;
import hospicloud.utils.TenantReportParamsHelper;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class CashierDashboardReportService {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.FRENCH);
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH);
    private static final DateTimeFormatter REF_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ReportGenerator reportGenerator;
    private final HopitalRepository hopitalRepository;
    private final TenantCashierService tenantCashierService;
    private final CurrentUserService currentUserService;

    public CashierDashboardReportService(
            ReportGenerator reportGenerator,
            HopitalRepository hopitalRepository,
            TenantCashierService tenantCashierService,
            CurrentUserService currentUserService) {
        this.reportGenerator = reportGenerator;
        this.hopitalRepository = hopitalRepository;
        this.tenantCashierService = tenantCashierService;
        this.currentUserService = currentUserService;
    }

    public byte[] genererPdf() {
        TenantAccessSupport.requirePrincipal(Role.CAISSIER, Role.TENANT_ADMIN);
        Integer idHopital = TenantAccessSupport.requireHopitalId(Role.CAISSIER, Role.TENANT_ADMIN);
        Hopital hopital = TenantReportParamsHelper.resolveActiveHopital(hopitalRepository, idHopital);

        TenantCashierWorkspaceDTO workspace = tenantCashierService.getWorkspace();
        TenantCashierKpisDTO kpis = workspace.getKpis() != null ? workspace.getKpis() : new TenantCashierKpisDTO();
        List<TenantCashierQueueItemDTO> queue = workspace.getQueue() != null ? workspace.getQueue() : List.of();
        List<TenantCashierHistoryItemDTO> history = workspace.getHistory() != null ? workspace.getHistory() : List.of();

        List<CashierQueueReportRowDTO> queueRows = buildQueueRows(queue);
        if (queueRows.isEmpty()) {
            queueRows = List.of(new CashierQueueReportRowDTO(
                    "—", "Aucun patient en attente de paiement", "—", "—", "—", "—", "—", "—", "—", "—", "—"));
        }

        List<CashierHistoryReportRowDTO> historyRows = buildHistoryRows(history);
        if (historyRows.isEmpty()) {
            historyRows = List.of(new CashierHistoryReportRowDTO(
                    "—", "—", "—", "Aucun encaissement récent", "—", "—", "—", "—", "—"));
        }

        LocalDateTime now = LocalDateTime.now();
        String reference = "CAISSE-H" + idHopital + "-" + now.format(REF_FORMAT);
        String barcodePayload = "SHAMBUA|" + reference;
        BigDecimal totalSolde = queue.stream()
                .map(TenantCashierQueueItemDTO::getBalanceDue)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> params = new HashMap<>();
        TenantReportParamsHelper.applyTenantBranding(params, hopital, idHopital);
        params.put("LOGO_HOPITAL", loadLogoInputStream(hopital));
        params.put("DATE_GENERATION", java.sql.Timestamp.valueOf(now));
        params.put("DATE_RAPPORT", now.format(DATE_FORMAT));
        params.put("REFERENCE_RAPPORT", reference);
        params.put("GENERE_PAR", resolveGenerateurLabel());
        params.put("KPI_ATTENTE", String.valueOf(kpis.getWaitingPayment()));
        params.put("KPI_ENCAISSE", formatMoney(BigDecimal.valueOf(kpis.getCollectedToday())));
        params.put("KPI_PARTIEL", String.valueOf(kpis.getPartialPayments()));
        params.put("KPI_SORTIE", String.valueOf(kpis.getAdminDischargePending()));
        params.put("TOTAL_SOLDE_ATTENTE", formatMoney(totalSolde));
        params.put("NB_FILE", String.valueOf(queue.size()));
        params.put("NB_HISTORIQUE", String.valueOf(history.size()));
        params.put("KPI_COLUMN_DS", new JRBeanCollectionDataSource(buildKpiRows(kpis)));
        params.put("STATUS_PIE_DS", new JRBeanCollectionDataSource(buildStatusPieRows(queue)));
        params.put("PAYMENT_METHOD_DS", new JRBeanCollectionDataSource(buildPaymentMethodRows(history)));
        params.put("BALANCE_BAR_DS", new JRBeanCollectionDataSource(buildBalanceBarRows(queue)));
        params.put("HISTORY_DS", new JRBeanCollectionDataSource(historyRows));
        params.put("CODE_BARRE_TEXTE", barcodePayload);
        params.put("BARCODE_IMAGE", generateBarcodeSafe(barcodePayload));

        try {
            params.put("SUBREPORT_HISTORY", compileSubreport("Caissier_Historique.jrxml"));
            return reportGenerator.generate(
                    "Dashboard_Caissier.jasper",
                    params,
                    new JRBeanCollectionDataSource(queueRows));
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de générer le rapport PDF du tableau de bord caissier.", e);
        }
    }

    private List<CashierQueueReportRowDTO> buildQueueRows(List<TenantCashierQueueItemDTO> queue) {
        return IntStream.range(0, queue.size())
                .mapToObj(i -> toQueueRow(queue.get(i), i + 1))
                .collect(Collectors.toList());
    }

    private CashierQueueReportRowDTO toQueueRow(TenantCashierQueueItemDTO item, int index) {
        return new CashierQueueReportRowDTO(
                String.valueOf(index),
                nullToDash(item.getPatientName()),
                nullToDash(item.getPatientId()),
                nullToDash(item.getInvoiceNumber()),
                formatQueueStatus(item.getStatus()),
                formatMoney(item.getTotalAmount()),
                formatMoney(item.getPaidAmount()),
                formatMoney(item.getBalanceDue()),
                formatPriority(item.getPriority()),
                nullToDash(item.getDepartment()),
                nullToDash(item.getDoctorName()));
    }

    private List<CashierHistoryReportRowDTO> buildHistoryRows(List<TenantCashierHistoryItemDTO> history) {
        return IntStream.range(0, history.size())
                .mapToObj(i -> toHistoryRow(history.get(i), i + 1))
                .collect(Collectors.toList());
    }

    private CashierHistoryReportRowDTO toHistoryRow(TenantCashierHistoryItemDTO item, int index) {
        return new CashierHistoryReportRowDTO(
                String.valueOf(index),
                nullToDash(item.getReceiptNumber()),
                nullToDash(item.getInvoiceNumber()),
                nullToDash(item.getPatientName()),
                formatMoney(item.getAmount()),
                formatPaymentMethod(item.getMethod()),
                item.getPaidAt() != null ? item.getPaidAt().format(DATE_TIME_FORMAT) : "—",
                nullToDash(item.getCashierName()),
                formatMoney(item.getBalanceAfter()));
    }

    private List<ReportChartRowDTO> buildKpiRows(TenantCashierKpisDTO kpis) {
        return List.of(
                new ReportChartRowDTO("En attente", kpis.getWaitingPayment()),
                new ReportChartRowDTO("Encaissé jour", kpis.getCollectedToday()),
                new ReportChartRowDTO("Partiels", kpis.getPartialPayments()),
                new ReportChartRowDTO("Sorties admin", kpis.getAdminDischargePending()));
    }

    private List<ReportChartRowDTO> buildStatusPieRows(List<TenantCashierQueueItemDTO> queue) {
        if (queue.isEmpty()) {
            return List.of(new ReportChartRowDTO("Aucune facture", 1L));
        }
        Map<String, Long> grouped = queue.stream()
                .collect(Collectors.groupingBy(
                        q -> formatQueueStatus(q.getStatus()),
                        LinkedHashMap::new,
                        Collectors.counting()));
        return grouped.entrySet().stream()
                .map(e -> new ReportChartRowDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private List<ReportChartRowDTO> buildPaymentMethodRows(List<TenantCashierHistoryItemDTO> history) {
        if (history.isEmpty()) {
            return List.of(new ReportChartRowDTO("Aucun paiement", 1L));
        }
        Map<String, Long> grouped = history.stream()
                .collect(Collectors.groupingBy(
                        h -> formatPaymentMethod(h.getMethod()),
                        LinkedHashMap::new,
                        Collectors.counting()));
        return grouped.entrySet().stream()
                .map(e -> new ReportChartRowDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private List<ReportChartRowDTO> buildBalanceBarRows(List<TenantCashierQueueItemDTO> queue) {
        if (queue.isEmpty()) {
            return List.of(new ReportChartRowDTO("Aucun solde", 0L));
        }
        return queue.stream()
                .limit(8)
                .map(q -> new ReportChartRowDTO(
                        truncateLabel(q.getPatientName(), 12),
                        q.getBalanceDue() != null
                                ? q.getBalanceDue().setScale(0, RoundingMode.HALF_UP).longValue()
                                : 0L))
                .collect(Collectors.toList());
    }

    private JasperReport compileSubreport(String jrxmlName) throws Exception {
        InputStream stream = new ClassPathResource("reports/" + jrxmlName).getInputStream();
        return JasperCompileManager.compileReport(stream);
    }

    private String resolveGenerateurLabel() {
        String username = currentUserService.getCurrentUsername();
        if (username != null && !username.isBlank()) {
            return username.trim();
        }
        return "Caissier";
    }

    private static String formatQueueStatus(String status) {
        if (status == null || status.isBlank()) {
            return "En attente";
        }
        return switch (status.trim().toLowerCase(Locale.ROOT)) {
            case "partial" -> "Paiement partiel";
            case "paid" -> "Payé";
            default -> "En attente";
        };
    }

    private static String formatPriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return "Normal";
        }
        return switch (priority.trim().toLowerCase(Locale.ROOT)) {
            case "high" -> "Haute";
            case "low" -> "Basse";
            default -> "Normal";
        };
    }

    private static String formatPaymentMethod(String method) {
        if (method == null || method.isBlank()) {
            return "Espèces";
        }
        return switch (method.trim().toLowerCase(Locale.ROOT)) {
            case "mobile_money" -> "Mobile money";
            case "card" -> "Carte bancaire";
            case "transfer" -> "Virement";
            default -> "Espèces";
        };
    }

    private static String formatMoney(BigDecimal value) {
        if (value == null) {
            return "0 GNF";
        }
        return value.setScale(0, RoundingMode.HALF_UP).toPlainString() + " GNF";
    }

    private static String truncateLabel(String value, int max) {
        if (value == null || value.isBlank()) {
            return "—";
        }
        String trimmed = value.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max - 1) + "…";
    }

    private BufferedImage generateBarcodeSafe(String payload) {
        try {
            return BarcodeService.generateCode128Image(payload, 420, 72);
        } catch (Exception e) {
            return null;
        }
    }

    private InputStream loadLogoInputStream(Hopital hopital) {
        if (hopital == null || hopital.getLogoUrl() == null || hopital.getLogoUrl().trim().isEmpty()) {
            return null;
        }
        String logoPath = hopital.getLogoUrl();
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(logoPath);
            if (is != null) {
                return is;
            }
            File logoFile = new File(logoPath);
            if (logoFile.exists() && logoFile.isFile()) {
                return new FileInputStream(logoFile);
            }
            File resourcesFile = new File("src/main/resources/" + logoPath);
            if (resourcesFile.exists() && resourcesFile.isFile()) {
                return new FileInputStream(resourcesFile);
            }
            return null;
        } catch (FileNotFoundException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String nullToDash(String value) {
        return value == null || value.isBlank() ? "—" : value.trim();
    }
}
