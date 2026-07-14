package hospicloud.services;

import hospicloud.dtos.CommencerConsultationResponseDTO;
import hospicloud.dtos.MedecinFileItemDTO;
import hospicloud.dtos.WaitingRoomCallEventDTO;

import java.util.List;

public interface MedecinFileAttenteService {
    List<MedecinFileItemDTO> listerMaFile();

    WaitingRoomCallEventDTO appelerPatient(Integer idAdmission);

    WaitingRoomCallEventDTO appelerDepuisRendezVous(Integer idRdv);

    CommencerConsultationResponseDTO commencerConsultation(Integer idAdmission);
}
