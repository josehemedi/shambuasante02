package hospicloud.services;

import hospicloud.dtos.patient.PatientMessageConversationDTO;

import java.util.List;

public interface PatientMessageService {

    List<PatientMessageConversationDTO> listConversations();
}
