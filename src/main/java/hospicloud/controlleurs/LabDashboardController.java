package hospicloud.controlleurs;

import hospicloud.dtos.lab.LabDashboardStatsDTO;
import hospicloud.dtos.lab.ResultatAnalyseCritiqueDTO;
import hospicloud.model.lab.CommandeAnalyse;
import hospicloud.services.LabDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lab/dashboard")
public class LabDashboardController {

    private final LabDashboardService labDashboardService;

    public LabDashboardController(LabDashboardService labDashboardService) {
        this.labDashboardService = labDashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<LabDashboardStatsDTO> getStats() {
        return ResponseEntity.ok(labDashboardService.getDashboardStats());
    }

    @GetMapping("/critiques")
    public ResponseEntity<List<ResultatAnalyseCritiqueDTO>> getResultatsCritiques(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(labDashboardService.getResultatsCritiques(page, size));
    }

    @PostMapping("/critiques/{id}/acquitter")
    public ResponseEntity<Void> acquitterResultat(@PathVariable String id) {
        labDashboardService.acquitterResultat(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/commandes")
    public ResponseEntity<List<CommandeAnalyse>> getCommandes(
            @RequestParam(required = false) String statut,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(labDashboardService.listerCommandes(statut, page, size));
    }
}
