package hospicloud.events;

import org.springframework.stereotype.Component;

import hospicloud.dtos.events.RendezVousCreatedEvent;
import hospicloud.dtos.events.RendezVousModifieEvent;
import hospicloud.messaging.RabbitProducer;

@Component
public class EventProducer {

    private final RabbitProducer rabbitProducer;

    public EventProducer(RabbitProducer rabbitProducer) {
        this.rabbitProducer = rabbitProducer;
    }

    // =====================================================
    // 📌 RDV MODIFIÉ → NOTIFICATION CIBLÉE MÉDECIN
    // =====================================================
    public void publishRendezVousModifie(RendezVousModifieEvent event) {

        rabbitProducer.send(
                "medecin." + event.getIdMedecin(),
                event
        );
    }

    // =====================================================
    // 📌 RDV CRÉÉ → EVENT GLOBAL (OU AUTRE ROUTING)
    // =====================================================
    public void publishRendezVousCreated(RendezVousCreatedEvent event) {

        rabbitProducer.send(
                "rdv.created",
                event
        );
        
    }
    
}