package hospicloud.repositories;

import hospicloud.dtos.patient.PatientMessageConversationDTO;

import java.util.List;

public interface PatientMessageRepository {

    List<PatientMessageConversationDTO> listConversations(Integer idPatient, Integer idHopital);
}
