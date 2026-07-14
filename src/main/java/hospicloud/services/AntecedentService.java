package hospicloud.services;

import java.util.List;
import java.util.Optional;
import hospicloud.model.Antecedent;

public interface AntecedentService {

    /**
     * Enregistre un nouvel antécédent. 
     * L'idHopital sera injecté automatiquement via le TenantContext dans le Repository.
     */
    void ajouterAntecedent(Antecedent antecedent);

    /**
     * Met à jour un antécédent existant.
     */
    void mettreAJourAntecedent(Antecedent antecedent);

    /**
     * Supprime un antécédent. 
     */
    void retirerAntecedent(int id);

    /**
     * Récupère la liste paginée des antécédents d'un patient.
     */
    List<Antecedent> recupererDossierPatient(int idPatient, int page, int size);

    /**
     * Génère une synthèse médicale triée par criticité.
     */
    List<Antecedent> genererSyntheseMedicale(int idPatient);

    /**
     * Alterne le statut de manière sécurisée.
     */
    void basculerStatut(int id);

    /**
     * Recherche un antécédent par ID.
     */
    Optional<Antecedent> trouverParId(int id);
}