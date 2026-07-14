package hospicloud.servicesImpl;

import hospicloud.dtos.TenantSubscriptionDTO;
import hospicloud.dtos.TenantSubscriptionHistoryDTO;
import hospicloud.dtos.reporting.SubscriptionPaymentReportRowDTO;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.model.Hopital;
import hospicloud.model.Role;
import hospicloud.repositories.AbonnementRepository;
import hospicloud.repositories.HopitalRepository;
import hospicloud.security.CurrentUserService;
import hospicloud.security.TenantAccessSupport;
import hospicloud.services.reporting.ReportGenerator;
import hospicloud.utils.TenantReportParamsHelper;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;

@Service
public class TenantSubscriptionPaymentReportService {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.FRENCH);
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH);
    private static final DateTimeFormatter REF_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ReportGenerator reportGenerator;
    private final HopitalRepository hopitalRepository;
    private final AbonnementRepository abonnementRepository;
    private final CurrentUserService currentUserService;

    public TenantSubscriptionPaymentReportService(
            ReportGenerator reportGenerator,
            HopitalRepository hopitalRepository,
            AbonnementRepository abonnementRepository,
            CurrentUserService currentUserService) {
        this.reportGenerator = reportGenerator;
        this.hopitalRepository = hopitalRepository;
        this.abonnementRepository = abonnementRepository;
        this.currentUserService = currentUserService;
    }

    public byte[] genererPdf() {
        TenantAccessSupport.requirePrincipal(Role.TENANT_ADMIN);
        Integer hopitalId = TenantAccessSupport.requireHopitalId(Role.TENANT_ADMIN);
        return genererPdf(hopitalId);
    }

    public byte[] genererPdf(Integer hopitalId) {
        Hopital hopital = TenantReportParamsHelper.resolveHopital(hopitalRepository, hopitalId);
        List<TenantSubscriptionHistoryDTO> payments =
                abonnementRepository.findAllSubscriptionPayments(hopitalId);

        TenantSubscriptionDTO current = abonnementRepository.findActiveSubscription(hopitalId).orElse(null);
        String planActuel = current != null && current.getPlanNom() != null ? current.getPlanNom() : "Starter";

        List<SubscriptionPaymentReportRowDTO> rows = IntStream.range(0, payments.size())
                .mapToObj(i -> toRow(payments.get(i), i + 1))
                .toList();

        if (rows.isEmpty()) {
            rows = List.of(new SubscriptionPaymentReportRowDTO(
                    "1", "—", "—", "0 USD", "—", "—", "—", "Aucun paiement enregistré"));
        }

        BigDecimal totalPaye = payments.stream()
                .map(TenantSubscriptionHistoryDTO::getMontantMensuel)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime now = LocalDateTime.now();
        String reference = "ABO-H" + hopitalId + "-" + REF_FORMAT.format(now);

        Map<String, Object> params = new HashMap<>();
        TenantReportParamsHelper.applyTenantBranding(params, hopital, hopitalId);
        params.put("LOGO_HOPITAL", loadLogoInputStream(hopital));
        params.put("DATE_GENERATION", java.sql.Timestamp.valueOf(now));
        params.put("REFERENCE_RAPPORT", reference);
        params.put("GENERE_PAR", resolveGeneratedBy());
        params.put("PLAN_ACTUEL", planActuel);
        params.put("NB_PAIEMENTS", String.valueOf(payments.size()));
        params.put("TOTAL_PAYE", formatUsd(totalPaye));
        params.put("PERIODE_RAPPORT", "Historique complet des paiements d'abonnement SaaS");

        try {
            return reportGenerator.generate(
                    "Abonnements_Paiements.jasper",
                    params,
                    new JRBeanCollectionDataSource(rows));
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de générer le rapport des paiements d'abonnement.", e);
        }
    }

    private SubscriptionPaymentReportRowDTO toRow(TenantSubscriptionHistoryDTO payment, int index) {
        return new SubscriptionPaymentReportRowDTO(
                String.valueOf(index),
                "INV-" + String.format("%04d", payment.getIdAbonnement()),
                payment.getPlanNom() != null ? payment.getPlanNom() : "Starter",
                formatUsd(payment.getMontantMensuel()),
                formatStatut(payment.getStatut()),
                formatDateTime(payment.getDateDebut()),
                formatDate(payment.getDateFin()),
                formatAction(payment.getAction()));
    }

    private String formatUsd(BigDecimal amount) {
        BigDecimal value = amount != null ? amount : BigDecimal.ZERO;
        return String.format(Locale.US, "%,.0f USD", value);
    }

    private String formatDateTime(LocalDateTime value) {
        return value != null ? DATE_TIME_FORMAT.format(value) : "—";
    }

    private String formatDate(LocalDateTime value) {
        return value != null ? DATE_FORMAT.format(value) : "—";
    }

    private String formatStatut(String statut) {
        if (statut == null) return "—";
        return switch (statut.toLowerCase(Locale.ROOT)) {
            case "actif" -> "Payé / Actif";
            case "annule" -> "Annulé";
            case "suspendu" -> "Suspendu";
            default -> statut;
        };
    }

    private String formatAction(String action) {
        if (action == null) return "Paiement";
        return switch (action.toLowerCase(Locale.ROOT)) {
            case "active" -> "Abonnement actif";
            case "renewed" -> "Renouvellement";
            case "suspended" -> "Suspension";
            case "closed" -> "Cycle clôturé";
            default -> "Paiement";
        };
    }

    private String resolveGeneratedBy() {
        try {
            return currentUserService.getCurrentUsername();
        } catch (ForbiddenException ex) {
            return "admin";
        }
    }

    private InputStream loadLogoInputStream(Hopital hopital) {
        if (hopital == null || hopital.getLogoUrl() == null || hopital.getLogoUrl().isBlank()) {
            return null;
        }
        try {
            File file = new File(hopital.getLogoUrl());
            if (file.exists() && file.isFile()) {
                return new FileInputStream(file);
            }
        } catch (FileNotFoundException ignored) {
            return null;
        }
        return null;
    }
}
