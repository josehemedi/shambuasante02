package hospicloud.repositories.archive;

import hospicloud.model.archive.DemandeAccesArchive;
import hospicloud.model.archive.StatutDemandeAccesArchive;

import java.util.List;
import java.util.Optional;

public interface DemandeAccesArchiveRepository {

    Long insert(DemandeAccesArchive demande);

    Optional<DemandeAccesArchive> findById(Integer hopitalId, Long id);

    List<DemandeAccesArchive> findByArchiveId(Integer hopitalId, Long archiveId);

    List<DemandeAccesArchive> findEnAttente(Integer hopitalId);

    boolean updateStatut(Integer hopitalId, Long id, StatutDemandeAccesArchive statut,
                         Integer traitePar, String observation);
}
