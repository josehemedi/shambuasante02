package hospicloud.services;

import hospicloud.model.Societe;

import java.util.List;
import java.util.Optional;

/**
 * Service Société - version multi-tenant propre
 * Toutes les opérations sont automatiquement liées au TenantContext (hôpital courant)
 */
public interface SocieteService {

    /** Crée une nouvelle société dans l’hôpital courant */
    void creerSociete(Societe societe);

    /** Met à jour une société existante dans l’hôpital courant */
    void mettreAJourSociete(Societe societe);

    /** Supprime une société de l’hôpital courant */
    void supprimerSociete(Long id);

    /** Liste toutes les sociétés de l’hôpital courant */
    List<Societe> listerParHopital();

    /** Récupère une société par ID (sécurisé par tenant) */
    Optional<Societe> recupererParId(Long id);

    /** Vérifie si la société appartient à l’hôpital courant */
    boolean verifierAppartenance(Long id);

    /** Liste globale (réservé ADMIN plateforme) */
    List<Societe> listerTout();

    /** Recherche par nom dans l’hôpital courant */
    Optional<Societe> trouverParNom(String nomSociete);
}