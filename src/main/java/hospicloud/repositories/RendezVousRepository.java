package hospicloud.repositories;

import hospicloud.dtos.TeleconsultationReminderCandidate;
import hospicloud.model.RendezVous;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface RendezVousRepository {

    // Consultation du planning
    List<RendezVous> listerParMedecin(Integer idMedecin);

    List<RendezVous> listerParMedecinEtDate(
            Integer idMedecin,
            LocalDate date);

    // Rendez-vous du jour
    List<RendezVous> listerRendezVousDuJourParMedecin(
            Integer idMedecin);

    // Détail
    RendezVous trouverParId(Integer idRdv);

    // Vérification disponibilité
    boolean estCreneauLibre(Integer idMedecin, LocalDateTime dateHeure);

    boolean estCreneauLibre(Integer idMedecin, LocalDateTime dateHeure, Integer dureeMinutes);

    // Création
    RendezVous creer(RendezVous rendezVous);

    // Validation
    void confirmerPresence(Integer idRdv);

    // Report
    void reporterRendezVous(
            Integer idRdv,
            LocalDateTime nouvelleDate);

    // Modification
    void modifierRendezVous(RendezVous rendezVous);

    // Annulation
    void annulerRendezVous(Integer idRdv);

    // Absence
    void marquerCommeAbsent(Integer idRdv);

    void marquerCommeTermine(Integer idRdv);

    List<RendezVous> listerParHopital();

    List<RendezVous> listerParHopital(Integer creePar);

    List<RendezVous> listerParPatient(Integer idPatient);

    void mettreAJourUrlVisio(Integer idRdv, String urlVisio);

    List<TeleconsultationReminderCandidate> listerTeleconsultationsPourRappel(
            LocalDateTime fenetreDebut,
            LocalDateTime fenetreFin);

    boolean reclamerRappel30Min(Integer idRdv, Integer idHopital);

    void reinitialiserRappel30Min(Integer idRdv, Integer idHopital);
}