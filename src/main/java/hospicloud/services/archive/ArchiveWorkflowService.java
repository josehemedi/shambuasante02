package hospicloud.services.archive;

import hospicloud.model.archive.TypeEpisode;
import hospicloud.model.reception.Admission;

import java.util.Optional;

/**
 * Enchaînement automatique vers l'archiviste après clôture médicale d'un épisode de soins.
 */
public interface ArchiveWorkflowService {

    /**
     * Soumet le dossier d'hospitalisation à l'archiviste (statut A_VERIFIER).
     * Idempotent par épisode et tenant. N'interrompt pas la sortie médicale en cas d'échec technique.
     */
    Optional<Long> soumettreApresAutorisationSortie(Integer hopitalId,
                                                    Admission admission,
                                                    Integer idPatient,
                                                    Integer idMedecin,
                                                    Integer idBonSortie);

    /**
     * Soumet un épisode (hospitalisation, urgence ou consultation) à l'archiviste après sortie officielle.
     * Notifie toujours les archivistes (même si le dossier archive existait déjà).
     */
    Optional<Long> soumettreApresSortieOfficielle(Integer hopitalId,
                                                  TypeEpisode typeEpisode,
                                                  Long episodeId,
                                                  Integer idPatient,
                                                  Integer idMedecin,
                                                  Integer idBonSortie,
                                                  String observation);

    /**
     * Filet de sécurité à la délivrance réception si l'envoi n'a pas eu lieu à l'autorisation.
     */
    Optional<Long> soumettreApresDelivranceSiAbsent(Integer hopitalId,
                                                    Integer idAdmission,
                                                    Integer idPatient,
                                                    Integer idBonSortie);
}
