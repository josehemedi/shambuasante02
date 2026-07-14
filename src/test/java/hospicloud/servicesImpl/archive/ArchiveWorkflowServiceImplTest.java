package hospicloud.servicesImpl.archive;

import hospicloud.model.archive.ArchiveDossier;
import hospicloud.model.archive.StatutArchive;
import hospicloud.model.archive.TypeEpisode;
import hospicloud.model.reception.Admission;
import hospicloud.repositories.archive.ArchiveDossierRepository;
import hospicloud.repositories.archive.HistoriqueArchivageRepository;
import hospicloud.security.CurrentUserService;
import hospicloud.services.RealtimeNotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArchiveWorkflowServiceImplTest {

    @Mock private ArchiveDossierRepository archiveRepository;
    @Mock private HistoriqueArchivageRepository historiqueRepository;
    @Mock private ArchiveAuditHelper auditHelper;
    @Mock private CurrentUserService currentUserService;
    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private RealtimeNotificationService realtimeNotificationService;

    @InjectMocks
    private ArchiveWorkflowServiceImpl service;

    @Test
    void soumettreApresAutorisationSortie_createsArchiveForHospitalisation() {
        Admission admission = new Admission();
        admission.setIdAdmission(42);
        admission.setIdHopital(1);
        admission.setIdPatient(100);
        admission.setIdMedecin(5);
        admission.setNiveauPriorite(3);

        when(archiveRepository.findByEpisode(1, TypeEpisode.HOSPITALISATION, 42L))
                .thenReturn(Optional.empty());
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(100L), eq(1)))
                .thenReturn(1);
        when(archiveRepository.insert(any(ArchiveDossier.class))).thenReturn(77L);
        when(historiqueRepository.insert(any())).thenReturn(1L);
        when(currentUserService.getCurrentUtilisateurId()).thenReturn(10);

        Optional<Long> result = service.soumettreApresAutorisationSortie(1, admission, 100, 5, 99);

        assertTrue(result.isPresent());
        assertEquals(77L, result.get());
        verify(archiveRepository).insert(argThat(a ->
                a.getHopitalId() == 1
                        && a.getStatutArchive() == StatutArchive.A_VERIFIER
                        && a.getTypeEpisode() == TypeEpisode.HOSPITALISATION));
        verify(auditHelper).log(eq("DOSSIER_ENVOYE_ARCHIVISTE"), eq("SUCCESS"), anyString(),
                eq(77L), isNull(), eq("A_VERIFIER"), anyString());
        verify(realtimeNotificationService).notifyArchivistesDossierPatientSorti(
                eq(1), eq(77L), eq(100L), eq("HOSPITALISATION"), eq(10));
    }

    @Test
    void soumettreApresAutorisationSortie_rejectsWrongTenant() {
        Admission admission = new Admission();
        admission.setIdAdmission(42);
        admission.setIdHopital(2);
        admission.setIdPatient(100);

        Optional<Long> result = service.soumettreApresAutorisationSortie(1, admission, 100, 5, 99);

        assertTrue(result.isEmpty());
        verify(archiveRepository, never()).insert(any());
    }

    @Test
    void soumettreApresAutorisationSortie_idempotentWhenExists() {
        Admission admission = new Admission();
        admission.setIdAdmission(42);
        admission.setIdHopital(1);
        admission.setIdPatient(100);

        ArchiveDossier existing = new ArchiveDossier();
        existing.setId(55L);
        when(archiveRepository.findByEpisode(1, TypeEpisode.HOSPITALISATION, 42L))
                .thenReturn(Optional.of(existing));

        Optional<Long> result = service.soumettreApresAutorisationSortie(1, admission, 100, 5, 99);

        assertEquals(55L, result.orElseThrow());
        verify(archiveRepository, never()).insert(any());
        verify(realtimeNotificationService).notifyArchivistesDossierPatientSorti(
                eq(1), eq(55L), eq(100L), eq("HOSPITALISATION"), any());
    }

    @Test
    void soumettreApresSortieOfficielle_createsConsultationArchiveAndNotifies() {
        when(archiveRepository.findByEpisode(1, TypeEpisode.CONSULTATION, 88L))
                .thenReturn(Optional.empty());
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(100L), eq(1)))
                .thenReturn(1);
        when(archiveRepository.insert(any(ArchiveDossier.class))).thenReturn(91L);
        when(historiqueRepository.insert(any())).thenReturn(1L);
        when(currentUserService.getCurrentUtilisateurId()).thenReturn(10);

        Optional<Long> result = service.soumettreApresSortieOfficielle(
                1, TypeEpisode.CONSULTATION, 88L, 100, 5, 99,
                "Sortie officielle de consultation — dossier prêt à être archivé");

        assertEquals(91L, result.orElseThrow());
        verify(realtimeNotificationService).notifyArchivistesDossierPatientSorti(
                eq(1), eq(91L), eq(100L), eq("CONSULTATION"), eq(10));
    }
}
