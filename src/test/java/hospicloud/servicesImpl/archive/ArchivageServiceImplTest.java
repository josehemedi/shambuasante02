package hospicloud.servicesImpl.archive;

import hospicloud.dtos.archive.ArchiveSearchFilter;
import hospicloud.dtos.archive.TransitionArchiveRequestDto;
import hospicloud.dtos.archive.VerificationDossierResultDto;
import hospicloud.exceptions.BadRequestException;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.exceptions.ResourceNotFoundException;
import hospicloud.model.Role;
import hospicloud.model.archive.*;
import hospicloud.repositories.archive.ArchiveDossierRepository;
import hospicloud.repositories.archive.HistoriqueArchivageRepository;
import hospicloud.security.CurrentUserService;
import hospicloud.security.TenantContext;
import hospicloud.security.archive.ArchivePermissionService;
import hospicloud.services.archive.VerificationDossierService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ArchivageServiceImplTest {

    @Mock private ArchiveDossierRepository archiveRepository;
    @Mock private HistoriqueArchivageRepository historiqueRepository;
    @Mock private VerificationDossierService verificationService;
    @Mock private ArchivePermissionService permissionService;
    @Mock private CurrentUserService currentUserService;
    @Mock private ArchiveAuditHelper auditHelper;
    @Mock private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ArchivageServiceImpl service;

    @BeforeEach
    void setUp() {
        TenantContext.setHopitalId(1);
        when(currentUserService.getCurrentUtilisateurId()).thenReturn(10);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void archiverEpisode_success_whenComplete() {
        when(permissionService.has(anyString())).thenReturn(true);
        doNothing().when(permissionService).require(ArchivePermissionService.ARCHIVE_ARCHIVER);

        ArchiveDossier archive = baseArchive(StatutArchive.PRET_A_ARCHIVER);
        when(archiveRepository.findById(1, 5L)).thenReturn(Optional.of(archive));
        when(archiveRepository.findOrCreateRegles(1)).thenReturn(new ReglesArchivageHopital());

        VerificationDossierResultDto verification = new VerificationDossierResultDto();
        verification.setComplet(true);
        verification.setPeutArchiver(true);
        when(verificationService.verifierAvecRegles(anyInt(), any(), anyLong(), anyLong(), any()))
                .thenReturn(verification);

        when(archiveRepository.updateStatut(any())).thenReturn(true);
        when(historiqueRepository.insert(any())).thenReturn(99L);
        when(archiveRepository.findById(1, 5L))
                .thenReturn(Optional.of(archive))
                .thenReturn(Optional.of(archiveWithStatus(StatutArchive.ARCHIVE)));

        TransitionArchiveRequestDto request = new TransitionArchiveRequestDto();
        request.setMotif("Fin de prise en charge");

        var result = service.archiverEpisode(5L, request);

        assertEquals(StatutArchive.ARCHIVE, result.getStatutArchive());
        verify(historiqueRepository).insert(any());
        verify(auditHelper).log(eq("DOSSIER_ARCHIVE"), eq("SUCCESS"), anyString(),
                eq(5L), anyString(), eq("ARCHIVE"), anyString());
    }

    @Test
    void archiverEpisode_rejectsIncompleteDossier() {
        doNothing().when(permissionService).require(ArchivePermissionService.ARCHIVE_ARCHIVER);

        ArchiveDossier archive = baseArchive(StatutArchive.PRET_A_ARCHIVER);
        when(archiveRepository.findById(1, 5L)).thenReturn(Optional.of(archive));
        when(archiveRepository.findOrCreateRegles(1)).thenReturn(new ReglesArchivageHopital());

        VerificationDossierResultDto verification = new VerificationDossierResultDto();
        verification.setComplet(false);
        verification.setPeutArchiver(false);
        verification.addManquant("Diagnostic manquant");
        when(verificationService.verifierAvecRegles(anyInt(), any(), anyLong(), anyLong(), any()))
                .thenReturn(verification);

        assertThrows(BadRequestException.class,
                () -> service.archiverEpisode(5L, new TransitionArchiveRequestDto()));
    }

    @Test
    void restaurerArchive_requiresMotif() {
        doNothing().when(permissionService).require(ArchivePermissionService.ARCHIVE_RESTAURER);

        ArchiveDossier archive = baseArchive(StatutArchive.ARCHIVE);
        when(archiveRepository.findById(1, 5L)).thenReturn(Optional.of(archive));

        assertThrows(BadRequestException.class,
                () -> service.restaurerArchive(5L, new TransitionArchiveRequestDto()));
    }

    @Test
    void consulter_deniesOtherHospital_viaTenantContext() {
        doNothing().when(permissionService).require(ArchivePermissionService.ARCHIVE_VOIR);
        when(archiveRepository.findById(1, 99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.consulter(99L));
    }

    @Test
    void consulter_blocksSuperAdminMedicalAccess() {
        doNothing().when(permissionService).require(ArchivePermissionService.ARCHIVE_VOIR);
        when(permissionService.isSuperAdminTechnicalOnly()).thenReturn(true);

        assertThrows(ForbiddenException.class, () -> service.consulter(1L));
    }

    @Test
    void medecinCannotAccessOtherDoctorArchive() {
        doNothing().when(permissionService).require(ArchivePermissionService.ARCHIVE_VOIR);
        when(permissionService.isSuperAdminTechnicalOnly()).thenReturn(false);
        when(currentUserService.getCurrentRole()).thenReturn(Role.MEDECIN);
        when(currentUserService.getCurrentMedecinId()).thenReturn(2);

        ArchiveDossier archive = baseArchive(StatutArchive.ARCHIVE);
        archive.setIdMedecin(7);
        when(archiveRepository.findById(1, 5L)).thenReturn(Optional.of(archive));

        assertThrows(ForbiddenException.class, () -> service.consulter(5L));
    }

    @Test
    void historiqueFailureRollsBackViaException() {
        doNothing().when(permissionService).require(ArchivePermissionService.ARCHIVE_ARCHIVER);

        ArchiveDossier archive = baseArchive(StatutArchive.PRET_A_ARCHIVER);
        when(archiveRepository.findById(1, 5L)).thenReturn(Optional.of(archive));
        when(archiveRepository.findOrCreateRegles(1)).thenReturn(new ReglesArchivageHopital());

        VerificationDossierResultDto verification = new VerificationDossierResultDto();
        verification.setPeutArchiver(true);
        when(verificationService.verifierAvecRegles(anyInt(), any(), anyLong(), anyLong(), any()))
                .thenReturn(verification);
        when(archiveRepository.updateStatut(any())).thenReturn(true);
        when(historiqueRepository.insert(any())).thenReturn(null);

        TransitionArchiveRequestDto request = new TransitionArchiveRequestDto();
        request.setMotif("Test");

        assertThrows(BadRequestException.class, () -> service.archiverEpisode(5L, request));
    }

    @Test
    void rechercher_appliesMedecinScope() {
        doNothing().when(permissionService).require(ArchivePermissionService.ARCHIVE_RECHERCHER);
        when(permissionService.isSuperAdminTechnicalOnly()).thenReturn(false);
        when(currentUserService.getCurrentRole()).thenReturn(Role.MEDECIN);
        when(currentUserService.getCurrentMedecinId()).thenReturn(42);
        when(archiveRepository.search(eq(1), any())).thenReturn(java.util.List.of());
        when(archiveRepository.count(eq(1), any())).thenReturn(0L);

        service.rechercher(new ArchiveSearchFilter());

        verify(archiveRepository).search(eq(1), argThat(f -> Integer.valueOf(42).equals(f.getIdMedecin())));
    }

    private ArchiveDossier baseArchive(StatutArchive statut) {
        ArchiveDossier a = new ArchiveDossier();
        a.setId(5L);
        a.setHopitalId(1);
        a.setPatientId(100L);
        a.setTypeEpisode(TypeEpisode.CONSULTATION);
        a.setEpisodeId(200L);
        a.setStatutArchive(statut);
        a.setVersion(1);
        return a;
    }

    private ArchiveDossier archiveWithStatus(StatutArchive statut) {
        ArchiveDossier a = baseArchive(statut);
        a.setNomPatient("Test Patient");
        return a;
    }
}
