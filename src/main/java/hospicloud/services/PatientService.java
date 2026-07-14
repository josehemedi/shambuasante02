package hospicloud.services;

import hospicloud.dtos.PatientDossierDTO;
import hospicloud.model.Patient;
import java.util.List;
import java.util.Optional;

public interface PatientService {
    
    // =====================================================
    // SERVICES GLOBAUX (PORTÉE ÉTABLISSEMENT)
    // =====================================================
    
    /**
     * Enregistre un nouveau patient dans l'établissement courant (SaaS Multi-Tenant).
     */
    void enregisterPatient(Patient patient);
    
    /**
     * Modifie les informations d'un patient au sein de l'établissement courant.
     */
    void modifierPatient(Patient patient);
    
    /**
     * Supprime un patient de l'établissement courant.
     */
    void supprimerPatient(Long idPatient);
    
    /**
     * Recherche un patient par son identifiant unique.
     */
    Optional<Patient> trouverPatientParId(Long idPatient);
    
    /**
     * Retourne tous les patients de l'hôpital courant (via TenantContext).
     */
    List<Patient> trouverTousLesPatients();

    /**
     * Patients de l'hôpital, filtrés optionnellement par créateur (cree_par).
     */
    List<Patient> trouverTousLesPatients(Boolean mine);
    
    /**
     * Recherche un patient par son code unique (ex: PAT-2026-0001).
     */
    Optional<Patient> trouverPatientParNumero(String numero);
    
    /**
     * Recherche globale par nom et/ou prénom au sein de l'établissement.
     */
    List<Patient> rechercherParNomEtPrenom(String nom, String prenom);
    
    // =====================================================
    // SERVICES EXPÉRIENCE MÉDECIN (VOS AJUSTEMENTS)
    // =====================================================
    
    /**
     * Assigne un patient existant de l'établissement au portefeuille d'un médecin.
     */
    void lierPatientAMedecin(Integer idMedecin, Long idPatient);
    
    /**
     * 📋 Liste des patients suivis personnellement par CE médecin.
     */
    List<Patient> listerPatientsParMedecin(Integer idMedecin);
    
    /**
     * 🔍 Recherche multicritère de patients parmi ceux suivis par ce médecin.
     */
    List<Patient> rechercherPatientsDuMedecin(Integer idMedecin, String nom, String prenom);
    
    /**
     * 🏥 Consultation sécurisée du dossier complet d'un patient par un médecin.
     * Lève une SecurityException si le médecin ne suit pas ce patient.
     */
    Patient consulterDossierPatientParMedecin(Integer idMedecin, Long idPatient);

    /**
     * Dossier complet d'un patient au sein de l'établissement courant (multi-tenant).
     */
    PatientDossierDTO obtenirDossierComplet(Long idPatient);

    /**
     * Dossier médical du patient authentifié (portail patient, multi-tenant).
     */
    PatientDossierDTO obtenirMonDossier();
}