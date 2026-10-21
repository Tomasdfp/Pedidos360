package cl.pedidos360.admin.consumer;

import cl.pedidos360.admin.dto.TicketEvent;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Consumidor de la Dead Letter Queue (DLQ) para tickets no entregados o con error.
 * Registra en logs de auditoría para su revisión y análisis de causas.
 */
@Component
@Slf4j
public class TicketDlqConsumer {

    @RabbitListener(queues = "${app.rabbitmq.queue.tickets-dlq:ticket.dlq}")
    public void auditarTicketFallido(
            @Payload TicketEvent ticket,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {

        log.warn("[TICKET DLQ AUDITORIA] Mensaje de ticket fallido capturado en DLQ: ID={}, Titulo='{}', Solicitante='{}', Descripcion='{}'",
                ticket.getId(), ticket.getTitulo(), ticket.getSolicitante(), ticket.getDescripcion());

        // Confirmamos lectura de auditoría
        channel.basicAck(deliveryTag, false);
        log.info("[TICKET DLQ ACK] Mensaje en DLQ DeliveryTag={} registrado en auditoría.", deliveryTag);
    }
}
