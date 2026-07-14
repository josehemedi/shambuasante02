package hospicloud.services;

import hospicloud.dtos.lab.LabDashboardStatsDTO;
import hospicloud.dtos.lab.ResultatAnalyseCritiqueDTO;
import hospicloud.model.lab.CommandeAnalyse;

import java.util.List;

public interface LabDashboardService {
    
    /**
     * Récupérer les stats globales.
     * Le tenant est injecté via le contexte de sécurité, et non les paramètres, pour garantir l'isolation.
     */
    LabDashboardStatsDTO getDashboardStats();

    /**
     * Récupère la liste paginée des résultats critiques non acquittés.
     */
    List<ResultatAnalyseCritiqueDTO> getResultatsCritiques(int page, int size);

    /**
     * Acquitte un résultat critique.
     */
    void acquitterResultat(String idResultat);

    /**
     * Liste des commandes d'analyse.
     */
    List<CommandeAnalyse> listerCommandes(String statut, int page, int size);
}
