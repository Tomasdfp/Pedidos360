package cl.pedidos360.admin;

import cl.pedidos360.admin.consumer.TicketConsumer;
import cl.pedidos360.admin.consumer.TicketDlqConsumer;
import cl.pedidos360.admin.dto.TicketEvent;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketConsumerTest {

    @Mock
    private Channel channel;

    @InjectMocks
    private TicketConsumer ticketConsumer;

    @InjectMocks
    private TicketDlqConsumer ticketDlqConsumer;

    @Test
    @DisplayName("Debe procesar ticket exitosamente y emitir ACK manual")
    public void testProcesarTicket_Success_conAck() throws IOException {
        TicketEvent ticket = TicketEvent.builder()
                .id(101L)
                .titulo("Generación de comprobante")
                .descripcion("Generar factura para orden #50")
                .solicitante("Cliente A")
                .prioridad("ALTA")
                .build();

        long deliveryTag = 111L;

        ticketConsumer.procesarTicket(ticket, channel, deliveryTag);

        verify(channel, times(1)).basicAck(deliveryTag, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    @DisplayName("Debe capturar fallo en ticket y emitir basicNack sin requeue hacia DLQ")
    public void testProcesarTicket_Fallo_conNackADlq() throws IOException {
        TicketEvent ticket = TicketEvent.builder()
                .id(102L)
                .titulo("Ticket Fallido")
                .descripcion("SIMULAR_ERROR_DLQ - Prueba de Dead Letter")
                .solicitante("Tester")
                .debeFallar(true)
                .build();

        long deliveryTag = 222L;

        ticketConsumer.procesarTicket(ticket, channel, deliveryTag);

        verify(channel, never()).basicAck(anyLong(), anyBoolean());
        verify(channel, times(1)).basicNack(deliveryTag, false, false);
    }

    @Test
    @DisplayName("Consumidor DLQ debe registrar ticket fallido y emitir ACK")
    public void testTicketDlqConsumer_conAck() throws IOException {
        TicketEvent ticket = TicketEvent.builder()
                .id(103L)
                .titulo("Ticket en DLQ")
                .descripcion("Fallo previo")
                .solicitante("Auditor")
                .build();

        long deliveryTag = 333L;

        ticketDlqConsumer.auditarTicketFallido(ticket, channel, deliveryTag);

        verify(channel, times(1)).basicAck(deliveryTag, false);
    }
}
