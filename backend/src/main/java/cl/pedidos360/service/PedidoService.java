package cl.pedidos360.service;

import cl.pedidos360.kafka.event.PedidoAnaliticaEvent;
import cl.pedidos360.kafka.event.PedidoAuditoriaEvent;
import cl.pedidos360.kafka.producer.KafkaEventProducer;
import cl.pedidos360.messaging.producer.PedidoEventProducer;
import cl.pedidos360.model.Pedido;
import cl.pedidos360.repository.PedidoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoEventProducer pedidoEventProducer;
    private final KafkaEventProducer kafkaEventProducer;

    public PedidoService(PedidoRepository pedidoRepository,
                         PedidoEventProducer pedidoEventProducer,
                         KafkaEventProducer kafkaEventProducer) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoEventProducer = pedidoEventProducer;
        this.kafkaEventProducer = kafkaEventProducer;
    }

    public List<Pedido> findAll() {
        return pedidoRepository.findAll();
    }

    public Optional<Pedido> findById(Long id) {
        return pedidoRepository.findById(id);
    }

    public Pedido save(Pedido pedido) {
        if (pedido.getFechaCreacion() == null) {
            pedido.setFechaCreacion(LocalDateTime.now());
        }
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        // 1. Integración RabbitMQ: Notificación y flujo de negocio transaccional (preexistente)
        pedidoEventProducer.publicarPedidoCreado(pedidoGuardado);

        // 2. Integración Apache Kafka: Streaming y Analítica en tiempo real (EP5)
        try {
            PedidoAnaliticaEvent analiticaEvent = PedidoAnaliticaEvent.builder()
                    .pedidoId(pedidoGuardado.getId())
                    .numeroOrden(pedidoGuardado.getNumeroOrden())
                    .cliente(pedidoGuardado.getCliente())
                    .monto(pedidoGuardado.getMonto())
                    .estado(pedidoGuardado.getEstado())
                    .origen("WEB_PORTAL")
                    .categoria("RETAIL")
                    .timestamp(LocalDateTime.now())
                    .build();
            kafkaEventProducer.enviarEventoAnalitica(analiticaEvent);

            // 3. Tópico de Auditoría Inmutable
            PedidoAuditoriaEvent auditoriaEvent = PedidoAuditoriaEvent.builder()
                    .idAuditoria(UUID.randomUUID().toString())
                    .pedidoId(pedidoGuardado.getId())
                    .numeroOrden(pedidoGuardado.getNumeroOrden())
                    .accion("CREACION")
                    .usuario(pedidoGuardado.getCliente())
                    .ipOrigen("127.0.0.1")
                    .detalle("Creación de nuevo pedido con monto $" + pedidoGuardado.getMonto())
                    .timestamp(LocalDateTime.now())
                    .build();
            kafkaEventProducer.enviarEventoAuditoria(auditoriaEvent);
        } catch (Exception ex) {
            log.warn("[KAFKA INTEGRATION] Error al emitir eventos de streaming (modo tolerante a fallos): {}", ex.getMessage());
        }

        return pedidoGuardado;
    }

    public Optional<Pedido> update(Long id, Pedido pedidoDetails) {
        return pedidoRepository.findById(id).map(existing -> {
            if (pedidoDetails.getNumeroOrden() != null) {
                existing.setNumeroOrden(pedidoDetails.getNumeroOrden());
            }
            if (pedidoDetails.getCliente() != null) {
                existing.setCliente(pedidoDetails.getCliente());
            }
            if (pedidoDetails.getDescripcion() != null) {
                existing.setDescripcion(pedidoDetails.getDescripcion());
            }
            if (pedidoDetails.getMonto() != null) {
                existing.setMonto(pedidoDetails.getMonto());
            }
            if (pedidoDetails.getEstado() != null) {
                existing.setEstado(pedidoDetails.getEstado());
            }
            return pedidoRepository.save(existing);
        });
    }

    public boolean deleteById(Long id) {
        if (pedidoRepository.existsById(id)) {
            pedidoRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
