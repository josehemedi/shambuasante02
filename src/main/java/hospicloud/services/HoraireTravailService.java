package hospicloud.services;

import java.util.List;
import java.util.Optional;
import hospicloud.model.HoraireTravail;

public interface HoraireTravailService {
    
    HoraireTravail creerHoraire(HoraireTravail horaire);
    
    HoraireTravail modifierHoraire(HoraireTravail horaire);
    
    boolean supprimerHoraire(Long id);
    
    Optional<HoraireTravail> obtenirParId(Long id);
    
    List<HoraireTravail> obtenirParMedecin(Integer medecinId);
    
    List<HoraireTravail> obtenirParMedecinEtJour(Integer medecinId, String jourSemaine);
    
    // Version multi-tenant optimisée avec cache Redis
    List<HoraireTravail> obtenirParMedecinJourEtHopital(Integer medecinId, String jourSemaine);
}