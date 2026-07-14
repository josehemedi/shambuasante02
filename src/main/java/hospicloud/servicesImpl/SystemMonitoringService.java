package hospicloud.servicesImpl;

import hospicloud.dto.SystemStatsDTO;
import hospicloud.services.ISystemMonitoringService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Implémentation du service de surveillance du système.
 * Utilise Micrometer pour récupérer les métriques et Spring Cache (avec Redis)
 * pour mettre en cache les résultats.
 */
@Service
public class SystemMonitoringService implements ISystemMonitoringService {

    private final MeterRegistry meterRegistry;

    /**
     * Injection du MeterRegistry via le constructeur.
     *
     * @param meterRegistry Le registre de métriques de Micrometer.
     */
    public SystemMonitoringService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Récupère les statistiques du système.
     * Les résultats sont mis en cache dans le cache "system-stats" pour une durée
     * de 10 secondes.
     * La clé de cache est statique ("stats") car nous voulons toujours la même
     * donnée.
     *
     * @return Un {@link SystemStatsDTO} avec les statistiques actuelles.
     */
    @Override
    @Cacheable(value = "system-stats", key = "'stats'")
    public SystemStatsDTO getSystemStats() {
        double cpuUsage = Optional.ofNullable(Search.in(meterRegistry).name("system.cpu.usage").gauge())
                .map(g -> g.value() * 100.0) // Convertir en pourcentage
                .orElse(0.0);

        double memoryUsed = Optional.ofNullable(Search.in(meterRegistry).name("jvm.memory.used").gauge())
                .map(g -> g.value())
                .orElse(0.0);

        double memoryMax = Optional.ofNullable(Search.in(meterRegistry).name("jvm.memory.max").gauge())
                .map(g -> g.value())
                .orElse(1.0); // Éviter la division par zéro

        double memoryUsage = (memoryMax > 0) ? (memoryUsed / memoryMax) * 100.0 : 0.0;

        return new SystemStatsDTO(
                cpuUsage,
                memoryUsage,
                Instant.now().toEpochMilli());
    }
}
