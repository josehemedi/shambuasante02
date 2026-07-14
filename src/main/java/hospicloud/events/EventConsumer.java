package hospicloud.events;

import hospicloud.dtos.events.RendezVousCreatedEvent;
import hospicloud.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EventConsumer {
    private static final Logger log = LoggerFactory.getLogger(EventConsumer.class);

    @RabbitListener(queues = "${app.rabbit.queue}")
    public void handleRendezVousCreated(RendezVousCreatedEvent event) {
        try {
            TenantContext.setHopitalId(event.getIdHopital());
            log.info("[Consumer] RendezVousCreatedEvent reçu -> idRdv={}, idHopital={}, idMedecin={}, date={}, motif={}, statut={}",
                    event.getIdRdv(), event.getIdHopital(), event.getIdMedecin(), event.getDateHeureRdv(), event.getMotifVisite(), event.getStatutRdv());
        } finally {
            TenantContext.clear();
        }
    }
}