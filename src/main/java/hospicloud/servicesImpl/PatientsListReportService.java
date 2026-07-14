package hospicloud.servicesImpl;

import hospicloud.dtos.reporting.PatientListReportRowDTO;
import hospicloud.dtos.reporting.ReportChartRowDTO;
import hospicloud.model.Hopital;
import hospicloud.model.Patient;
import hospicloud.repositories.HopitalRepository;
import hospicloud.security.CurrentUserService;
import hospicloud.security.TenantAuthorization;
import hospicloud.services.PatientService;
import hospicloud.services.reporting.ReportGenerator;
import hospicloud.utils.BarcodeService;
import hospicloud.utils.TenantReportParamsHelper;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class PatientsListReportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH);
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.FRENCH);
    private static final DateTimeFormatter REF_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ReportGenerator reportGenerator;
    private final HopitalRepository hopitalRepository;
    private final PatientService patientService;
    private final CurrentUserService currentUserService;

    public PatientsListReportService(
            ReportGenerator reportGenerator,
            HopitalRepository hopitalRepository,
            PatientService patientService,
            CurrentUserService currentUserService) {
        this.reportGenerator = reportGenerator;
        this.hopitalRepository = hopitalRepository;
        this.patientService = patientService;
        this.currentUserService = currentUserService;
    }

    public byte[] genererPdf(Boolean mine) {
        Integer idHopital = currentUserService.getCurrentHopitalId();
        TenantAuthorization.assertSameTenant(idHopital);
        Hopital hopital = TenantReportParamsHelper.resolveActiveHopital(hopitalRepository, idHopital);

        List<Patient> patients = patientService.trouverTousLesPatients(mine);
        List<PatientListReportRowDTO> rows = buildRows(patients);
        if (rows.isEmpty()) {
            rows = List.of(new PatientListReportRowDTO(
                    "—", "—", "Aucun patient enregistré", "—", "—", "—", "—", "—", "—", "—", "—", "—", "—", "—"));
        }

        int total = patients.size();
        int active = (int) patients.stream().filter(Patient::isEstActif).count();
        int admitted = (int) patients.stream()
                .filter(p -> "ADMIS".equalsIgnoreCase(nullToEmpty(p.getStatutClinique())))
                .count();
        int outpatient = (int) patients.stream()
                .filter(p -> {
                    String s = p.getStatutClinique();
                    return s == null || s.isBlank() || "AMBULATOIRE".equalsIgnoreCase(s);
                })
                .count();

        LocalDateTime now = LocalDateTime.now();
        String reference = "PAT-LIST-H" + idHopital + "-" + now.format(REF_FORMAT);
        String barcodePayload = "SHAMBUA|" + reference;
        String generePar = resolveGenerateurLabel();

        Map<String, Object> params = new HashMap<>();
        TenantReportParamsHelper.applyTenantBranding(params, hopital, idHopital);
        params.put("LOGO_HOPITAL", loadLogoInputStream(hopital));
        params.put("DATE_GENERATION", java.sql.Timestamp.valueOf(now));
        params.put("DATE_RAPPORT", now.format(DATE_FORMAT));
        params.put("REFERENCE_RAPPORT", reference);
        params.put("GENERE_PAR", generePar);
        params.put("PERIMETRE_RAPPORT", buildPerimetreLabel(mine));
        params.put("KPI_TOTAL", String.valueOf(total));
        params.put("KPI_ACTIFS", String.valueOf(active));
        params.put("KPI_ADMIS", String.valueOf(admitted));
        params.put("KPI_AMBULATOIRES", String.valueOf(outpatient));
        params.put("KPI_COLUMN_DS", new JRBeanCollectionDataSource(buildKpiRows(total, active, admitted, outpatient)));
        params.put("STATUT_CLINIQUE_DS", new JRBeanCollectionDataSource(buildClinicalStatusRows(patients)));
        params.put("ACTIF_DS", new JRBeanCollectionDataSource(buildActiveRows(active, total - active)));
        params.put("SEXE_DS", new JRBeanCollectionDataSource(buildSexeRows(patients)));
        params.put("CODE_BARRE_TEXTE", barcodePayload);
        params.put("BARCODE_IMAGE", generateBarcodeSafe(barcodePayload));

        try {
            return reportGenerator.generate(
                    "Liste_Patients.jasper",
                    params,
                    new JRBeanCollectionDataSource(rows));
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de générer le rapport PDF de la liste des patients.", e);
        }
    }

    private List<PatientListReportRowDTO> buildRows(List<Patient> patients) {
        if (patients == null || patients.isEmpty()) {
            return List.of();
        }
        return IntStream.range(0, patients.size())
                .mapToObj(i -> toRow(patients.get(i), i + 1))
                .collect(Collectors.toList());
    }

    private PatientListReportRowDTO toRow(Patient patient, int index) {
        return new PatientListReportRowDTO(
                String.valueOf(index),
                nullToDash(patient.getCodePatient()),
                formatNomComplet(patient),
                formatSexe(patient.getSexe()),
                formatDate(patient.getDateNaissance()),
                formatAge(patient.getDateNaissance()),
                nullToDash(patient.getTelephone()),
                nullToDash(patient.getEmail()),
                nullToDash(patient.getGroupeSanguin()),
                patient.isEstActif() ? "Actif" : "Inactif",
                formatStatutClinique(patient.getStatutClinique()),
                formatDateTime(patient.getDateEnregistrement()),
                nullToDash(patient.getProfession()),
                nullToDash(patient.getNumeroMatricule()));
    }

    private List<ReportChartRowDTO> buildKpiRows(int total, int active, int admitted, int outpatient) {
        return List.of(
                new ReportChartRowDTO("Total", (long) total),
                new ReportChartRowDTO("Actifs", (long) active),
                new ReportChartRowDTO("Admis", (long) admitted),
                new ReportChartRowDTO("Ambulatoires", (long) outpatient));
    }

    private List<ReportChartRowDTO> buildClinicalStatusRows(List<Patient> patients) {
        if (patients == null || patients.isEmpty()) {
            return List.of(new ReportChartRowDTO("Aucun", 1L));
        }
        Map<String, Long> grouped = patients.stream()
                .collect(Collectors.groupingBy(
                        p -> formatStatutClinique(p.getStatutClinique()),
                        LinkedHashMap::new,
                        Collectors.counting()));
        return grouped.entrySet().stream()
                .map(e -> new ReportChartRowDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private List<ReportChartRowDTO> buildActiveRows(int active, int inactive) {
        return List.of(
                new ReportChartRowDTO("Actifs", (long) active),
                new ReportChartRowDTO("Inactifs", (long) inactive));
    }

    private List<ReportChartRowDTO> buildSexeRows(List<Patient> patients) {
        if (patients == null || patients.isEmpty()) {
            return List.of(new ReportChartRowDTO("Non renseigné", 1L));
        }
        Map<String, Long> grouped = patients.stream()
                .collect(Collectors.groupingBy(
                        p -> formatSexe(p.getSexe()),
                        LinkedHashMap::new,
                        Collectors.counting()));
        return grouped.entrySet().stream()
                .map(e -> new ReportChartRowDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private String buildPerimetreLabel(Boolean mine) {
        if (currentUserService.isMedecin()) {
            return "Patients suivis par le médecin connecté";
        }
        if (Boolean.TRUE.equals(mine)) {
            return "Patients enregistrés par l'utilisateur connecté";
        }
        return "Tous les patients de l'établissement";
    }

    private String resolveGenerateurLabel() {
        String username = currentUserService.getCurrentUsername();
        if (username != null && !username.isBlank()) {
            return username.trim();
        }
        if (currentUserService.isMedecin()) {
            return "Médecin";
        }
        return "Personnel de l'établissement";
    }

    private static String formatNomComplet(Patient patient) {
        if (patient == null) {
            return "—";
        }
        String nom = ((patient.getPrenom() != null ? patient.getPrenom().trim() : "")
                + " "
                + (patient.getNom() != null ? patient.getNom().trim() : "")).trim();
        return nom.isBlank() ? "—" : nom;
    }

    private static String formatSexe(String sexe) {
        if (sexe == null || sexe.isBlank()) {
            return "—";
        }
        return switch (sexe.trim().toUpperCase(Locale.ROOT)) {
            case "M" -> "Homme";
            case "F" -> "Femme";
            default -> sexe.trim();
        };
    }

    private static String formatStatutClinique(String statut) {
        if (statut == null || statut.isBlank()) {
            return "Ambulatoire";
        }
        return switch (statut.trim().toUpperCase(Locale.ROOT)) {
            case "ADMIS" -> "Admis";
            case "SORTIE_AUTORISEE" -> "Sortie autorisée";
            case "SORTI" -> "Sorti";
            case "AMBULATOIRE" -> "Ambulatoire";
            default -> statut.trim();
        };
    }

    private static String formatAge(LocalDate dateNaissance) {
        if (dateNaissance == null) {
            return "—";
        }
        int years = Period.between(dateNaissance, LocalDate.now()).getYears();
        return years + " ans";
    }

    private static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : "—";
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMAT) : "—";
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

    private static String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
