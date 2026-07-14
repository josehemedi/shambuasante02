package hospicloud.repositories;

import hospicloud.dtos.lab.LabDashboardStatsDTO;
import hospicloud.dtos.lab.ResultatAnalyseCritiqueDTO;
import hospicloud.model.lab.CommandeAnalyse;

import java.util.List;

public interface LabDashboardRepository {

    /**
     * Agrège les statistiques du tableau de bord en une seule requête DB pour un locataire donné.
     * @param idLocataire L'identifiant du locataire (tenant).
     * @return Les statistiques du tableau de bord.
     */
    LabDashboardStatsDTO getStatsByLocataire(String idLocataire);

    /**
     * Récupère la liste des résultats critiques non encore acquittés.
     * @param idLocataire L'identifiant du locataire (tenant).
     * @param limit Nombre max de résultats à ramener.
     * @param offset L'offset pour la pagination.
     * @return Liste paginée de résultats critiques.
     */
    List<ResultatAnalyseCritiqueDTO> getResultatsCritiquesNonAcquittes(String idLocataire, int limit, int offset);

    /**
     * Acquitte un résultat critique.
     * @param idResultat L'ID du résultat
     * @param idLocataire Le tenant
     */
    void acquitterResultatCritique(String idResultat, String idLocataire);
    
    /**
     * Liste les commandes d'analyses avec pagination.
     */
    List<CommandeAnalyse> getCommandesAnalyses(String idLocataire, String statut, int limit, int offset);
}
