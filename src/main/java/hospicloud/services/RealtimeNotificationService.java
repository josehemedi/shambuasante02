package hospicloud.services;

import hospicloud.model.RendezVous;

import java.math.BigDecimal;

public interface RealtimeNotificationService {
    void notifyRendezVousCreated(RendezVous rdv);

    void notifyPaymentRecorded(
            Integer hopitalId,
            Integer idFacture,
            BigDecimal amount,
            String invoiceNumber,
            String patientName,
            String cashierLabel,
            String method,
            String paymentStatus,
            Integer excludeUserId);

    void notifyArchivistesDossierPatientSorti(
            Integer hopitalId,
            Long archiveId,
            Long patientId,
            String typeEpisode,
            Integer excludeUserId);

    /** Notifie le médecin qu'un patient vient d'être ajouté à sa file d'attente. */
    void notifyPatientAjouteFileMedecin(
            Integer hopitalId,
            Integer idMedecin,
            Integer idAdmission,
            Integer idRdv,
            String nomPatient,
            String motif,
            Integer numeroPassage);
}
