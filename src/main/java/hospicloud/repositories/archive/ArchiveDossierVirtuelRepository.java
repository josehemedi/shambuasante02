package hospicloud.repositories.archive;

import hospicloud.model.archive.ArchiveDossierVirtuel;

import java.util.List;
import java.util.Optional;

public interface ArchiveDossierVirtuelRepository {

    Long insert(ArchiveDossierVirtuel folder);

    boolean updateNom(Integer hopitalId, Long id, String nom);

    boolean updateParent(Integer hopitalId, Long id, Long parentId);

    boolean deleteIfEmpty(Integer hopitalId, Long id);

    Optional<ArchiveDossierVirtuel> findById(Integer hopitalId, Long id);

    List<ArchiveDossierVirtuel> listChildren(Integer hopitalId, Long parentId);

    List<ArchiveDossierVirtuel> listAll(Integer hopitalId);

    boolean existsByNom(Integer hopitalId, Long parentId, String nom, Long excludeId);

    boolean isDescendantOf(Integer hopitalId, Long folderId, Long potentialAncestorId);

    int countChildren(Integer hopitalId, Long folderId);

    int countDossiers(Integer hopitalId, Long folderId);
}
