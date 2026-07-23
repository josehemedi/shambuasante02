package hospicloud.servicesImpl;

import hospicloud.dtos.reception.ReceptionDashboardStatsDTO;
import hospicloud.dtos.reception.AdmissionDTO;
import hospicloud.dtos.reception.MedecinDisponibleDTO;
import hospicloud.dtos.reception.ReceptionRegistrationPointDTO;
import hospicloud.model.reception.Admission;
import hospicloud.dtos.reception.ReceptionRdvCreateDTO;
import hospicloud.dtos.reception.WalkInRegistrationRequestDTO;
import hospicloud.dtos.reception.WalkInRegistrationResponseDTO;
import hospicloud.exceptions.BadRequestException;
import hospicloud.model.Medecin;
import hospicloud.model.Patient;
import hospicloud.model.RendezVous;
import hospicloud.repositories.PatientRepository;
import hospicloud.repositories.MedecinRepository;
import hospicloud.repositories.ReceptionDashboardRepository;
import hospicloud.repositories.UtilisateurRepository;
import hospicloud.security.MedecinQueueTopics;
import hospicloud.security.ReceptionLiveTopics;
import hospicloud.security.TenantContext;
import hospicloud.security.CurrentUserService;
import hospicloud.services.PatientService;
import hospicloud.services.RealtimeNotificationService;
import hospicloud.services.RendezVousService;
import hospicloud.services.ReceptionDashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class ReceptionDashboardServiceImpl implements ReceptionDashboardService {

    private static final Logger log = LoggerFactory.getLogger(ReceptionDashboardServiceImpl.class);

    private final ReceptionDashboardRepository receptionRepository;
    private final RendezVousService rendezVousService;
    private final PatientRepository patientRepository;
    private final PatientService patientService;
    private final MedecinRepository medecinRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final CurrentUserService currentUserService;
    private final RealtimeNotificationService realtimeNotificationService;

    public ReceptionDashboardServiceImpl(ReceptionDashboardRepository receptionRepository,
                                         RendezVousService rendezVousService,
                                         PatientRepository patientRepository,
                                         PatientService patientService,
                                         MedecinRepository medecinRepository,
                                         UtilisateurRepository utilisateurRepository,
                                         SimpMessagingTemplate messagingTemplate,
                                         CurrentUserService currentUserService,
                                         RealtimeNotificationService realtimeNotificationService) {
        this.receptionRepository = receptionRepository;
        this.rendezVousService = rendezVousService;
        this.patientRepository = patientRepository;
        this.patientService = patientService;
        this.medecinRepository = medecinRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.messagingTemplate = messagingTemplate;
        this.currentUserService = currentUserService;
        this.realtimeNotificationService = realtimeNotificationService;
    }

    private Integer getTenantId() {
        return TenantContext.getRequiredHopitalId();
    }

    @Override
    @Transactional(readOnly = true)
    public ReceptionDashboardStatsDTO getDashboardStats() {
        return receptionRepository.getDashboardStats(getTenantId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdmissionDTO> getFileAttente() {
        List<AdmissionDTO> fileAttente = receptionRepository.getAdmissionsEnAttente(getTenantId());
        LocalDateTime now = LocalDateTime.now();
        
        // Calcul du temps d'attente dynamiquement
        for (AdmissionDTO admission : fileAttente) {
            if (admission.getTempsArrivee() != null) {
                long minutes = Duration.between(admission.getTempsArrivee(), now).toMinutes();
                admission.setTempsAttenteMinutes(minutes);
            }
        }
        return fileAttente;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReceptionRegistrationPointDTO> getInscriptionsParHeure() {
        return receptionRepository.getInscriptionsParHeure(getTenantId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RendezVous> listerRendezVousDuJour() {
        return receptionRepository.listerRendezVousDuJour(getTenantId());
    }

    @Override
    @Transactional
    public RendezVous creerRendezVous(ReceptionRdvCreateDTO dto) {
        Integer tenantId = getTenantId();

        if (dto.getDateHeureRdv() == null || !dto.getDateHeureRdv().isAfter(LocalDateTime.now().minusMinutes(1))) {
            throw new BadRequestException("La date du rendez-vous doit être dans le futur.");
        }

        patientRepository.trouverPatientParId(dto.getIdPatient().longValue())
                .orElseThrow(() -> new BadRequestException("Patient introuvable pour cet établissement."));
        medecinRepository.trouverParId(dto.getIdMedecin())
                .orElseThrow(() -> new BadRequestException("Médecin introuvable pour cet établissement."));

        patientRepository.lierPatientAMedecin(dto.getIdMedecin(), dto.getIdPatient().longValue());

        boolean isTeleconsultation = "TELECONSULTATION".equalsIgnoreCase(dto.getCanal());

        RendezVous rdv = new RendezVous();
        rdv.setIdHopital(tenantId);
        rdv.setIdPatient(dto.getIdPatient());
        rdv.setIdMedecin(dto.getIdMedecin());
        rdv.setDateHeureRdv(dto.getDateHeureRdv());
        rdv.setDureeEstimee(dto.getDureeEstimee() != null ? dto.getDureeEstimee() : 30);
        rdv.setMotifVisite(dto.getMotifVisite());
        rdv.setCanal(dto.getCanal() != null ? dto.getCanal() : "PHYSIQUE");
        // Téléconsultation planifiée par la réception = déjà confirmée (pas une demande patient)
        if (isTeleconsultation && (dto.getStatutRdv() == null || dto.getStatutRdv().isBlank())) {
            rdv.setStatutRdv("CONFIRME");
        } else {
            rdv.setStatutRdv(dto.getStatutRdv() != null ? dto.getStatutRdv() : "PROGRAMME");
        }
        Integer userId = currentUserService.getCurrentUtilisateurId();
        rdv.setCreePar(userId);

        RendezVous saved = rendezVousService.creerEtPublier(rdv);

        // Relier le compte portail patient (même email) pour que le RDV apparaisse côté patient
        patientRepository.trouverPatientParId(dto.getIdPatient().longValue()).ifPresent(patient -> {
            if (patient.getEmail() != null && !patient.getEmail().isBlank()) {
                utilisateurRepository.linkPatientAccountByEmail(
                        dto.getIdPatient(), tenantId, patient.getEmail());
            }
        });

        // File d'attente physique : uniquement pour les RDV présentiels
        if (dto.isInscrireFileAttente() && !isTeleconsultation) {
            Admission admission = new Admission();
            admission.setIdHopital(tenantId);
            admission.setIdPatient(dto.getIdPatient());
            admission.setIdMedecin(dto.getIdMedecin());
            admission.setIdRendezVous(saved.getIdRdv());
            admission.setNiveauPriorite(3);
            admission.setTempsArrivee(LocalDateTime.now());
            admission.setStatut("EN_ATTENTE");
            admission.setCreePar(userId);
            admission.setCheckInPar(userId);
            Integer idAdmission = receptionRepository.creerAdmissionRetourId(admission);
            Integer numeroPassage = idAdmission != null
                    ? receptionRepository.allouerNumeroPassage(idAdmission, tenantId)
                    : null;
            String nomPatient = patientRepository.trouverPatientParId(dto.getIdPatient().longValue())
                    .map(this::responseNomPatient)
                    .orElse("Patient");
            publierFileMedecin(tenantId, dto.getIdMedecin(), idAdmission, saved.getIdRdv(),
                    nomPatient, dto.getMotifVisite(), numeroPassage);
        } else {
            messagingTemplate.convertAndSend(ReceptionLiveTopics.destination(tenantId), "NEW_RDV");
        }

        return saved;
    }

    @Override
    @Transactional
    public void changerStatutAdmission(Integer idAdmission, String nouveauStatut) {
        Integer tenantId = getTenantId();
        
        Admission existante = receptionRepository.trouverAdmissionParId(idAdmission, tenantId);
        if (existante == null) {
            throw new IllegalArgumentException("Admission introuvable");
        }

        String normalized = nouveauStatut != null ? nouveauStatut.trim().toUpperCase() : "";
        // Alias métier réception → codes internes
        if ("RECU".equals(normalized) || "RECEIVED".equals(normalized)) {
            normalized = "ENREGISTRE";
        } else if ("WAITING".equals(normalized) || "ATTENTE".equals(normalized)) {
            normalized = "EN_ATTENTE";
        } else if ("WAITING_TRIAGE".equals(normalized) || "TRIAGE".equals(normalized)) {
            normalized = "ATTENTE_TRIAGE";
        } else if ("ORIENTED".equals(normalized)) {
            normalized = "ORIENTE";
        }
        java.util.Set<String> allowed = java.util.Set.of(
                "ATTENTE_TRIAGE", "EN_ATTENTE", "ORIENTE", "ENREGISTRE", "APPELE", "EN_CONSULTATION", "TERMINE", "ABSENT");
        if (!allowed.contains(normalized)) {
            throw new BadRequestException(
                    "Statut invalide. Valeurs: ATTENTE_TRIAGE, EN_ATTENTE, ORIENTE, ENREGISTRE (reçu), ABSENT.");
        }

        receptionRepository.mettreAJourStatutAdmission(idAdmission, tenantId, normalized);

        messagingTemplate.convertAndSend(ReceptionLiveTopics.destination(tenantId), "STATUS_UPDATED");
        if (existante.getIdMedecin() != null) {
            String nomPatient = existante.getIdPatient() != null
                    ? patientRepository.trouverPatientParId(existante.getIdPatient().longValue())
                        .map(this::responseNomPatient)
                        .orElse("Patient")
                    : "Patient";
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "STATUS_UPDATED");
            payload.put("idHopital", tenantId);
            payload.put("idMedecin", existante.getIdMedecin());
            payload.put("idAdmission", idAdmission);
            payload.put("idRendezVous", existante.getIdRendezVous());
            payload.put("patientNom", nomPatient);
            payload.put("statut", normalized);
            payload.put("numeroPassage", existante.getNumeroPassage());
            messagingTemplate.convertAndSend(
                    MedecinQueueTopics.destination(tenantId, existante.getIdMedecin()), payload);
        }
    }

    @Override
    @Transactional
    public void inscrirePatientFileAttente(Admission admission, boolean reqRendezVousStrict) {
        Integer tenantId = getTenantId();
        
        // Politiques de validation métier
        if (reqRendezVousStrict) {
            boolean aRdv = receptionRepository.aRendezVousAujourdhui(admission.getIdPatient(), tenantId);
            if (!aRdv) {
                throw new IllegalStateException("Le patient n'a pas de rendez-vous prévu pour aujourd'hui.");
            }
        }

        admission.setIdHopital(tenantId);
        admission.setStatut("EN_ATTENTE");
        if (admission.getTempsArrivee() == null) {
            admission.setTempsArrivee(LocalDateTime.now());
        }
        Integer userId = currentUserService.getCurrentUtilisateurId();
        if (admission.getCreePar() == null) {
            admission.setCreePar(userId);
        }
        if (admission.getCheckInPar() == null) {
            admission.setCheckInPar(userId);
        }

        Integer idAdmission = receptionRepository.creerAdmissionRetourId(admission);
        Integer numeroPassage = idAdmission != null
                ? receptionRepository.allouerNumeroPassage(idAdmission, tenantId)
                : null;
        String nomPatient = admission.getIdPatient() != null
                ? patientRepository.trouverPatientParId(admission.getIdPatient().longValue())
                    .map(this::responseNomPatient)
                    .orElse("Patient")
                : "Patient";
        if (admission.getIdMedecin() != null) {
            publierFileMedecin(tenantId, admission.getIdMedecin(), idAdmission, admission.getIdRendezVous(),
                    nomPatient, "Arrivée en file d'attente", numeroPassage);
        } else {
            messagingTemplate.convertAndSend(ReceptionLiveTopics.destination(tenantId), "NEW_ADMISSION");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedecinDisponibleDTO> listerMedecinsDisponibles(String specialiteOuService, boolean uniquementEnHoraire) {
        return receptionRepository.listerMedecinsDisponibles(getTenantId(), specialiteOuService, uniquementEnHoraire);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> listerSpecialites() {
        return receptionRepository.listerSpecialites(getTenantId());
    }

    @Override
    @Transactional
    public WalkInRegistrationResponseDTO enregistrerArrivee(WalkInRegistrationRequestDTO request) {
        Integer tenantId = getTenantId();
        Integer userId = currentUserService.getCurrentUtilisateurId();

        if (!StringUtils.hasText(request.getMotifConsultation())) {
            throw new BadRequestException("Le motif général est obligatoire.");
        }
        if (!StringUtils.hasText(request.getServiceDemande())) {
            throw new BadRequestException("Le service demandé est obligatoire.");
        }
        if (!StringUtils.hasText(request.getTypeVisite())) {
            throw new BadRequestException("Le type de visite est obligatoire.");
        }

        Patient patient = resolveOrCreatePatient(request);
        Integer idPatient = patient.getIdPatient().intValue();

        String filtre = StringUtils.hasText(request.getSpecialite())
                ? request.getSpecialite().trim()
                : request.getServiceDemande().trim();

        MedecinDisponibleDTO choix = resolveMedecinOptional(request, filtre);
        Integer idMedecin = choix != null ? choix.getIdMedecin() : null;

        if (idMedecin != null) {
            patientRepository.lierPatientAMedecin(idMedecin, patient.getIdPatient());
        }

        // Priorité clinique : triage uniquement (réception = normale)
        int priorite = 3;
        LocalDateTime arrivee = LocalDateTime.now();
        String motif = request.getMotifConsultation().trim();
        String typeVisite = request.getTypeVisite().trim();
        String service = request.getServiceDemande().trim();
        String motifRdv = typeVisite + " — " + motif + " — Service : " + service;

        Integer idRdv = null;
        if (idMedecin != null) {
            RendezVous rdv = new RendezVous();
            rdv.setIdHopital(tenantId);
            rdv.setIdPatient(idPatient);
            rdv.setIdMedecin(idMedecin);
            rdv.setDateHeureRdv(arrivee.plusMinutes(5));
            rdv.setDureeEstimee(30);
            rdv.setMotifVisite(motifRdv);
            rdv.setCanal("PHYSIQUE");
            rdv.setStatutRdv("PROGRAMME");
            rdv.setCreePar(userId);
            RendezVous savedRdv = rendezVousService.creerEtPublier(rdv);
            idRdv = savedRdv.getIdRdv();
        }

        Admission admission = new Admission();
        admission.setIdHopital(tenantId);
        admission.setIdPatient(idPatient);
        admission.setIdMedecin(idMedecin);
        admission.setIdRendezVous(idRdv);
        admission.setNiveauPriorite(priorite);
        admission.setTempsArrivee(arrivee);
        admission.setStatut("ATTENTE_TRIAGE");
        admission.setCreePar(userId);
        admission.setCheckInPar(userId);
        admission.setTypeVisite(typeVisite);
        admission.setMotifGeneral(motif);
        admission.setServiceDemande(service);
        admission.setObservationsAdmin(StringUtils.hasText(request.getObservationsAdministratives())
                ? request.getObservationsAdministratives().trim() : null);
        admission.setModePaiement(StringUtils.hasText(request.getModePaiement())
                ? request.getModePaiement().trim() : null);

        Integer idAdmission = receptionRepository.creerAdmissionRetourId(admission);
        Integer numeroPassage = idAdmission != null
                ? receptionRepository.allouerNumeroPassage(idAdmission, tenantId)
                : null;

        String nomPatient = responseNomPatient(patient);
        publierNouvelleVisite(tenantId, idMedecin, idAdmission, idRdv, nomPatient, motif, service, numeroPassage);

        WalkInRegistrationResponseDTO response = new WalkInRegistrationResponseDTO();
        response.setIdPatient(idPatient);
        response.setCodePatient(patient.getCodePatient());
        response.setNomPatient(nomPatient);
        response.setIdMedecin(idMedecin);
        response.setNomMedecin(choix != null ? choix.getNomComplet() : null);
        response.setSpecialiteMedecin(choix != null ? choix.getSpecialite() : null);
        response.setIdAdmission(idAdmission);
        response.setIdRendezVous(idRdv);
        response.setNiveauPriorite(priorite);
        response.setMotifConsultation(motif);
        response.setServiceDemande(service);
        response.setNumeroPassage(numeroPassage);
        response.setStatut("ATTENTE_TRIAGE");

        String ticketPart = numeroPassage != null ? " — ticket " + String.format("%03d", numeroPassage) : "";
        if (choix != null) {
            response.setMessage("Visite créée · " + service + " · Dr " + choix.getNomComplet()
                    + " · en attente de triage" + ticketPart + ".");
        } else {
            response.setMessage("Visite créée · orientée vers « " + service
                    + " » · en attente de triage" + ticketPart + ".");
        }
        return response;
    }

    private void publierNouvelleVisite(Integer tenantId,
                                       Integer idMedecin,
                                       Integer idAdmission,
                                       Integer idRdv,
                                       String nomPatient,
                                       String motif,
                                       String service,
                                       Integer numeroPassage) {
        Map<String, Object> receptionPayload = new HashMap<>();
        receptionPayload.put("type", "NOUVELLE_VISITE");
        receptionPayload.put("idHopital", tenantId);
        receptionPayload.put("idAdmission", idAdmission);
        receptionPayload.put("idRendezVous", idRdv);
        receptionPayload.put("idMedecin", idMedecin);
        receptionPayload.put("patientNom", nomPatient);
        receptionPayload.put("motif", motif);
        receptionPayload.put("service", service);
        receptionPayload.put("numeroPassage", numeroPassage);
        receptionPayload.put("statut", "ATTENTE_TRIAGE");

        messagingTemplate.convertAndSend(ReceptionLiveTopics.destination(tenantId), "NEW_ADMISSION");
        messagingTemplate.convertAndSend(ReceptionLiveTopics.destination(tenantId), receptionPayload);

        if (idMedecin != null) {
            messagingTemplate.convertAndSend(ReceptionLiveTopics.destination(tenantId), "NEW_RDV");
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "PATIENT_EN_FILE");
            payload.put("idHopital", tenantId);
            payload.put("idMedecin", idMedecin);
            payload.put("idAdmission", idAdmission);
            payload.put("idRendezVous", idRdv);
            payload.put("patientNom", nomPatient);
            payload.put("motif", motif);
            payload.put("service", service);
            payload.put("numeroPassage", numeroPassage);
            messagingTemplate.convertAndSend(MedecinQueueTopics.destination(tenantId, idMedecin), payload);

            try {
                realtimeNotificationService.notifyPatientAjouteFileMedecin(
                        tenantId, idMedecin, idAdmission, idRdv, nomPatient, motif, numeroPassage);
            } catch (Exception ex) {
                log.warn("Notification médecin file d'attente ignorée: {}", ex.getMessage());
            }
        } else {
            // Le tableau de bord réception est déjà notifié via STOMP (NEW_ADMISSION).
            log.info("Visite sans médecin — service « {} » informé via canal réception (admission #{})",
                    service, idAdmission);
        }
    }

    private void publierFileMedecin(Integer tenantId,
                                    Integer idMedecin,
                                    Integer idAdmission,
                                    Integer idRdv,
                                    String nomPatient,
                                    String motif,
                                    Integer numeroPassage) {
        if (idMedecin == null) {
            messagingTemplate.convertAndSend(ReceptionLiveTopics.destination(tenantId), "NEW_ADMISSION");
            return;
        }
        publierNouvelleVisite(tenantId, idMedecin, idAdmission, idRdv, nomPatient, motif, null, numeroPassage);
    }

    private String responseNomPatient(Patient patient) {
        return ((patient.getPrenom() != null ? patient.getPrenom() : "") + " "
                + (patient.getNom() != null ? patient.getNom() : "")).trim();
    }

    private Patient resolveOrCreatePatient(WalkInRegistrationRequestDTO request) {
        if (request.getIdPatient() != null) {
            return patientRepository.trouverPatientParId(request.getIdPatient().longValue())
                    .orElseThrow(() -> new BadRequestException("Patient introuvable pour cet établissement."));
        }

        Patient patient = new Patient();
        patient.setNom(request.getNom().trim());
        patient.setPrenom(request.getPrenom().trim());
        patient.setSexe(normalizeSexe(request.getSexe()));
        patient.setTelephone(request.getTelephone());
        patient.setEstActif(true);
        patient.setStatutClinique("AMBULATOIRE");
        patient.setDateNaissance(resolveDateNaissance(request));
        patient.setDateEnregistrement(LocalDateTime.now());
        patientService.enregisterPatient(patient);
        if (patient.getIdPatient() == null) {
            throw new BadRequestException("Échec de l'enregistrement du patient.");
        }
        return patient;
    }

    private LocalDate resolveDateNaissance(WalkInRegistrationRequestDTO request) {
        if (StringUtils.hasText(request.getDateNaissance())) {
            return LocalDate.parse(request.getDateNaissance());
        }
        if (request.getAge() != null && request.getAge() >= 0 && request.getAge() <= 130) {
            return LocalDate.now().minusYears(request.getAge());
        }
        throw new BadRequestException("Indiquez l'âge ou la date de naissance du patient.");
    }

    private String normalizeSexe(String sexe) {
        if (!StringUtils.hasText(sexe)) {
            throw new BadRequestException("Le sexe du patient est obligatoire.");
        }
        String s = sexe.trim().toUpperCase(Locale.ROOT);
        if (s.startsWith("F") || s.equals("FEMME") || s.equals("FEMALE")) {
            return "F";
        }
        if (s.startsWith("M") || s.equals("HOMME") || s.equals("MALE")) {
            return "M";
        }
        throw new BadRequestException("Sexe invalide (attendu : M ou F).");
    }

    private MedecinDisponibleDTO resolveMedecinOptional(WalkInRegistrationRequestDTO request, String filtre) {
        if (request.getIdMedecin() != null) {
            Medecin medecin = medecinRepository.trouverParId(request.getIdMedecin())
                    .orElseThrow(() -> new BadRequestException("Médecin introuvable pour cet établissement."));
            MedecinDisponibleDTO dto = new MedecinDisponibleDTO();
            dto.setIdMedecin(medecin.getIdMedecin());
            dto.setNom(medecin.getNom());
            dto.setPrenom(medecin.getPrenom());
            dto.setNomComplet(((medecin.getPrenom() != null ? medecin.getPrenom() : "") + " "
                    + (medecin.getNom() != null ? medecin.getNom() : "")).trim());
            dto.setSpecialite(medecin.getSpecialite());
            dto.setDisponible(medecin.getDisponibiliteStatus());
            return dto;
        }

        if (!request.isAffectationAutomatique()) {
            return null;
        }

        List<MedecinDisponibleDTO> candidats = receptionRepository.listerMedecinsDisponibles(
                getTenantId(), filtre, false);
        if (candidats.isEmpty()) {
            candidats = receptionRepository.listerMedecinsDisponibles(getTenantId(), null, false);
        }
        if (candidats.isEmpty()) {
            return null;
        }

        return candidats.stream()
                .filter(MedecinDisponibleDTO::isEnHoraire)
                .findFirst()
                .orElse(candidats.get(0));
    }

    private int mapUrgenceToPriorite(String niveauUrgence) {
        if (!StringUtils.hasText(niveauUrgence)) {
            return 3;
        }
        return switch (niveauUrgence.trim().toUpperCase(Locale.ROOT)) {
            case "URGENCE", "URGENT", "1" -> 1;
            case "HAUTE", "HIGH", "2" -> 2;
            default -> 3;
        };
    }
}
