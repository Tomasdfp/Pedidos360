package cl.pedidos360.kafka.consumer;

import cl.pedidos360.kafka.event.PedidoAnaliticaEvent;
import cl.pedidos360.kafka.event.PedidoAuditoriaEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Consumidores de Apache Kafka organizados por Consumer Groups separados:
 * - 'pedidos-analitica-group': Procesa métricas de ventas y streaming analítico.
 * - 'pedidos-auditoria-group': Procesa trazabilidad y registro de auditoría.
 * Separación clara entre infraestructura de mensajería y lógica de negocio.
 */
@Component
@Slf4j
public class KafkaEventConsumers {

    /**
     * Consumidor 1: Analítica en tiempo real.
     */
    @KafkaListener(
            topics = "${app.kafka.topics.pedidos-analitica.name:pedidos.analitica.v1}",
            groupId = "${app.kafka.consumer-groups.analitica:pedidos-analitica-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumirAnalitica(
            @Payload PedidoAnaliticaEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        log.info("[KAFKA ANALYTICS CONSUMER] Evento recibido. Partition={}, Offset={}, Key='{}', PedidoId={}, Monto={}",
                partition, offset, key, event.getPedidoId(), event.getMonto());

        try {
            // Lógica de dominio: procesamiento de analítica y métricas
            procesarMetricasAnalitica(event);
            log.info("[KAFKA ANALYTICS OK] Métricas actualizadas para pedido #{}", event.getNumeroOrden());
        } catch (Exception ex) {
            log.error("[KAFKA ANALYTICS ERROR] Error procesando analítica para pedido #{}: {}",
                    event.getNumeroOrden(), ex.getMessage(), ex);
        }
    }

    /**
     * Consumidor 2: Auditoría y trazabilidad inmutable.
     */
    @KafkaListener(
            topics = "${app.kafka.topics.pedidos-auditoria.name:pedidos.auditoria.v1}",
            groupId = "${app.kafka.consumer-groups.auditoria:pedidos-auditoria-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumirAuditoria(
            @Payload PedidoAuditoriaEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("[KAFKA AUDIT CONSUMER] Evento recibido. Partition={}, Offset={}, Accion='{}', PedidoId={}",
                partition, offset, event.getAccion(), event.getPedidoId());

        try {
            // Lógica de dominio: registro inmutable de auditoría
            registrarAuditoria(event);
            log.info("[KAFKA AUDIT OK] Auditoría registrada para acción '{}' en pedido #{}",
                    event.getAccion(), event.getNumeroOrden());
        } catch (Exception ex) {
            log.error("[KAFKA AUDIT ERROR] Error registrando auditoría: {}", ex.getMessage(), ex);
        }
    }

    private void procesarMetricasAnalitica(PedidoAnaliticaEvent event) {
        // Simulación de cálculo de agregación y KPIs en streaming
        log.debug("KPI actualizado: Venta registrada por ${} para cliente '{}'", event.getMonto(), event.getCliente());
    }

    private void registrarAuditoria(PedidoAuditoriaEvent event) {
        // Simulación de persistencia en almacén inmutable de logs
        log.debug("AuditLog: ID={} | Acción={} | Usuario={} | Detalle={}",
                event.getIdAuditoria(), event.getAccion(), event.getUsuario(), event.getDetalle());
    }
}
