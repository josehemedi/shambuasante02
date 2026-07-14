package hospicloud.repositories;

import java.util.List;
import java.util.Optional;
import hospicloud.model.Societe;

/**
 * Repository adapté au multi-tenant.
 * L'idHopital est désormais implicitement géré via TenantContext.
 */
public interface SocieteRepository {
    int enregistrerSociete(Societe societe);
    int modifierSociete(Societe societe);
    
    // idHopital supprimé de la signature, récupéré via TenantContext
    int supprimerSociete(Long idSociete);
    
    Optional<Societe> trouverParId(Long id);
    
    // ListerSocietes() ne devrait normalement lister que les sociétés du tenant courant
    List<Societe> ListerSocietes(); 
    
    // Recherche globale conservée (nécessaire pour des besoins admin plateforme)
    Optional<Societe> trouverParNom(String nomSociete); 
    List<Societe> listerParHopital();

    
    boolean existeParId(Long id);
}