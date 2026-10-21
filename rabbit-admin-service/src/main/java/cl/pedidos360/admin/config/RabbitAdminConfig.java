package cl.pedidos360.admin.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración centralizada de RabbitMQ para el microservicio Administrador.
 * Declara el bean RabbitAdmin para operaciones dinámicas (crear/eliminar colas,
 * exchanges y bindings) y la infraestructura base para el procesamiento de tickets.
 */
@Configuration
public class RabbitAdminConfig {

    @Value("${app.rabbitmq.exchange.tickets:tickets.exchange}")
    private String ticketsExchangeName;

    @Value("${app.rabbitmq.queue.tickets:ticket.queue}")
    private String ticketsQueueName;

    @Value("${app.rabbitmq.routing-key.tickets:ticket.nuevo}")
    private String ticketsRoutingKey;

    @Value("${app.rabbitmq.exchange.dlx:tickets.dlx}")
    private String ticketsDlxName;

    @Value("${app.rabbitmq.queue.tickets-dlq:ticket.dlq}")
    private String ticketsDlqName;

    @Value("${app.rabbitmq.routing-key.tickets-dlq:ticket.dead-letter}")
    private String ticketsDlqRoutingKey;

    // =========================================================================
    // 1. Beans Base: RabbitAdmin, MessageConverter y RabbitTemplate
    // =========================================================================

    /**
     * Bean central de RabbitAdmin para gestionar dinámicamente recursos AMQP
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

    // =========================================================================
    // 2. Infraestructura Base de Tickets (Exchanges, Queues y DLQ)
    // =========================================================================

    @Bean
    public DirectExchange ticketsExchange() {
        return new DirectExchange(ticketsExchangeName, true, false);
    }

    @Bean
    public DirectExchange ticketsDlxExchange() {
        return new DirectExchange(ticketsDlxName, true, false);
    }

    @Bean
    public Queue ticketsQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", ticketsDlxName);
        args.put("x-dead-letter-routing-key", ticketsDlqRoutingKey);
        return new Queue(ticketsQueueName, true, false, false, args);
    }

    @Bean
    public Queue ticketsDlq() {
        return new Queue(ticketsDlqName, true, false, false);
    }

    @Bean
    public Binding ticketsBinding(Queue ticketsQueue, DirectExchange ticketsExchange) {
        return BindingBuilder.bind(ticketsQueue).to(ticketsExchange).with(ticketsRoutingKey);
    }

    @Bean
    public Binding ticketsDlqBinding(Queue ticketsDlq, DirectExchange ticketsDlxExchange) {
        return BindingBuilder.bind(ticketsDlq).to(ticketsDlxExchange).with(ticketsDlqRoutingKey);
    }
}
