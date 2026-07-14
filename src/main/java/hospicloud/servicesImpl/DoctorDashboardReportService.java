package hospicloud.servicesImpl;

import hospicloud.dtos.DashboardDTO;
import hospicloud.dtos.DoctorConsultationActiveDTO;
import hospicloud.dtos.MedecinFileItemDTO;
import hospicloud.dtos.DoctorPendingNoteDTO;
import hospicloud.dtos.RendezVousJourDTO;
import hospicloud.dtos.StatistiqueMedecinDTO;
import hospicloud.dtos.reporting.DoctorDashboardScheduleRowDTO;
import hospicloud.dtos.reporting.ReportChartRowDTO;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.model.Hopital;
import hospicloud.model.Medecin;
import hospicloud.model.Utilisateur;
import hospicloud.repositories.HopitalRepository;
import hospicloud.repositories.MedecinRepository;
import hospicloud.repositories.UtilisateurRepository;
import hospicloud.security.CurrentUserService;
import hospicloud.security.TenantAuthorization;
import hospicloud.services.DashboardService;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DoctorDashboardReportService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.FRENCH);
    private static final DateTimeFormatter REF_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final DashboardService dashboardService;
    private final ReportGenerator reportGenerator;
    private final HopitalRepository hopitalRepository;
    private final CurrentUserService currentUserService;
    private final MedecinRepository medecinRepository;
    private final UtilisateurRepository utilisateurRepository;

    public DoctorDashboardReportService(
            DashboardService dashboardService,
            ReportGenerator reportGenerator,
            HopitalRepository hopitalRepository,
            CurrentUserService currentUserService,
            MedecinRepository medecinRepository,
            UtilisateurRepository utilisateurRepository) {
        this.dashboardService = dashboardService;
        this.reportGenerator = reportGenerator;
        this.hopitalRepository = hopitalRepository;
        this.currentUserService = currentUserService;
        this.medecinRepository = medecinRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    public byte[] genererPdf() {
        Integer idHopital = currentUserService.getCurrentHopitalId();
        Medecin medecin = resolveConnectedMedecin(idHopital);
        String nomMedecin = formatMedecinName(medecin);
        return genererPdf(idHopital, nomMedecin, medecin.getIdMedecin());
    }

    public byte[] genererPdf(Integer idHopital, String nomMedecin, Integer idMedecin) {
        TenantAuthorization.assertSameTenant(idHopital);
        Hopital hopital = TenantReportParamsHelper.resolveActiveHopital(hopitalRepository, idHopital);
        DashboardDTO dashboard = dashboardService.getDashboardData();

        List<DoctorDashboardScheduleRowDTO> scheduleRows = buildScheduleRows(dashboard.getRendezVousDuJour());
        if (scheduleRows.isEmpty()) {
            scheduleRows = List.of(new DoctorDashboardScheduleRowDTO("—", "—", "Aucun rendez-vous aujourd'hui", "—", "—"));
        }

        int rdvCount = sizeOf(dashboard.getRendezVousDuJour());
        int queueCount = sizeOf(dashboard.getFilePatients());
        int activeCount = sizeOf(dashboard.getConsultationsActives());
        int notesCount = sizeOf(dashboard.getNotesEnAttente());

        LocalDateTime now = LocalDateTime.now();
        String reference = "DASH-H" + idHopital + "-M" + (idMedecin != null ? idMedecin : "0") + "-" + now.format(REF_FORMAT);
        String barcodePayload = "SHAMBUA|" + reference;

        Map<String, Object> params = new HashMap<>();
        TenantReportParamsHelper.applyTenantBranding(params, hopital, idHopital);
        params.put("LOGO_HOPITAL", loadLogoInputStream(hopital));
        params.put("NOM_MEDECIN", nullToDash(nomMedecin));
        params.put("ID_MEDECIN", idMedecin != null ? String.valueOf(idMedecin) : "—");
        params.put("DATE_GENERATION", java.sql.Timestamp.valueOf(now));
        params.put("DATE_RAPPORT", now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH)));
        params.put("REFERENCE_RAPPORT", reference);
        params.put("KPI_RDV", String.valueOf(rdvCount));
        params.put("KPI_FILE", String.valueOf(queueCount));
        params.put("KPI_ACTIVES", String.valueOf(activeCount));
        params.put("KPI_NOTES", String.valueOf(notesCount));
        params.put("STATS_CONSULTATIONS", String.valueOf(statValue(dashboard.getStatistiques(), "consultations")));
        params.put("STATS_PATIENTS", String.valueOf(statValue(dashboard.getStatistiques(), "patients")));
        params.put("STATS_EXAMENS", String.valueOf(statValue(dashboard.getStatistiques(), "examens")));
        params.put("FILE_ATTENTE_RESUME", buildQueueResume(dashboard.getFilePatients()));
        params.put("CONSULTATIONS_ACTIVES_RESUME", buildActiveResume(dashboard.getConsultationsActives()));
        params.put("NOTES_EN_ATTENTE_RESUME", buildNotesResume(dashboard.getNotesEnAttente()));
        params.put("KPI_COLUMN_DS", new JRBeanCollectionDataSource(buildKpiRows(rdvCount, queueCount, activeCount, notesCount)));
        params.put("RDV_STATUS_DS", new JRBeanCollectionDataSource(buildRdvStatusRows(dashboard.getRendezVousDuJour())));
        params.put("QUEUE_PRIORITY_DS", new JRBeanCollectionDataSource(buildQueuePriorityRows(dashboard.getFilePatients())));
        params.put("CANAL_DS", new JRBeanCollectionDataSource(buildCanalRows(dashboard.getRendezVousDuJour())));
        params.put("WORKLOAD_BAR_DS", new JRBeanCollectionDataSource(buildWorkloadRows(rdvCount, queueCount, activeCount, notesCount)));
        params.put("CODE_BARRE_TEXTE", barcodePayload);
        params.put("BARCODE_IMAGE", generateBarcodeSafe(barcodePayload));

        try {
            return reportGenerator.generate(
                    "Dashboard_Medecin.jasper",
                    params,
                    new JRBeanCollectionDataSource(scheduleRows));
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de générer le rapport PDF du tableau de bord médecin.", e);
        }
    }

    private List<DoctorDashboardScheduleRowDTO> buildScheduleRows(List<RendezVousJourDTO> rendezVous) {
        if (rendezVous == null || rendezVous.isEmpty()) {
            return List.of();
        }
        return rendezVous.stream()
                .map(rdv -> new DoctorDashboardScheduleRowDTO(
                        rdv.getDateHeureRdv() != null ? rdv.getDateHeureRdv().format(TIME_FORMAT) : "—",
                        nullToDash(rdv.getNomPatient()),
                        nullToDash(rdv.getMotifVisite()),
                        formatStatutRdv(rdv.getStatutRdv()),
                        formatCanal(rdv.getCanal())))
                .collect(Collectors.toList());
    }

    private List<ReportChartRowDTO> buildKpiRows(int rdv, int queue, int active, int notes) {
        return List.of(
                new ReportChartRowDTO("RDV du jour", (long) rdv),
                new ReportChartRowDTO("File d'attente", (long) queue),
                new ReportChartRowDTO("Consult. actives", (long) active),
                new ReportChartRowDTO("Notes en attente", (long) notes));
    }

    private List<ReportChartRowDTO> buildWorkloadRows(int rdv, int queue, int active, int notes) {
        return List.of(
                new ReportChartRowDTO("Agenda", (long) rdv),
                new ReportChartRowDTO("File", (long) queue),
                new ReportChartRowDTO("Actives", (long) active),
                new ReportChartRowDTO("Notes", (long) notes));
    }

    private List<ReportChartRowDTO> buildRdvStatusRows(List<RendezVousJourDTO> rendezVous) {
        if (rendezVous == null || rendezVous.isEmpty()) {
            return List.of(new ReportChartRowDTO("Aucun RDV", 1L));
        }
        Map<String, Long> grouped = rendezVous.stream()
                .collect(Collectors.groupingBy(
                        r -> formatStatutRdv(r.getStatutRdv()),
                        LinkedHashMap::new,
                        Collectors.counting()));
        return grouped.entrySet().stream()
                .map(e -> new ReportChartRowDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private List<ReportChartRowDTO> buildQueuePriorityRows(List<MedecinFileItemDTO> queue) {
        if (queue == null || queue.isEmpty()) {
            return List.of(new ReportChartRowDTO("File vide", 1L));
        }
        Map<String, Long> grouped = queue.stream()
                .collect(Collectors.groupingBy(
                        q -> {
                            String p = q.getPriority();
                            if (p == null || p.isBlank()) {
                                return "Normal";
                            }
                            return switch (p.toLowerCase(Locale.ROOT)) {
                                case "high" -> "Haute";
                                case "low" -> "Basse";
                                default -> "Normal";
                            };
                        },
                        LinkedHashMap::new,
                        Collectors.counting()));
        return grouped.entrySet().stream()
                .map(e -> new ReportChartRowDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private List<ReportChartRowDTO> buildCanalRows(List<RendezVousJourDTO> rendezVous) {
        if (rendezVous == null || rendezVous.isEmpty()) {
            return List.of(new ReportChartRowDTO("Aucun", 1L));
        }
        Map<String, Long> grouped = rendezVous.stream()
                .collect(Collectors.groupingBy(
                        r -> formatCanal(r.getCanal()),
                        LinkedHashMap::new,
                        Collectors.counting()));
        return grouped.entrySet().stream()
                .map(e -> new ReportChartRowDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private String buildQueueResume(List<MedecinFileItemDTO> queue) {
        if (queue == null || queue.isEmpty()) {
            return "Aucun patient en file d'attente.";
        }
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (MedecinFileItemDTO item : queue) {
            if (i > 1) {
                sb.append("\n");
            }
            sb.append(i++).append(". ")
                    .append(nullToDash(item.getPatientName()))
                    .append(" · ")
                    .append(nullToDash(item.getRoom()))
                    .append(" · attente ")
                    .append(nullToDash(item.getWaited()))
                    .append(" · priorité ")
                    .append(nullToDash(item.getPriority()));
        }
        return sb.toString();
    }

    private String buildActiveResume(List<DoctorConsultationActiveDTO> active) {
        if (active == null || active.isEmpty()) {
            return "Aucune consultation active.";
        }
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (DoctorConsultationActiveDTO item : active) {
            if (i > 1) {
                sb.append("\n");
            }
            sb.append(i++).append(". ")
                    .append(nullToDash(item.getPatientName()))
                    .append(" · ")
                    .append(nullToDash(item.getMotif()))
                    .append(" · ")
                    .append(formatCanal(item.getCanal()));
            if (item.getStartedAt() != null) {
                sb.append(" · début ").append(item.getStartedAt().format(TIME_FORMAT));
            }
        }
        return sb.toString();
    }

    private String buildNotesResume(List<DoctorPendingNoteDTO> notes) {
        if (notes == null || notes.isEmpty()) {
            return "Toutes les notes cliniques sont à jour.";
        }
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (DoctorPendingNoteDTO item : notes) {
            if (i > 1) {
                sb.append("\n");
            }
            sb.append(i++).append(". ")
                    .append(nullToDash(item.getPatientName()))
                    .append(" · ")
                    .append(nullToDash(item.getMotif()));
            if (item.getConsultationDate() != null) {
                sb.append(" · ").append(item.getConsultationDate().format(DATE_TIME_FORMAT));
            }
        }
        return sb.toString();
    }

    private long statValue(StatistiqueMedecinDTO stats, String key) {
        if (stats == null) {
            return 0L;
        }
        return switch (key) {
            case "consultations" -> stats.getConsultationsAujourdhui();
            case "patients" -> stats.getPatientsTotal();
            case "examens" -> stats.getExamensEnAttente();
            default -> 0L;
        };
    }

    private BufferedImage generateBarcodeSafe(String payload) {
        try {
            return BarcodeService.generateCode128Image(payload, 420, 72);
        } catch (Exception e) {
            return null;
        }
    }

    private String formatStatutRdv(String statut) {
        if (statut == null || statut.isBlank()) {
            return "Programmé";
        }
        return switch (statut.toUpperCase(Locale.ROOT)) {
            case "CONFIRME", "CONFIRMÉ" -> "Confirmé";
            case "ANNULE", "ANNULÉ" -> "Annulé";
            case "TERMINE", "TERMINÉ" -> "Terminé";
            case "EN_COURS" -> "En cours";
            default -> statut.trim();
        };
    }

    private String formatCanal(String canal) {
        if (canal == null || canal.isBlank()) {
            return "Présentiel";
        }
        return "TELECONSULTATION".equalsIgnoreCase(canal.trim()) ? "Téléconsultation" : "Présentiel";
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

    private Medecin resolveConnectedMedecin(Integer idHopital) {
        Integer medecinId = currentUserService.getCurrentMedecinId();
        if (medecinId == null) {
            String email = currentUserService.getCurrentUsername();
            if (email != null && !email.isBlank()) {
                medecinId = utilisateurRepository.findByEmail(email)
                        .map(Utilisateur::getIdMedecin)
                        .orElseGet(() -> utilisateurRepository.findByEmailAnyStatus(email)
                                .map(Utilisateur::getIdMedecin)
                                .orElse(null));
            }
        }
        if (medecinId == null) {
            throw new ForbiddenException(
                    "Aucun profil médecin n'est associé à votre compte.");
        }
        return medecinRepository.trouverParId(medecinId)
                .filter(m -> idHopital.equals(m.getIdHopital()))
                .orElseThrow(() -> new ForbiddenException(
                        "Médecin introuvable dans votre établissement (tenant " + idHopital + ")."));
    }

    private String formatMedecinName(Medecin medecin) {
        if (medecin == null) {
            return "Médecin";
        }
        String nom = ((medecin.getPrenom() != null ? medecin.getPrenom().trim() : "")
                + " "
                + (medecin.getNom() != null ? medecin.getNom().trim() : "")).trim();
        if (nom.isBlank()) {
            return "Dr";
        }
        return nom.startsWith("Dr") ? nom : "Dr " + nom;
    }
}
