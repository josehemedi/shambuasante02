package hospicloud.repositories;

import hospicloud.dtos.reception.ReceptionDashboardStatsDTO;
import hospicloud.dtos.reception.AdmissionDTO;
import hospicloud.dtos.reception.ReceptionRegistrationPointDTO;
import hospicloud.model.reception.Admission;

import java.util.List;

public interface ReceptionDashboardRepository {

    ReceptionDashboardStatsDTO getDashboardStats(Integer idHopital);

    List<AdmissionDTO> getAdmissionsEnAttente(Integer idHopital);

    List<ReceptionRegistrationPointDTO> getInscriptionsParHeure(Integer idHopital);

    Admission trouverAdmissionParId(Integer idAdmission, Integer idHopital);

    void mettreAJourStatutAdmission(Integer idAdmission, Integer idHopital, String nouveauStatut);

    void creerAdmission(Admission admission);

    boolean aRendezVousAujourdhui(Integer idPatient, Integer idHopital);

    Admission trouverAdmissionActiveParPatient(Integer idPatient, Integer idHopital);

    List<hospicloud.model.RendezVous> listerRendezVousDuJour(Integer idHopital);

    List<hospicloud.dtos.reception.MedecinDisponibleDTO> listerMedecinsDisponibles(
            Integer idHopital, String specialiteOuService, boolean uniquementEnHoraire);

    List<String> listerSpecialites(Integer idHopital);

    Integer creerAdmissionRetourId(Admission admission);

    /** Attribue le prochain numéro de passage du jour et le persiste sur l'admission. */
    Integer allouerNumeroPassage(Integer idAdmission, Integer idHopital);
}
