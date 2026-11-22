package cl.pedidos360.kafka.producer;

import cl.pedidos360.kafka.event.PedidoAnaliticaEvent;
import cl.pedidos360.kafka.event.PedidoAuditoriaEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Productor de eventos de streaming para Apache Kafka.
 * Publica eventos estructurados a los tópicos de Analítica y Auditoría
 * utilizando KafkaTemplate y confirmaciones asíncronas de entrega.
 */
@Component
@Slf4j
public class KafkaEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.pedidos-analitica.name:pedidos.analitica.v1}")
    private String analiticaTopic;

    @Value("${app.kafka.topics.pedidos-auditoria.name:pedidos.auditoria.v1}")
    private String auditoriaTopic;

    public KafkaEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publica evento de analítica con clave de particionamiento (numeroOrden)
     * para asegurar que los eventos de un mismo pedido vayan a la misma partición.
     */
    public void enviarEventoAnalitica(PedidoAnaliticaEvent event) {
        String key = event.getNumeroOrden() != null ? event.getNumeroOrden() : String.valueOf(event.getPedidoId());
        log.info("[KAFKA PRODUCER] Publicando evento en tópico '{}' con key='{}': {}", analiticaTopic, key, event);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(analiticaTopic, key, event);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("[KAFKA PRODUCER OK] Evento Analítica enviado a partición={} con offset={}",
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("[KAFKA PRODUCER ERROR] Fallo al enviar evento a '{}': {}", analiticaTopic, ex.getMessage(), ex);
            }
        });
    }

    /**
     * Publica evento de auditoría en el tópico dedicado de auditoría.
     */
    public void enviarEventoAuditoria(PedidoAuditoriaEvent event) {
        String key = event.getNumeroOrden() != null ? event.getNumeroOrden() : String.valueOf(event.getPedidoId());
        log.info("[KAFKA PRODUCER] Publicando evento en tópico '{}' con key='{}': accion={}", auditoriaTopic, key, event.getAccion());

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(auditoriaTopic, key, event);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("[KAFKA PRODUCER OK] Evento Auditoría enviado a partición={} con offset={}",
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("[KAFKA PRODUCER ERROR] Fallo al enviar auditoría a '{}': {}", auditoriaTopic, ex.getMessage(), ex);
            }
        });
    }
}
