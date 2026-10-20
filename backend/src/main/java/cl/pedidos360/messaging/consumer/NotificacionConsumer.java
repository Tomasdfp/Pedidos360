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
 * Consumidor asíncrono para el dominio funcional de Notificaciones.
 * Escucha la cola de notificaciones, procesa el envío de alertas y gestiona
 * el ciclo de vida del mensaje mediante confirmación manual (ACK) y desvío a DLQ (NACK).
 */
@Component
@Slf4j
public class NotificacionConsumer {

    /**
     * Consume eventos de creación de pedidos desde la cola notificacion.queue.
     * Aplica confirmación manual (ACK) y gestión de errores con desvío a DLQ (NACK sin requeue).
     *
     * @param event       Evento deserializado de PedidoCreadoEvent
     * @param channel     Canal AMQP para control manual de ACK/NACK
     * @param deliveryTag Etiqueta única de entrega del mensaje
     * @throws IOException Si ocurre un error de comunicación con el canal de RabbitMQ
     */
    @RabbitListener(queues = "${app.rabbitmq.queue.notificaciones}")
    public void procesarNotificacionPedido(
            @Payload PedidoCreadoEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {

        log.info("[RABBITMQ NOTIFICACION CONSUMER] Mensaje recibido. DeliveryTag={}, EventId={}, Orden={}",
                deliveryTag, event.getEventId(), event.getNumeroOrden());

        try {
            // Validación de negocio / Simulación de errores para pruebas de DLQ
            if (event.getMonto() != null && event.getMonto() <= 0) {
                throw new IllegalArgumentException("El monto del pedido es inválido ($" + event.getMonto() + ")");
            }

            if (event.getDescripcion() != null && event.getDescripcion().contains("SIMULAR_ERROR_DLQ")) {
                throw new RuntimeException("Fallo forzado para validación de Dead Letter Queue (DLQ)");
            }

            // Simulación del procesamiento de la notificación (Email, SMS, Push)
            log.info("[NOTIFICACION ENVIADA] Notificación enviada al cliente '{}' para el pedido '{}' (Monto: ${}). Estado: {}",
                    event.getCliente(), event.getNumeroOrden(), event.getMonto(), event.getEstado());

            // Confirmación explícita exitosa (ACK)
            channel.basicAck(deliveryTag, false);
            log.info("[RABBITMQ ACK] Mensaje con DeliveryTag={} confirmado exitosamente.", deliveryTag);

        } catch (Exception ex) {
            log.error("[RABBITMQ NACK -> DLQ] Error procesando notificación para la orden {}: '{}'. " +
                      "Rechazando mensaje sin requeue para enrutamiento automático a DLQ...",
                    event.getNumeroOrden(), ex.getMessage());

            // NACK explícito con requeue=false -> RabbitMQ lo envía automáticamente al DLX/DLQ
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
