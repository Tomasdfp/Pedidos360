package cl.pedidos360.messaging.producer;

import cl.pedidos360.messaging.event.PedidoCreadoEvent;
import cl.pedidos360.model.Pedido;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Productor de eventos de mensajería asíncrona para el dominio Pedidos.
 * Encapsula la lógica de publicación hacia RabbitMQ aislando completamente la capa de negocio.
 */
@Component
@Slf4j
public class PedidoEventProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange.pedidos:pedidos.exchange}")
    private String pedidosExchange;

    @Value("${app.rabbitmq.routing-key.pedido-creado:pedido.creado}")
    private String pedidoCreadoRoutingKey;

    public PedidoEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publica el evento de pedido creado a través del exchange configurado en RabbitMQ.
     *
     * @param pedido La entidad Pedido recién persistida.
     */
    public void publicarPedidoCreado(Pedido pedido) {
        PedidoCreadoEvent event = PedidoCreadoEvent.builder()
                .id(pedido.getId())
                .numeroOrden(pedido.getNumeroOrden())
                .cliente(pedido.getCliente())
                .descripcion(pedido.getDescripcion())
                .monto(pedido.getMonto())
                .estado(pedido.getEstado())
                .fechaCreacion(pedido.getFechaCreacion())
                .eventId(UUID.randomUUID().toString())
                .tipoEvento("PEDIDO_CREADO")
                .timestamp(LocalDateTime.now())
                .build();

        log.info("[RABBITMQ PRODUCER] Publicando evento {} para orden {} hacia exchange: '{}' con routing-key: '{}'",
                event.getTipoEvento(), event.getNumeroOrden(), pedidosExchange, pedidoCreadoRoutingKey);

        try {
            rabbitTemplate.convertAndSend(pedidosExchange, pedidoCreadoRoutingKey, event);
            log.info("[RABBITMQ PRODUCER] Evento publicado exitosamente: ID={}", event.getEventId());
        } catch (Exception e) {
            log.error("[RABBITMQ PRODUCER] Error al publicar evento de pedido {} en RabbitMQ: {}",
                    event.getNumeroOrden(), e.getMessage(), e);
            // Dependiendo del requerimiento se puede relanzar o manejar en outbox pattern
        }
    }
}
