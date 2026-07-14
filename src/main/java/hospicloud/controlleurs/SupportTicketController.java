package hospicloud.controlleurs;

import hospicloud.dtos.SupportTicketCreateDTO;
import hospicloud.dtos.SupportTicketDTO;
import hospicloud.dtos.SupportTicketStatusUpdateDTO;
import hospicloud.services.SupportTicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/support/tickets")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    public SupportTicketController(SupportTicketService supportTicketService) {
        this.supportTicketService = supportTicketService;
    }

    @PostMapping
    public ResponseEntity<SupportTicketDTO> create(@RequestBody SupportTicketCreateDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supportTicketService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<SupportTicketDTO>> list(
            @RequestParam(value = "hopitalId", required = false) Integer hopitalId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "module", required = false) String module,
            @RequestParam(value = "priority", required = false) String priority,
            @RequestParam(value = "requestId", required = false) String requestId,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "limit", defaultValue = "50") int limit) {
        return ResponseEntity.ok(supportTicketService.search(
                hopitalId, status, module, priority, requestId, search, limit));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SupportTicketDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody SupportTicketStatusUpdateDTO update) {
        return ResponseEntity.ok(supportTicketService.updateStatus(id, update));
    }
}
