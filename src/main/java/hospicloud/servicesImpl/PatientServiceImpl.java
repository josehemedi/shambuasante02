package hospicloud.servicesImpl;

import hospicloud.dtos.PatientDossierDTO;
import hospicloud.exceptions.ForbiddenException;
import hospicloud.exceptions.patient.PatientNotFoundException;
import hospicloud.model.Patient;
import hospicloud.repositories.AntecedentRepository;
import hospicloud.repositories.OrdonnanceRepository;
import hospicloud.repositories.PatientRepository;
import hospicloud.repositories.RendezVousRepository;
import hospicloud.security.CurrentUserContext;
import hospicloud.security.CurrentUserService;
import hospicloud.security.TenantAuthorization;
import hospicloud.services.ConsultationMedicaleService;
import hospicloud.services.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final RendezVousRepository rendezVousRepository;
    private final ConsultationMedicaleService consultationMedicaleService;
    private final AntecedentRepository antecedentRepository;
    private final OrdonnanceRepository ordonnanceRepository;
    private final CurrentUserService currentUserService;

    @Autowired
    public PatientServiceImpl(
            PatientRepository patientRepository,
            RendezVousRepository rendezVousRepository,
            ConsultationMedicaleService consultationMedicaleService,
            AntecedentRepository antecedentRepository,
            OrdonnanceRepository ordonnanceRepository,
            CurrentUserService currentUserService) {
        this.patientRepository = patientRepository;
        this.rendezVousRepository = rendezVousRepository;
        this.consultationMedicaleService = consultationMedicaleService;
        this.antecedentRepository = antecedentRepository;
        this.ordonnanceRepository = ordonnanceRepository;
        this.currentUserService = currentUserService;
    }

    // =====================================================
    // LOGIQUE DE VOTRE ANCIEN SERVICE (PORTÉE ÉTABLISSEMENT)
    // =====================================================

    @Override
    @Transactional
    public void enregisterPatient(Patient patient) {
        if (patient == null) {
            throw new IllegalArgumentException("Le patient ne peut pas être null");
        }
        if (patient.getNom() == null || patient.getNom().isBlank()) {
            throw new IllegalArgumentException("Le nom du patient est obligatoire.");
        }
        if (patient.getPrenom() == null || patient.getPrenom().isBlank()) {
            throw new IllegalArgumentException("Le prénom du patient est obligatoire.");
        }
        if (patient.getSexe() == null || patient.getSexe().isBlank()) {
            throw new IllegalArgumentException("Le sexe du patient est obligatoire.");
        }
        if (patient.getDateNaissance() == null) {
            throw new IllegalArgumentException("La date de naissance est obligatoire.");
        }

        if (patient.getDateEnregistrement() == null) {
            patient.setDateEnregistrement(LocalDateTime.now());
        }

        Integer userId = currentUserService.getCurrentUtilisateurId();
        if (userId != null) {
            patient.setCreePar(userId);
        }

        patientRepository.enregistrerPatient(patient);

        Integer medecinId = currentUserService.getCurrentMedecinId();
        if (medecinId != null && patient.getIdPatient() != null) {
            patientRepository.lierPatientAMedecin(medecinId, patient.getIdPatient());
        }
    }

    @Override
    @Transactional
    public void modifierPatient(Patient patient) {
        if (patient == null || patient.getIdPatient() == null) {
            throw new IllegalArgumentException("Patient ou id manquant");
        }
        ensureMedecinCanAccessPatient(patient.getIdPatient());
        Integer userId = currentUserService.getCurrentUtilisateurId();
        if (userId != null) {
            patient.setModifiePar(userId);
        }
        patientRepository.modifierPatient(patient);
    }

    @Override
    @Transactional
    public void supprimerPatient(Long idPatient) {
        if (idPatient == null) {
            throw new IllegalArgumentException("L'identifiant du patient ne peut pas être null");
        }
        ensureMedecinCanAccessPatient(idPatient);
        patientRepository.supprimerPatient(idPatient);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Patient> trouverPatientParId(Long idPatient) {
        if (idPatient == null) return Optional.empty();
        Optional<Patient> patient = patientRepository.trouverPatientParId(idPatient);
        if (patient.isEmpty()) {
            return patient;
        }
        if (currentUserService.isMedecin()) {
            Integer medecinId = currentUserService.getCurrentMedecinId();
            if (medecinId == null
                    || !patientRepository.estPatientAssigneAuMedecin(medecinId, idPatient)) {
                return Optional.empty();
            }
        }
        return patient;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Patient> trouverTousLesPatients() {
        return trouverTousLesPatients((Boolean) null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Patient> trouverTousLesPatients(Boolean mine) {
        if (currentUserService.isMedecin()) {
            Integer medecinId = currentUserService.getCurrentMedecinId();
            if (medecinId == null) {
                return List.of();
            }
            // Le médecin ne voit jamais toute la file hôpital — uniquement ses patients attribués.
            return patientRepository.listerPatientsParMedecin(medecinId);
        }
        if (Boolean.TRUE.equals(mine)) {
            Integer creePar = currentUserService.getCurrentUtilisateurId();
            if (creePar == null) {
                return List.of();
            }
            return patientRepository.trouverTousLesPatients(creePar);
        }
        return patientRepository.trouverTousLesPatients();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Patient> trouverPatientParNumero(String numero) {
        if (numero == null || numero.trim().isEmpty()) return Optional.empty();
        Optional<Patient> patient = patientRepository.trouverPatientParNumero(numero);
        if (patient.isEmpty() || !currentUserService.isMedecin()) {
            return patient;
        }
        if (currentUserService.getCurrentMedecinId() == null) {
            return Optional.empty();
        }
        return patient;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Patient> rechercherParNomEtPrenom(String nom, String prenom) {
        if (currentUserService.isMedecin()) {
            Integer medecinId = currentUserService.getCurrentMedecinId();
            if (medecinId == null) {
                return List.of();
            }
            return patientRepository.rechercherPatientsDuMedecin(medecinId, nom, prenom);
        }
        return patientRepository.rechercherParNomEtPrenom(nom, prenom);
    }

    // =====================================================
    // NOUVELLES MÉTHODES AJOUTÉES (PORTÉE MÉDECIN)
    // =====================================================

    @Override
    @Transactional
    public void lierPatientAMedecin(Integer idMedecin, Long idPatient) {
        if (idMedecin == null || idPatient == null) {
            throw new IllegalArgumentException("Les identifiants médecin et patient sont obligatoires pour créer une liaison.");
        }
        patientRepository.lierPatientAMedecin(idMedecin, idPatient);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Patient> listerPatientsParMedecin(Integer idMedecin) {
        if (idMedecin == null) {
            throw new IllegalArgumentException("L'identifiant du médecin est obligatoire pour lister ses patients.");
        }
        return patientRepository.listerPatientsParMedecin(idMedecin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Patient> rechercherPatientsDuMedecin(Integer idMedecin, String nom, String prenom) {
        if (idMedecin == null) {
            throw new IllegalArgumentException("L'identifiant du médecin est obligatoire pour effectuer une recherche ciblée.");
        }
        return patientRepository.rechercherPatientsDuMedecin(idMedecin, nom, prenom);
    }

    @Override
    @Transactional(readOnly = true)
    public Patient consulterDossierPatientParMedecin(Integer idMedecin, Long idPatient) {
        if (idMedecin == null || idPatient == null) {
            throw new IllegalArgumentException("Les identifiants médecin et patient sont obligatoires pour consulter le dossier.");
        }
        return patientRepository.consulterDossierPatientParMedecin(idMedecin, idPatient);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientDossierDTO obtenirMonDossier() {
        Integer idPatient = CurrentUserContext.getPatientId();
        if (idPatient == null) {
            throw new ForbiddenException("Profil patient requis.");
        }
        TenantAuthorization.assertPatientOwns(idPatient);

        Patient patient = patientRepository.trouverPatientParId(idPatient.longValue())
                .orElseThrow(() -> new PatientNotFoundException(idPatient));
        return construireDossier(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientDossierDTO obtenirDossierComplet(Long idPatient) {
        if (idPatient == null) {
            throw new IllegalArgumentException("L'identifiant du patient est obligatoire.");
        }
        TenantAuthorization.assertStaffRole();
        ensureMedecinCanAccessPatient(idPatient);

        Patient patient = trouverPatientParId(idPatient)
                .orElseThrow(() -> new PatientNotFoundException(idPatient.intValue()));
        return construireDossier(patient);
    }

    private PatientDossierDTO construireDossier(Patient patient) {
        TenantAuthorization.assertSameTenant(patient.getIdHopital());

        Integer patientId = patient.getIdPatient().intValue();
        PatientDossierDTO dossier = new PatientDossierDTO();
        dossier.setPatient(patient);
        dossier.setRendezVous(rendezVousRepository.listerParPatient(patientId));
        dossier.setConsultations(consultationMedicaleService.obtenirHistoriquePatient(patientId));
        dossier.setAntecedents(antecedentRepository.listerParPatient(patientId, 0, 50));

        List<hospicloud.model.Ordonnance> ordonnances = ordonnanceRepository.listerParPatient(patientId);
        if (ordonnances != null) {
            for (hospicloud.model.Ordonnance o : ordonnances) {
                // Évite d'envoyer les QR binaires dans le JSON dossier.
                o.setQrCodeImage(null);
            }
        }
        dossier.setOrdonnances(ordonnances);
        return dossier;
    }

    private void ensureMedecinCanAccessPatient(Long idPatient) {
        if (!currentUserService.isMedecin() || idPatient == null) {
            return;
        }
        Integer medecinId = currentUserService.getCurrentMedecinId();
        if (medecinId == null) {
            throw new ForbiddenException("Accès refusé : profil médecin incomplet.");
        }
        Patient patient = patientRepository.trouverPatientParId(idPatient)
                .orElseThrow(() -> new ForbiddenException("Patient introuvable dans votre établissement."));
        TenantAuthorization.assertSameTenant(patient.getIdHopital());
        if (!patientRepository.estPatientAssigneAuMedecin(medecinId, idPatient)) {
            throw new ForbiddenException("Accès refusé : ce patient ne vous est pas attribué.");
        }
    }
}
