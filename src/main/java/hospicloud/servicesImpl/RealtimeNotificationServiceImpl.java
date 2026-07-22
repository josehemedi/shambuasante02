package hospicloud.servicesImpl;



import hospicloud.dtos.LiveNotificationDTO;

import hospicloud.model.Hopital;

import hospicloud.model.Medecin;

import hospicloud.model.Patient;

import hospicloud.model.RendezVous;

import hospicloud.model.Role;

import hospicloud.repositories.HopitalRepository;

import hospicloud.repositories.MedecinRepository;

import hospicloud.repositories.PatientRepository;

import hospicloud.repositories.UtilisateurRepository;

import hospicloud.security.RealtimeNotificationTopics;

import hospicloud.security.TenantContext;

import hospicloud.services.RealtimeNotificationService;

import hospicloud.utils.NotificationService;

import hospicloud.utils.TenantReportParamsHelper;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import org.springframework.stereotype.Service;

import org.springframework.util.StringUtils;



import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;

import java.math.BigDecimal;

import java.math.RoundingMode;

import java.util.List;

import java.util.Locale;

import java.util.Optional;

import java.util.UUID;



@Service

public class RealtimeNotificationServiceImpl implements RealtimeNotificationService {



    private static final Logger log = LoggerFactory.getLogger(RealtimeNotificationServiceImpl.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");



    private final SimpMessagingTemplate messagingTemplate;

    private final UtilisateurRepository utilisateurRepository;

    private final PatientRepository patientRepository;

    private final MedecinRepository medecinRepository;

    private final HopitalRepository hopitalRepository;

    private final NotificationService notificationService;



    public RealtimeNotificationServiceImpl(SimpMessagingTemplate messagingTemplate,

                                           UtilisateurRepository utilisateurRepository,

                                           PatientRepository patientRepository,

                                           MedecinRepository medecinRepository,

                                           HopitalRepository hopitalRepository,

                                           NotificationService notificationService) {

        this.messagingTemplate = messagingTemplate;

        this.utilisateurRepository = utilisateurRepository;

        this.patientRepository = patientRepository;

        this.medecinRepository = medecinRepository;

        this.hopitalRepository = hopitalRepository;

        this.notificationService = notificationService;

    }



    @Override

    public void notifyRendezVousCreated(RendezVous rdv) {

        if (rdv == null || rdv.getIdHopital() == null) {

            return;

        }



        Integer previousTenant = TenantContext.getHopitalId();

        TenantContext.setHopitalId(rdv.getIdHopital());

        try {

            Patient patient = rdv.getIdPatient() != null

                    ? patientRepository.trouverPatientParId(rdv.getIdPatient().longValue()).orElse(null)

                    : null;

            Medecin medecin = rdv.getIdMedecin() != null

                    ? medecinRepository.trouverParId(rdv.getIdMedecin()).orElse(null)

                    : null;

            Hopital hopital = hopitalRepository.rechercherhopitalParId(rdv.getIdHopital().longValue());



            String nomPatient = formatName(patient != null ? patient.getPrenom() : null, patient != null ? patient.getNom() : null, "Patient");

            String nomMedecin = formatName(medecin != null ? medecin.getPrenom() : null, medecin != null ? medecin.getNom() : null, "Médecin");

            String nomHopital = TenantReportParamsHelper.resolveNomCommercial(hopital, "Shambua Santé");

            String dateFormatee = rdv.getDateHeureRdv() != null ? rdv.getDateHeureRdv().format(DATE_FORMATTER) : "—";

            String motif = StringUtils.hasText(rdv.getMotifVisite()) ? rdv.getMotifVisite() : "Consultation";



            sendToPatientUser(rdv, nomPatient, nomMedecin, dateFormatee, motif);

            sendToMedecinUser(rdv, nomPatient, nomMedecin, dateFormatee, motif);

            // Emails de confirmation seulement si le RDV n'est pas une simple demande en attente
            if (!"EN_ATTENTE".equalsIgnoreCase(rdv.getStatutRdv())) {
                sendConfirmationEmails(rdv, patient, medecin, nomPatient, nomMedecin, nomHopital, dateFormatee, motif);
            }

        } finally {

            if (previousTenant != null) {

                TenantContext.setHopitalId(previousTenant);

            } else {

                TenantContext.clear();

            }

        }

    }



    @Override
    public void notifyRendezVousAccepted(RendezVous rdv) {
        notifyRendezVousDecision(rdv, true);
    }

    @Override
    public void notifyRendezVousRejected(RendezVous rdv) {
        notifyRendezVousDecision(rdv, false);
    }

    private void notifyRendezVousDecision(RendezVous rdv, boolean accepted) {
        if (rdv == null || rdv.getIdHopital() == null || rdv.getIdPatient() == null) {
            return;
        }
        Integer previousTenant = TenantContext.getHopitalId();
        TenantContext.setHopitalId(rdv.getIdHopital());
        try {
            Patient patient = patientRepository.trouverPatientParId(rdv.getIdPatient().longValue()).orElse(null);
            Medecin medecin = rdv.getIdMedecin() != null
                    ? medecinRepository.trouverParId(rdv.getIdMedecin()).orElse(null)
                    : null;
            String nomMedecin = formatName(
                    medecin != null ? medecin.getPrenom() : null,
                    medecin != null ? medecin.getNom() : null,
                    "Médecin");
            String dateFormatee = rdv.getDateHeureRdv() != null ? rdv.getDateHeureRdv().format(DATE_FORMATTER) : "—";

            Optional<Integer> userId = utilisateurRepository.findUtilisateurIdByPatient(
                    rdv.getIdPatient(), rdv.getIdHopital());
            if (userId.isEmpty()) {
                return;
            }

            LiveNotificationDTO dto = baseNotification(rdv);
            if (accepted) {
                dto.setType("RDV_ACCEPTED");
                dto.setTitle("Appointment accepted");
                dto.setTitleFr("Rendez-vous accepté");
                dto.setMessage("Dr " + nomMedecin + " accepted your appointment on " + dateFormatee + ".");
                dto.setMessageFr("Le Dr " + nomMedecin + " a accepté votre rendez-vous du " + dateFormatee + ".");
                dto.setTone("success");
            } else {
                dto.setType("RDV_REJECTED");
                dto.setTitle("Appointment declined");
                dto.setTitleFr("Rendez-vous refusé");
                dto.setMessage("Dr " + nomMedecin + " could not accept your appointment on " + dateFormatee + ".");
                dto.setMessageFr("Le Dr " + nomMedecin + " n'a pas pu accepter votre rendez-vous du " + dateFormatee + ".");
                dto.setTone("warning");
            }
            dto.setLink("/appointments");
            publish(rdv.getIdHopital(), userId.get(), dto);

            if (accepted) {
                Hopital hopital = hopitalRepository.rechercherhopitalParId(rdv.getIdHopital().longValue());
                String nomHopital = TenantReportParamsHelper.resolveNomCommercial(hopital, "Shambua Santé");
                String nomPatient = formatName(
                        patient != null ? patient.getPrenom() : null,
                        patient != null ? patient.getNom() : null,
                        "Patient");
                String motif = StringUtils.hasText(rdv.getMotifVisite()) ? rdv.getMotifVisite() : "Consultation";
                sendConfirmationEmails(rdv, patient, medecin, nomPatient, nomMedecin, nomHopital, dateFormatee, motif);
            }
        } finally {
            if (previousTenant != null) {
                TenantContext.setHopitalId(previousTenant);
            } else {
                TenantContext.clear();
            }
        }
    }

    @Override
    public void notifyPaymentRecorded(

            Integer hopitalId,

            Integer idFacture,

            BigDecimal amount,

            String invoiceNumber,

            String patientName,

            String cashierLabel,

            String method,

            String paymentStatus,

            Integer excludeUserId) {

        if (hopitalId == null || idFacture == null || amount == null) {

            return;

        }



        Integer previousTenant = TenantContext.getHopitalId();

        TenantContext.setHopitalId(hopitalId);

        try {

            List<Integer> adminUserIds = utilisateurRepository.findActiveUtilisateurIdsByRole(

                    hopitalId, Role.TENANT_ADMIN);

            if (adminUserIds.isEmpty()) {

                log.debug("Aucun administrateur actif pour notifier le paiement facture={}", idFacture);

                return;

            }



            String montant = formatAmount(amount);

            String facture = StringUtils.hasText(invoiceNumber) ? invoiceNumber : String.valueOf(idFacture);

            String patient = StringUtils.hasText(patientName) ? patientName : "Patient";

            String caissier = StringUtils.hasText(cashierLabel) ? cashierLabel : "Caisse";

            String mode = formatPaymentMethod(method);

            String statut = formatPaymentStatus(paymentStatus);



            LiveNotificationDTO dto = new LiveNotificationDTO();

            dto.setId(UUID.randomUUID().toString());

            dto.setType("PAYMENT_RECORDED");

            dto.setIdHopital(hopitalId);

            dto.setCreatedAt(LocalDateTime.now());

            dto.setTitle("Payment recorded");

            dto.setTitleFr("Encaissement enregistré");

            dto.setMessage("Payment of " + montant + " recorded for " + patient

                    + " (invoice " + facture + ") via " + mode + ". Cashier: " + caissier

                    + ". Status: " + statut + ".");

            dto.setMessageFr("Paiement de " + montant + " enregistré pour " + patient

                    + " (facture " + facture + ") par " + mode + ". Caissier : " + caissier

                    + ". Statut : " + statut + ".");

            dto.setLink("/billing");

            dto.setTone("success");



            for (Integer adminUserId : adminUserIds) {

                if (excludeUserId != null && excludeUserId.equals(adminUserId)) {

                    continue;

                }

                publish(hopitalId, adminUserId, dto);

            }

        } finally {

            if (previousTenant != null) {

                TenantContext.setHopitalId(previousTenant);

            } else {

                TenantContext.clear();

            }

        }

    }



    @Override
    public void notifyArchivistesDossierPatientSorti(
            Integer hopitalId,
            Long archiveId,
            Long patientId,
            String typeEpisode,
            Integer excludeUserId) {
        if (hopitalId == null || archiveId == null || patientId == null) {
            return;
        }

        Integer previousTenant = TenantContext.getHopitalId();
        TenantContext.setHopitalId(hopitalId);
        try {
            List<Integer> archivisteIds = utilisateurRepository.findActiveUtilisateurIdsByRole(
                    hopitalId, Role.ARCHIVISTE);
            if (archivisteIds.isEmpty()) {
                log.debug("Aucun archiviste actif pour notifier la sortie patient archive={}", archiveId);
                return;
            }

            Patient patient = patientRepository.trouverPatientParId(patientId).orElse(null);
            String nomPatient = formatName(
                    patient != null ? patient.getPrenom() : null,
                    patient != null ? patient.getNom() : null,
                    "Patient");
            String episodeLabel = formatTypeEpisode(typeEpisode);

            LiveNotificationDTO dto = new LiveNotificationDTO();
            dto.setId(UUID.randomUUID().toString());
            dto.setType("ARCHIVE_DOSSIER_SORTIE");
            dto.setIdHopital(hopitalId);
            dto.setCreatedAt(LocalDateTime.now());
            dto.setTitle("Patient discharged — record ready to archive");
            dto.setTitleFr("Sortie officielle — dossier prêt à archiver");
            dto.setMessage("Patient " + nomPatient + " has been officially discharged (" + episodeLabel
                    + "). The medical record is ready to be archived.");
            dto.setMessageFr("Le patient " + nomPatient + " a reçu une sortie officielle ("
                    + episodeLabel + "). Son dossier est prêt à être archivé — veuillez le vérifier.");
            dto.setLink("/archives/" + archiveId);
            dto.setTone("warning");

            for (Integer archivisteId : archivisteIds) {
                if (excludeUserId != null && excludeUserId.equals(archivisteId)) {
                    continue;
                }
                publish(hopitalId, archivisteId, dto);
            }
        } finally {
            if (previousTenant != null) {
                TenantContext.setHopitalId(previousTenant);
            } else {
                TenantContext.clear();
            }
        }
    }

    @Override
    public void notifyPatientAjouteFileMedecin(
            Integer hopitalId,
            Integer idMedecin,
            Integer idAdmission,
            Integer idRdv,
            String nomPatient,
            String motif,
            Integer numeroPassage) {
        if (hopitalId == null || idMedecin == null) {
            return;
        }

        Optional<Integer> userId = utilisateurRepository.findUtilisateurIdByMedecin(idMedecin, hopitalId);
        if (userId.isEmpty()) {
            log.debug("Aucun compte utilisateur pour notifier la file médecin id_medecin={}", idMedecin);
            return;
        }

        String patientLabel = StringUtils.hasText(nomPatient) ? nomPatient : "Patient";
        String motifLabel = StringUtils.hasText(motif) ? motif : "—";
        String ticket = numeroPassage != null ? String.format("%03d", numeroPassage) : "—";

        LiveNotificationDTO dto = new LiveNotificationDTO();
        dto.setId(UUID.randomUUID().toString());
        dto.setType("PATIENT_EN_FILE");
        dto.setIdHopital(hopitalId);
        dto.setIdRdv(idRdv);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setTitle("Patient added to your queue");
        dto.setTitleFr("Patient ajouté à votre file");
        dto.setMessage(patientLabel + " is waiting (ticket " + ticket + "). Reason: " + motifLabel + ".");
        dto.setMessageFr(patientLabel + " est en attente (ticket " + ticket + "). Motif : " + motifLabel + ".");
        dto.setLink("/waiting-room");
        dto.setTone("warning");

        publish(hopitalId, userId.get(), dto);
    }

    @Override
    public void notifyOrdonnanceEnvoyeeAuPatient(
            Integer hopitalId,
            Integer idPatient,
            Long idOrdonnance,
            String numeroOrdonnance,
            String nomMedecin) {
        if (hopitalId == null || idPatient == null) {
            return;
        }

        Optional<Integer> userId = utilisateurRepository.findUtilisateurIdByPatient(idPatient, hopitalId);
        if (userId.isEmpty()) {
            log.debug("Aucun compte utilisateur patient pour id_patient={} (ordonnance)", idPatient);
            return;
        }

        String ref = StringUtils.hasText(numeroOrdonnance)
                ? numeroOrdonnance
                : (idOrdonnance != null ? "ORD-" + idOrdonnance : "Ordonnance");
        String medecinLabel = StringUtils.hasText(nomMedecin) ? nomMedecin : "votre médecin";

        LiveNotificationDTO dto = new LiveNotificationDTO();
        dto.setId(UUID.randomUUID().toString());
        dto.setType("ORDONNANCE_ENVOYEE");
        dto.setIdHopital(hopitalId);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setTitle("New prescription received");
        dto.setTitleFr("Nouvelle ordonnance reçue");
        dto.setMessage(medecinLabel + " sent you prescription " + ref + ".");
        dto.setMessageFr(medecinLabel + " vous a transmis l'ordonnance " + ref + ".");
        dto.setLink("/records");
        dto.setTone("success");

        publish(hopitalId, userId.get(), dto);
    }

    @Override
    public void notifyDocumentCliniqueEnvoyeAuPatient(
            Integer hopitalId,
            Integer idPatient,
            String typeDocument,
            String titre,
            String nomMedecin,
            Integer idDocument) {
        if (hopitalId == null || idPatient == null) {
            return;
        }

        Optional<Integer> userId = utilisateurRepository.findUtilisateurIdByPatient(idPatient, hopitalId);
        if (userId.isEmpty()) {
            log.debug("Aucun compte utilisateur patient pour id_patient={} (document clinique)", idPatient);
            return;
        }

        String type = typeDocument != null ? typeDocument.toUpperCase(Locale.ROOT) : "DOCUMENT";
        String typeFr = switch (type) {
            case "LABO", "LAB_RESULT" -> "Résultat de laboratoire";
            case "CONSULTATION" -> "Fiche de consultation";
            case "ORDONNANCE" -> "Ordonnance";
            default -> "Document médical";
        };
        String medecinLabel = StringUtils.hasText(nomMedecin) ? nomMedecin : "votre médecin";
        String titreLabel = StringUtils.hasText(titre) ? titre : typeFr;

        LiveNotificationDTO dto = new LiveNotificationDTO();
        dto.setId(UUID.randomUUID().toString());
        dto.setType("DOCUMENT_CLINIQUE_ENVOYE");
        dto.setIdHopital(hopitalId);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setTitle(typeFr + " received");
        dto.setTitleFr(typeFr + " reçu");
        dto.setMessage(medecinLabel + " sent you: " + titreLabel);
        dto.setMessageFr(medecinLabel + " vous a transmis : " + titreLabel);
        dto.setLink(type.contains("LAB") ? "/my-lab-results" : "/records");
        dto.setTone("success");

        publish(hopitalId, userId.get(), dto);
    }

    private String formatTypeEpisode(String typeEpisode) {
        if (!StringUtils.hasText(typeEpisode)) {
            return "épisode de soins";
        }
        return switch (typeEpisode.toUpperCase(Locale.ROOT)) {
            case "HOSPITALISATION" -> "hospitalisation";
            case "URGENCE" -> "passage aux urgences";
            case "CONSULTATION" -> "consultation";
            default -> typeEpisode.toLowerCase(Locale.ROOT);
        };
    }



    private void sendToPatientUser(RendezVous rdv, String nomPatient, String nomMedecin,

                                   String dateFormatee, String motif) {

        if (rdv.getIdPatient() == null) return;



        Optional<Integer> userId = utilisateurRepository.findUtilisateurIdByPatient(

                rdv.getIdPatient(), rdv.getIdHopital());

        if (userId.isEmpty()) {

            log.debug("Aucun compte utilisateur patient pour id_patient={}", rdv.getIdPatient());

            return;

        }



        LiveNotificationDTO dto = baseNotification(rdv);

        boolean demande = "EN_ATTENTE".equalsIgnoreCase(rdv.getStatutRdv());
        if (demande) {
            dto.setType("RDV_DEMANDE");
            dto.setTitle("Appointment request sent");
            dto.setTitleFr("Demande de rendez-vous envoyée");
            dto.setMessage("Your request with Dr " + nomMedecin + " for " + dateFormatee + " is pending approval. Reason: " + motif + ".");
            dto.setMessageFr("Votre demande avec le Dr " + nomMedecin + " pour le " + dateFormatee + " est en attente d'acceptation. Motif : " + motif + ".");
            dto.setTone("warning");
        } else {
            dto.setTitle("Appointment confirmed");
            dto.setTitleFr("Rendez-vous confirmé");
            dto.setMessage("Your appointment with Dr " + nomMedecin + " is scheduled on " + dateFormatee + ". Reason: " + motif + ".");
            dto.setMessageFr("Votre rendez-vous avec le Dr " + nomMedecin + " est prévu le " + dateFormatee + ". Motif : " + motif + ".");
            dto.setTone("primary");
        }

        dto.setLink("/appointments");



        publish(rdv.getIdHopital(), userId.get(), dto);

    }



    private void sendToMedecinUser(RendezVous rdv, String nomPatient, String nomMedecin,

                                   String dateFormatee, String motif) {

        if (rdv.getIdMedecin() == null) return;



        Optional<Integer> userId = utilisateurRepository.findUtilisateurIdByMedecin(

                rdv.getIdMedecin(), rdv.getIdHopital());

        if (userId.isEmpty()) {

            log.debug("Aucun compte utilisateur médecin pour id_medecin={}", rdv.getIdMedecin());

            return;

        }



        LiveNotificationDTO dto = baseNotification(rdv);

        boolean demande = "EN_ATTENTE".equalsIgnoreCase(rdv.getStatutRdv());
        if (demande) {
            dto.setType("RDV_DEMANDE");
            dto.setTitle("New appointment request");
            dto.setTitleFr("Nouvelle demande de rendez-vous");
            dto.setMessage(nomPatient + " requested an appointment on " + dateFormatee + ". Reason: " + motif + ".");
            dto.setMessageFr(nomPatient + " a demandé un rendez-vous le " + dateFormatee + ". Motif : " + motif + ".");
            dto.setTone("warning");
        } else {
            dto.setTitle("New appointment scheduled");
            dto.setTitleFr("Nouveau rendez-vous planifié");
            dto.setMessage("A new appointment with " + nomPatient + " is scheduled on " + dateFormatee + ". Reason: " + motif + ".");
            dto.setMessageFr("Un nouveau rendez-vous avec " + nomPatient + " est prévu le " + dateFormatee + ". Motif : " + motif + ".");
            dto.setTone("success");
        }

        dto.setLink("/appointments");



        publish(rdv.getIdHopital(), userId.get(), dto);

    }



    private LiveNotificationDTO baseNotification(RendezVous rdv) {

        LiveNotificationDTO dto = new LiveNotificationDTO();

        dto.setId(UUID.randomUUID().toString());

        dto.setType("RDV_CREATED");

        dto.setIdRdv(rdv.getIdRdv());

        dto.setIdHopital(rdv.getIdHopital());

        dto.setCreatedAt(LocalDateTime.now());

        return dto;

    }



    private void publish(Integer tenantId, Integer userId, LiveNotificationDTO dto) {

        messagingTemplate.convertAndSend(RealtimeNotificationTopics.destination(tenantId, userId), dto);

        log.info("Notification temps réel envoyée tenant={} utilisateur={} ({})", tenantId, userId, dto.getType());

    }



    private void sendConfirmationEmails(RendezVous rdv, Patient patient, Medecin medecin,

                                        String nomPatient, String nomMedecin, String nomHopital,

                                        String dateFormatee, String motif) {

        if (medecin != null && StringUtils.hasText(medecin.getEmail())) {

            try {

                notificationService.notifierConfirmationRendezVous(

                        medecin.getEmail(), nomMedecin, nomPatient, dateFormatee);

            } catch (RuntimeException e) {

                log.warn("Échec email confirmation médecin RDV {}: {}", rdv.getIdRdv(), e.getMessage());

            }

        }



        String emailPatient = resolvePatientEmail(rdv, patient);

        if (!StringUtils.hasText(emailPatient)) {

            log.warn("RDV {} : aucun email patient trouvé (fiche patient ou compte utilisateur).", rdv.getIdRdv());

            return;

        }



        try {

            notificationService.notifierCreationRendezVousPatient(

                    emailPatient,

                    nomPatient,

                    nomMedecin,

                    nomHopital,

                    dateFormatee,

                    motif,

                    rdv.getCanal(),

                    rdv.getDureeEstimee(),

                    rdv.getUrlVisio());

            log.info("Email de confirmation RDV envoyé au patient {} pour RDV {}", emailPatient, rdv.getIdRdv());

        } catch (RuntimeException e) {

            log.warn("Échec email confirmation patient RDV {}: {}", rdv.getIdRdv(), e.getMessage());

        }

    }



    private String resolvePatientEmail(RendezVous rdv, Patient patient) {

        if (patient != null && StringUtils.hasText(patient.getEmail())) {

            return patient.getEmail().trim();

        }

        if (rdv.getIdPatient() == null || rdv.getIdHopital() == null) {

            return null;

        }

        return utilisateurRepository.findEmailByPatient(rdv.getIdPatient(), rdv.getIdHopital()).orElse(null);

    }



    private String formatName(String prenom, String nom, String fallback) {

        String full = ((prenom != null ? prenom : "") + " " + (nom != null ? nom : "")).trim();

        return full.isEmpty() ? fallback : full;

    }



    private static String formatAmount(BigDecimal amount) {

        return amount.setScale(0, RoundingMode.HALF_UP).toPlainString() + " GNF";

    }



    private static String formatPaymentMethod(String method) {

        if (!StringUtils.hasText(method)) {

            return "Espèces";

        }

        return switch (method.trim().toLowerCase(Locale.ROOT)) {

            case "mobile_money" -> "Mobile money";

            case "card" -> "Carte bancaire";

            case "transfer" -> "Virement";

            default -> "Espèces";

        };

    }



    private static String formatPaymentStatus(String status) {

        if (!StringUtils.hasText(status)) {

            return "En attente";

        }

        return switch (status.trim().toUpperCase(Locale.ROOT)) {

            case "PAYE" -> "Payé";

            case "PARTIEL" -> "Paiement partiel";

            default -> status;

        };

    }

}


