package hospicloud.services;

import hospicloud.dtos.AiChatRequestDTO;
import hospicloud.dtos.AiChatResponseDTO;
import hospicloud.dtos.AiStatusDTO;

import java.util.List;

public interface AiClinicalAssistantService {

    AiStatusDTO getStatus();

    List<String> getSuggestedPrompts();

    AiChatResponseDTO chat(AiChatRequestDTO request);
}
