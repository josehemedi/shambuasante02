package hospicloud.controlleurs;

import hospicloud.dto.SystemStatsDTO;
import hospicloud.services.ISystemMonitoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST pour exposer les points de terminaison de surveillance.
 */
@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private final ISystemMonitoringService systemMonitoringService;

    /**
     * Injection du service de surveillance via le constructeur.
     *
     * @param systemMonitoringService Le service pour obtenir les statistiques.
     */
    public MonitoringController(ISystemMonitoringService systemMonitoringService) {
        this.systemMonitoringService = systemMonitoringService;
    }

    /**
     * Point de terminaison GET pour récupérer les statistiques du système.
     *
     * @return Une {@link ResponseEntity} contenant le {@link SystemStatsDTO}.
     */
    @GetMapping("/system-stats")
    public ResponseEntity<SystemStatsDTO> getSystemStats() {
        SystemStatsDTO stats = systemMonitoringService.getSystemStats();
        return ResponseEntity.ok(stats);
    }
}
