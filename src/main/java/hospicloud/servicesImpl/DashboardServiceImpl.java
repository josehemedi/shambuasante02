package hospicloud.servicesImpl;

import hospicloud.dtos.DashboardDTO;
import hospicloud.dtos.DashboardStatsDTO;
import hospicloud.dtos.DoctorWorkspaceDTO;
import hospicloud.dtos.MrrSeriesPointDTO;
import hospicloud.dtos.PlanDistributionItemDTO;
import hospicloud.dtos.TenantOverviewDTO;
import hospicloud.dtos.RendezVousJourDTO;
import hospicloud.dtos.StatistiqueMedecinDTO;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.model.Medecin;
import hospicloud.model.RendezVous;
import hospicloud.model.Utilisateur;
import hospicloud.repositories.AbonnementRepository;
import hospicloud.repositories.DoctorDashboardRepository;
import hospicloud.repositories.DoctorWorkspaceRepository;
import hospicloud.repositories.HopitalRepository;
import hospicloud.repositories.LogsActiviteRepository;
import hospicloud.repositories.MedecinFileAttenteRepository;
import hospicloud.repositories.MedecinRepository;
import hospicloud.repositories.RendezVousRepository;
import hospicloud.repositories.UtilisateurRepository;
import hospicloud.security.CurrentUserService;
import hospicloud.services.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);

    private final MedecinRepository medecinRepository;
    private final RendezVousRepository rendezVousRepository;
    private final HopitalRepository hopitalRepository;
    private final AbonnementRepository abonnementRepository;
    private final LogsActiviteRepository logsActiviteRepository;
    private final CurrentUserService currentUserService;
    private final DoctorDashboardRepository doctorDashboardRepository;
    private final DoctorWorkspaceRepository doctorWorkspaceRepository;
    private final MedecinFileAttenteRepository medecinFileAttenteRepository;
    private final UtilisateurRepository utilisateurRepository;

    public DashboardServiceImpl(MedecinRepository medecinRepository,
            RendezVousRepository rendezVousRepository,
            HopitalRepository hopitalRepository,
            AbonnementRepository abonnementRepository,
            LogsActiviteRepository logsActiviteRepository,
            CurrentUserService currentUserService,
            DoctorDashboardRepository doctorDashboardRepository,
            DoctorWorkspaceRepository doctorWorkspaceRepository,
            MedecinFileAttenteRepository medecinFileAttenteRepository,
            UtilisateurRepository utilisateurRepository) {
        this.medecinRepository = medecinRepository;
        this.rendezVousRepository = rendezVousRepository;
        this.hopitalRepository = hopitalRepository;
        this.abonnementRepository = abonnementRepository;
        this.logsActiviteRepository = logsActiviteRepository;
        this.currentUserService = currentUserService;
        this.doctorDashboardRepository = doctorDashboardRepository;
        this.doctorWorkspaceRepository = doctorWorkspaceRepository;
        this.medecinFileAttenteRepository = medecinFileAttenteRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public DashboardStatsDTO getDashboardStats() {
        YearMonth currentMonth = YearMonth.now();
        LocalDate firstDayCurrentMonth = currentMonth.atDay(1);

        Long hopitauxActifs = hopitalRepository.countAllActifs();
        Long hopitauxActifsMoisPrecedent = hopitalRepository.countAllActifsExistingBefore(firstDayCurrentMonth);
        Long utilisateursActifs = utilisateurRepository.countAllActive();
        Long utilisateursActifsMoisPrecedent = utilisateurRepository.countAllActiveExistingBefore(firstDayCurrentMonth);
        BigDecimal mrr = abonnementRepository.calculatePlatformMrr();
        BigDecimal mrrMoisPrecedent = abonnementRepository.calculatePlatformMrrAtEndOfMonth(
                currentMonth.minusMonths(1).atEndOfMonth());

        BigDecimal pourcentageCroissanceHopitaux = calculatePercentage(hopitauxActifsMoisPrecedent, hopitauxActifs);
        BigDecimal pourcentageCroissanceUtilisateurs = calculatePercentage(utilisateursActifsMoisPrecedent,
                utilisateursActifs);
        BigDecimal pourcentageCroissanceMrr = calculatePercentage(mrrMoisPrecedent, mrr);
        BigDecimal croissanceSaaS = calculateGrowthComposite(
                pourcentageCroissanceHopitaux,
                pourcentageCroissanceUtilisateurs,
                pourcentageCroissanceMrr);

        return new DashboardStatsDTO(
                hopitauxActifs,
                utilisateursActifs,
                mrr,
                croissanceSaaS,
                pourcentageCroissanceHopitaux,
                pourcentageCroissanceUtilisateurs,
                pourcentageCroissanceMrr);
    }

    @Override
    public List<MrrSeriesPointDTO> getMrrSeries(int months) {
        int safeMonths = months <= 0 ? 6 : Math.min(months, 24);
        return abonnementRepository.getMrrSeriesLastMonths(safeMonths);
    }

    @Override
    public List<PlanDistributionItemDTO> getPlanDistribution() {
        return abonnementRepository.getPlanDistribution();
    }

    @Override
    public List<TenantOverviewDTO> getTenantsOverview() {
        return abonnementRepository.listTenantsOverview();
    }

    private BigDecimal calculatePercentage(BigDecimal previous, BigDecimal current) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.valueOf(100)
                    : BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculatePercentage(Long previous, Long current) {
        BigDecimal previousValue = previous == null ? BigDecimal.ZERO : BigDecimal.valueOf(previous);
        BigDecimal currentValue = current == null ? BigDecimal.ZERO : BigDecimal.valueOf(current);
        return calculatePercentage(previousValue, currentValue);
    }

    private BigDecimal calculateGrowthComposite(BigDecimal hopitauxGrowth, BigDecimal usersGrowth, BigDecimal mrrGrowth) {
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        if (hopitauxGrowth != null) {
            sum = sum.add(hopitauxGrowth);
            count++;
        }
        if (usersGrowth != null) {
            sum = sum.add(usersGrowth);
            count++;
        }
        if (mrrGrowth != null) {
            sum = sum.add(mrrGrowth);
            count++;
        }
        return count > 0 ? sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    @Override
    public DashboardDTO getDashboardData() {
        Integer idHopital = resolveTenantId();
        Medecin medecin = resolveConnectedMedecin(idHopital);
        Integer idMedecin = medecin.getIdMedecin();

        StatistiqueMedecinDTO statistiques = medecinRepository.getDashboardStats(idMedecin, idHopital);

        List<RendezVous> tousLesRdvs = rendezVousRepository.listerParMedecin(idMedecin);
        java.time.LocalDateTime debutJour = java.time.LocalDate.now().atStartOfDay();
        List<RendezVousJourDTO> rendezVousDuJour = tousLesRdvs.stream()
                .filter(rdv -> rdv.getDateHeureRdv() != null && !rdv.getDateHeureRdv().isBefore(debutJour))
                .filter(rdv -> {
                    String statut = rdv.getStatutRdv() == null ? "" : rdv.getStatutRdv().toUpperCase();
                    return !statut.equals("ANNULE") && !statut.equals("ABSENT");
                })
                .sorted(java.util.Comparator.comparing(RendezVous::getDateHeureRdv))
                .map(this::mapToRendezVousJourDTO)
                .collect(Collectors.toList());

        return new DashboardDTO(
                statistiques,
                rendezVousDuJour,
                List.of(),
                medecinFileAttenteRepository.listerFileDuMedecin(idMedecin, idHopital),
                doctorDashboardRepository.findActiveConsultations(idMedecin, idHopital),
                doctorDashboardRepository.findPendingNotes(idMedecin, idHopital)
        );
    }

    @Override
    public DoctorWorkspaceDTO getDoctorWorkspaceData() {
        Integer idHopital = resolveTenantId();
        Medecin medecin = resolveConnectedMedecin(idHopital);
        Integer idMedecin = medecin.getIdMedecin();

        StatistiqueMedecinDTO statistiques = medecinRepository.getDashboardStats(idMedecin, idHopital);

        List<RendezVousJourDTO> agendaDuJour = rendezVousRepository.listerParMedecin(idMedecin)
                .stream()
                .filter(rdv -> rdv.getDateHeureRdv() != null
                        && !rdv.getDateHeureRdv().isBefore(java.time.LocalDate.now().atStartOfDay()))
                .filter(rdv -> {
                    String statut = rdv.getStatutRdv() == null ? "" : rdv.getStatutRdv().toUpperCase();
                    return !statut.equals("ANNULE") && !statut.equals("ABSENT");
                })
                .sorted(java.util.Comparator.comparing(RendezVous::getDateHeureRdv))
                .map(this::mapToRendezVousJourDTO)
                .collect(Collectors.toList());

        return new DoctorWorkspaceDTO(
                (long) agendaDuJour.size(),
                statistiques.getExamensEnAttente(),
                statistiques.getNotificationsNonLues(),
                agendaDuJour,
                doctorWorkspaceRepository.findRecentActivities(idMedecin, idHopital)
        );
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
                    "Aucun profil médecin n'est associé à votre compte. Contactez l'administrateur de votre hôpital.");
        }

        return medecinRepository.trouverParId(medecinId)
                .filter(medecin -> idHopital.equals(medecin.getIdHopital()))
                .orElseThrow(() -> new ForbiddenException(
                        "Médecin introuvable dans votre établissement (tenant " + idHopital + ")."));
    }

    private Integer resolveTenantId() {
        try {
            return currentUserService.getCurrentHopitalId();
        } catch (IllegalStateException e) {
            logger.warn("Aucun tenant détecté pour le dashboard: {}", e.getMessage());
            throw new IllegalStateException(
                    "Le tenant courant est introuvable. Veuillez fournir l'en-tête X-Hopital-Id.");
        }
    }

    private RendezVousJourDTO mapToRendezVousJourDTO(RendezVous rdv) {
        return new RendezVousJourDTO(
                rdv.getIdRdv(),
                rdv.getNomPatient(),
                rdv.getMotifVisite(),
                rdv.getDateHeureRdv(),
                rdv.getDureeEstimee(),
                rdv.getStatutRdv(),
                rdv.getCanal());
    }
}