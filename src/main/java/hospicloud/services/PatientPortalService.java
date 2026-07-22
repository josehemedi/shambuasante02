package hospicloud.services;

import hospicloud.dtos.SupportTicketDTO;
import hospicloud.dtos.patient.*;
import hospicloud.model.Facture;
import hospicloud.model.Ordonnance;
import hospicloud.model.Patient;
import hospicloud.model.RendezVous;

import java.util.List;
import java.util.Map;

public interface PatientPortalService {

    List<PublicHospitalDTO> searchHospitals(String query);

    PatientRegistrationResponseDTO register(PatientRegistrationRequestDTO request);

    Patient getMyProfile();

    Patient updateMyProfile(PatientProfileUpdateDTO request);

    List<RendezVous> listMyAppointments();

    RendezVous requestAppointment(PatientAppointmentRequestDTO request);

    RendezVous cancelMyAppointment(Integer idRdv, String motif);

    RendezVous rescheduleMyAppointment(Integer idRdv, String nouvelleDateHeure);

    List<Facture> listMyInvoices();

    List<Ordonnance> listMyPrescriptions();

    List<Map<String, Object>> listMyLabResults();

    List<Map<String, Object>> listMyDocuments();

    /** Fichier partagé par le médecin (bytes + métadonnées pour Content-Disposition). */
    Map<String, Object> downloadMyDocument(Integer idDocument);

    SupportTicketDTO requestAssistance(PatientAssistanceRequestDTO request);

    List<Map<String, Object>> listDoctorsForBooking(String specialite);
}
