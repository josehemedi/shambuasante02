package hospicloud.services;

import hospicloud.dtos.*;
import java.util.List;
import java.util.Map;

public interface ConsultationMedicaleService {
    
    ConsultationResponseDTO creerConsultation(ConsultationRequestDTO dto);
    
    List<ConsultationResponseDTO> obtenirHistoriquePatient(Integer idPatient);

    List<ConsultationResponseDTO> obtenirHistoriqueMedecin(Integer idMedecin);
    
    ConsultationResponseDTO completerConsultation(Long idConsultation, String observations, String diagnostic);
    
    ConsultationResponseDTO mettreAJourConstantes(Long idConsultation, ConsultationRequestDTO dto);
    
    Map<String, Object> getOrdonnanceParams(Long idConsultation);

    byte[] genererPdfFicheConsultation(Long idConsultation);

    LiveKitTokenResponse genererTokenTeleconsultation(Long idRdv);

    ConsultationResponseDTO obtenirParRdv(Integer idRdv);

    ConsultationResponseDTO enregistrerFiche(Long idConsultation, ConsultationFicheDTO fiche);

    ConsultationResponseDTO obtenirOuCreerParRdv(Integer idRdv);

    ConsultationResponseDTO obtenirParId(Long idConsultation);
}