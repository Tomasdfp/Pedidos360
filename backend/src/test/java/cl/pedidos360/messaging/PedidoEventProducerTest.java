package cl.pedidos360.messaging;

import cl.pedidos360.messaging.event.PedidoCreadoEvent;
import cl.pedidos360.messaging.producer.PedidoEventProducer;
import cl.pedidos360.model.Pedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PedidoEventProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PedidoEventProducer pedidoEventProducer;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(pedidoEventProducer, "pedidosExchange", "pedidos.exchange");
        ReflectionTestUtils.setField(pedidoEventProducer, "pedidoCreadoRoutingKey", "pedido.creado");
    }

    @Test
    @DisplayName("Debe publicar evento PedidoCreadoEvent con routing-key pedido.creado")
    public void testPublicarPedidoCreado() {
        Pedido pedido = Pedido.builder()
                .id(100L)
                .numeroOrden("ORD-100")
                .cliente("Carlos Soto")
                .descripcion("Monitor Curvo")
                .monto(199990.0)
                .estado("PENDIENTE")
                .fechaCreacion(LocalDateTime.now())
                .build();

        pedidoEventProducer.publicarPedidoCreado(pedido);

        ArgumentCaptor<PedidoCreadoEvent> eventCaptor = ArgumentCaptor.forClass(PedidoCreadoEvent.class);
        verify(rabbitTemplate).convertAndSend(
                eq("pedidos.exchange"),
                eq("pedido.creado"),
                eventCaptor.capture()
        );

        PedidoCreadoEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);
        assertEquals(100L, capturedEvent.getId());
        assertEquals("ORD-100", capturedEvent.getNumeroOrden());
        assertEquals("Carlos Soto", capturedEvent.getCliente());
        assertEquals(199990.0, capturedEvent.getMonto());
        assertEquals("PEDIDO_CREADO", capturedEvent.getTipoEvento());
        assertNotNull(capturedEvent.getEventId());
        assertNotNull(capturedEvent.getTimestamp());
    }
}
