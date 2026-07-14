package hospicloud.services;

import hospicloud.model.RendezVous;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface RendezVousService {

    // Création avec mécanisme de notification/publication
    RendezVous creerEtPublier(RendezVous rdv);

    // Consultations du planning
    List<RendezVous> listerParMedecin(Integer idMedecin);
    
    List<RendezVous> listerParMedecinEtDate(Integer idMedecin, LocalDate date);

    List<RendezVous> listerRendezVousDuJourParMedecin(Integer idMedecin);

    // Détail
    RendezVous obtenirParId(Integer idRdv);

    // Validation de disponibilité
    boolean verifierCreneau(Integer idMedecin, LocalDateTime dateHeure);

    // Modifications et Cycle de vie
    void modifierRendezVous(RendezVous rdv);

    void reporterRendezVous(Integer idRdv, LocalDateTime nouvelleDate);

    void confirmerPresence(Integer idRdv);

    void annulerRendezVous(Integer idRdv);

    void marquerCommeAbsent(Integer idRdv);

    void marquerCommeTermine(Integer idRdv);

    List<RendezVous> listerParHopital();

    List<RendezVous> listerParHopital(Boolean mine);
}