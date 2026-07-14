package hospicloud.servicesImpl;

import hospicloud.dtos.CashierInvoiceDetailDTO;
import hospicloud.dtos.reporting.CashierInvoiceLineRowDTO;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.model.Hopital;
import hospicloud.model.Role;
import hospicloud.repositories.HopitalRepository;
import hospicloud.repositories.TenantCashierRepository;
import hospicloud.security.CurrentUserService;
import hospicloud.security.TenantAccessSupport;
import hospicloud.services.reporting.ReportGenerator;
import hospicloud.utils.QrCodeService;
import hospicloud.utils.TenantReportParamsHelper;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class CashierInvoiceReportService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH);
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.FRENCH);

    private final ReportGenerator reportGenerator;
    private final HopitalRepository hopitalRepository;
    private final TenantCashierRepository tenantCashierRepository;
    private final CurrentUserService currentUserService;

    public CashierInvoiceReportService(
            ReportGenerator reportGenerator,
            HopitalRepository hopitalRepository,
            TenantCashierRepository tenantCashierRepository,
            CurrentUserService currentUserService) {
        this.reportGenerator = reportGenerator;
        this.hopitalRepository = hopitalRepository;
        this.tenantCashierRepository = tenantCashierRepository;
        this.currentUserService = currentUserService;
    }

    public byte[] genererPdf(Integer idFacture) {
        TenantAccessSupport.requirePrincipal(Role.CAISSIER, Role.TENANT_ADMIN);
        Integer hopitalId = TenantAccessSupport.requireHopitalId(Role.CAISSIER, Role.TENANT_ADMIN);
        return genererPdf(idFacture, hopitalId);
    }

    public byte[] genererPdf(Integer idFacture, Integer hopitalId) {
        CashierInvoiceDetailDTO detail = tenantCashierRepository.findInvoiceDetail(idFacture, hopitalId)
                .orElseThrow(() -> new ForbiddenException("Facture introuvable pour votre établissement"));

        List<CashierInvoiceLineRowDTO> lines = tenantCashierRepository.findInvoiceLines(idFacture, hopitalId);
        if (lines.isEmpty()) {
            lines = List.of(new CashierInvoiceLineRowDTO(
                    "1", "Prestations médicales", "1",
                    formatMoney(detail.getTotalTtc()),
                    formatMoney(detail.getTotalTtc()),
                    "Consultation"));
        }

        Hopital hopital = TenantReportParamsHelper.resolveActiveHopital(hopitalRepository, hopitalId);
        String reference = "FAC-H" + hopitalId + "-" + idFacture;
        String qrPayload = "SHAMBUA|FAC|" + hopitalId + "|" + idFacture + "|" + detail.getInvoiceNumber();

        Map<String, Object> params = new HashMap<>();
        TenantReportParamsHelper.applyTenantBranding(params, hopital, hopitalId);
        params.put("LOGO_HOPITAL", loadLogoInputStream(hopital));
        params.put("NUMERO_FACTURE", detail.getInvoiceNumber());
        params.put("DATE_FACTURE", detail.getInvoiceDate() != null
                ? detail.getInvoiceDate().format(DATE_FORMAT) : "—");
        params.put("REFERENCE_FACTURE", reference);
        params.put("DATE_GENERATION", new java.sql.Timestamp(System.currentTimeMillis()));
        params.put("GENERE_PAR", resolveGenerateurLabel());
        params.put("NOM_PATIENT", detail.getPatientName());
        params.put("CODE_PATIENT", nullToDash(detail.getPatientCode()));
        params.put("TELEPHONE_PATIENT", nullToDash(detail.getPatientPhone()));
        params.put("SEXE_PATIENT", formatSexe(detail.getPatientSex()));
        params.put("AGE_PATIENT", detail.getPatientAge() != null
                ? detail.getPatientAge() + " ans" : "—");
        params.put("MONTANT_HT", formatMoney(detail.getTotalHt()));
        params.put("TVA", formatMoney(detail.getTva()));
        params.put("MONTANT_TTC", formatMoney(detail.getTotalTtc()));
        params.put("MONTANT_PAYE", formatMoney(detail.getPaidAmount()));
        params.put("SOLDE", formatMoney(detail.getBalanceDue()));
        params.put("STATUT_PAIEMENT", formatStatut(detail.getPaymentStatus()));
        params.put("QR_CODE_TEXTE", qrPayload);
        params.put("QR_CODE_IMAGE", generateQrSafe(qrPayload));

        try {
            return reportGenerator.generate(
                    "Facture_Patient.jasper",
                    params,
                    new JRBeanCollectionDataSource(lines));
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de générer la facture PDF.", e);
        }
    }

    private String resolveGenerateurLabel() {
        String username = currentUserService.getCurrentUsername();
        return username != null && !username.isBlank() ? username.trim() : "Caisse";
    }

    private static String formatMoney(java.math.BigDecimal value) {
        if (value == null) {
            return "0 GNF";
        }
        return value.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString() + " GNF";
    }

    private static String formatStatut(String statut) {
        if (statut == null) return "En attente";
        return switch (statut.trim().toUpperCase(Locale.ROOT)) {
            case "PAYE" -> "Payée";
            case "PARTIEL" -> "Paiement partiel";
            case "IMPAYE" -> "Impayée";
            default -> statut;
        };
    }

    private static String formatSexe(String sexe) {
        if (sexe == null || sexe.isBlank()) return "—";
        return switch (sexe.trim().toUpperCase(Locale.ROOT)) {
            case "F", "FEMME" -> "Femme";
            case "M", "HOMME" -> "Homme";
            default -> sexe;
        };
    }

    private BufferedImage generateQrSafe(String payload) {
        try {
            return QrCodeService.generateBufferedImage(payload, 180);
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
            if (is != null) return is;
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
        }
    }

    private static String nullToDash(String value) {
        return value == null || value.isBlank() ? "—" : value.trim();
    }
}
