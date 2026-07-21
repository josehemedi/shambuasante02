package hospicloud.servicesImpl;

import hospicloud.dtos.SubscriptionInvoiceDTO;
import hospicloud.dtos.reporting.PlatformInvoiceReportRowDTO;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.model.Role;
import hospicloud.repositories.AbonnementRepository;
import hospicloud.security.CurrentUserService;
import hospicloud.security.TenantAccessSupport;
import hospicloud.services.reporting.ReportGenerator;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

@Service
public class PlatformSubscriptionInvoicesReportService {

    private static final DateTimeFormatter REF_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH);

    private final ReportGenerator reportGenerator;
    private final AbonnementRepository abonnementRepository;
    private final CurrentUserService currentUserService;

    public PlatformSubscriptionInvoicesReportService(
            ReportGenerator reportGenerator,
            AbonnementRepository abonnementRepository,
            CurrentUserService currentUserService) {
        this.reportGenerator = reportGenerator;
        this.abonnementRepository = abonnementRepository;
        this.currentUserService = currentUserService;
    }

    public byte[] genererPdf() {
        TenantAccessSupport.requirePrincipal(Role.SUPER_ADMIN);
        List<SubscriptionInvoiceDTO> invoices = abonnementRepository.listAllPlatformInvoices(5000);

        List<PlatformInvoiceReportRowDTO> rows = IntStream.range(0, invoices.size())
                .mapToObj(i -> toRow(invoices.get(i), i + 1))
                .toList();

        if (rows.isEmpty()) {
            rows = List.of(new PlatformInvoiceReportRowDTO(
                    "1", "—", "—", "—", "0 USD", "—", "—", "Aucune facture"));
        }

        BigDecimal total = invoices.stream()
                .map(SubscriptionInvoiceDTO::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Set<String> hopitaux = new HashSet<>();
        for (SubscriptionInvoiceDTO invoice : invoices) {
            if (invoice.getTenant() != null && !invoice.getTenant().isBlank()) {
                hopitaux.add(invoice.getTenant());
            }
        }

        LocalDateTime now = LocalDateTime.now();
        String reference = "PLT-INV-" + REF_FORMAT.format(now);

        Map<String, Object> params = new HashMap<>();
        params.put("NOM_PLATEFORME", "Shambua Santé — Plateforme");
        params.put("DATE_GENERATION", java.sql.Timestamp.valueOf(now));
        params.put("REFERENCE_RAPPORT", reference);
        params.put("GENERE_PAR", resolveGeneratedBy());
        params.put("NB_FACTURES", String.valueOf(invoices.size()));
        params.put("NB_HOPITAUX", String.valueOf(hopitaux.size()));
        params.put("TOTAL_MONTANT", formatUsd(total));
        params.put("PERIODE_RAPPORT", "Historique complet des factures d'abonnement de tous les hôpitaux");

        try {
            return reportGenerator.generate(
                    "Factures_Abonnements_Plateforme.jasper",
                    params,
                    new JRBeanCollectionDataSource(rows));
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de générer l'export des factures plateforme.", e);
        }
    }

    private PlatformInvoiceReportRowDTO toRow(SubscriptionInvoiceDTO invoice, int index) {
        return new PlatformInvoiceReportRowDTO(
                String.valueOf(index),
                invoice.getTenant() != null ? invoice.getTenant() : "—",
                invoice.getId() != null ? invoice.getId() : "—",
                invoice.getPlan() != null ? invoice.getPlan() : "Starter",
                formatUsd(invoice.getAmount()),
                formatStatut(invoice.getStatus()),
                formatDisplayDate(invoice.getDate()),
                formatDisplayDate(invoice.getDueDate()));
    }

    private String formatUsd(BigDecimal amount) {
        BigDecimal value = amount != null ? amount : BigDecimal.ZERO;
        return String.format(Locale.US, "%,.0f USD", value);
    }

    private String formatStatut(String status) {
        if (status == null) return "—";
        return switch (status.toLowerCase(Locale.ROOT)) {
            case "paid" -> "Payée";
            case "pending" -> "En attente";
            case "overdue" -> "En retard";
            default -> status;
        };
    }

    private String formatDisplayDate(String isoDate) {
        if (isoDate == null || isoDate.isBlank()) return "—";
        try {
            return DISPLAY_DATE.format(java.time.LocalDate.parse(isoDate));
        } catch (Exception ex) {
            return isoDate;
        }
    }

    private String resolveGeneratedBy() {
        try {
            return currentUserService.getCurrentUsername();
        } catch (ForbiddenException ex) {
            return "super-admin";
        }
    }
}
