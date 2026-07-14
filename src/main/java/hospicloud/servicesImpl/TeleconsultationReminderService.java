package hospicloud.servicesImpl;

import hospicloud.dtos.TeleconsultationReminderCandidate;
import hospicloud.repositories.RendezVousRepository;
import hospicloud.utils.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TeleconsultationReminderService {

    private static final Logger log = LoggerFactory.getLogger(TeleconsultationReminderService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final RendezVousRepository rendezVousRepository;
    private final NotificationService notificationService;
    private final LiveKitService liveKitService;
    private final boolean enabled;
    private final boolean smsEnabled;
    private final int minutesBefore;
    private final int windowMinutes;
    private final String frontendBaseUrl;

    public TeleconsultationReminderService(
            RendezVousRepository rendezVousRepository,
            NotificationService notificationService,
            LiveKitService liveKitService,
            @Value("${app.teleconsultation.reminder.enabled:true}") boolean enabled,
            @Value("${app.teleconsultation.reminder.sms-enabled:true}") boolean smsEnabled,
            @Value("${app.teleconsultation.reminder.minutes-before:30}") int minutesBefore,
            @Value("${app.teleconsultation.reminder.window-minutes:2}") int windowMinutes,
            @Value("${app.frontend.base-url:http://localhost:5173}") String frontendBaseUrl) {
        this.rendezVousRepository = rendezVousRepository;
        this.notificationService = notificationService;
        this.liveKitService = liveKitService;
        this.enabled = enabled;
        this.smsEnabled = smsEnabled;
        this.minutesBefore = Math.max(1, minutesBefore);
        this.windowMinutes = Math.max(1, windowMinutes);
        this.frontendBaseUrl = frontendBaseUrl;
    }

    @Scheduled(cron = "${app.teleconsultation.reminder.cron:0 * * * * ?}")
    public void envoyerRappelsTeleconsultation() {
        if (!enabled) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fenetreDebut = now.plusMinutes(minutesBefore - windowMinutes);
        LocalDateTime fenetreFin = now.plusMinutes(minutesBefore + windowMinutes);

        List<TeleconsultationReminderCandidate> candidats =
                rendezVousRepository.listerTeleconsultationsPourRappel(fenetreDebut, fenetreFin);

        if (candidats.isEmpty()) {
            return;
        }

        log.info("Rappels téléconsultation : {} séance(s) dans la fenêtre T-{} min", candidats.size(), minutesBefore);

        for (TeleconsultationReminderCandidate candidat : candidats) {
            try {
                traiterCandidat(candidat);
            } catch (Exception ex) {
                log.warn(
                        "Échec rappel téléconsultation RDV {} (hôpital {}) : {}",
                        candidat.getIdRdv(),
                        candidat.getIdHopital(),
                        ex.getMessage());
            }
        }
    }

    @Transactional
    void traiterCandidat(TeleconsultationReminderCandidate candidat) {
        if (!rendezVousRepository.reclamerRappel30Min(candidat.getIdRdv(), candidat.getIdHopital())) {
            return;
        }

        String lien = resoudreLienTeleconsultation(candidat);
        String dateFormatee = candidat.getDateHeureRdv() != null
                ? candidat.getDateHeureRdv().format(DATE_FORMATTER)
                : "—";
        String nomPatient = valeurOuDefaut(candidat.getNomPatient(), "Patient");
        String nomMedecin = valeurOuDefaut(candidat.getNomMedecin(), "Médecin");
        String nomHopital = candidat.getNomHopital();

        if (StringUtils.hasText(candidat.getEmailPatient())) {
            try {
                notificationService.notifierRappelTeleconsultationPatient(
                        candidat.getEmailPatient(),
                        nomPatient,
                        nomMedecin,
                        nomHopital,
                        dateFormatee,
                        lien,
                        minutesBefore);
                log.info(
                        "Rappel patient envoyé — RDV {} hôpital {} → {}",
                        candidat.getIdRdv(),
                        candidat.getIdHopital(),
                        candidat.getEmailPatient());
            } catch (RuntimeException ex) {
                log.warn("Échec email rappel patient RDV {} : {}", candidat.getIdRdv(), ex.getMessage());
            }
        } else {
            log.warn("Rappel RDV {} : email patient manquant", candidat.getIdRdv());
        }

        if (StringUtils.hasText(candidat.getEmailMedecin())) {
            try {
                notificationService.notifierRappelTeleconsultationMedecin(
                        candidat.getEmailMedecin(),
                        nomMedecin,
                        nomPatient,
                        nomHopital,
                        dateFormatee,
                        lien,
                        minutesBefore);
                log.info(
                        "Rappel médecin envoyé — RDV {} hôpital {} → {}",
                        candidat.getIdRdv(),
                        candidat.getIdHopital(),
                        candidat.getEmailMedecin());
            } catch (RuntimeException ex) {
                log.warn("Échec email rappel médecin RDV {} : {}", candidat.getIdRdv(), ex.getMessage());
            }
        } else {
            log.warn("Rappel RDV {} : email médecin manquant", candidat.getIdRdv());
        }

        if (smsEnabled) {
            envoyerSmsPatient(candidat, nomPatient, nomMedecin, nomHopital, dateFormatee, lien);
            envoyerSmsMedecin(candidat, nomMedecin, nomPatient, nomHopital, dateFormatee, lien);
        }
    }

    private void envoyerSmsPatient(
            TeleconsultationReminderCandidate candidat,
            String nomPatient,
            String nomMedecin,
            String nomHopital,
            String dateFormatee,
            String lien) {
        if (!StringUtils.hasText(candidat.getTelephonePatient())) {
            log.warn("Rappel RDV {} : téléphone patient manquant", candidat.getIdRdv());
            return;
        }
        try {
            notificationService.notifierRappelTeleconsultationSmsPatient(
                    candidat.getTelephonePatient(),
                    nomPatient,
                    nomMedecin,
                    nomHopital,
                    dateFormatee,
                    lien,
                    minutesBefore);
            log.info(
                    "Rappel SMS patient envoyé — RDV {} hôpital {} → {}",
                    candidat.getIdRdv(),
                    candidat.getIdHopital(),
                    candidat.getTelephonePatient());
        } catch (RuntimeException ex) {
            log.warn("Échec SMS rappel patient RDV {} : {}", candidat.getIdRdv(), ex.getMessage());
        }
    }

    private void envoyerSmsMedecin(
            TeleconsultationReminderCandidate candidat,
            String nomMedecin,
            String nomPatient,
            String nomHopital,
            String dateFormatee,
            String lien) {
        if (!StringUtils.hasText(candidat.getTelephoneMedecin())) {
            log.warn("Rappel RDV {} : téléphone médecin manquant", candidat.getIdRdv());
            return;
        }
        try {
            notificationService.notifierRappelTeleconsultationSmsMedecin(
                    candidat.getTelephoneMedecin(),
                    nomMedecin,
                    nomPatient,
                    nomHopital,
                    dateFormatee,
                    lien,
                    minutesBefore);
            log.info(
                    "Rappel SMS médecin envoyé — RDV {} hôpital {} → {}",
                    candidat.getIdRdv(),
                    candidat.getIdHopital(),
                    candidat.getTelephoneMedecin());
        } catch (RuntimeException ex) {
            log.warn("Échec SMS rappel médecin RDV {} : {}", candidat.getIdRdv(), ex.getMessage());
        }
    }

    private String resoudreLienTeleconsultation(TeleconsultationReminderCandidate candidat) {
        if (StringUtils.hasText(candidat.getUrlVisio())) {
            return candidat.getUrlVisio();
        }
        String base = frontendBaseUrl.endsWith("/")
                ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                : frontendBaseUrl;
        String room = liveKitService.generateRoomName(candidat.getIdHopital(), candidat.getIdRdv());
        return base + "/teleconsultation?rdv=" + candidat.getIdRdv() + "&room=" + room;
    }

    private static String valeurOuDefaut(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }
}
