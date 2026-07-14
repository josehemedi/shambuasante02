package hospicloud.repositories;

import hospicloud.dtos.StatistiqueMedecinDTO;
import hospicloud.model.Medecin;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des médecins (multi-tenant via id_hopital).
 */
public interface MedecinRepository {

    void creer(Medecin medecin);

    /** Insert et retourne l'id_medecin généré (multi-tenant). */
    Integer creerEtRetournerId(Medecin medecin);

    Optional<Medecin> trouverParId(Integer idMedecin);

    Optional<Medecin> trouverParEmail(String email);

    List<Medecin> listerParHopital(Integer idHopital);

    void mettreAJour(Medecin medecin);

    void changerDisponibilite(Integer idMedecin, Boolean status);

// Statistiques pour le médecin connecté
    long getNombrePatients(Integer medecinId, Integer hopitalId);
    long getConsultationsAujourdhui(Integer medecinId, Integer hopitalId);
    long getRendezVousAujourdhui(Integer medecinId, Integer hopitalId);
    long getHospitalisationsEncours(Integer medecinId, Integer hopitalId);
    long getExamensEnAttente(Integer medecinId, Integer hopitalId);
    long getNotificationsNonLues(Integer medecinId, Integer hopitalId);
    
    StatistiqueMedecinDTO getDashboardStats(Integer medecinId, Integer hopitalId);
}