package hospicloud.repositories;

import hospicloud.model.Ordonnance;
import java.util.List;
import java.util.Optional;

/**
 * Interface Repository pour la gestion des ordonnances.
 * L'isolation Multi-Tenant est garantie par l'utilisation de TenantContext.
 */
public interface OrdonnanceRepository {

    /**
     * Enregistre une nouvelle ordonnance.
     */
    void creerOrdonnance(Ordonnance ordonnance);

    /**
     * Récupère l'historique des ordonnances pour un patient spécifique.
     */
    List<Ordonnance> listerParPatient(Integer idPatient);

    /**
     * Ordonnances prescrites par un médecin du tenant courant (avec nom patient).
     */
    List<Ordonnance> listerParMedecin(Integer idMedecin);

    /**
     * Trouve une ordonnance par son ID.
     */
    Optional<Ordonnance> trouverParId(Long idOrdonnance);

    /**
     * Met à jour le statut d'une ordonnance.
     */
    void mettreAJourStatut(Long idOrdonnance, String nouveauStatut);

    /**
     * Annule une ordonnance (Soft delete via changement de statut).
     */
    void annulerOrdonnance(Long idOrdonnance);
    
   
}