package hospicloud.services.archive;

import hospicloud.dtos.archive.ArchiveContenuSnapshotDto;
import hospicloud.model.archive.ArchiveDossier;

public interface ArchiveSnapshotService {

    /**
     * Construit puis persiste un instantané complet du dossier patient lié à l'archive.
     */
    ArchiveContenuSnapshotDto capturerEtPersister(ArchiveDossier archive);

    /**
     * Lit le snapshot JSON déjà stocké (null si absent).
     */
    ArchiveContenuSnapshotDto lire(Integer hopitalId, Long archiveId);
}
