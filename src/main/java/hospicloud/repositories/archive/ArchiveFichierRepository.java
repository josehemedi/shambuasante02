package hospicloud.repositories.archive;

import hospicloud.model.archive.ArchiveFichier;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ArchiveFichierRepository {

    Long upsert(ArchiveFichier fichier);

    Optional<ArchiveFichier> findById(Integer hopitalId, Long id);

    Optional<ArchiveFichier> findByArchiveAndType(Integer hopitalId, Long archiveId, String typeFichier);

    List<ArchiveFichier> findByArchiveId(Integer hopitalId, Long archiveId);

    List<ArchiveFichier> findByArchiveIds(Integer hopitalId, Collection<Long> archiveIds);

    boolean deleteById(Integer hopitalId, Long id);
}
