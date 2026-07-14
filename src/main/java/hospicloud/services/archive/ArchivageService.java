package hospicloud.services.archive;

import hospicloud.dtos.archive.*;
import hospicloud.model.archive.ReglesArchivageHopital;

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

    ReglesArchivageHopital getRegles();

    ReglesArchivageHopital updateRegles(ReglesArchivageHopital regles);
}
