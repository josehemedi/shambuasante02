package hospicloud.services;

import hospicloud.dtos.OrdonnanceEnvoiResponse;
import hospicloud.dtos.OrdonnanceRequest;
import hospicloud.model.Ordonnance;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface de service pour la gestion des ordonnances.
 * Orchestre les appels au repository et gère la logique métier.
 */
public interface OrdonnanceService {

    /**
     * Crée une nouvelle ordonnance à partir de la requête DTO.
     */
    void creerOrdonnance(OrdonnanceRequest request);

    /**
     * Récupère toutes les ordonnances d'un patient donné.
     */
    List<Ordonnance> listerParPatient(Integer idPatient);

    /** Ordonnances du médecin connecté (tenant courant). */
    List<Ordonnance> listerParMedecin(Integer idMedecin);

    /**
     * Récupère une ordonnance spécifique par son ID.
     */
    Optional<Ordonnance> trouverParId(Long idOrdonnance);

    /**
     * Effectue le renouvellement d'une ordonnance.
     */
    void renouvelerOrdonnance(Long idAncienne, OrdonnanceRequest nouvelleReq);

    /**
     * Annule une ordonnance existante.
     */
    void annulerOrdonnance(Long idOrdonnance);

    /**
     * Prépare les paramètres nécessaires pour la génération du rapport Jasper.
     * @param idConsultation ID de la consultation liée à l'ordonnance.
     * @return Map contenant les données pour le rapport.
     */
    Map<String, Object> getOrdonnanceParams(Long idConsultation);
    
    /**
     * Prépare les paramètres nécessaires pour la génération du rapport Jasper.
     * @param idOrdonnance ID de l'ordonnance.
     * @return Map contenant les données pour le rapport.
     */
    Map<String, Object> getOrdonnanceParamsFromOrdonnance(Long idOrdonnance);

    /**
     * Génère le PDF professionnel JasperReports (avec QR) pour une ordonnance du tenant courant.
     */
    byte[] genererPdfOrdonnance(Long idOrdonnance);

    /**
     * Envoie l'ordonnance PDF au patient concerné (e-mail professionnel).
     * @param idOrdonnance identifiant de l'ordonnance
     * @param idMedecinConnecte médecin connecté (doit être le prescripteur)
     */
    OrdonnanceEnvoiResponse envoyerAuPatient(Long idOrdonnance, Integer idMedecinConnecte);
}
