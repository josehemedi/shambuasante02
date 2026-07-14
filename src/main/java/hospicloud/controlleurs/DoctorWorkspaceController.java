package hospicloud.controlleurs;

import hospicloud.dtos.DoctorWorkspaceDTO;
import hospicloud.services.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspace")
public class DoctorWorkspaceController {

    private final DashboardService dashboardService;

    public DoctorWorkspaceController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/medecin")
    public ResponseEntity<DoctorWorkspaceDTO> getMedecinWorkspace() {
        return ResponseEntity.ok(dashboardService.getDoctorWorkspaceData());
    }
}
