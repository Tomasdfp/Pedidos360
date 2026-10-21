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
 * Consumidor funcional para el procesamiento asíncrono de Tickets de soporte/documentos.
 * Escucha la cola 'ticket.queue', gestiona confirmaciones manuales (ACK)
 * y desvío hacia Dead Letter Queue (DLQ) en caso de fallas no recuperables.
 */
@Component
@Slf4j
public class TicketConsumer {

    @RabbitListener(queues = "${app.rabbitmq.queue.tickets:ticket.queue}")
    public void procesarTicket(
            @Payload TicketEvent ticket,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {

        log.info("[RABBIT TICKET CONSUMER] Mensaje recibido. DeliveryTag={}, TicketId={}, Titulo='{}', Prioridad='{}'",
                deliveryTag, ticket.getId(), ticket.getTitulo(), ticket.getPrioridad());

        try {
            // Validación / Simulación de errores para validación de DLQ
            if (ticket.isDebeFallar() ||
                (ticket.getDescripcion() != null && ticket.getDescripcion().contains("SIMULAR_ERROR_DLQ"))) {
                throw new RuntimeException("Error forzado en procesamiento de ticket para validación de DLQ");
            }

            // Simulación de procesamiento de ticket (ej. generación de documento, asignación técnica)
            log.info("[TICKET PROCESADO] Ticket #{} ('{}') generado y asignado para solicitante '{}'.",
                    ticket.getId(), ticket.getTitulo(), ticket.getSolicitante());

            // Confirmación explícita exitosa (ACK)
            channel.basicAck(deliveryTag, false);
            log.info("[RABBIT TICKET ACK] Mensaje con DeliveryTag={} confirmado exitosamente.", deliveryTag);

        } catch (Exception ex) {
            log.error("[RABBIT TICKET NACK -> DLQ] Error al procesar ticket #{}: '{}'. " +
                      "Rechazando mensaje sin requeue para envío a Dead Letter Queue...",
                    ticket.getId(), ex.getMessage());

            // NACK sin requeue -> RabbitMQ lo desvía automáticamente a 'ticket.dlq' a través de tickets.dlx
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
