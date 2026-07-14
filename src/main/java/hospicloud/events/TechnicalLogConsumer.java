package hospicloud.events;

import hospicloud.dtos.events.TechnicalLogEvent;
import hospicloud.repositories.TechnicalLogRepository;
import hospicloud.security.TenantContext;
import hospicloud.servicesImpl.TechnicalLogServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TechnicalLogConsumer {

    private static final Logger log = LoggerFactory.getLogger(TechnicalLogConsumer.class);
    private final TechnicalLogRepository technicalLogRepository;

    public TechnicalLogConsumer(TechnicalLogRepository technicalLogRepository) {
        this.technicalLogRepository = technicalLogRepository;
    }

    @RabbitListener(queues = "${app.rabbit.technical.queue:technical.logs.queue}")
    public void handleTechnicalLog(TechnicalLogEvent event) {
        try {
            if (event.getHopitalId() != null) {
                TenantContext.setHopitalId(event.getHopitalId());
            }
            event.setStatus(TechnicalLogServiceImpl.normalizeStatus(event.getStatus()));
            technicalLogRepository.insert(event);
            log.debug("[TechnicalLog] enregistré module={} action={} hopital={}",
                    event.getModule(), event.getAction(), event.getHopitalId());
        } catch (Exception e) {
            log.error("Échec persistance log technique: {}", e.getMessage());
        } finally {
            TenantContext.clear();
        }
    }
}
