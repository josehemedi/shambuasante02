package hospicloud.servicesImpl;

import hospicloud.exceptions.ForbiddenException;
import hospicloud.exceptions.rendezvous.RendezVousNotFoundException;
import hospicloud.model.Medecin;
import hospicloud.model.Patient;
import hospicloud.model.RendezVous;
import hospicloud.repositories.MedecinRepository;
import hospicloud.repositories.PatientRepository;
import hospicloud.repositories.RendezVousRepository;
import hospicloud.security.TenantAuthorization;
import hospicloud.security.TenantContext;
import hospicloud.security.CurrentUserService;
import hospicloud.services.RendezVousService;
import hospicloud.services.RealtimeNotificationService;
import hospicloud.servicesImpl.LiveKitService;
import hospicloud.utils.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RendezVousServiceImpl implements RendezVousService {

    private static final Logger log = LoggerFactory.getLogger(RendezVousServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final RendezVousRepository repository;
    private final MedecinRepository medecinRepository;
    private final PatientRepository patientRepository;
    private final NotificationService notificationService;
    private final RealtimeNotificationService realtimeNotificationService;
    private final LiveKitService liveKitService;
    private final String frontendBaseUrl;
    private final CurrentUserService currentUserService;

    public RendezVousServiceImpl(RendezVousRepository repository,
                                 MedecinRepository medecinRepository,
                                 PatientRepository patientRepository,
                                 NotificationService notificationService,
                                 RealtimeNotificationService realtimeNotificationService,
                                 LiveKitService liveKitService,
                                 @Value("${app.frontend.base-url:http://localhost:5173}") String frontendBaseUrl,
                                 CurrentUserService currentUserService) {
        this.repository = repository;
        this.medecinRepository = medecinRepository;
        this.patientRepository = patientRepository;
        this.notificationService = notificationService;
        this.realtimeNotificationService = realtimeNotificationService;
        this.liveKitService = liveKitService;
        this.frontendBaseUrl = frontendBaseUrl;
        this.currentUserService = currentUserService;
    }

    // =====================================================
    // CRÉATION + ENVOI D'ÉVÉNEMENT
    // =====================================================
    @Override
    @Transactional
    public RendezVous creerEtPublier(RendezVous rdv) {

        if (rdv == null) {
            throw new IllegalArgumentException("Rendez-vous obligatoire");
        }

        Integer hopitalId = TenantContext.getRequiredHopitalId();
        rdv.setIdHopital(hopitalId);
        validerAppartenanceTenant(rdv);
        assertMedecinPeutPlanifierRendezVous(rdv);

        // ================= DEFAULT VALUES =================
        if (rdv.getStatutRdv() == null) {
            rdv.setStatutRdv("PROGRAMME");
        }

        if (rdv.getDureeEstimee() == null) {
            rdv.setDureeEstimee(30);
        }

        if (rdv.getCanal() == null || rdv.getCanal().isBlank()) {
            rdv.setCanal("PHYSIQUE");
        }

        if (rdv.getCreePar() == null) {
            Integer userId = currentUserService.getCurrentUtilisateurId();
            if (userId != null) {
                rdv.setCreePar(userId);
            }
        }

        // ================= SAVE =================
        RendezVous saved = repository.creer(rdv);

        if ("TELECONSULTATION".equalsIgnoreCase(saved.getCanal())) {
            traiterTeleconsultation(saved);
        }

        // ================= EVENT (future RabbitMQ) =================
        publierEvenement(saved, "RDV_CREE");

        return saved;
    }

    private void traiterTeleconsultation(RendezVous saved) {
        String lien = genererLienTeleconsultation(saved);
        saved.setUrlVisio(lien);
        repository.mettreAJourUrlVisio(saved.getIdRdv(), lien);

        Medecin medecin = medecinRepository.trouverParId(saved.getIdMedecin()).orElse(null);
        Patient patient = saved.getIdPatient() == null
                ? null
                : patientRepository.trouverPatientParId(saved.getIdPatient().longValue()).orElse(null);

        String nomMedecin = medecin != null
                ? concatNomPrenom(medecin.getNom(), medecin.getPrenom())
                : "Médecin";
        String nomPatient = patient != null
                ? concatNomPrenom(patient.getNom(), patient.getPrenom())
                : "Patient";
        String emailMedecin = medecin != null ? medecin.getEmail() : null;
        String emailPatient = patient != null ? patient.getEmail() : null;
        String dateFormatee = formaterDate(saved.getDateHeureRdv());

        if (StringUtils.hasText(emailMedecin)) {
            try {
                notificationService.notifierTeleconsultationMedecin(
                        emailMedecin, nomMedecin, nomPatient, dateFormatee, lien);
            } catch (RuntimeException e) {
                log.warn("Échec envoi email médecin pour RDV {}: {}", saved.getIdRdv(), e.getMessage());
            }
        }

        if (StringUtils.hasText(emailPatient)) {
            try {
                notificationService.notifierTeleconsultationPatient(
                        emailPatient, nomPatient, nomMedecin, dateFormatee, lien);
            } catch (RuntimeException e) {
                log.warn("Échec envoi email patient pour RDV {}: {}", saved.getIdRdv(), e.getMessage());
            }
        }
    }

    private String genererLienTeleconsultation(RendezVous saved) {
        String base = frontendBaseUrl.endsWith("/")
                ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                : frontendBaseUrl;
        String room = liveKitService.generateRoomName(saved.getIdHopital(), saved.getIdRdv());
        return base + "/teleconsultation?rdv=" + saved.getIdRdv() + "&room=" + room;
    }

    // =====================================================
    // CONSULTATIONS ET LECTURE
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public List<RendezVous> listerParMedecin(Integer idMedecin) {
        return repository.listerParMedecin(idMedecin);
    }
    @Override
    @Transactional(readOnly = true)
    public List<RendezVous> listerParMedecinEtDate(Integer idMedecin, LocalDate date) {
        return repository.listerParMedecinEtDate(idMedecin, date);
    }
    @Override
    @Transactional(readOnly = true)
    public List<RendezVous> listerRendezVousDuJourParMedecin(Integer idMedecin) {
        return repository.listerRendezVousDuJourParMedecin(idMedecin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RendezVous> listerParHopital() {
        return listerParHopital((Boolean) null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RendezVous> listerParHopital(Boolean mine) {
        TenantAuthorization.assertStaffRole();
        Integer creePar = null;
        if (Boolean.TRUE.equals(mine)) {
            creePar = currentUserService.getCurrentUtilisateurId();
            if (creePar == null) {
                return List.of();
            }
        }
        return repository.listerParHopital(creePar);
    }

    @Override
    @Transactional(readOnly = true)
    public RendezVous obtenirParId(Integer idRdv) {
        RendezVous rdv = repository.trouverParId(idRdv);
        if (rdv == null) {
            throw new RendezVousNotFoundException(idRdv);
        }
        TenantAuthorization.assertPatientOwns(rdv.getIdPatient());
        return rdv;
    }

    // =====================================================
    // VÉRIFICATION DE CRÉNEAU
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public boolean verifierCreneau(Integer idMedecin, LocalDateTime dateHeure) {
        return repository.estCreneauLibre(idMedecin, dateHeure);
    }

    // =====================================================
    // MODIFICATIONS & REPORT
    // =====================================================
    @Override
    @Transactional
    public void modifierRendezVous(RendezVous rdv) {
        TenantAuthorization.assertStaffRole();
        if (rdv == null || rdv.getIdRdv() == null) {
            throw new IllegalArgumentException("Rendez-vous invalide pour modification");
        }
        
        // Le repository s'occupe de valider l'absence de conflit avant l'update SQL
        repository.modifierRendezVous(rdv);
        
        publierEvenement(rdv, "RDV_MODIFIE");
    }

    @Override
    @Transactional
    public void reporterRendezVous(Integer idRdv, LocalDateTime nouvelleDate) {
        TenantAuthorization.assertStaffRole();
        RendezVous rdvActuel = repository.trouverParId(idRdv);
        if (rdvActuel == null) {
            throw new RendezVousNotFoundException(idRdv);
        }

        Medecin medecin = medecinRepository.trouverParId(rdvActuel.getIdMedecin()).orElse(null);
        Patient patient = rdvActuel.getIdPatient() == null
                ? null
                : patientRepository.trouverPatientParId(rdvActuel.getIdPatient().longValue()).orElse(null);

        repository.reporterRendezVous(idRdv, nouvelleDate);
        
        RendezVous rdvAjuste = obtenirParId(idRdv);
        envoyerNotificationReport(rdvActuel, rdvAjuste, medecin, patient);
        publierEvenement(rdvAjuste, "RDV_REPORTE");
    }

    // =====================================================
    // GESTION DU CYCLE DE VIE (STATUTS)
    // =====================================================
    @Override
    @Transactional
    public void confirmerPresence(Integer idRdv) {
        TenantAuthorization.assertStaffRole();
        repository.confirmerPresence(idRdv);
        
        RendezVous rdv = obtenirParId(idRdv);
        publierEvenement(rdv, "PATIENT_PRESENT");
    }

    @Override
    @Transactional
    public void annulerRendezVous(Integer idRdv) {
        TenantAuthorization.assertStaffRole();
        repository.annulerRendezVous(idRdv);
        
        RendezVous rdv = obtenirParId(idRdv);
        publierEvenement(rdv, "RDV_ANNULE");
    }

    @Override
    @Transactional
    public void marquerCommeAbsent(Integer idRdv) {
        TenantAuthorization.assertStaffRole();
        repository.marquerCommeAbsent(idRdv);
        
        RendezVous rdv = obtenirParId(idRdv);
        publierEvenement(rdv, "PATIENT_ABSENT");
    }

    @Override
    @Transactional
    public void marquerCommeTermine(Integer idRdv) {
        TenantAuthorization.assertStaffRole();
        RendezVous rdv = obtenirParId(idRdv);
        if (rdv.getStatutRdv() != null && "VALIDE".equalsIgnoreCase(rdv.getStatutRdv())) {
            return;
        }
        if (rdv.getStatutRdv() != null
                && ("ANNULE".equalsIgnoreCase(rdv.getStatutRdv())
                || "ABSENT".equalsIgnoreCase(rdv.getStatutRdv()))) {
            throw new ForbiddenException("Ce rendez-vous est annulé ou marqué absent.");
        }
        repository.marquerCommeTermine(idRdv);
        publierEvenement(obtenirParId(idRdv), "RDV_TERMINE");
    }

    private void assertMedecinPeutPlanifierRendezVous(RendezVous rdv) {
        if (!currentUserService.isMedecin()) {
            return;
        }
        Integer medecinId = currentUserService.getCurrentMedecinId();
        if (medecinId == null) {
            throw new ForbiddenException("Profil médecin incomplet — création de rendez-vous refusée.");
        }
        if (rdv.getIdMedecin() != null && !medecinId.equals(rdv.getIdMedecin())) {
            throw new ForbiddenException("Un médecin ne peut planifier un rendez-vous que pour son propre agenda.");
        }
        rdv.setIdMedecin(medecinId);
        if (rdv.getIdPatient() == null) {
            throw new IllegalArgumentException("Le patient est obligatoire pour créer un rendez-vous.");
        }
        patientRepository.trouverPatientParId(rdv.getIdPatient().longValue())
                .orElseThrow(() -> new ForbiddenException(
                        "Ce patient n'appartient pas à votre établissement."));
    }

    private void validerAppartenanceTenant(RendezVous rdv) {
        if (rdv.getIdPatient() != null) {
            patientRepository.trouverPatientParId(rdv.getIdPatient().longValue())
                    .orElseThrow(() -> new IllegalArgumentException("Patient introuvable pour cet établissement"));
        }
        if (rdv.getIdMedecin() != null) {
            medecinRepository.trouverParId(rdv.getIdMedecin())
                    .orElseThrow(() -> new IllegalArgumentException("Médecin introuvable pour cet établissement"));
        }
    }

    private void envoyerNotificationReport(RendezVous rdvActuel,
                                           RendezVous rdvAjuste,
                                           Medecin medecin,
                                           Patient patient) {
        String emailMedecin = medecin != null ? medecin.getEmail() : rdvActuel.getEmailMedecin();
        String nomMedecin = medecin != null
                ? concatNomPrenom(medecin.getNom(), medecin.getPrenom())
                : rdvActuel.getNomMedecin();
        String nomPatient = patient != null
                ? concatNomPrenom(patient.getNom(), patient.getPrenom())
                : rdvActuel.getNomPatient();

        if (!StringUtils.hasText(emailMedecin)) {
            log.warn("Aucun email médecin disponible pour le report du RDV {}", rdvActuel.getIdRdv());
            return;
        }

        try {
            notificationService.notifierReportRendezVous(
                    emailMedecin,
                    StringUtils.hasText(nomMedecin) ? nomMedecin : "Médecin",
                    StringUtils.hasText(nomPatient) ? nomPatient : "Patient",
                    formaterDate(rdvActuel.getDateHeureRdv()),
                    formaterDate(rdvAjuste.getDateHeureRdv())
            );
        } catch (RuntimeException e) {
            log.warn("Erreur lors de l'envoi de l'email de report au médecin {}", emailMedecin, e);
        }
    }

    private String concatNomPrenom(String nom, String prenom) {
        if (!StringUtils.hasText(nom) && !StringUtils.hasText(prenom)) {
            return null;
        }
        if (!StringUtils.hasText(nom)) {
            return prenom.trim();
        }
        if (!StringUtils.hasText(prenom)) {
            return nom.trim();
        }
        return nom.trim() + " " + prenom.trim();
    }

    private String formaterDate(LocalDateTime date) {
        return date != null ? date.format(DATE_FORMATTER) : "-";
    }

    // =====================================================
    // SYSTEME D'ÉVÉNEMENTS (Futur Broker / RabbitMQ)
    // =====================================================
    private void publierEvenement(RendezVous rdv, String typeEvenement) {
        log.info("Event RDV [{}] hopital={} patient={} medecin={} date={}",
                typeEvenement, rdv.getIdHopital(), rdv.getIdPatient(), rdv.getIdMedecin(), rdv.getDateHeureRdv());
        if ("RDV_CREE".equals(typeEvenement)) {
            realtimeNotificationService.notifyRendezVousCreated(rdv);
        }
    }
}