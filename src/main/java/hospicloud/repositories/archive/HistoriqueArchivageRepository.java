package hospicloud.repositories.archive;

import hospicloud.model.archive.HistoriqueArchivage;

import java.util.List;

public interface HistoriqueArchivageRepository {

    Long insert(HistoriqueArchivage historique);

    List<HistoriqueArchivage> findByArchiveId(Integer hopitalId, Long archiveId);
}
