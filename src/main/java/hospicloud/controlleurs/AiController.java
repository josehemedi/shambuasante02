package hospicloud.controlleurs;

import hospicloud.dtos.AiChatRequestDTO;
import hospicloud.dtos.AiChatResponseDTO;
import hospicloud.dtos.AiStatusDTO;
import hospicloud.services.AiClinicalAssistantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiClinicalAssistantService aiClinicalAssistantService;

    public AiController(AiClinicalAssistantService aiClinicalAssistantService) {
        this.aiClinicalAssistantService = aiClinicalAssistantService;
    }

    @GetMapping("/status")
    public ResponseEntity<AiStatusDTO> status() {
        return ResponseEntity.ok(aiClinicalAssistantService.getStatus());
    }

    @GetMapping("/prompts")
    public ResponseEntity<List<String>> prompts() {
        return ResponseEntity.ok(aiClinicalAssistantService.getSuggestedPrompts());
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponseDTO> chat(@Valid @RequestBody AiChatRequestDTO request) {
        return ResponseEntity.ok(aiClinicalAssistantService.chat(request));
    }

    @PostMapping("/analyze")
    public ResponseEntity<AiChatResponseDTO> analyze(@Valid @RequestBody AiChatRequestDTO request) {
        return ResponseEntity.ok(aiClinicalAssistantService.chat(request));
    }
}
