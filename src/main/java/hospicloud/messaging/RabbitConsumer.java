package hospicloud.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitConsumer {

    @RabbitListener(queues = "${app.rabbit.queue}")
    public void handleMessage(Object payload) {
        // TODO: adapter le traitement selon le type attendu
        System.out.println("[RabbitConsumer] message reçu: " + payload);
    }
    @RabbitListener(queues = "medecin.queue")
    public void debug(RendezVousModifieEvent event) {

        System.out.println("🔥 MESSAGE REÇU MEDCIN");
        System.out.println("ID medecin = " + event.getIdMedecin());
    }
}
