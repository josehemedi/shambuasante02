package hospicloud.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitProducer {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;

    public RabbitProducer(RabbitTemplate rabbitTemplate,
                          @Value("${app.rabbit.exchange}") String exchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
    }

    public void send(String routingKey, Object event) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
        System.out.println("SEND -> exchange=" + exchange +
                " routingKey=" + routingKey +
                " event=" + event);
    }
}
