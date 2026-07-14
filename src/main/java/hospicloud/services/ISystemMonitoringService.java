package hospicloud.services;

import hospicloud.dto.SystemStatsDTO;

/**
 * Interface pour le service de surveillance du système.
 * Définit le contrat pour obtenir les statistiques du système.
 */
public interface ISystemMonitoringService {

    /**
     * Récupère les statistiques actuelles du système (CPU et mémoire).
     *
     * @return Un {@link SystemStatsDTO} contenant les statistiques.
     */
    SystemStatsDTO getSystemStats();
}
