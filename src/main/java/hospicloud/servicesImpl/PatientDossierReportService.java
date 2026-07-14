package hospicloud.servicesImpl;

import hospicloud.dtos.ConsultationResponseDTO;
import hospicloud.dtos.PatientDossierDTO;
import hospicloud.dtos.reporting.DossierConsultationRowDTO;
import hospicloud.dtos.reporting.ReportChartRowDTO;
import hospicloud.dtos.reporting.ReportNumericChartRowDTO;
import hospicloud.exceptions.patient.PatientNotFoundException;
import hospicloud.model.Antecedent;
import hospicloud.model.Hopital;
import hospicloud.model.Patient;
import hospicloud.model.RendezVous;
import hospicloud.repositories.HopitalRepository;
import hospicloud.security.TenantAuthorization;
import hospicloud.services.reporting.ReportGenerator;
import hospicloud.utils.TenantReportParamsHelper;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PatientDossierReportService {

    private static final DateTimeFormatter CONSULTATION_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter SHORT_DATE_LABEL =
            DateTimeFormatter.ofPattern("dd/MM/yy");

    private final ReportGenerator reportGenerator;
    private final HopitalRepository hopitalRepository;

    public PatientDossierReportService(
            ReportGenerator reportGenerator,
            HopitalRepository hopitalRepository) {
        this.reportGenerator = reportGenerator;
        this.hopitalRepository = hopitalRepository;
    }

    public byte[] genererPdf(PatientDossierDTO dossier) {
        if (dossier == null || dossier.getPatient() == null) {
            throw new PatientNotFoundException(0);
        }

        Patient patient = dossier.getPatient();
        TenantAuthorization.assertSameTenant(patient.getIdHopital());

        Hopital hopital = TenantReportParamsHelper.resolveActiveHopital(hopitalRepository, patient.getIdHopital());
        PatientDossierDTO dossierTenant = filtrerDonneesParTenant(dossier, patient.getIdHopital());

        List<DossierConsultationRowDTO> consultationRows = buildConsultationRows(dossierTenant.getConsultations());
        if (consultationRows.isEmpty()) {
            consultationRows = List.of(new DossierConsultationRowDTO("—", "—", "Aucune consultation enregistrée", "—"));
        }

        Map<String, Object> params = new HashMap<>();
        TenantReportParamsHelper.applyTenantBranding(params, hopital, patient.getIdHopital());
        params.put("LOGO_HOPITAL", loadLogoInputStream(hopital));
        params.put("NOM_PATIENT", formatNomPatient(patient));
        params.put("CODE_PATIENT", nullToDash(patient.getCodePatient()));
        params.put("AGE_PATIENT", formatAgePatient(patient.getDateNaissance()));
        params.put("SEXE_PATIENT", nullToDash(patient.getSexe()));
        params.put("GROUPE_SANGUIN", nullToDash(patient.getGroupeSanguin()));
        params.put("TELEPHONE_PATIENT", nullToDash(patient.getTelephone()));
        params.put("EMAIL_PATIENT", nullToDash(patient.getEmail()));
        params.put("DATE_GENERATION", java.sql.Timestamp.valueOf(LocalDateTime.now()));
        params.put("NB_CONSULTATIONS", String.valueOf(sizeOf(dossierTenant.getConsultations())));
        params.put("NB_ANTECEDENTS", String.valueOf(sizeOf(dossierTenant.getAntecedents())));
        params.put("NB_RDV", String.valueOf(sizeOf(dossierTenant.getRendezVous())));
        params.put("ANTECEDENT_CHART_DS", new JRBeanCollectionDataSource(buildAntecedentChartRows(dossierTenant.getAntecedents())));
        params.put("CONSULTATIONS_BAR_DS", new JRBeanCollectionDataSource(buildConsultationBarRows(dossierTenant.getConsultations())));
        params.put("CONSULTATIONS_COLUMN_DS", new JRBeanCollectionDataSource(buildConsultationBarRows(dossierTenant.getConsultations())));
        params.put("KPI_COLUMN_DS", new JRBeanCollectionDataSource(buildKpiColumnRows(dossierTenant)));
        params.put("RDV_STATUS_DS", new JRBeanCollectionDataSource(buildRdvStatusChartRows(dossierTenant.getRendezVous())));
        params.put("POIDS_CHART_DS", new JRBeanCollectionDataSource(
                buildVitalNumericRows(dossierTenant.getConsultations(), c ->
                        c.getPoids() != null ? c.getPoids().doubleValue() : null, "Poids (kg)")));
        params.put("TEMPERATURE_CHART_DS", new JRBeanCollectionDataSource(
                buildVitalNumericRows(dossierTenant.getConsultations(), c ->
                        c.getTemperature() != null ? c.getTemperature().doubleValue() : null, "Température (°C)")));
        params.put("FREQUENCE_CHART_DS", new JRBeanCollectionDataSource(
                buildVitalNumericRows(dossierTenant.getConsultations(), c ->
                        c.getFrequenceCardiaque() != null ? c.getFrequenceCardiaque().doubleValue() : null,
                        "Fréq. cardiaque (bpm)")));

        try {
            return reportGenerator.generate(
                    "Dossier_Patient.jasper",
                    params,
                    new JRBeanCollectionDataSource(consultationRows));
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de générer le dossier médical PDF.", e);
        }
    }

    private PatientDossierDTO filtrerDonneesParTenant(PatientDossierDTO dossier, Integer idHopital) {
        PatientDossierDTO filtre = new PatientDossierDTO();
        filtre.setPatient(dossier.getPatient());

        if (dossier.getConsultations() != null) {
            filtre.setConsultations(dossier.getConsultations().stream()
                    .filter(c -> c.getIdHopital() == null || idHopital.equals(c.getIdHopital()))
                    .collect(Collectors.toList()));
        }
        if (dossier.getAntecedents() != null) {
            filtre.setAntecedents(dossier.getAntecedents().stream()
                    .filter(a -> a.getIdHopital() == null || idHopital.equals(a.getIdHopital()))
                    .collect(Collectors.toList()));
        }
        if (dossier.getRendezVous() != null) {
            filtre.setRendezVous(dossier.getRendezVous().stream()
                    .filter(r -> r.getIdHopital() == null || idHopital.equals(r.getIdHopital()))
                    .collect(Collectors.toList()));
        }
        return filtre;
    }

    private List<DossierConsultationRowDTO> buildConsultationRows(List<ConsultationResponseDTO> consultations) {
        if (consultations == null || consultations.isEmpty()) {
            return List.of();
        }
        return consultations.stream()
                .map(c -> new DossierConsultationRowDTO(
                        nullToDash(c.getDateConsultation()),
                        nullToDash(c.getMotifVisite()),
                        nullToDash(c.getDiagnostic()),
                        c.getNomMedecin() != null && !c.getNomMedecin().isBlank()
                                ? "Dr " + c.getNomMedecin().trim()
                                : "—"))
                .collect(Collectors.toList());
    }

    private List<ReportChartRowDTO> buildAntecedentChartRows(List<Antecedent> antecedents) {
        if (antecedents == null || antecedents.isEmpty()) {
            return List.of(new ReportChartRowDTO("Aucun antécédent", 1L));
        }

        Map<String, Long> grouped = antecedents.stream()
                .collect(Collectors.groupingBy(
                        a -> {
                            String type = a.getTypeAntecedent();
                            return type == null || type.isBlank() ? "Autre" : type.trim();
                        },
                        Collectors.counting()));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new ReportChartRowDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private List<ReportChartRowDTO> buildConsultationBarRows(List<ConsultationResponseDTO> consultations) {
        if (consultations == null || consultations.isEmpty()) {
            return List.of(new ReportChartRowDTO("Aucune", 0L));
        }

        Map<String, Long> grouped = new LinkedHashMap<>();
        List<LocalDateTime> parsedDates = new ArrayList<>();

        for (ConsultationResponseDTO consultation : consultations) {
            LocalDateTime date = parseConsultationDate(consultation.getDateConsultation());
            if (date != null) {
                parsedDates.add(date);
            }
        }

        parsedDates.stream()
                .sorted(Comparator.reverseOrder())
                .limit(12)
                .sorted()
                .forEach(date -> {
                    String key = date.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.FRENCH));
                    grouped.merge(key, 1L, Long::sum);
                });

        if (grouped.isEmpty()) {
            return List.of(new ReportChartRowDTO("Non daté", (long) consultations.size()));
        }

        return grouped.entrySet().stream()
                .map(e -> new ReportChartRowDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private List<ReportNumericChartRowDTO> buildVitalNumericRows(
            List<ConsultationResponseDTO> consultations,
            java.util.function.Function<ConsultationResponseDTO, Double> valueExtractor,
            String emptyLabel) {

        if (consultations == null || consultations.isEmpty()) {
            return List.of(new ReportNumericChartRowDTO("Aucune consultation", 0.0));
        }

        record DatedVital(LocalDateTime date, String label, Double value) {
        }

        List<DatedVital> points = new ArrayList<>();
        for (ConsultationResponseDTO consultation : consultations) {
            Double value = valueExtractor.apply(consultation);
            if (value == null) {
                continue;
            }
            LocalDateTime date = parseConsultationDate(consultation.getDateConsultation());
            String label = date != null
                    ? date.format(SHORT_DATE_LABEL)
                    : "Consultation";
            points.add(new DatedVital(date, label, value));
        }

        if (points.isEmpty()) {
            return List.of(new ReportNumericChartRowDTO("Aucune " + emptyLabel.toLowerCase(), 0.0));
        }

        points.sort(Comparator.comparing(
                DatedVital::date,
                Comparator.nullsLast(Comparator.naturalOrder())));

        if (points.size() > 10) {
            points = points.subList(points.size() - 10, points.size());
        }

        return points.stream()
                .map(p -> new ReportNumericChartRowDTO(p.label(), p.value()))
                .collect(Collectors.toList());
    }

    private List<ReportChartRowDTO> buildKpiColumnRows(PatientDossierDTO dossier) {
        return List.of(
                new ReportChartRowDTO("Consultations", (long) sizeOf(dossier.getConsultations())),
                new ReportChartRowDTO("Antécédents", (long) sizeOf(dossier.getAntecedents())),
                new ReportChartRowDTO("Rendez-vous", (long) sizeOf(dossier.getRendezVous())));
    }

    private List<ReportChartRowDTO> buildRdvStatusChartRows(List<RendezVous> rendezVous) {
        if (rendezVous == null || rendezVous.isEmpty()) {
            return List.of(new ReportChartRowDTO("Aucun RDV", 1L));
        }
        Map<String, Long> grouped = rendezVous.stream()
                .collect(Collectors.groupingBy(
                        r -> {
                            String statut = r.getStatutRdv();
                            if (statut == null || statut.isBlank()) {
                                return "Programmé";
                            }
                            return switch (statut.toUpperCase()) {
                                case "CONFIRME", "CONFIRMÉ" -> "Confirmé";
                                case "ANNULE", "ANNULÉ" -> "Annulé";
                                case "TERMINE", "TERMINÉ" -> "Terminé";
                                case "EN_COURS" -> "En cours";
                                default -> statut.trim();
                            };
                        },
                        Collectors.counting()));
        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new ReportChartRowDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private LocalDateTime parseConsultationDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(raw.trim(), CONSULTATION_DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private String formatNomPatient(Patient patient) {
        String nom = ((patient.getPrenom() != null ? patient.getPrenom().trim() : "")
                + " "
                + (patient.getNom() != null ? patient.getNom().trim() : "")).trim();
        return nom.isBlank() ? "Patient" : nom;
    }

    private String formatAgePatient(LocalDate dateNaissance) {
        if (dateNaissance == null) {
            return "—";
        }
        return Period.between(dateNaissance, LocalDate.now()).getYears() + " ans";
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

    private static int sizeOf(List<?> list) {
        return list == null ? 0 : list.size();
    }

    private static String nullToDash(String value) {
        return value == null || value.isBlank() ? "—" : value.trim();
    }
}
