package hospicloud.utils;

public interface NotificationService {

    void notifierModificationRendezVous(
            String emailMedecin,
            String nomMedecin,
            String nomPatient,
            String ancienneDate,
            String nouvelleDate);

    void notifierReportRendezVous(
            String emailMedecin,
            String nomMedecin,
            String nomPatient,
            String ancienneDate,
            String nouvelleDate);

    void notifierAnnulationRendezVous(
            String emailMedecin,
            String nomMedecin,
            String nomPatient,
            String dateRdv);

    void notifierConfirmationRendezVous(
            String emailMedecin,
            String nomMedecin,
            String nomPatient,
            String dateRdv);

    void notifierConfirmationRendezVousPatient(
            String emailPatient,
            String nomPatient,
            String nomMedecin,
            String dateRdv);

    void notifierCreationRendezVousPatient(
            String emailPatient,
            String nomPatient,
            String nomMedecin,
            String nomHopital,
            String dateRdv,
            String motif,
            String canal,
            Integer dureeMinutes,
            String lienTeleconsultation);

    void notifierTeleconsultationPatient(
            String emailPatient,
            String nomPatient,
            String nomMedecin,
            String dateRdv,
            String lienTeleconsultation);

    void notifierTeleconsultationMedecin(
            String emailMedecin,
            String nomMedecin,
            String nomPatient,
            String dateRdv,
            String lienTeleconsultation);

    void notifierRappelTeleconsultationPatient(
            String emailPatient,
            String nomPatient,
            String nomMedecin,
            String nomHopital,
            String dateRdv,
            String lienTeleconsultation,
            int minutesAvant);

    void notifierRappelTeleconsultationMedecin(
            String emailMedecin,
            String nomMedecin,
            String nomPatient,
            String nomHopital,
            String dateRdv,
            String lienTeleconsultation,
            int minutesAvant);

    void notifierRappelTeleconsultationSmsPatient(
            String telephonePatient,
            String nomPatient,
            String nomMedecin,
            String nomHopital,
            String dateRdv,
            String lienTeleconsultation,
            int minutesAvant);

    void notifierRappelTeleconsultationSmsMedecin(
            String telephoneMedecin,
            String nomMedecin,
            String nomPatient,
            String nomHopital,
            String dateRdv,
            String lienTeleconsultation,
            int minutesAvant);

    /**
     * Envoie l'ordonnance médicale au patient (e-mail HTML + PDF joint).
     */
    void notifierOrdonnancePatient(
            String emailPatient,
            String nomPatient,
            String nomMedecin,
            String nomHopital,
            String numeroOrdonnance,
            String datePrescription,
            String diagnostic,
            byte[] pdfOrdonnance);
}