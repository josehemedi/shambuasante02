package hospicloud.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class RabbitConfig {

    @Value("${app.rabbit.queue:rendezvous.queue}")
    private String queueName;

    @Value("${app.rabbit.exchange:rendezvous.exchange}")
    private String exchangeName;

    @Value("${app.rabbit.routing-key:rendezvous.#}")
    private String routingKey;
    // ce sont les propriéts de la queue de rendezvous auquel n'aura besoin
    // New per-queue property values with sensible defaults
    @Value("${app.rabbit.patient.queue:patient.queue}")
    private String patientQueueName;

    @Value("${app.rabbit.patient.routing-key:patient.#}")
    private String patientRoutingKey;
    
    @Value("${app.rabbit.medecin.queue:medecin.queue}")
    private String medecinQueueName;

    @Value("${app.rabbit.medecin.routing-key:medecin.#}")
    private String medecinRoutingKey;

    @Value("${app.rabbit.societe.queue:societe.queue}")
    private String societeQueueName;

    @Value("${app.rabbit.societe.routing-key:societe.#}")
    private String societeRoutingKey;

    @Value("${app.rabbit.antecedent.queue:antecedent.queue}")
    private String antecedentQueueName;

    @Value("${app.rabbit.antecedent.routing-key:antecedent.#}")
    private String antecedentRoutingKey;

    @Value("${app.rabbit.technical.queue:technical.logs.queue}")
    private String technicalQueueName;

    @Value("${app.rabbit.technical.routing-key:technical.log}")
    private String technicalRoutingKey;

    // Dead Letter / Retry configuration
    @Value("${app.rabbit.dlx.exchange:rendezvous.dlx.exchange}")
    private String deadLetterExchangeName;

    @Value("${app.rabbit.dlx.queue:rendezvous.dlq}")
    private String deadLetterQueueName;

    // Retry exchange and 3-level retry queues (chained)
    @Value("${app.rabbit.retry.exchange:rendezvous.retry.exchange}")
    private String retryExchangeName;

    @Value("${app.rabbit.retry1.queue:rendezvous.retry.queue.1}")
    private String retryQueue1Name;
    @Value("${app.rabbit.retry1.ttl:10000}")
    private Integer retryQueue1Ttl;

    @Value("${app.rabbit.retry2.queue:rendezvous.retry.queue.2}")
    private String retryQueue2Name;
    @Value("${app.rabbit.retry2.ttl:30000}")
    private Integer retryQueue2Ttl;

    @Value("${app.rabbit.retry3.queue:rendezvous.retry.queue.3}")
    private String retryQueue3Name;
    @Value("${app.rabbit.retry3.ttl:60000}")
    private Integer retryQueue3Ttl;

    @Bean
    public Queue rendezvousQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public TopicExchange rendezvousExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Binding binding(Queue rendezvousQueue, TopicExchange rendezvousExchange) {
        return BindingBuilder.bind(rendezvousQueue).to(rendezvousExchange).with(routingKey);
    }
    // Patient queue and binding
    @Bean
    public Queue patientQueue() {
        return new Queue(patientQueueName, true);
    }
    @Bean
    public Binding patientBinding(Queue patientQueue, TopicExchange rendezvousExchange) {
        return BindingBuilder.bind(patientQueue).to(rendezvousExchange).with(patientRoutingKey);
    }// Medecin queue and binding
    @Bean
    public Queue medecinQueue() {
        return new Queue(medecinQueueName, true);
    }
    @Bean
    public Binding medecinBinding(Queue medecinQueue, TopicExchange rendezvousExchange) {
        return BindingBuilder.bind(medecinQueue).to(rendezvousExchange).with(medecinRoutingKey);
    }
    // Societe queue and binding
    @Bean
    public Queue societeQueue() {
        return new Queue(societeQueueName, true);
    }
    @Bean
    public Binding societeBinding(Queue societeQueue, TopicExchange rendezvousExchange) {
        return BindingBuilder.bind(societeQueue).to(rendezvousExchange).with(societeRoutingKey);
    }// Antecedent queue and binding
    @Bean
    public Queue antecedentQueue() {
        return new Queue(antecedentQueueName, true);
    }
    @Bean
    public Binding antecedentBinding(Queue antecedentQueue, TopicExchange rendezvousExchange) {
        return BindingBuilder.bind(antecedentQueue).to(rendezvousExchange).with(antecedentRoutingKey);
    }

    // Facture/Rapport queue
    @Bean
    public Queue factureQueue() {
        return new Queue("rapport.queue", true);
    }
    @Bean
    public Binding factureBinding(Queue factureQueue, TopicExchange rendezvousExchange) {
        return BindingBuilder.bind(factureQueue).to(rendezvousExchange).with("rapport.#");
    }

    // Enregistrement async (patients, arrivées)
    @Bean
    public Queue enregistrementQueue() {
        return new Queue("enregistrement.queue", true);
    }
    @Bean
    public Binding enregistrementBinding(Queue enregistrementQueue, TopicExchange rendezvousExchange) {
        return BindingBuilder.bind(enregistrementQueue).to(rendezvousExchange).with("enregistrement.#");
    }

    @Bean
    public Queue technicalLogsQueue() {
        return new Queue(technicalQueueName, true);
    }

    @Bean
    public Binding technicalLogsBinding(Queue technicalLogsQueue, TopicExchange rendezvousExchange) {
        return BindingBuilder.bind(technicalLogsQueue).to(rendezvousExchange).with(technicalRoutingKey);
    }

    // Dead Letter Exchange and Queue
    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(deadLetterExchangeName);
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(deadLetterQueueName, true);
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with("#");
    }

    // Retry exchange (dedicated) and chained retry queues: retry.1 -> retry.2 -> retry.3 -> DLX
    @Bean
    public TopicExchange retryExchange() {
        return new TopicExchange(retryExchangeName);
    }

    @Bean
    public Queue retryQueue1() {
        java.util.Map<String, Object> args = new java.util.HashMap<>();
        // when TTL expires on retry1, dead-letter to retry exchange with routing key retry.2
        args.put("x-dead-letter-exchange", retryExchangeName);
        args.put("x-dead-letter-routing-key", "retry.2");
        args.put("x-message-ttl", retryQueue1Ttl);
        return new Queue(retryQueue1Name, true, false, false, args);
    }

    @Bean
    public Queue retryQueue2() {
        java.util.Map<String, Object> args = new java.util.HashMap<>();
        // when TTL expires on retry2, dead-letter to retry exchange with routing key retry.3
        args.put("x-dead-letter-exchange", retryExchangeName);
        args.put("x-dead-letter-routing-key", "retry.3");
        args.put("x-message-ttl", retryQueue2Ttl);
        return new Queue(retryQueue2Name, true, false, false, args);
    }

    @Bean
    public Queue retryQueue3() {
        java.util.Map<String, Object> args = new java.util.HashMap<>();
        // when TTL expires on retry3, dead-letter to DLX (final DLQ)
        args.put("x-dead-letter-exchange", deadLetterExchangeName);
        args.put("x-dead-letter-routing-key", "dlq");
        args.put("x-message-ttl", retryQueue3Ttl);
        return new Queue(retryQueue3Name, true, false, false, args);
    }

    @Bean
    public Binding retry1Binding(Queue retryQueue1, TopicExchange retryExchange) {
        return BindingBuilder.bind(retryQueue1).to(retryExchange).with("retry.1");
    }

    @Bean
    public Binding retry2Binding(Queue retryQueue2, TopicExchange retryExchange) {
        return BindingBuilder.bind(retryQueue2).to(retryExchange).with("retry.2");
    }

    @Bean
    public Binding retry3Binding(Queue retryQueue3, TopicExchange retryExchange) {
        return BindingBuilder.bind(retryQueue3).to(retryExchange).with("retry.3");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange("hospicloud.events.exchange");
    }
    @Bean
    public Queue retryQueue() {
        // keep a legacy single retryQueue bean (not used by multi-retry flow)
        java.util.Map<String, Object> args = new java.util.HashMap<>();
        args.put("x-dead-letter-exchange", exchangeName); // route back to main exchange when TTL expires
        args.put("x-message-ttl", 60000);
        return new Queue("rendezvous.retry.legacy", true, false, false, args);
    }
}