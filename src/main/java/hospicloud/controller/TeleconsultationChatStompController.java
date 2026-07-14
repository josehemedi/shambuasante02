package hospicloud.controller;



import hospicloud.dtos.TeleconsultationChatSendDTO;

import hospicloud.services.TeleconsultationChatService;

import org.springframework.messaging.handler.annotation.DestinationVariable;

import org.springframework.messaging.handler.annotation.MessageMapping;

import org.springframework.messaging.handler.annotation.Payload;

import org.springframework.stereotype.Controller;



@Controller

public class TeleconsultationChatStompController {



    private final TeleconsultationChatService chatService;



    public TeleconsultationChatStompController(TeleconsultationChatService chatService) {

        this.chatService = chatService;

    }



    @MessageMapping("/teleconsultation/{idRdv}/chat")

    public void sendMessage(@DestinationVariable Integer idRdv,

                            @Payload TeleconsultationChatSendDTO payload) {

        chatService.sendMessage(idRdv, payload.getContent());

    }

}

