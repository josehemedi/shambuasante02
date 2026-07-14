package hospicloud.repositories;

import hospicloud.model.ConsultationMedicale;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConsultationMedicaleRepository {
    
    // Créer une consultation (l'id_hopital sera injecté à l'intérieur depuis le TenantContext)
    ConsultationMedicale save(ConsultationMedicale consultation);
    
    // Consulter l'historique d'un patient (fiches finalisées uniquement)
    List<ConsultationMedicale> findByPatient(Integer idPatient);

    // Historique des fiches finalisées pour un médecin
    List<ConsultationMedicale> findByMedecin(Integer idMedecin);

    /** Toutes les consultations de l'établissement (continuité des soins). */
    List<ConsultationMedicale> findByHopital(Integer idHopital);
    
    // Récupérer une consultation spécifique
    Optional<ConsultationMedicale> findById(Long idConsultation);
    
    // Ajouter des observations et un diagnostic
    void updateObservationsEtDiagnostic(Long idConsultation, String observations, String diagnostic);

    Optional<ConsultationMedicale> findActiveForPatientAndMedecin(Integer idPatient, Integer idMedecin);

    Optional<ConsultationMedicale> findByRdv(Integer idRdv);

    void updateFiche(ConsultationMedicale consultation);

    void signerConsultation(Long idConsultation, LocalDateTime dateSignature);

    void ensureSchema();
}