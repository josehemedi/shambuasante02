package hospicloud.services;

import hospicloud.dto.MetricHistoryDTO;
import hospicloud.model.SystemMetricHistory;
import hospicloud.repositories.SystemMetricRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour lire les données de monitoring persistées.
 */
@Service
public class MonitoringService {

    private final SystemMetricRepository metricRepository;

    public MonitoringService(SystemMetricRepository metricRepository) {
        this.metricRepository = metricRepository;
    }

    /**
     * Récupère l'historique des métriques pour le super-administrateur.
     * 
     * @return Une liste de DTOs de métriques.
     */
    public List<MetricHistoryDTO> getSystemMetricHistory() {
        // Pour le monitoring global, nous utilisons un tenantId statique.
        String tenantId = "super-admin";
        List<SystemMetricHistory> metrics = metricRepository.findByTenantId(tenantId);

        return metrics.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private MetricHistoryDTO convertToDTO(SystemMetricHistory metric) {
        return new MetricHistoryDTO(
                metric.getMetricName(),
                metric.getMetricValue(),
                metric.getCreatedAt());
    }
}
