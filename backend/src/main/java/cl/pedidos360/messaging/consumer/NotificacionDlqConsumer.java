package cl.pedidos360.messaging.consumer;

import cl.pedidos360.messaging.event.PedidoCreadoEvent;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Consumidor de la Dead Letter Queue (DLQ) para notificaciones no entregadas o fallidas.
 * Registra en logs de auditoría los mensajes enviados a DLQ para trazabilidad y monitoreo.
 */
@Component
@Slf4j
public class NotificacionDlqConsumer {

    /**
     * Consume mensajes que fueron rechazados (NACK) y enviados al Dead Letter Queue notificacion.dlq.
     *
     * @param event       Evento que no pudo ser procesado
     * @param channel     Canal AMQP
     * @param deliveryTag Etiqueta de entrega
     * @throws IOException Si ocurre error en la confirmación
     */
    @RabbitListener(queues = "${app.rabbitmq.queue.notificaciones-dlq}")
    public void procesarMensajeFallidoDlq(
            @Payload PedidoCreadoEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {

        log.warn("[RABBITMQ DLQ AUDITORIA] Mensaje no entregado capturado en DLQ: Orden='{}', Cliente='{}', Monto={}, EventId='{}', Descripcion='{}'",
                event.getNumeroOrden(), event.getCliente(), event.getMonto(), event.getEventId(), event.getDescripcion());

        // Confirmamos el procesamiento de auditoría del mensaje en DLQ
        channel.basicAck(deliveryTag, false);
        log.info("[RABBITMQ DLQ ACK] Mensaje en DLQ DeliveryTag={} registrado y confirmado en auditoría.", deliveryTag);
    }
}
