package hospicloud.services;

import hospicloud.model.SystemMetricHistory;
import hospicloud.repositories.SystemMetricRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service pour persister périodiquement les métriques système
 * et nettoyer les anciennes données.
 */
@Service
public class MetricPersistenceService {

    private static final Logger log = LoggerFactory.getLogger(MetricPersistenceService.class);
    private static final String TENANT_ID_SUPER_ADMIN = "super-admin"; // Tenant statique pour les métriques globales

    private final MeterRegistry meterRegistry;
    private final SystemMetricRepository metricRepository;

    public MetricPersistenceService(MeterRegistry meterRegistry, SystemMetricRepository metricRepository) {
        this.meterRegistry = meterRegistry;
        this.metricRepository = metricRepository;
    }

    /**
     * Tâche planifiée pour enregistrer les métriques CPU et mémoire chaque minute.
     */
    @Scheduled(fixedRate = 60000) // 60000 ms = 1 minute
    public void recordSystemMetrics() {
        log.info("Enregistrement des métriques système...");

        // Enregistrer l'utilisation du CPU
        double cpuUsage = Optional.ofNullable(Search.in(meterRegistry).name("system.cpu.usage").gauge())
                .map(g -> g.value() * 100.0)
                .orElse(0.0);
        saveMetric("system.cpu.usage", cpuUsage);

        // Enregistrer l'utilisation de la mémoire
        double memoryUsed = Optional.ofNullable(Search.in(meterRegistry).name("jvm.memory.used").gauge())
                .map(g -> g.value()).orElse(0.0);
        double memoryMax = Optional.ofNullable(Search.in(meterRegistry).name("jvm.memory.max").gauge())
                .map(g -> g.value()).orElse(1.0);
        double memoryUsage = (memoryMax > 0) ? (memoryUsed / memoryMax) * 100.0 : 0.0;
        saveMetric("jvm.memory.usage.percent", memoryUsage);

        log.info("Métriques enregistrées : CPU={}%, Mémoire={}%", String.format("%.2f", cpuUsage),
                String.format("%.2f", memoryUsage));
    }

    private void saveMetric(String metricName, Double value) {
        SystemMetricHistory metric = new SystemMetricHistory();
        metric.setTenantId(TENANT_ID_SUPER_ADMIN);
        metric.setMetricName(metricName);
        metric.setMetricValue(value);
        metric.setCreatedAt(LocalDateTime.now());
        metricRepository.save(metric);
    }

    /**
     * Tâche planifiée pour nettoyer les anciennes métriques (plus de 30 jours),
     * s'exécute tous les jours à 2h du matin.
     */
    @Scheduled(cron = "0 0 2 * * ?") // Tous les jours à 2h00
    public void cleanOldMetrics() {
        log.info("Nettoyage des anciennes métriques...");
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        int deletedRows = metricRepository.deleteOlderThan(cutoffDate);
        log.info("{} anciennes lignes de métriques ont été supprimées.", deletedRows);
    }
}
