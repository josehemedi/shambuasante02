package hospicloud.repositories;

import hospicloud.dtos.LaboratoryKpisDTO;
import hospicloud.dtos.LaboratoryTestItemDTO;
import hospicloud.dtos.MedecinDemandeAnalyseResponseDTO;
import hospicloud.model.AnalyseLaboratoire;

import java.util.List;

public interface LaboratoryRepository {
    LaboratoryKpisDTO getKpis(Integer idHopital);

    List<LaboratoryTestItemDTO> listTests(Integer idHopital, int limit);

    Integer resolveOrCreateTypeAnalyse(Integer idHopital, String testCode, String testName);

    Integer insertAnalyse(AnalyseLaboratoire analyse, Integer idHopital);

    List<MedecinDemandeAnalyseResponseDTO> listDemandesMedecin(Integer idHopital, Integer idUtilisateurMedecin);

    List<MedecinDemandeAnalyseResponseDTO> listDemandesHopital(Integer idHopital);

    MedecinDemandeAnalyseResponseDTO trouverDemande(Integer idAnalyse, Integer idHopital);

    void soumettreResultat(Integer idAnalyse, Integer idHopital, Integer idLaborantin,
                           String resultatTexte, String interpretation, String valeursReference, String statut);
}
