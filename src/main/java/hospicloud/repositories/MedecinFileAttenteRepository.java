package hospicloud.repositories;

import hospicloud.dtos.MedecinFileItemDTO;
import hospicloud.model.reception.Admission;

import java.util.List;
import java.util.Optional;

public interface MedecinFileAttenteRepository {
    List<MedecinFileItemDTO> listerFileDuMedecin(Integer idMedecin, Integer idHopital);

    Optional<Admission> trouverAdmission(Integer idAdmission, Integer idHopital);

    Optional<Admission> trouverAdmissionOuverteParRdv(Integer idRdv, Integer idHopital);

    int prochainNumeroPassage(Integer idHopital);

    void marquerAppele(Integer idAdmission, Integer idHopital, Integer numeroPassage, String salle);

    void mettreAJourStatut(Integer idAdmission, Integer idHopital, String statut);

    Integer creerAdmissionPourAppel(Admission admission);

    String trouverNomPatient(Integer idPatient, Integer idHopital);

    String trouverNomMedecin(Integer idMedecin, Integer idHopital);

    String trouverSalleRdv(Integer idRdv, Integer idHopital);
}
