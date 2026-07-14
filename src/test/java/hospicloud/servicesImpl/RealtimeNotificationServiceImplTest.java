package hospicloud.servicesImpl;

import hospicloud.dtos.LiveNotificationDTO;
import hospicloud.model.Hopital;
import hospicloud.model.Medecin;
import hospicloud.model.Patient;
import hospicloud.model.RendezVous;
import hospicloud.repositories.HopitalRepository;
import hospicloud.repositories.MedecinRepository;
import hospicloud.repositories.PatientRepository;
import hospicloud.repositories.UtilisateurRepository;
import hospicloud.utils.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RealtimeNotificationServiceImplTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private MedecinRepository medecinRepository;
    @Mock
    private HopitalRepository hopitalRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RealtimeNotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void notifyRendezVousCreated_shouldEmailPatientUsingAccountEmailFallback() {
        RendezVous rdv = new RendezVous();
        rdv.setIdRdv(42);
        rdv.setIdHopital(1);
        rdv.setIdPatient(10);
        rdv.setIdMedecin(20);
        rdv.setDateHeureRdv(LocalDateTime.of(2026, 7, 15, 14, 30));
        rdv.setMotifVisite("Consultation de suivi");
        rdv.setCanal("PHYSIQUE");
        rdv.setDureeEstimee(30);

        Patient patient = new Patient();
        patient.setIdPatient(10L);
        patient.setNom("Diallo");
        patient.setPrenom("Amara");

        Medecin medecin = new Medecin();
        medecin.setIdMedecin(20);
        medecin.setNom("Kabila");
        medecin.setPrenom("Jean");
        medecin.setEmail("medecin@example.com");

        Hopital hopital = new Hopital();
        hopital.setIdHopital(1);
        hopital.setNomCommercial("Clinique Shambua");

        when(patientRepository.trouverPatientParId(10L)).thenReturn(Optional.of(patient));
        when(medecinRepository.trouverParId(20)).thenReturn(Optional.of(medecin));
        when(hopitalRepository.rechercherhopitalParId(1L)).thenReturn(hopital);
        when(utilisateurRepository.findEmailByPatient(10, 1)).thenReturn(Optional.of("amara.diallo@gmail.com"));
        when(utilisateurRepository.findUtilisateurIdByPatient(10, 1)).thenReturn(Optional.of(6));
        when(utilisateurRepository.findUtilisateurIdByMedecin(20, 1)).thenReturn(Optional.empty());

        service.notifyRendezVousCreated(rdv);

        verify(notificationService).notifierCreationRendezVousPatient(
                eq("amara.diallo@gmail.com"),
                eq("Amara Diallo"),
                eq("Jean Kabila"),
                eq("Clinique Shambua"),
                eq("15/07/2026 14:30"),
                eq("Consultation de suivi"),
                eq("PHYSIQUE"),
                eq(30),
                eq(null));
    }

    @Test
    void notifyRendezVousCreated_shouldSkipPatientEmailWhenNoAddressFound() {
        RendezVous rdv = new RendezVous();
        rdv.setIdRdv(43);
        rdv.setIdHopital(1);
        rdv.setIdPatient(11);
        rdv.setIdMedecin(20);
        rdv.setDateHeureRdv(LocalDateTime.now());
        rdv.setCanal("PHYSIQUE");

        Patient patient = new Patient();
        patient.setIdPatient(11L);
        patient.setNom("Test");
        patient.setPrenom("User");

        when(patientRepository.trouverPatientParId(11L)).thenReturn(Optional.of(patient));
        when(medecinRepository.trouverParId(20)).thenReturn(Optional.empty());
        when(hopitalRepository.rechercherhopitalParId(1L)).thenReturn(new Hopital());
        when(utilisateurRepository.findEmailByPatient(11, 1)).thenReturn(Optional.empty());

        service.notifyRendezVousCreated(rdv);

        verify(notificationService, never()).notifierCreationRendezVousPatient(
                any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void notifyPaymentRecorded_shouldPublishToTenantAdminsOfSameHospital() {
        when(utilisateurRepository.findActiveUtilisateurIdsByRole(1, hospicloud.model.Role.TENANT_ADMIN))
                .thenReturn(List.of(4, 7));

        service.notifyPaymentRecorded(
                1,
                101,
                new BigDecimal("150000"),
                "FAC-2026-101",
                "Amara Diallo",
                "caissier@hopital.test",
                "cash",
                "PARTIEL",
                99);

        ArgumentCaptor<LiveNotificationDTO> dtoCaptor = ArgumentCaptor.forClass(LiveNotificationDTO.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/tenant/1/user/4/notifications"), dtoCaptor.capture());
        verify(messagingTemplate).convertAndSend(eq("/topic/tenant/1/user/7/notifications"), any(LiveNotificationDTO.class));

        LiveNotificationDTO dto = dtoCaptor.getValue();
        assertEquals("PAYMENT_RECORDED", dto.getType());
        assertEquals(1, dto.getIdHopital());
        assertEquals("Encaissement enregistré", dto.getTitleFr());
        assertEquals("/billing", dto.getLink());
    }

    @Test
    void notifyPaymentRecorded_shouldSkipExcludedCollectorAdmin() {
        when(utilisateurRepository.findActiveUtilisateurIdsByRole(2, hospicloud.model.Role.TENANT_ADMIN))
                .thenReturn(List.of(4));

        service.notifyPaymentRecorded(
                2,
                55,
                new BigDecimal("50000"),
                "FAC-55",
                "Test Patient",
                "admin@hopital.test",
                "card",
                "PAYE",
                4);

        verify(messagingTemplate, never()).convertAndSend(any(String.class), any(LiveNotificationDTO.class));
    }

    @Test
    void notifyArchivistesDossierPatientSorti_shouldPublishToActiveArchivists() {
        Patient patient = new Patient();
        patient.setIdPatient(100L);
        patient.setNom("Diallo");
        patient.setPrenom("Fatou");

        when(utilisateurRepository.findActiveUtilisateurIdsByRole(1, hospicloud.model.Role.ARCHIVISTE))
                .thenReturn(List.of(12, 15));
        when(patientRepository.trouverPatientParId(100L)).thenReturn(Optional.of(patient));

        service.notifyArchivistesDossierPatientSorti(1, 77L, 100L, "HOSPITALISATION", null);

        ArgumentCaptor<LiveNotificationDTO> dtoCaptor = ArgumentCaptor.forClass(LiveNotificationDTO.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/tenant/1/user/12/notifications"), dtoCaptor.capture());
        verify(messagingTemplate).convertAndSend(eq("/topic/tenant/1/user/15/notifications"), any(LiveNotificationDTO.class));

        LiveNotificationDTO dto = dtoCaptor.getValue();
        assertEquals("ARCHIVE_DOSSIER_SORTIE", dto.getType());
        assertEquals("Patient sorti — dossier à archiver", dto.getTitleFr());
        assertEquals("/archives/77", dto.getLink());
        assertEquals("warning", dto.getTone());
    }
}
