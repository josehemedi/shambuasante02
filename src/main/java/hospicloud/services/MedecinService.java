package hospicloud.services;

import hospicloud.dtos.MedecinRequest;
import hospicloud.dtos.MedecinResponse;
import hospicloud.dtos.StatistiqueMedecinDTO;

import java.util.List;
import java.util.Optional;

public interface MedecinService {
   // les informations liées aux ecritures
    void creer(MedecinRequest request);

    Optional<MedecinResponse> trouverParId(Integer idMedecin);
   // les informations liées à la lecture
    List<MedecinResponse> listerParHopital();

    MedecinResponse mettreAJour(Integer idMedecin, MedecinRequest request);

    void changerDisponibilite(Integer idMedecin, Boolean status);
    
// nouvelles methodes à implementer pour le DashBoard
    StatistiqueMedecinDTO getDashboardStats(Integer medecinId);
    
    long getNombrePatients(Integer medecinId, Integer hopitalId);
    long getConsultationsAujourdhui(Integer medecinId, Integer hopitalId);
    long getRendezVousAujourdhui(Integer medecinId, Integer hopitalId);
    long getHospitalisationsEncours(Integer medecinId, Integer hopitalId);
    long getExamensEnAttente(Integer medecinId, Integer hopitalId);
    long getNotificationsNonLues(Integer medecinId, Integer hopitalId);
}