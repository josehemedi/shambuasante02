package hospicloud.services;

import hospicloud.dtos.LabResultatSubmitDTO;
import hospicloud.dtos.MedecinDemandeAnalyseResponseDTO;

import java.util.List;

public interface LaborantinLabService {
    List<MedecinDemandeAnalyseResponseDTO> listerFileHopital();

    MedecinDemandeAnalyseResponseDTO soumettreResultat(Integer idAnalyse, LabResultatSubmitDTO request);
}
