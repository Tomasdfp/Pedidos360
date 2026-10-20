package cl.pedidos360.config;

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
 * Configuración centralizada de RabbitMQ para el microservicio de Pedidos.
 * Declara de manera explícita todos los Exchanges, Colas (Queues), Bindings
 * y las políticas de Dead Letter Queue (DLQ / DLX) para el flujo asíncrono.
 */
@Configuration
public class RabbitMQConfig {

    // =========================================================================
    // Nombres inyectados desde application.properties (Configuración centralizada)
    // =========================================================================
    @Value("${app.rabbitmq.exchange.pedidos:pedidos.exchange}")
    private String pedidosExchangeName;

    @Value("${app.rabbitmq.queue.pedidos:pedidos.queue}")
    private String pedidosQueueName;

    @Value("${app.rabbitmq.routing-key.pedido-creado:pedido.creado}")
    private String pedidoCreadoRoutingKey;

    @Value("${app.rabbitmq.queue.notificaciones:notificacion.queue}")
    private String notificacionesQueueName;

    @Value("${app.rabbitmq.routing-key.notificaciones:pedido.creado}")
    private String notificacionesRoutingKey;

    @Value("${app.rabbitmq.exchange.dlx:pedidos.dlx}")
    private String dlxExchangeName;

    @Value("${app.rabbitmq.queue.pedidos-dlq:pedidos.dlq}")
    private String pedidosDlqName;

    @Value("${app.rabbitmq.routing-key.pedidos-dlq:pedidos.dead-letter}")
    private String pedidosDlqRoutingKey;

    @Value("${app.rabbitmq.queue.notificaciones-dlq:notificacion.dlq}")
    private String notificacionesDlqName;

    @Value("${app.rabbitmq.routing-key.notificaciones-dlq:notificacion.dead-letter}")
    private String notificacionesDlqRoutingKey;

    // =========================================================================
    // 1. Convertidor JSON y RabbitTemplate
    // =========================================================================
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

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    // =========================================================================
    // 2. Exchanges (Principal y Dead Letter Exchange - DLX)
    // =========================================================================
    /**
     * Exchange principal para publicación de eventos del dominio Pedidos.
     */
    @Bean
    public DirectExchange pedidosExchange() {
        return new DirectExchange(pedidosExchangeName, true, false);
    }

    /**
     * Dead Letter Exchange (DLX) para redirigir mensajes fallidos o rechazados con NACK.
     */
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(dlxExchangeName, true, false);
    }

    // =========================================================================
    // 3. Colas Principales con configuración de Dead Letter
    // =========================================================================
    /**
     * Cola principal de pedidos con redirección a DLQ en caso de falla no recuperable.
     */
    @Bean
    public Queue pedidosQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", dlxExchangeName);
        args.put("x-dead-letter-routing-key", pedidosDlqRoutingKey);
        return new Queue(pedidosQueueName, true, false, false, args);
    }

    /**
     * Cola de notificaciones con redirección a notificaciones.dlq en caso de falla.
     */
    @Bean
    public Queue notificacionesQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", dlxExchangeName);
        args.put("x-dead-letter-routing-key", notificacionesDlqRoutingKey);
        return new Queue(notificacionesQueueName, true, false, false, args);
    }

    // =========================================================================
    // 4. Colas DLQ (Dead Letter Queues)
    // =========================================================================
    @Bean
    public Queue pedidosDlq() {
        return new Queue(pedidosDlqName, true, false, false);
    }

    @Bean
    public Queue notificacionesDlq() {
        return new Queue(notificacionesDlqName, true, false, false);
    }

    // =========================================================================
    // 5. Bindings de Colas Principales hacia pedidos.exchange
    // =========================================================================
    @Bean
    public Binding pedidosBinding(Queue pedidosQueue, DirectExchange pedidosExchange) {
        return BindingBuilder.bind(pedidosQueue).to(pedidosExchange).with(pedidoCreadoRoutingKey);
    }

    @Bean
    public Binding notificacionesBinding(Queue notificacionesQueue, DirectExchange pedidosExchange) {
        return BindingBuilder.bind(notificacionesQueue).to(pedidosExchange).with(notificacionesRoutingKey);
    }

    // =========================================================================
    // 6. Bindings de Colas DLQ hacia dlxExchange
    // =========================================================================
    @Bean
    public Binding pedidosDlqBinding(Queue pedidosDlq, DirectExchange dlxExchange) {
        return BindingBuilder.bind(pedidosDlq).to(dlxExchange).with(pedidosDlqRoutingKey);
    }

    @Bean
    public Binding notificacionesDlqBinding(Queue notificacionesDlq, DirectExchange dlxExchange) {
        return BindingBuilder.bind(notificacionesDlq).to(dlxExchange).with(notificacionesDlqRoutingKey);
    }
}
