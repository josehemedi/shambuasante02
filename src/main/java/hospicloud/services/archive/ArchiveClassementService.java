package hospicloud.services.archive;

import hospicloud.dtos.archive.*;

import java.util.List;

public interface ArchiveClassementService {

    ArchiveExplorerContentDto explorer(Long folderId);

    List<ArchiveDossierVirtuelDto> arbre();

    ArchiveDossierVirtuelDto creerDossier(CreateDossierVirtuelRequest request);

    ArchiveDossierVirtuelDto renommer(Long folderId, RenameDossierVirtuelRequest request);

    ArchiveDossierVirtuelDto deplacerDossierVirtuel(Long folderId, MoveDossierVirtuelRequest request);

    void supprimerDossier(Long folderId);

    ArchiveDossierResponseDto deplacerArchive(Long archiveId, MoveArchiveDossierRequest request);
}
