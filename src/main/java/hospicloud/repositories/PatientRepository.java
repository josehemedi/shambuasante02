package hospicloud.repositories;

import hospicloud.model.Patient;
import java.util.List;
import java.util.Optional;

public interface PatientRepository {
    
    // Les méthodes d'écriture ne prennent plus d'ID hôpital en paramètre
    void enregistrerPatient(Patient patient);
    void modifierPatient(Patient patient);
    void supprimerPatient(Long idPatient);
    
    // Lecture sécurisée : retourne Optional pour gérer l'absence de manière explicite
    Optional<Patient> trouverPatientParId(Long idPatient);
    
    // Retourne tous les patients de l'hôpital courant (via TenantContext)
    List<Patient> trouverTousLesPatients();

    List<Patient> trouverTousLesPatients(Integer creePar);
    
    // Recherche spécifique au locataire courant
    Optional<Patient> trouverPatientParNumero(String numero);
    List<Patient> rechercherParNomEtPrenom(String nom, String prenom);
    
 // Associer un patient existant à un médecin
    void lierPatientAMedecin(Integer idMedecin, Long idPatient);
    
    // 📋 Patients explicitement liés au médecin via medecin_patient
    List<Patient> listerPatientsParMedecin(Integer idMedecin);

    /** Alias explicite pour le filtre « mes patients attribués ». */
    List<Patient> listerPatientsAssignesAuMedecin(Integer idMedecin);

    /** True si le patient est lié au médecin via medecin_patient (tenant courant). */
    boolean estPatientAssigneAuMedecin(Integer idMedecin, Long idPatient);
    
    // 🔍 Recherche de patients parmi les patients suivis par CE médecin
    List<Patient> rechercherPatientsDuMedecin(Integer idMedecin, String nom, String prenom);
    
    // 🏥 Consultation du dossier du patient (sécurisée : vérifie si le médecin a le droit)
    Patient consulterDossierPatientParMedecin(Integer idMedecin, Long idPatient);

    void mettreAJourStatutClinique(Long idPatient, String statut);

    /** Recherche une fiche patient par email dans le tenant courant. */
    Optional<Patient> trouverPatientParEmail(String email);

    /** Crée ou met à jour les dossiers patients démo et leurs liaisons médecin. */
    void syncDemoPatients();
}