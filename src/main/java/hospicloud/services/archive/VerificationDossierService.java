package hospicloud.services.archive;

import hospicloud.dtos.archive.VerificationDossierResultDto;
import hospicloud.model.archive.ReglesArchivageHopital;
import hospicloud.model.archive.TypeEpisode;

public interface VerificationDossierService {

    VerificationDossierResultDto verifier(Integer hopitalId, TypeEpisode typeEpisode,
                                          Long episodeId, Long patientId);

    VerificationDossierResultDto verifierAvecRegles(Integer hopitalId, TypeEpisode typeEpisode,
                                                    Long episodeId, Long patientId,
                                                    ReglesArchivageHopital regles);
}
