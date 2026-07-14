package hospicloud.services.archive;

import hospicloud.dtos.archive.*;
import hospicloud.model.archive.ReglesArchivageHopital;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ArchivageService {

    ArchivePageResponseDto rechercher(ArchiveSearchFilter filter);

    ArchiveDossierResponseDto consulter(Long id);

    List<ArchiveDossierResponseDto> listerParPatient(Long patientId);

    ArchiveStatistiquesDto statistiques();

    VerificationDossierResultDto verifierDossier(VerifierDossierRequestDto request);

    ArchiveDossierResponseDto enregistrerEpisode(EnregistrerEpisodeRequestDto request);

    ArchiveDossierResponseDto marquerCommeIncomplet(Long id, TransitionArchiveRequestDto request);

    ArchiveDossierResponseDto marquerCommePretAArchiver(Long id, TransitionArchiveRequestDto request);

    ArchiveDossierResponseDto archiverEpisode(Long id, TransitionArchiveRequestDto request);

    ArchiveDossierResponseDto restaurerArchive(Long id, TransitionArchiveRequestDto request);

    List<HistoriqueArchivageDto> historique(Long id);

    List<ArchiveFichierDto> listerFichiers(Long archiveId);

    byte[] telechargerFichier(Long archiveId, Long fichierId);

    ArchiveFichierDto getFichierMeta(Long archiveId, Long fichierId);

    ArchiveFichierDto regenererPdf(Long archiveId);

    ArchiveFichierDto uploaderPieceJointe(Long archiveId, MultipartFile file, String libelle);

    void supprimerPieceJointe(Long archiveId, Long fichierId);

    ReglesArchivageHopital getRegles();

    ReglesArchivageHopital updateRegles(ReglesArchivageHopital regles);
}
