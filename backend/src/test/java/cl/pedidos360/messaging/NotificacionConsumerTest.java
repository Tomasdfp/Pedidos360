package cl.pedidos360.messaging;

import cl.pedidos360.messaging.consumer.NotificacionConsumer;
import cl.pedidos360.messaging.consumer.NotificacionDlqConsumer;
import cl.pedidos360.messaging.event.PedidoCreadoEvent;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificacionConsumerTest {

    @Mock
    private Channel channel;

    @InjectMocks
    private NotificacionConsumer notificacionConsumer;

    @InjectMocks
    private NotificacionDlqConsumer notificacionDlqConsumer;

    @Test
    @DisplayName("Debe procesar notificación exitosa y emitir basicAck manual")
    public void testProcesarNotificacionExitosa_conAck() throws IOException {
        PedidoCreadoEvent event = PedidoCreadoEvent.builder()
                .id(1L)
                .numeroOrden("ORD-001")
                .cliente("Maria Gomez")
                .descripcion("Audífonos Bluetooth")
                .monto(45000.0)
                .estado("PENDIENTE")
                .fechaCreacion(LocalDateTime.now())
                .build();

        long deliveryTag = 12345L;

        notificacionConsumer.procesarNotificacionPedido(event, channel, deliveryTag);

        // Verifica que se confirme el mensaje con basicAck
        verify(channel, times(1)).basicAck(deliveryTag, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    @DisplayName("Debe capturar error con monto inválido y emitir basicNack con requeue=false hacia DLQ")
    public void testProcesarNotificacionErrorMonto_conNackADlq() throws IOException {
        PedidoCreadoEvent event = PedidoCreadoEvent.builder()
                .id(2L)
                .numeroOrden("ORD-002")
                .cliente("Juan Perez")
                .descripcion("Producto Error")
                .monto(-100.0) // Monto inválido
                .estado("PENDIENTE")
                .fechaCreacion(LocalDateTime.now())
                .build();

        long deliveryTag = 54321L;

        notificacionConsumer.procesarNotificacionPedido(event, channel, deliveryTag);

        // Verifica que NO se haga ACK y se haga NACK con requeue=false
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
        verify(channel, times(1)).basicNack(deliveryTag, false, false);
    }

    @Test
    @DisplayName("Debe capturar simulación de error forzado y desviar a DLQ")
    public void testProcesarNotificacionSimulacionError_conNackADlq() throws IOException {
        PedidoCreadoEvent event = PedidoCreadoEvent.builder()
                .id(3L)
                .numeroOrden("ORD-003")
                .cliente("Pedro Soto")
                .descripcion("SIMULAR_ERROR_DLQ - Prueba de Dead Letter Queue")
                .monto(75000.0)
                .estado("PENDIENTE")
                .fechaCreacion(LocalDateTime.now())
                .build();

        long deliveryTag = 99999L;

        notificacionConsumer.procesarNotificacionPedido(event, channel, deliveryTag);

        // NACK sin requeue para desvío a DLQ
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
        verify(channel, times(1)).basicNack(deliveryTag, false, false);
    }

    @Test
    @DisplayName("Consumidor DLQ debe procesar mensaje fallido y emitir ACK de auditoría")
    public void testConsumidorDlq_conAck() throws IOException {
        PedidoCreadoEvent event = PedidoCreadoEvent.builder()
                .id(4L)
                .numeroOrden("ORD-004-DLQ")
                .cliente("Auditoria Test")
                .descripcion("Mensaje en Dead Letter Queue")
                .monto(10000.0)
                .estado("FALLIDO")
                .eventId("UUID-DLQ-123")
                .build();

        long deliveryTag = 77777L;

        notificacionDlqConsumer.procesarMensajeFallidoDlq(event, channel, deliveryTag);

        verify(channel, times(1)).basicAck(deliveryTag, false);
    }
}
