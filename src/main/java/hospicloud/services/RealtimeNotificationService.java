package hospicloud.services;

import hospicloud.model.RendezVous;

import java.math.BigDecimal;

public interface RealtimeNotificationService {
    void notifyRendezVousCreated(RendezVous rdv);

    /** Notifie le patient que le médecin a accepté sa demande. */
    void notifyRendezVousAccepted(RendezVous rdv);

    /** Notifie le patient que le médecin a refusé sa demande. */
    void notifyRendezVousRejected(RendezVous rdv);

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

    /** Notifie le patient qu'une ordonnance vient de lui être transmise. */
    void notifyOrdonnanceEnvoyeeAuPatient(
            Integer hopitalId,
            Integer idPatient,
            Long idOrdonnance,
            String numeroOrdonnance,
            String nomMedecin);

    /** Notifie le patient qu'un document clinique (labo / consultation / fichier) lui a été transmis. */
    void notifyDocumentCliniqueEnvoyeAuPatient(
            Integer hopitalId,
            Integer idPatient,
            String typeDocument,
            String titre,
            String nomMedecin,
            Integer idDocument);
}
