package hospicloud.controlleurs;

import hospicloud.dtos.TechnicalLogDTO;
import hospicloud.dtos.TechnicalLogKpisDTO;
import hospicloud.services.TechnicalLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final TechnicalLogService technicalLogService;

    public AuditController(TechnicalLogService technicalLogService) {
        this.technicalLogService = technicalLogService;
    }

    @GetMapping("/kpis")
    public ResponseEntity<TechnicalLogKpisDTO> getKpis() {
        return ResponseEntity.ok(technicalLogService.getKpis());
    }

    @GetMapping("/logs")
    public ResponseEntity<List<TechnicalLogDTO>> getLogs(
            @RequestParam(value = "hopitalId", required = false) Integer hopitalId,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "userEmail", required = false) String userEmail,
            @RequestParam(value = "module", required = false) String module,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "requestId", required = false) String requestId,
            @RequestParam(value = "endpoint", required = false) String endpoint,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "dateFrom", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(value = "dateTo", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        return ResponseEntity.ok(technicalLogService.search(
                hopitalId, userId, userEmail, module, action, status,
                requestId, endpoint, search, dateFrom, dateTo, limit));
    }

    @GetMapping("/modules")
    public ResponseEntity<List<String>> getModules() {
        return ResponseEntity.ok(technicalLogService.listModules());
    }

    @GetMapping("/actions")
    public ResponseEntity<List<String>> getActions() {
        return ResponseEntity.ok(technicalLogService.listActions());
    }
}
