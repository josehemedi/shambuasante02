package hospicloud.services.archive;

import hospicloud.dtos.archive.DemandeAccesArchiveDto;
import hospicloud.dtos.archive.DemandeAccesRequestDto;

import java.util.List;

public interface DemandeAccesArchiveService {

    DemandeAccesArchiveDto creerDemande(Long archiveId, DemandeAccesRequestDto request);

    DemandeAccesArchiveDto accepter(Long demandeId, String observation);

    DemandeAccesArchiveDto refuser(Long demandeId, String observation);

    List<DemandeAccesArchiveDto> listerEnAttente();

    List<DemandeAccesArchiveDto> listerParArchive(Long archiveId);
}
