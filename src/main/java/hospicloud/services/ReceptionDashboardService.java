package hospicloud.services;

import hospicloud.dtos.reception.AdmissionDTO;
import hospicloud.dtos.reception.MedecinDisponibleDTO;
import hospicloud.dtos.reception.ReceptionDashboardStatsDTO;
import hospicloud.dtos.reception.ReceptionRegistrationPointDTO;
import hospicloud.dtos.reception.ReceptionRdvCreateDTO;
import hospicloud.dtos.reception.WalkInRegistrationRequestDTO;
import hospicloud.dtos.reception.WalkInRegistrationResponseDTO;
import hospicloud.model.RendezVous;
import hospicloud.model.reception.Admission;

import java.util.List;

public interface ReceptionDashboardService {

    ReceptionDashboardStatsDTO getDashboardStats();

    List<AdmissionDTO> getFileAttente();

    List<ReceptionRegistrationPointDTO> getInscriptionsParHeure();

    List<RendezVous> listerRendezVousDuJour();

    RendezVous creerRendezVous(ReceptionRdvCreateDTO dto);

    void changerStatutAdmission(Integer idAdmission, String nouveauStatut);

    void inscrirePatientFileAttente(Admission admission, boolean reqRendezVousStrict);

    List<MedecinDisponibleDTO> listerMedecinsDisponibles(String specialiteOuService, boolean uniquementEnHoraire);

    List<String> listerSpecialites();

    WalkInRegistrationResponseDTO enregistrerArrivee(WalkInRegistrationRequestDTO request);
}
