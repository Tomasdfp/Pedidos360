package cl.pedidos360.admin.service;

import cl.pedidos360.admin.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Servicio dedicado para la administración de infraestructura de RabbitMQ.
 * Encapsula completamente el uso de RabbitAdmin, aislando a los controladores
 * de los detalles de bajo nivel de la librería AMQP.
 */
@Service
@Slf4j
public class RabbitAdminService {

    private final RabbitAdmin rabbitAdmin;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange.tickets:tickets.exchange}")
    private String ticketsExchange;

    @Value("${app.rabbitmq.routing-key.tickets:ticket.nuevo}")
    private String ticketsRoutingKey;

    public RabbitAdminService(RabbitAdmin rabbitAdmin, RabbitTemplate rabbitTemplate) {
        this.rabbitAdmin = rabbitAdmin;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Declara y crea una nueva cola en RabbitMQ con las especificaciones del DTO.
     */
    public QueueInfoDto createQueue(QueueRequestDto dto) {
        log.info("[RABBIT ADMIN SERVICE] Creando cola: '{}' (durable={}, autoDelete={})",
                dto.getName(), dto.isDurable(), dto.isAutoDelete());

        Map<String, Object> arguments = new HashMap<>();
        if (dto.getDeadLetterExchange() != null && !dto.getDeadLetterExchange().isBlank()) {
            arguments.put("x-dead-letter-exchange", dto.getDeadLetterExchange().trim());
        }
        if (dto.getDeadLetterRoutingKey() != null && !dto.getDeadLetterRoutingKey().isBlank()) {
            arguments.put("x-dead-letter-routing-key", dto.getDeadLetterRoutingKey().trim());
        }

        Queue queue = new Queue(dto.getName().trim(), dto.isDurable(), dto.isExclusive(), dto.isAutoDelete(), arguments);
        String declaredName = rabbitAdmin.declareQueue(queue);

        log.info("[RABBIT ADMIN SERVICE] Cola creada exitosamente: '{}'", declaredName);
        return getQueueInfo(dto.getName());
    }

    /**
     * Elimina una cola existente por su nombre.
     */
    public boolean deleteQueue(String queueName) {
        log.warn("[RABBIT ADMIN SERVICE] Solicitando eliminación de cola: '{}'", queueName);
        boolean deleted = rabbitAdmin.deleteQueue(queueName.trim());
        log.info("[RABBIT ADMIN SERVICE] Cola '{}' eliminada: {}", queueName, deleted);
        return deleted;
    }

    /**
     * Declara y crea un nuevo Exchange en RabbitMQ.
     */
    public void createExchange(ExchangeRequestDto dto) {
        log.info("[RABBIT ADMIN SERVICE] Creando exchange: '{}' de tipo '{}'", dto.getName(), dto.getType());

        Exchange exchange = switch (dto.getType().toUpperCase()) {
            case "TOPIC" -> new TopicExchange(dto.getName().trim(), dto.isDurable(), dto.isAutoDelete());
            case "FANOUT" -> new FanoutExchange(dto.getName().trim(), dto.isDurable(), dto.isAutoDelete());
            case "HEADERS" -> new HeadersExchange(dto.getName().trim(), dto.isDurable(), dto.isAutoDelete());
            default -> new DirectExchange(dto.getName().trim(), dto.isDurable(), dto.isAutoDelete());
        };

        rabbitAdmin.declareExchange(exchange);
        log.info("[RABBIT ADMIN SERVICE] Exchange '{}' creado exitosamente", dto.getName());
    }

    /**
     * Elimina un exchange existente por su nombre.
     */
    public boolean deleteExchange(String exchangeName) {
        log.warn("[RABBIT ADMIN SERVICE] Eliminando exchange: '{}'", exchangeName);
        boolean deleted = rabbitAdmin.deleteExchange(exchangeName.trim());
        log.info("[RABBIT ADMIN SERVICE] Exchange '{}' eliminado: {}", exchangeName, deleted);
        return deleted;
    }

    /**
     * Crea un Binding vinculando una cola con un exchange bajo una routing key.
     */
    public void createBinding(BindingRequestDto dto) {
        log.info("[RABBIT ADMIN SERVICE] Creando binding entre cola '{}' y exchange '{}' con routing-key '{}'",
                dto.getQueueName(), dto.getExchangeName(), dto.getRoutingKey());

        Binding binding = new Binding(
                dto.getQueueName().trim(),
                Binding.DestinationType.QUEUE,
                dto.getExchangeName().trim(),
                dto.getRoutingKey().trim(),
                null
        );

        rabbitAdmin.declareBinding(binding);
        log.info("[RABBIT ADMIN SERVICE] Binding creado exitosamente.");
    }

    /**
     * Elimina un Binding existente.
     */
    public void removeBinding(BindingRequestDto dto) {
        log.info("[RABBIT ADMIN SERVICE] Eliminando binding entre cola '{}' y exchange '{}'",
                dto.getQueueName(), dto.getExchangeName());

        Binding binding = new Binding(
                dto.getQueueName().trim(),
                Binding.DestinationType.QUEUE,
                dto.getExchangeName().trim(),
                dto.getRoutingKey().trim(),
                null
        );

        rabbitAdmin.removeBinding(binding);
        log.info("[RABBIT ADMIN SERVICE] Binding eliminado exitosamente.");
    }

    /**
     * Obtiene información en tiempo real sobre el estado de una cola (mensajes y consumidores).
     */
    public QueueInfoDto getQueueInfo(String queueName) {
        Properties properties = rabbitAdmin.getQueueProperties(queueName.trim());
        if (properties == null) {
            return QueueInfoDto.builder()
                    .name(queueName)
                    .messageCount(0)
                    .consumerCount(0)
                    .exists(false)
                    .status("NO_EXISTE")
                    .build();
        }

        int messageCount = (int) properties.getOrDefault(RabbitAdmin.QUEUE_MESSAGE_COUNT, 0);
        int consumerCount = (int) properties.getOrDefault(RabbitAdmin.QUEUE_CONSUMER_COUNT, 0);

        return QueueInfoDto.builder()
                .name(queueName)
                .messageCount(messageCount)
                .consumerCount(consumerCount)
                .exists(true)
                .status("ACTIVA")
                .build();
    }

    /**
     * Publica un ticket en la cola de tickets para demostrar el funcionamiento del consumidor.
     */
    public void publishTicket(TicketEvent ticket) {
        log.info("[RABBIT ADMIN SERVICE] Publicando ticket '{}' al exchange '{}' con routing-key '{}'",
                ticket.getTitulo(), ticketsExchange, ticketsRoutingKey);
        rabbitTemplate.convertAndSend(ticketsExchange, ticketsRoutingKey, ticket);
    }
}
