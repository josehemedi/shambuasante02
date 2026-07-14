package hospicloud.controlleurs;

import hospicloud.dtos.TeleconsultationChatMessageDTO;
import hospicloud.dtos.TeleconsultationChatReadReceiptDTO;
import hospicloud.dtos.TeleconsultationChatSendDTO;
import hospicloud.services.TeleconsultationChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultations/teleconsultation")
@CrossOrigin(origins = "*")
public class TeleconsultationChatController {

    private final TeleconsultationChatService chatService;

    public TeleconsultationChatController(TeleconsultationChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/{idRdv}/messages")
    public ResponseEntity<List<TeleconsultationChatMessageDTO>> listMessages(@PathVariable Integer idRdv) {
        return ResponseEntity.ok(chatService.listMessages(idRdv));
    }

    @PostMapping("/{idRdv}/messages")
    public ResponseEntity<TeleconsultationChatMessageDTO> sendMessage(
            @PathVariable Integer idRdv,
            @RequestBody TeleconsultationChatSendDTO payload) {
        TeleconsultationChatMessageDTO saved = chatService.sendMessage(idRdv, payload.getContent());
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PostMapping("/{idRdv}/messages/read")
    public ResponseEntity<?> markAsRead(@PathVariable Integer idRdv) {
        TeleconsultationChatReadReceiptDTO receipt = chatService.markMessagesAsRead(idRdv);
        if (receipt == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(receipt);
    }
}
