package hospicloud.servicesImpl;

import hospicloud.dtos.events.RendezVousCreatedEvent;
import hospicloud.model.Medecin;
import hospicloud.model.Patient;
import hospicloud.model.RendezVous;
import hospicloud.repositories.MedecinRepository;
import hospicloud.repositories.PatientRepository;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.repositories.RendezVousRepository;
import hospicloud.security.CurrentUserService;
import hospicloud.security.TenantContext;
import hospicloud.services.RealtimeNotificationService;
import hospicloud.utils.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RendezVousServiceTest {

    @Mock
    private RendezVousRepository rendezVousRepository;

    @Mock
    private MedecinRepository medecinRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private RealtimeNotificationService realtimeNotificationService;

    @Mock
    private LiveKitService liveKitService;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private RendezVousServiceImpl rendezVousService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        TenantContext.setHopitalId(1);
    }

    @Test
    void creerEtPublier_shouldSaveAndPublishEvent() {

        // =========================
        // GIVEN
        // =========================
        RendezVous rdv = new RendezVous();
        rdv.setIdHopital(1);
        rdv.setIdPatient(10);
        rdv.setIdMedecin(20);
        rdv.setDateHeureRdv(LocalDateTime.of(2026, 6, 6, 10, 0));
        rdv.setMotifVisite("Consultation");
        rdv.setStatutRdv("EN_ATTENTE");

        RendezVous saved = new RendezVous();
        saved.setIdRdv(100);
        saved.setIdHopital(1);
        saved.setIdPatient(10);
        saved.setIdMedecin(20);
        saved.setDateHeureRdv(rdv.getDateHeureRdv());
        saved.setMotifVisite("Consultation");
        saved.setStatutRdv("EN_ATTENTE");

        when(rendezVousRepository.creer(rdv)).thenReturn(saved);
        when(patientRepository.trouverPatientParId(10L)).thenReturn(Optional.of(new Patient()));
        when(medecinRepository.trouverParId(20)).thenReturn(Optional.of(new Medecin()));

        // =========================
        // WHEN
        // =========================
        RendezVous result = rendezVousService.creerEtPublier(rdv);

        // =========================
        // THEN
        // =========================
        assertNotNull(result);
        assertEquals(100, result.getIdRdv());

        verify(rendezVousRepository, times(1)).creer(rdv);
    }

    @Test
    void creerEtPublier_shouldCallRepositoryOnlyOnce() {

        RendezVous rdv = new RendezVous();
        rdv.setIdHopital(1);

        RendezVous saved = new RendezVous();
        saved.setIdRdv(1);
        saved.setIdHopital(1);

        when(rendezVousRepository.creer(rdv)).thenReturn(saved);
        when(patientRepository.trouverPatientParId(anyLong())).thenReturn(Optional.of(new Patient()));
        when(medecinRepository.trouverParId(any())).thenReturn(Optional.of(new Medecin()));

        rendezVousService.creerEtPublier(rdv);

        verify(rendezVousRepository, times(1)).creer(rdv);
    }

    @Test
    void creerEtPublier_shouldAllowMedecinToScheduleHospitalPatient() {
        RendezVous rdv = new RendezVous();
        rdv.setIdPatient(10);
        rdv.setIdMedecin(20);
        rdv.setDateHeureRdv(LocalDateTime.of(2026, 6, 6, 10, 0));

        RendezVous saved = new RendezVous();
        saved.setIdRdv(100);

        when(currentUserService.isMedecin()).thenReturn(true);
        when(currentUserService.getCurrentMedecinId()).thenReturn(20);
        when(patientRepository.trouverPatientParId(10L)).thenReturn(Optional.of(new Patient()));
        when(medecinRepository.trouverParId(20)).thenReturn(Optional.of(new Medecin()));
        when(rendezVousRepository.creer(rdv)).thenReturn(saved);

        RendezVous result = rendezVousService.creerEtPublier(rdv);

        assertNotNull(result);
        verify(rendezVousRepository, times(1)).creer(rdv);
    }

    @Test
    void creerEtPublier_shouldReturnSavedObject() {

        RendezVous rdv = new RendezVous();
        rdv.setIdHopital(1);

        RendezVous saved = new RendezVous();
        saved.setIdRdv(999);

        when(rendezVousRepository.creer(rdv)).thenReturn(saved);
        when(patientRepository.trouverPatientParId(anyLong())).thenReturn(Optional.of(new Patient()));
        when(medecinRepository.trouverParId(any())).thenReturn(Optional.of(new Medecin()));

        RendezVous result = rendezVousService.creerEtPublier(rdv);

        assertSame(saved, result);
    }

    @Test
    void reporterRendezVous_shouldNotifyMedecin() {
        Integer idRdv = 1;
        Integer idMedecin = 20;
        Integer idPatient = 10;
        LocalDateTime ancienneDate = LocalDateTime.of(2026, 6, 1, 10, 0);
        LocalDateTime nouvelleDate = LocalDateTime.of(2026, 6, 2, 11, 0);

        RendezVous rdvActuel = new RendezVous();
        rdvActuel.setIdRdv(idRdv);
        rdvActuel.setIdMedecin(idMedecin);
        rdvActuel.setIdPatient(idPatient);
        rdvActuel.setDateHeureRdv(ancienneDate);

        RendezVous rdvAjuste = new RendezVous();
        rdvAjuste.setIdRdv(idRdv);
        rdvAjuste.setIdMedecin(idMedecin);
        rdvAjuste.setIdPatient(idPatient);
        rdvAjuste.setDateHeureRdv(nouvelleDate);

        Medecin medecin = new Medecin();
        medecin.setIdMedecin(idMedecin);
        medecin.setNom("Dupont");
        medecin.setPrenom("Jean");
        medecin.setEmail("medecin@example.com");

        Patient patient = new Patient();
        patient.setIdPatient(idPatient.longValue());
        patient.setNom("Martin");
        patient.setPrenom("Alice");

        when(rendezVousRepository.trouverParId(idRdv)).thenReturn(rdvActuel, rdvAjuste);
        when(medecinRepository.trouverParId(idMedecin)).thenReturn(Optional.of(medecin));
        when(patientRepository.trouverPatientParId(idPatient.longValue())).thenReturn(Optional.of(patient));

        rendezVousService.reporterRendezVous(idRdv, nouvelleDate);

        verify(notificationService).notifierReportRendezVous(
                "medecin@example.com",
                "Dupont Jean",
                "Martin Alice",
                "01/06/2026 10:00",
                "02/06/2026 11:00"
        );
    }
}