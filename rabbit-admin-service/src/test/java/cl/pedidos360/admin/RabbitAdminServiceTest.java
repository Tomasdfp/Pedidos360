package cl.pedidos360.admin;

import cl.pedidos360.admin.dto.*;
import cl.pedidos360.admin.service.RabbitAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RabbitAdminServiceTest {

    @Mock
    private RabbitAdmin rabbitAdmin;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitAdminService adminService;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(adminService, "ticketsExchange", "tickets.exchange");
        ReflectionTestUtils.setField(adminService, "ticketsRoutingKey", "ticket.nuevo");
    }

    @Test
    @DisplayName("Debe crear una cola con propiedades y DLX")
    public void testCreateQueue() {
        QueueRequestDto dto = QueueRequestDto.builder()
                .name("test.queue")
                .durable(true)
                .autoDelete(false)
                .deadLetterExchange("test.dlx")
                .deadLetterRoutingKey("test.dlq")
                .build();

        when(rabbitAdmin.declareQueue(any(Queue.class))).thenReturn("test.queue");

        Properties props = new Properties();
        props.put(RabbitAdmin.QUEUE_MESSAGE_COUNT, 0);
        props.put(RabbitAdmin.QUEUE_CONSUMER_COUNT, 0);
        when(rabbitAdmin.getQueueProperties("test.queue")).thenReturn(props);

        QueueInfoDto result = adminService.createQueue(dto);

        assertNotNull(result);
        assertEquals("test.queue", result.getName());
        assertEquals("ACTIVA", result.getStatus());
        verify(rabbitAdmin).declareQueue(any(Queue.class));
    }

    @Test
    @DisplayName("Debe eliminar una cola existente")
    public void testDeleteQueue() {
        when(rabbitAdmin.deleteQueue("test.queue")).thenReturn(true);

        boolean deleted = adminService.deleteQueue("test.queue");

        assertTrue(deleted);
        verify(rabbitAdmin).deleteQueue("test.queue");
    }

    @Test
    @DisplayName("Debe crear un exchange Direct, Topic o Fanout")
    public void testCreateExchange() {
        ExchangeRequestDto dto = ExchangeRequestDto.builder()
                .name("test.exchange")
                .type("DIRECT")
                .durable(true)
                .build();

        adminService.createExchange(dto);

        verify(rabbitAdmin).declareExchange(any(DirectExchange.class));
    }

    @Test
    @DisplayName("Debe crear un binding entre cola y exchange")
    public void testCreateBinding() {
        BindingRequestDto dto = BindingRequestDto.builder()
                .queueName("cola.test")
                .exchangeName("exchange.test")
                .routingKey("test.key")
                .build();

        adminService.createBinding(dto);

        ArgumentCaptor<Binding> captor = ArgumentCaptor.forClass(Binding.class);
        verify(rabbitAdmin).declareBinding(captor.capture());

        Binding captured = captor.getValue();
        assertEquals("cola.test", captured.getDestination());
        assertEquals("exchange.test", captured.getExchange());
        assertEquals("test.key", captured.getRoutingKey());
    }

    @Test
    @DisplayName("Debe publicar un ticket al exchange correspondiente")
    public void testPublishTicket() {
        TicketEvent ticket = TicketEvent.builder()
                .id(1L)
                .titulo("Problema con despacho")
                .descripcion("El pedido no ha llegado")
                .solicitante("Juan Gomez")
                .build();

        adminService.publishTicket(ticket);

        verify(rabbitTemplate).convertAndSend(eq("tickets.exchange"), eq("ticket.nuevo"), eq(ticket));
    }
}
