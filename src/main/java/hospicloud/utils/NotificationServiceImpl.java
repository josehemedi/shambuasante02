package hospicloud.utils;

import hospicloud.utils.NotificationService;
import hospicloud.utils.EmailService;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final EmailService emailService;
    private final SmsService smsService;

    public NotificationServiceImpl(EmailService emailService, SmsService smsService) {
        this.emailService = emailService;
        this.smsService = smsService;
    }

    @Override
    public void notifierModificationRendezVous(
            String emailMedecin,
            String nomMedecin,
            String nomPatient,
            String ancienneDate,
            String nouvelleDate
    ) {

        String sujet = "📅 Modification de rendez-vous";

        String html = """
        <!DOCTYPE html>
        <html>
        <body style="font-family: Arial; background:#f4f6f9; padding:20px;">

        <div style="max-width:600px; margin:auto; background:white; padding:20px; border-radius:10px;">

        <h2 style="color:#2c3e50;">🏥 Hôpital Cloud System</h2>

        <p>Bonjour Dr <b>%s</b>,</p>

        <p>Le rendez-vous suivant a été modifié :</p>

        <table style="width:100%%; border-collapse: collapse;">
        <tr><td><b>Patient</b></td><td>%s</td></tr>
        <tr><td><b>Ancienne date</b></td><td>%s</td></tr>
        <tr><td><b>Nouvelle date</b></td><td>%s</td></tr>
        </table>

        <p style="margin-top:20px;">Merci de consulter votre planning.</p>

        <hr>
        <small style="color:gray;">Hôpital Cloud System - Notification automatique</small>

        </div>

        </body>
        </html>
        """.formatted(nomMedecin, nomPatient, ancienneDate, nouvelleDate);

        emailService.envoyerEmailHtml(emailMedecin, sujet, html);
    }

    @Override
    public void notifierReportRendezVous(
            String emailMedecin,
            String nomMedecin,
            String nomPatient,
            String ancienneDate,
            String nouvelleDate
    ) {

        String sujet = "📅 Report de rendez-vous";

        String html = """
        <!DOCTYPE html>
        <html>
        <body style="font-family: Arial; background:#f4f6f9; padding:20px;">

        <div style="max-width:600px; margin:auto; background:white; padding:20px; border-radius:10px;">

        <h2 style="color:#2c3e50;">🏥 Hôpital Cloud System</h2>

        <p>Bonjour Dr <b>%s</b>,</p>

        <p>Le rendez-vous suivant a été reporté :</p>

        <table style="width:100%%; border-collapse: collapse;">
        <tr><td><b>Patient</b></td><td>%s</td></tr>
        <tr><td><b>Ancienne date</b></td><td>%s</td></tr>
        <tr><td><b>Nouvelle date</b></td><td>%s</td></tr>
        </table>

        <p style="margin-top:20px;">Merci de consulter votre planning.</p>

        <hr>
        <small style="color:gray;">Hôpital Cloud System - Notification automatique</small>

        </div>

        </body>
        </html>
        """.formatted(nomMedecin, nomPatient, ancienneDate, nouvelleDate);

        emailService.envoyerEmailHtml(emailMedecin, sujet, html);
    }

    @Override
    public void notifierAnnulationRendezVous(
            String emailMedecin,
            String nomMedecin,
            String nomPatient,
            String dateRdv
    ) {

        String sujet = "❌ Annulation de rendez-vous";

        String html = """
        <!DOCTYPE html>
        <html>
        <body style="font-family: Arial; background:#f4f6f9; padding:20px;">

        <div style="max-width:600px; margin:auto; background:white; padding:20px; border-radius:10px;">

        <h2 style="color:#c0392b;">❌ Annulation de rendez-vous</h2>

        <p>Bonjour Dr <b>%s</b>,</p>

        <p>Le rendez-vous suivant a été annulé :</p>

        <table style="width:100%%; border-collapse: collapse;">
        <tr><td><b>Patient</b></td><td>%s</td></tr>
        <tr><td><b>Date du rendez-vous</b></td><td>%s</td></tr>
        </table>

        <p style="margin-top:20px;">Veuillez mettre à jour votre planning.</p>

        <hr>
        <small style="color:gray;">Hôpital Cloud System - Notification automatique</small>

        </div>

        </body>
        </html>
        """.formatted(nomMedecin, nomPatient, dateRdv);

        emailService.envoyerEmailHtml(emailMedecin, sujet, html);
    }

    @Override
    public void notifierConfirmationRendezVous(
            String emailMedecin,
            String nomMedecin,
            String nomPatient,
            String dateRdv
    ) {

        String sujet = "✅ Confirmation de rendez-vous";

        String html = """
        <!DOCTYPE html>
        <html>
        <body style="font-family: Arial; background:#f4f6f9; padding:20px;">

        <div style="max-width:600px; margin:auto; background:white; padding:20px; border-radius:10px;">

        <h2 style="color:#27ae60;">✅ Rendez-vous confirmé</h2>

        <p>Bonjour Dr <b>%s</b>,</p>

        <p>Un rendez-vous a été confirmé :</p>

        <table style="width:100%%; border-collapse: collapse;">
        <tr><td><b>Patient</b></td><td>%s</td></tr>
        <tr><td><b>Date du rendez-vous</b></td><td>%s</td></tr>
        </table>

        <p style="margin-top:20px;">Merci de préparer la consultation.</p>

        <hr>
        <small style="color:gray;">Hôpital Cloud System - Notification automatique</small>

        </div>

        </body>
        </html>
        """.formatted(nomMedecin, nomPatient, dateRdv);

        emailService.envoyerEmailHtml(emailMedecin, sujet, html);
    }

    @Override
    public void notifierConfirmationRendezVousPatient(
            String emailPatient,
            String nomPatient,
            String nomMedecin,
            String dateRdv
    ) {
        String sujet = "✅ Confirmation de votre rendez-vous";

        String html = """
        <!DOCTYPE html>
        <html>
        <body style="font-family: Arial; background:#f4f6f9; padding:20px;">
        <div style="max-width:600px; margin:auto; background:white; padding:20px; border-radius:10px;">
        <h2 style="color:#27ae60;">✅ Rendez-vous confirmé</h2>
        <p>Bonjour <b>%s</b>,</p>
        <p>Votre rendez-vous médical a été confirmé :</p>
        <table style="width:100%%; border-collapse: collapse;">
        <tr><td><b>Médecin</b></td><td>Dr %s</td></tr>
        <tr><td><b>Date</b></td><td>%s</td></tr>
        </table>
        <p style="margin-top:20px;">Merci de vous présenter à l'heure indiquée.</p>
        <hr>
        <small style="color:gray;">Hôpital Cloud System - Notification automatique</small>
        </div>
        </body>
        </html>
        """.formatted(nomPatient, nomMedecin, dateRdv);

        emailService.envoyerEmailHtml(emailPatient, sujet, html);
    }

    @Override
    public void notifierCreationRendezVousPatient(
            String emailPatient,
            String nomPatient,
            String nomMedecin,
            String nomHopital,
            String dateRdv,
            String motif,
            String canal,
            Integer dureeMinutes,
            String lienTeleconsultation
    ) {
        String etablissement = nomHopital != null && !nomHopital.isBlank() ? nomHopital : "Shambua Santé";
        boolean isTele = canal != null && "TELECONSULTATION".equalsIgnoreCase(canal.trim());
        String modeLabel = isTele ? "Téléconsultation (vidéo)" : "Consultation en présentiel";
        String motifLabel = motif != null && !motif.isBlank() ? motif : "Consultation";
        int duree = dureeMinutes != null && dureeMinutes > 0 ? dureeMinutes : 30;
        String sujet = "✅ Confirmation de votre rendez-vous — " + etablissement;

        String teleBlock = "";
        if (isTele && lienTeleconsultation != null && !lienTeleconsultation.isBlank()) {
            teleBlock = """
            <p style="text-align:center; margin:24px 0;">
              <a href="%s" style="background:#2563eb; color:white; padding:14px 28px; text-decoration:none; border-radius:8px; font-weight:bold; display:inline-block;">
                Rejoindre la téléconsultation
              </a>
            </p>
            <p style="font-size:13px; color:#555;">Lien direct :<br><a href="%s">%s</a></p>
            """.formatted(lienTeleconsultation, lienTeleconsultation, lienTeleconsultation);
        } else {
            teleBlock = """
            <p style="margin-top:16px; padding:12px; background:#eff6ff; border-radius:8px; color:#1e40af;">
              Merci de vous présenter à l'établissement <b>%s</b> à l'heure indiquée avec une pièce d'identité si nécessaire.
            </p>
            """.formatted(etablissement);
        }

        String html = """
        <!DOCTYPE html>
        <html>
        <body style="font-family: Arial, sans-serif; background:#f4f6f9; padding:20px;">
        <div style="max-width:600px; margin:auto; background:white; padding:24px; border-radius:12px; box-shadow:0 4px 12px rgba(0,0,0,0.06);">
        <p style="margin:0 0 8px; font-size:12px; color:#64748b; text-transform:uppercase; letter-spacing:0.05em;">%s</p>
        <h2 style="color:#27ae60; margin-top:0;">Votre rendez-vous est confirmé</h2>
        <p>Bonjour <b>%s</b>,</p>
        <p>Un rendez-vous médical vient d'être planifié pour vous. Voici le récapitulatif :</p>
        <table style="width:100%%; border-collapse: collapse; margin:16px 0; font-size:14px;">
        <tr><td style="padding:8px 0; color:#64748b; width:38%%;"><b>Médecin</b></td><td>Dr %s</td></tr>
        <tr><td style="padding:8px 0; color:#64748b;"><b>Date et heure</b></td><td>%s</td></tr>
        <tr><td style="padding:8px 0; color:#64748b;"><b>Motif</b></td><td>%s</td></tr>
        <tr><td style="padding:8px 0; color:#64748b;"><b>Mode</b></td><td>%s</td></tr>
        <tr><td style="padding:8px 0; color:#64748b;"><b>Durée estimée</b></td><td>%d minutes</td></tr>
        </table>
        %s
        <hr style="border:none; border-top:1px solid #e2e8f0; margin:20px 0;">
        <small style="color:#94a3b8;">%s — Notification automatique Shambua Santé. En cas d'empêchement, contactez votre établissement.</small>
        </div>
        </body>
        </html>
        """.formatted(
                etablissement,
                nomPatient,
                nomMedecin,
                dateRdv,
                motifLabel,
                modeLabel,
                duree,
                teleBlock,
                etablissement
        );

        emailService.envoyerEmailHtml(emailPatient, sujet, html);
    }

    @Override
    public void notifierTeleconsultationPatient(
            String emailPatient,
            String nomPatient,
            String nomMedecin,
            String dateRdv,
            String lienTeleconsultation
    ) {
        String sujet = "🩺 Votre lien de téléconsultation — Hôpital Cloud";

        String html = """
        <!DOCTYPE html>
        <html>
        <body style="font-family: Arial; background:#f4f6f9; padding:20px;">
        <div style="max-width:600px; margin:auto; background:white; padding:24px; border-radius:10px;">
        <h2 style="color:#2c3e50;">Téléconsultation confirmée</h2>
        <p>Bonjour <b>%s</b>,</p>
        <p>Votre rendez-vous de téléconsultation avec <b>Dr %s</b> est confirmé.</p>
        <table style="width:100%%; border-collapse: collapse; margin:16px 0;">
        <tr><td style="padding:8px 0;"><b>Date</b></td><td>%s</td></tr>
        </table>
        <p>Cliquez sur le bouton ci-dessous pour rejoindre la consultation vidéo :</p>
        <p style="text-align:center; margin:24px 0;">
          <a href="%s" style="background:#2563eb; color:white; padding:12px 24px; text-decoration:none; border-radius:8px; font-weight:bold;">
            Rejoindre la téléconsultation
          </a>
        </p>
        <p style="font-size:13px; color:#555;">Ou copiez ce lien :<br><a href="%s">%s</a></p>
        <hr>
        <small style="color:gray;">Hôpital Cloud System — Ne partagez pas ce lien.</small>
        </div>
        </body>
        </html>
        """.formatted(nomPatient, nomMedecin, dateRdv, lienTeleconsultation, lienTeleconsultation, lienTeleconsultation);

        emailService.envoyerEmailHtml(emailPatient, sujet, html);
    }

    @Override
    public void notifierTeleconsultationMedecin(
            String emailMedecin,
            String nomMedecin,
            String nomPatient,
            String dateRdv,
            String lienTeleconsultation
    ) {
        String sujet = "📹 Téléconsultation planifiée avec " + nomPatient;

        String html = """
        <!DOCTYPE html>
        <html>
        <body style="font-family: Arial; background:#f4f6f9; padding:20px;">
        <div style="max-width:600px; margin:auto; background:white; padding:24px; border-radius:10px;">
        <h2 style="color:#2c3e50;">Nouvelle téléconsultation</h2>
        <p>Bonjour Dr <b>%s</b>,</p>
        <p>Une téléconsultation a été planifiée pour le patient <b>%s</b>.</p>
        <table style="width:100%%; border-collapse: collapse; margin:16px 0;">
        <tr><td style="padding:8px 0;"><b>Date</b></td><td>%s</td></tr>
        <tr><td style="padding:8px 0;"><b>Lien salle</b></td><td><a href="%s">%s</a></td></tr>
        </table>
        <p>Le patient a reçu le lien par email.</p>
        <hr>
        <small style="color:gray;">Hôpital Cloud System</small>
        </div>
        </body>
        </html>
        """.formatted(nomMedecin, nomPatient, dateRdv, lienTeleconsultation, lienTeleconsultation);

        emailService.envoyerEmailHtml(emailMedecin, sujet, html);
    }

    @Override
    public void notifierRappelTeleconsultationPatient(
            String emailPatient,
            String nomPatient,
            String nomMedecin,
            String nomHopital,
            String dateRdv,
            String lienTeleconsultation,
            int minutesAvant
    ) {
        String etablissement = nomHopital != null && !nomHopital.isBlank() ? nomHopital : "ShambuaSanté";
        String sujet = "⏰ Rappel : téléconsultation dans " + minutesAvant + " min — " + etablissement;

        String html = """
        <!DOCTYPE html>
        <html>
        <body style="font-family: Arial, sans-serif; background:#f4f6f9; padding:20px;">
        <div style="max-width:600px; margin:auto; background:white; padding:24px; border-radius:12px; box-shadow:0 4px 12px rgba(0,0,0,0.06);">
        <p style="margin:0 0 8px; font-size:12px; color:#64748b; text-transform:uppercase; letter-spacing:0.05em;">%s</p>
        <h2 style="color:#1e40af; margin-top:0;">Votre téléconsultation commence bientôt</h2>
        <p>Bonjour <b>%s</b>,</p>
        <p>Votre séance avec <b>Dr %s</b> débute dans environ <b>%d minutes</b>.</p>
        <table style="width:100%%; border-collapse: collapse; margin:16px 0;">
        <tr><td style="padding:8px 0; color:#64748b;"><b>Date</b></td><td>%s</td></tr>
        </table>
        <p style="text-align:center; margin:24px 0;">
          <a href="%s" style="background:#2563eb; color:white; padding:14px 28px; text-decoration:none; border-radius:8px; font-weight:bold; display:inline-block;">
            Rejoindre la téléconsultation
          </a>
        </p>
        <p style="font-size:13px; color:#555;">Installez-vous dans un endroit calme et vérifiez votre caméra et micro avant de rejoindre.</p>
        <p style="font-size:13px; color:#555;">Lien direct :<br><a href="%s">%s</a></p>
        <hr style="border:none; border-top:1px solid #e2e8f0; margin:20px 0;">
        <small style="color:#94a3b8;">%s — Notification automatique sécurisée. Ne partagez pas ce lien.</small>
        </div>
        </body>
        </html>
        """.formatted(
                etablissement,
                nomPatient,
                nomMedecin,
                minutesAvant,
                dateRdv,
                lienTeleconsultation,
                lienTeleconsultation,
                lienTeleconsultation,
                etablissement
        );

        emailService.envoyerEmailHtml(emailPatient, sujet, html);
    }

    @Override
    public void notifierRappelTeleconsultationMedecin(
            String emailMedecin,
            String nomMedecin,
            String nomPatient,
            String nomHopital,
            String dateRdv,
            String lienTeleconsultation,
            int minutesAvant
    ) {
        String etablissement = nomHopital != null && !nomHopital.isBlank() ? nomHopital : "ShambuaSanté";
        String sujet = "⏰ Rappel téléconsultation dans " + minutesAvant + " min — " + nomPatient;

        String html = """
        <!DOCTYPE html>
        <html>
        <body style="font-family: Arial, sans-serif; background:#f4f6f9; padding:20px;">
        <div style="max-width:600px; margin:auto; background:white; padding:24px; border-radius:12px; box-shadow:0 4px 12px rgba(0,0,0,0.06);">
        <p style="margin:0 0 8px; font-size:12px; color:#64748b; text-transform:uppercase; letter-spacing:0.05em;">%s</p>
        <h2 style="color:#1e40af; margin-top:0;">Téléconsultation imminente</h2>
        <p>Bonjour Dr <b>%s</b>,</p>
        <p>Votre téléconsultation avec le patient <b>%s</b> débute dans environ <b>%d minutes</b>.</p>
        <table style="width:100%%; border-collapse: collapse; margin:16px 0;">
        <tr><td style="padding:8px 0; color:#64748b;"><b>Date</b></td><td>%s</td></tr>
        <tr><td style="padding:8px 0; color:#64748b;"><b>Salle</b></td><td><a href="%s">%s</a></td></tr>
        </table>
        <p style="text-align:center; margin:24px 0;">
          <a href="%s" style="background:#2563eb; color:white; padding:14px 28px; text-decoration:none; border-radius:8px; font-weight:bold; display:inline-block;">
            Ouvrir la salle vidéo
          </a>
        </p>
        <hr style="border:none; border-top:1px solid #e2e8f0; margin:20px 0;">
        <small style="color:#94a3b8;">%s — Notification automatique multi-tenant.</small>
        </div>
        </body>
        </html>
        """.formatted(
                etablissement,
                nomMedecin,
                nomPatient,
                minutesAvant,
                dateRdv,
                lienTeleconsultation,
                lienTeleconsultation,
                lienTeleconsultation,
                etablissement
        );

        emailService.envoyerEmailHtml(emailMedecin, sujet, html);
    }

    @Override
    public void notifierRappelTeleconsultationSmsPatient(
            String telephonePatient,
            String nomPatient,
            String nomMedecin,
            String nomHopital,
            String dateRdv,
            String lienTeleconsultation,
            int minutesAvant
    ) {
        String etablissement = nomHopital != null && !nomHopital.isBlank() ? nomHopital : "ShambuaSanté";
        String message = "%s — Rappel: téléconsultation dans %d min avec Dr %s (%s). Rejoindre: %s"
                .formatted(etablissement, minutesAvant, nomMedecin, dateRdv, lienTeleconsultation);
        smsService.envoyerSms(telephonePatient, message);
    }

    @Override
    public void notifierRappelTeleconsultationSmsMedecin(
            String telephoneMedecin,
            String nomMedecin,
            String nomPatient,
            String nomHopital,
            String dateRdv,
            String lienTeleconsultation,
            int minutesAvant
    ) {
        String etablissement = nomHopital != null && !nomHopital.isBlank() ? nomHopital : "ShambuaSanté";
        String message = "%s — Téléconsultation dans %d min avec %s (%s). Salle: %s"
                .formatted(etablissement, minutesAvant, nomPatient, dateRdv, lienTeleconsultation);
        smsService.envoyerSms(telephoneMedecin, message);
    }

    @Override
    public void notifierOrdonnancePatient(
            String emailPatient,
            String nomPatient,
            String nomMedecin,
            String nomHopital,
            String numeroOrdonnance,
            String datePrescription,
            String diagnostic,
            byte[] pdfOrdonnance
    ) {
        String etablissement = nomHopital != null && !nomHopital.isBlank() ? nomHopital : "Shambua Santé";
        String ref = numeroOrdonnance != null && !numeroOrdonnance.isBlank() ? numeroOrdonnance : "Ordonnance";
        String diag = diagnostic != null && !diagnostic.isBlank() ? diagnostic : "Selon prescription jointe";
        String dateLabel = datePrescription != null && !datePrescription.isBlank() ? datePrescription : "—";
        String sujet = "Votre ordonnance médicale — " + etablissement;

        String html = """
        <!DOCTYPE html>
        <html>
        <body style="margin:0;padding:0;background:#eef3f9;font-family:'Segoe UI',Arial,sans-serif;color:#0b1f4a;">
          <div style="max-width:640px;margin:24px auto;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 8px 28px rgba(15,40,80,0.12);">
            <div style="background:linear-gradient(120deg,#0f2850,#1753a0,#2563eb);padding:28px 32px;color:#fff;">
              <p style="margin:0;font-size:11px;letter-spacing:0.22em;text-transform:uppercase;color:#bae6fd;font-weight:600;">Shambua Santé</p>
              <h1 style="margin:10px 0 0;font-size:24px;font-weight:600;">Ordonnance médicale</h1>
              <p style="margin:8px 0 0;font-size:14px;color:rgba(255,255,255,0.82);">Document confidentiel transmis par votre médecin</p>
            </div>
            <div style="padding:28px 32px;">
              <p style="margin:0 0 16px;font-size:15px;">Bonjour <b>%s</b>,</p>
              <p style="margin:0 0 20px;font-size:14px;line-height:1.55;color:#334155;">
                <b>%s</b> vous a transmis une ordonnance depuis <b>%s</b>.
                Le PDF authentifié est joint à cet e-mail — conservez-le précieusement.
              </p>
              <table style="width:100%%;border-collapse:collapse;background:#f8fafc;border-radius:12px;overflow:hidden;">
                <tr>
                  <td style="padding:12px 16px;border-bottom:1px solid #e2e8f0;font-size:12px;color:#64748b;text-transform:uppercase;letter-spacing:0.06em;">Référence</td>
                  <td style="padding:12px 16px;border-bottom:1px solid #e2e8f0;font-size:14px;font-weight:600;text-align:right;">%s</td>
                </tr>
                <tr>
                  <td style="padding:12px 16px;border-bottom:1px solid #e2e8f0;font-size:12px;color:#64748b;text-transform:uppercase;letter-spacing:0.06em;">Date</td>
                  <td style="padding:12px 16px;border-bottom:1px solid #e2e8f0;font-size:14px;font-weight:600;text-align:right;">%s</td>
                </tr>
                <tr>
                  <td style="padding:12px 16px;font-size:12px;color:#64748b;text-transform:uppercase;letter-spacing:0.06em;">Diagnostic</td>
                  <td style="padding:12px 16px;font-size:14px;font-weight:600;text-align:right;">%s</td>
                </tr>
              </table>
              <p style="margin:22px 0 0;font-size:13px;line-height:1.5;color:#64748b;">
                Ce document est strictement confidentiel et réservé au patient nommé.
                En cas de question, contactez votre établissement de soins.
              </p>
            </div>
            <div style="padding:16px 32px 24px;border-top:1px solid #e2e8f0;background:#f8fafc;">
              <p style="margin:0;font-size:11px;color:#94a3b8;">%s — Notification sécurisée multi-tenant</p>
            </div>
          </div>
        </body>
        </html>
        """.formatted(nomPatient, nomMedecin, etablissement, ref, dateLabel, diag, etablissement);

        String fileName = "ordonnance_" + ref.replaceAll("[^A-Za-z0-9_-]", "_") + ".pdf";
        emailService.envoyerEmailHtmlAvecPieceJointe(
                emailPatient,
                sujet,
                html,
                fileName,
                pdfOrdonnance,
                "application/pdf");
    }

    @Override
    public void notifierDocumentCliniquePatient(
            String emailPatient,
            String nomPatient,
            String nomMedecin,
            String nomHopital,
            String typeDocument,
            String titre,
            String resume,
            String attachmentFileName,
            byte[] attachmentBytes,
            String contentType
    ) {
        String etablissement = nomHopital != null && !nomHopital.isBlank() ? nomHopital : "Shambua Santé";
        String typeLabel = switch (typeDocument != null ? typeDocument.toUpperCase() : "") {
            case "LABO", "LAB_RESULT" -> "Résultat de laboratoire";
            case "CONSULTATION" -> "Fiche de consultation";
            case "ORDONNANCE" -> "Ordonnance";
            default -> "Document médical";
        };
        String sujet = typeLabel + " — " + etablissement;
        String bodyResume = resume != null && !resume.isBlank()
                ? resume.replace("\n", "<br/>")
                : "Consultez le détail dans votre espace patient Shambua Santé.";

        String html = """
        <!DOCTYPE html>
        <html>
        <body style="margin:0;padding:0;background:#eef3f9;font-family:'Segoe UI',Arial,sans-serif;color:#0b1f4a;">
          <div style="max-width:640px;margin:24px auto;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 8px 28px rgba(15,40,80,0.12);">
            <div style="background:linear-gradient(120deg,#0f2850,#1753a0,#2563eb);padding:28px 32px;color:#fff;">
              <p style="margin:0;font-size:11px;letter-spacing:0.22em;text-transform:uppercase;color:#bae6fd;font-weight:600;">Shambua Santé</p>
              <h1 style="margin:10px 0 0;font-size:24px;font-weight:600;">%s</h1>
              <p style="margin:8px 0 0;font-size:14px;color:rgba(255,255,255,0.82);">Document confidentiel transmis par votre médecin</p>
            </div>
            <div style="padding:28px 32px;">
              <p style="margin:0 0 16px;font-size:15px;">Bonjour <b>%s</b>,</p>
              <p style="margin:0 0 16px;font-size:14px;line-height:1.55;color:#334155;">
                <b>%s</b> vous a transmis un document depuis <b>%s</b> :
              </p>
              <p style="margin:0 0 12px;font-size:15px;font-weight:600;">%s</p>
              <div style="padding:14px 16px;background:#f8fafc;border-radius:12px;font-size:13px;line-height:1.55;color:#475569;">
                %s
              </div>
              <p style="margin:22px 0 0;font-size:13px;line-height:1.5;color:#64748b;">
                Retrouvez également ce document dans votre espace patient (Documents / résultats).
              </p>
            </div>
            <div style="padding:16px 32px 24px;border-top:1px solid #e2e8f0;background:#f8fafc;">
              <p style="margin:0;font-size:11px;color:#94a3b8;">%s — Notification sécurisée multi-tenant</p>
            </div>
          </div>
        </body>
        </html>
        """.formatted(typeLabel, nomPatient, nomMedecin, etablissement, titre, bodyResume, etablissement);

        if (attachmentBytes != null && attachmentBytes.length > 0 && attachmentFileName != null) {
            emailService.envoyerEmailHtmlAvecPieceJointe(
                    emailPatient,
                    sujet,
                    html,
                    attachmentFileName,
                    attachmentBytes,
                    contentType != null ? contentType : "application/octet-stream");
        } else {
            emailService.envoyerEmailHtml(emailPatient, sujet, html);
        }
    }
}