package hospicloud.dto;

import java.time.LocalDateTime;

/**
 * DTO pour exposer l'historique d'une métrique.
 */
public record MetricHistoryDTO(
        String metricName,
        Double value,
        LocalDateTime timestamp) {
}
