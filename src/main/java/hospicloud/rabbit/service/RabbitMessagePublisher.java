package hospicloud.rabbit.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RabbitMessagePublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    public RabbitMessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate=rabbitTemplate;
    }
    
    @Value("${app.rabbit.exchange:rendezvous.exchange}")
    private String mainExchange;

    @Value("${app.rabbit.retry.exchange:rendezvous.retry.exchange}")
    private String retryExchange;

    @Value("${app.rabbit.dlx.exchange:rendezvous.dlx.exchange}")
    private String dlxExchange;

    @Value("${app.rabbit.retry1.queue:rendezvous.retry.queue.1}")
    private String retry1RoutingKey; // we use routing keys like retry.1, retry.2, retry.3

    @Value("${app.rabbit.retry2.queue:rendezvous.retry.queue.2}")
    private String retry2RoutingKey;

    @Value("${app.rabbit.retry3.queue:rendezvous.retry.queue.3}")
    private String retry3RoutingKey;
     
    // Publish directly to the main exchange (normal flow)
    public void sendToMain(String routingKey, Object message) {
        rabbitTemplate.convertAndSend(mainExchange, routingKey, message);
    }

    // Start the retry flow by publishing to retry.1 on the retry exchange
    public void sendToRetryLevel1(Object message) {
        rabbitTemplate.convertAndSend(retryExchange, "retry.1", message);
    }

    // You can publish directly to level 2 or 3 if you need to jump levels
    public void sendToRetryLevel2(Object message) {
        rabbitTemplate.convertAndSend(retryExchange, "retry.2", message);
    }

    public void sendToRetryLevel3(Object message) {
        rabbitTemplate.convertAndSend(retryExchange, "retry.3", message);
    }

    // Send to Dead Letter Exchange (DLQ)
    public void sendToDLQ(Object message) {
        rabbitTemplate.convertAndSend(dlxExchange, "dlq", message);
    }
 }