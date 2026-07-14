package hospicloud.servicesImpl;

import hospicloud.dtos.DashboardDTO;
import hospicloud.dtos.DashboardStatsDTO;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.model.Medecin;
import hospicloud.model.Utilisateur;
import hospicloud.repositories.AbonnementRepository;
import hospicloud.repositories.DoctorDashboardRepository;
import hospicloud.repositories.DoctorWorkspaceRepository;
import hospicloud.repositories.HopitalRepository;
import hospicloud.repositories.LogsActiviteRepository;
import hospicloud.repositories.MedecinRepository;
import hospicloud.repositories.RendezVousRepository;
import hospicloud.repositories.UtilisateurRepository;
import hospicloud.security.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private MedecinRepository medecinRepository;

    @Mock
    private RendezVousRepository rendezVousRepository;

    @Mock
    private HopitalRepository hopitalRepository;

    @Mock
    private AbonnementRepository abonnementRepository;

    @Mock
    private LogsActiviteRepository logsActiviteRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private DoctorDashboardRepository doctorDashboardRepository;

    @Mock
    private DoctorWorkspaceRepository doctorWorkspaceRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void shouldBuildDashboardStatsWithMonthOverMonthGrowth() {
        when(currentUserService.getCurrentHopitalId()).thenReturn(42);
        when(hopitalRepository.countActifsByHopital(42)).thenReturn(5L);
        when(hopitalRepository.countActifsByHopitalInPeriod(eq(42), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(4L);
        when(logsActiviteRepository.countActiveUsersInPeriod(eq(42), any(LocalDate.class), any(LocalDate.class)))
                .thenAnswer(invocation -> {
                    LocalDate startDate = invocation.getArgument(1);
                    return startDate.getMonthValue() == LocalDate.now().getMonthValue() ? 20L : 15L;
                });
        when(abonnementRepository.calculateMrrForPeriod(eq(42), any(LocalDate.class), any(LocalDate.class)))
                .thenAnswer(invocation -> {
                    LocalDate startDate = invocation.getArgument(1);
                    return startDate.getMonthValue() == LocalDate.now().getMonthValue() ? new BigDecimal("5000.00")
                            : new BigDecimal("4000.00");
                });

        DashboardStatsDTO stats = dashboardService.getDashboardStats();

        assertThat(stats.getHopitauxActifs()).isEqualTo(5L);
        assertThat(stats.getUtilisateursActifs()).isEqualTo(20L);
        assertThat(stats.getMrr()).isEqualByComparingTo("5000.00");
        assertThat(stats.getCroissanceSaaS()).isEqualByComparingTo("27.78");
        assertThat(stats.getPourcentageCroissanceHopitaux()).isEqualByComparingTo("25.00");
        assertThat(stats.getPourcentageCroissanceUtilisateurs()).isEqualByComparingTo("33.33");
        assertThat(stats.getPourcentageCroissanceMrr()).isEqualByComparingTo("25.00");
    }

    @Test
    void shouldResolveConnectedMedecinFromJwtContext() {
        when(currentUserService.getCurrentHopitalId()).thenReturn(7);
        when(currentUserService.getCurrentMedecinId()).thenReturn(2);

        Medecin doctor = new Medecin();
        doctor.setIdMedecin(2);
        doctor.setIdHopital(7);

        when(medecinRepository.trouverParId(2)).thenReturn(Optional.of(doctor));
        when(medecinRepository.getDashboardStats(2, 7)).thenReturn(null);
        when(rendezVousRepository.listerRendezVousDuJourParMedecin(2)).thenReturn(List.of());
        when(doctorDashboardRepository.findFilePatients(2, 7)).thenReturn(List.of());
        when(doctorDashboardRepository.findActiveConsultations(2, 7)).thenReturn(List.of());
        when(doctorDashboardRepository.findPendingNotes(2, 7)).thenReturn(List.of());

        DashboardDTO dashboard = dashboardService.getDashboardData();

        assertThat(dashboard).isNotNull();
        assertThat(dashboard.getRendezVousDuJour()).isEmpty();
    }
}
