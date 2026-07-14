package hospicloud.servicesImpl;

import hospicloud.dtos.TeleconsultationReminderCandidate;
import hospicloud.repositories.RendezVousRepository;
import hospicloud.utils.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TeleconsultationReminderServiceTest {

    @Mock
    private RendezVousRepository rendezVousRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private LiveKitService liveKitService;

    private TeleconsultationReminderService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new TeleconsultationReminderService(
                rendezVousRepository,
                notificationService,
                liveKitService,
                true,
                true,
                30,
                2,
                "http://localhost:5173");
    }

    @Test
    void traiterCandidat_shouldNotifyPatientAndDoctorWhenClaimSucceeds() {
        TeleconsultationReminderCandidate candidat = buildCandidat();

        when(rendezVousRepository.reclamerRappel30Min(42, 1)).thenReturn(true);
        when(liveKitService.generateRoomName(1, 42)).thenReturn("tenant-1-teleconsultation-42");

        service.traiterCandidat(candidat);

        verify(notificationService).notifierRappelTeleconsultationPatient(
                eq("patient@test.com"),
                eq("Amara Diallo"),
                eq("Ngozi Achebe"),
                eq("Clinique Shambua"),
                eq("10/07/2026 15:00"),
                eq("http://localhost:5173/teleconsultation?rdv=42&room=tenant-1-teleconsultation-42"),
                eq(30));

        verify(notificationService).notifierRappelTeleconsultationMedecin(
                eq("medecin@test.com"),
                eq("Ngozi Achebe"),
                eq("Amara Diallo"),
                eq("Clinique Shambua"),
                eq("10/07/2026 15:00"),
                eq("http://localhost:5173/teleconsultation?rdv=42&room=tenant-1-teleconsultation-42"),
                eq(30));

        verify(notificationService).notifierRappelTeleconsultationSmsPatient(
                eq("0620000001"),
                eq("Amara Diallo"),
                eq("Ngozi Achebe"),
                eq("Clinique Shambua"),
                eq("10/07/2026 15:00"),
                eq("http://localhost:5173/teleconsultation?rdv=42&room=tenant-1-teleconsultation-42"),
                eq(30));

        verify(notificationService).notifierRappelTeleconsultationSmsMedecin(
                eq("+224620000002"),
                eq("Ngozi Achebe"),
                eq("Amara Diallo"),
                eq("Clinique Shambua"),
                eq("10/07/2026 15:00"),
                eq("http://localhost:5173/teleconsultation?rdv=42&room=tenant-1-teleconsultation-42"),
                eq(30));
    }

    @Test
    void traiterCandidat_shouldUseExistingVisioLink() {
        TeleconsultationReminderCandidate candidat = buildCandidat();
        candidat.setUrlVisio("http://localhost:5173/teleconsultation?rdv=42&room=existing");

        when(rendezVousRepository.reclamerRappel30Min(42, 1)).thenReturn(true);

        service.traiterCandidat(candidat);

        ArgumentCaptor<String> lienCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationService).notifierRappelTeleconsultationPatient(
                eq("patient@test.com"),
                eq("Amara Diallo"),
                eq("Ngozi Achebe"),
                eq("Clinique Shambua"),
                eq("10/07/2026 15:00"),
                lienCaptor.capture(),
                eq(30));
        assertEquals("http://localhost:5173/teleconsultation?rdv=42&room=existing", lienCaptor.getValue());
        verify(liveKitService, never()).generateRoomName(org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void traiterCandidat_shouldSkipWhenAlreadyClaimed() {
        TeleconsultationReminderCandidate candidat = buildCandidat();
        when(rendezVousRepository.reclamerRappel30Min(42, 1)).thenReturn(false);

        service.traiterCandidat(candidat);

        verify(notificationService, never()).notifierRappelTeleconsultationPatient(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                anyInt());
    }

    @Test
    void envoyerRappelsTeleconsultation_shouldQueryWindowAroundThirtyMinutes() {
        when(rendezVousRepository.listerTeleconsultationsPourRappel(
                org.mockito.ArgumentMatchers.any(LocalDateTime.class),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(java.util.List.of());

        service.envoyerRappelsTeleconsultation();

        ArgumentCaptor<LocalDateTime> debutCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> finCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(rendezVousRepository).listerTeleconsultationsPourRappel(debutCaptor.capture(), finCaptor.capture());

        long ecartMinutes = java.time.Duration.between(debutCaptor.getValue(), finCaptor.getValue()).toMinutes();
        assertEquals(4, ecartMinutes);
        assertTrue(debutCaptor.getValue().isBefore(finCaptor.getValue()));
    }

    private static TeleconsultationReminderCandidate buildCandidat() {
        TeleconsultationReminderCandidate candidat = new TeleconsultationReminderCandidate();
        candidat.setIdRdv(42);
        candidat.setIdHopital(1);
        candidat.setDateHeureRdv(LocalDateTime.of(2026, 7, 10, 15, 0));
        candidat.setEmailPatient("patient@test.com");
        candidat.setNomPatient("Amara Diallo");
        candidat.setEmailMedecin("medecin@test.com");
        candidat.setNomMedecin("Ngozi Achebe");
        candidat.setTelephonePatient("0620000001");
        candidat.setTelephoneMedecin("+224620000002");
        candidat.setNomHopital("Clinique Shambua");
        return candidat;
    }
}
