package hospicloud.repositories.archive;

import hospicloud.dtos.archive.ArchiveSearchFilter;
import hospicloud.dtos.archive.ArchiveStatistiquesDto;
import hospicloud.model.archive.ArchiveDossier;
import hospicloud.model.archive.ReglesArchivageHopital;
import hospicloud.model.archive.StatutArchive;
import hospicloud.model.archive.TypeEpisode;

import java.util.List;
import java.util.Optional;

public interface ArchiveDossierRepository {

    Long insert(ArchiveDossier archive);

    boolean updateStatut(ArchiveDossier archive);

    Optional<ArchiveDossier> findById(Integer hopitalId, Long id);

    Optional<ArchiveDossier> findByEpisode(Integer hopitalId, TypeEpisode typeEpisode, Long episodeId);

    List<ArchiveDossier> search(Integer hopitalId, ArchiveSearchFilter filter);

    long count(Integer hopitalId, ArchiveSearchFilter filter);

    List<ArchiveDossier> listByDossierVirtuel(Integer hopitalId, Long dossierVirtuelId);

    boolean updateDossierVirtuelId(Integer hopitalId, Long archiveId, Long dossierVirtuelId);

    boolean saveContenuSnapshot(Integer hopitalId,
                                Long archiveId,
                                String contenuSnapshot,
                                java.time.LocalDateTime snapshotAt,
                                String nomPatientFige,
                                String numeroDossierFige);

    String findContenuSnapshot(Integer hopitalId, Long archiveId);

    ArchiveStatistiquesDto computeStatistiques(Integer hopitalId);

    ReglesArchivageHopital findOrCreateRegles(Integer hopitalId);

    boolean updateRegles(ReglesArchivageHopital regles);
}
