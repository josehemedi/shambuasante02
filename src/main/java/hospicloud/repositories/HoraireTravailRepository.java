package hospicloud.repositories;

import java.util.List;
import java.util.Optional;
import hospicloud.model.HoraireTravail;

public interface HoraireTravailRepository {
    
    // On ne passe plus l'ID en argument, il sera récupéré via le contexte
    HoraireTravail enregistrer(HoraireTravail horaire);
    int modifier(HoraireTravail horaire);
    int supprimerParId(Long id);
    
    Optional<HoraireTravail> trouverParId(Long id);
    List<HoraireTravail> trouverParMedecinId(Integer medecinId);
    
    // Gardez cette signature seulement si vous avez besoin de faire des recherches
    // sur des hôpitaux différents de celui de la session courante (cas rare)
    List<HoraireTravail> trouverParMedecinIdEtJour(Integer medecinId, String jourSemaine);
}