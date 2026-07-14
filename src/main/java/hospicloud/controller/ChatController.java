package hospicloud.controller;

import hospicloud.model.ChatMessage;
import hospicloud.repository.ChatRepository;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class ChatController {

    private final ChatRepository chatRepository;

    public ChatController(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @MessageMapping("/chat/{consultationId}")
    @SendTo("/topic/consultation/{consultationId}")
    public ChatMessage sendMessage(@DestinationVariable Long consultationId, ChatMessage chatMessage) {
        chatMessage.setConsultationId(consultationId);
        chatMessage.setCreatedAt(LocalDateTime.now());
        return chatRepository.saveMessage(chatMessage);
    }
}
