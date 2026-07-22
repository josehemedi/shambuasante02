package hospicloud.servicesImpl;

import hospicloud.exceptions.BadRequestException;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.model.Hopital;
import hospicloud.model.Medecin;
import hospicloud.model.Patient;
import hospicloud.model.Role;
import hospicloud.model.reception.Admission;
import hospicloud.repositories.HopitalRepository;
import hospicloud.repositories.MedecinRepository;
import hospicloud.repositories.PatientRepository;
import hospicloud.repositories.ReceptionDashboardRepository;
import hospicloud.repositories.RendezVousRepository;
import hospicloud.security.CurrentUserService;
import hospicloud.security.TenantAccessSupport;
import hospicloud.security.TenantContext;
import hospicloud.services.reporting.ReportGenerator;
import hospicloud.utils.QrCodeService;
import hospicloud.utils.TenantReportParamsHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class ReceptionTicketReportService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH);
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm", Locale.FRENCH);
    private static final DateTimeFormatter REF_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ReportGenerator reportGenerator;
    private final ReceptionDashboardRepository receptionDashboardRepository;
    private final HopitalRepository hopitalRepository;
    private final PatientRepository patientRepository;
    private final MedecinRepository medecinRepository;
    private final RendezVousRepository rendezVousRepository;
    private final CurrentUserService currentUserService;

    public ReceptionTicketReportService(
            ReportGenerator reportGenerator,
            ReceptionDashboardRepository receptionDashboardRepository,
            HopitalRepository hopitalRepository,
            PatientRepository patientRepository,
            MedecinRepository medecinRepository,
            RendezVousRepository rendezVousRepository,
            CurrentUserService currentUserService) {
        this.reportGenerator = reportGenerator;
        this.receptionDashboardRepository = receptionDashboardRepository;
        this.hopitalRepository = hopitalRepository;
        this.patientRepository = patientRepository;
        this.medecinRepository = medecinRepository;
        this.rendezVousRepository = rendezVousRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public byte[] genererPdf(Integer idAdmission) {
        TenantAccessSupport.requirePrincipal(Role.RECEPTION, Role.TENANT_ADMIN);
        Integer hopitalId = TenantContext.getRequiredHopitalId();
        return genererPdf(idAdmission, hopitalId);
    }

    @Transactional(readOnly = true)
    public byte[] genererPdf(Integer idAdmission, Integer hopitalId) {
        if (idAdmission == null) {
            throw new BadRequestException("Identifiant d'admission requis pour le ticket.");
        }
        Admission admission = receptionDashboardRepository.trouverAdmissionParId(idAdmission, hopitalId);
        if (admission == null) {
            throw new ForbiddenException("Admission introuvable pour votre établissement.");
        }

        Hopital hopital = TenantReportParamsHelper.resolveHopital(hopitalRepository, hopitalId);

        String nomPatient = "Patient";
        String codePatient = "";
        if (admission.getIdPatient() != null) {
            Patient patient = patientRepository.trouverPatientParId(admission.getIdPatient().longValue()).orElse(null);
            if (patient != null) {
                nomPatient = ((patient.getPrenom() != null ? patient.getPrenom() : "") + " "
                        + (patient.getNom() != null ? patient.getNom() : "")).trim();
                if (nomPatient.isBlank()) nomPatient = "Patient";
                codePatient = patient.getCodePatient() != null ? patient.getCodePatient() : "";
            }
        }

        String nomMedecin = "—";
        String specialite = "";
        if (admission.getIdMedecin() != null) {
            Medecin medecin = medecinRepository.trouverParId(admission.getIdMedecin()).orElse(null);
            if (medecin != null) {
                nomMedecin = ("Dr " + (medecin.getPrenom() != null ? medecin.getPrenom() : "") + " "
                        + (medecin.getNom() != null ? medecin.getNom() : "")).trim();
                specialite = medecin.getSpecialite() != null ? medecin.getSpecialite() : "";
            }
        }

        String motif = "Consultation / accueil";
        String service = specialite;
        if (admission.getIdRendezVous() != null) {
            try {
                var rdv = rendezVousRepository.trouverParId(admission.getIdRendezVous());
                if (rdv != null && StringUtils.hasText(rdv.getMotifVisite())) {
                    motif = rdv.getMotifVisite().trim();
                    if (motif.contains("— Service :")) {
                        String[] parts = motif.split("— Service :", 2);
                        motif = parts[0].trim();
                        if (parts.length > 1 && StringUtils.hasText(parts[1])) {
                            service = parts[1].trim();
                        }
                    }
                }
            } catch (Exception ignored) {
                // motif par défaut
            }
        }

        Integer numero = admission.getNumeroPassage();
        if (numero == null) {
            numero = receptionDashboardRepository.allouerNumeroPassage(idAdmission, hopitalId);
        }
        String numeroFmt = numero != null ? String.format("%03d", numero) : "—";

        LocalDateTime arrivee = admission.getTempsArrivee() != null
                ? admission.getTempsArrivee()
                : LocalDateTime.now();
        LocalDateTime now = LocalDateTime.now();
        String reference = "TKT-H" + hopitalId + "-A" + idAdmission + "-" + REF_FORMAT.format(now);

        String qrPayload = "SHAMBUA|TICKET|" + hopitalId + "|" + idAdmission + "|" + numeroFmt;

        Map<String, Object> params = new HashMap<>();
        TenantReportParamsHelper.applyTenantBranding(params, hopital, hopitalId);
        params.put("LOGO_HOPITAL", loadLogoInputStream(hopital));
        params.put("DATE_GENERATION", java.sql.Timestamp.valueOf(now));
        params.put("GENERE_PAR", resolveGeneratedBy());
        params.put("REFERENCE_TICKET", reference);
        params.put("NUMERO_PASSAGE", numeroFmt);
        params.put("NOM_PATIENT", nomPatient);
        params.put("CODE_PATIENT", codePatient);
        params.put("NOM_MEDECIN", nomMedecin);
        params.put("SPECIALITE", specialite);
        params.put("SERVICE", service);
        params.put("MOTIF_VISITE", motif);
        params.put("PRIORITE", formatPriorite(admission.getNiveauPriorite()));
        params.put("STATUT", formatStatut(admission.getStatut()));
        params.put("HEURE_ARRIVEE", TIME_FORMAT.format(arrivee));
        params.put("DATE_JOUR", DATE_FORMAT.format(arrivee.toLocalDate()));
        params.put("QR_CODE_IMAGE", generateQrSafe(qrPayload));

        try {
            return reportGenerator.generate("Ticket_Passage_Accueil.jasper", params, null);
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de générer le ticket de passage JasperReports.", e);
        }
    }

    private String formatPriorite(Integer niveau) {
        if (niveau == null) return "Priorité normale";
        return switch (niveau) {
            case 1 -> "URGENCE";
            case 2 -> "Priorité haute";
            default -> "Priorité normale";
        };
    }

    private String formatStatut(String statut) {
        if (statut == null) return "En attente";
        return switch (statut.toUpperCase(Locale.ROOT)) {
            case "ATTENTE_TRIAGE" -> "En attente de triage";
            case "ORIENTE" -> "Orienté";
            case "ENREGISTRE" -> "Reçu";
            case "APPELE" -> "Appelé";
            case "EN_CONSULTATION" -> "En consultation";
            case "ABSENT" -> "Absent";
            case "TERMINE" -> "Terminé";
            default -> "En attente";
        };
    }

    private String resolveGeneratedBy() {
        try {
            return currentUserService.getCurrentUsername();
        } catch (Exception ex) {
            return "accueil";
        }
    }

    private BufferedImage generateQrSafe(String payload) {
        try {
            return QrCodeService.generateBufferedImage(payload, 160);
        } catch (Exception e) {
            return null;
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
