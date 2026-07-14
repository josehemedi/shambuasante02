package hospicloud.services;

import hospicloud.dtos.MedecinDemandeAnalyseRequestDTO;
import hospicloud.dtos.MedecinDemandeAnalyseResponseDTO;

import java.util.List;

public interface MedecinLabService {

    MedecinDemandeAnalyseResponseDTO creerDemande(MedecinDemandeAnalyseRequestDTO request);

    List<MedecinDemandeAnalyseResponseDTO> listerMesDemandes();
}
