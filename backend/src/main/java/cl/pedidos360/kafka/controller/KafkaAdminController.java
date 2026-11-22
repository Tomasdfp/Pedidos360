package cl.pedidos360.kafka.controller;

import cl.pedidos360.kafka.dto.KafkaTopicRequestDto;
import cl.pedidos360.kafka.dto.KafkaTopicSummaryDto;
import cl.pedidos360.kafka.event.PedidoAnaliticaEvent;
import cl.pedidos360.kafka.producer.KafkaEventProducer;
import cl.pedidos360.kafka.service.KafkaAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión de Kafka y simulación de streaming analítico.
 */
@RestController
@RequestMapping("/api/kafka")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
public class KafkaAdminController {

    private final KafkaAdminService kafkaAdminService;
    private final KafkaEventProducer kafkaEventProducer;

    public KafkaAdminController(KafkaAdminService kafkaAdminService, KafkaEventProducer kafkaEventProducer) {
        this.kafkaAdminService = kafkaAdminService;
        this.kafkaEventProducer = kafkaEventProducer;
    }

    @GetMapping("/topics")
    public ResponseEntity<List<KafkaTopicSummaryDto>> listTopics() {
        return ResponseEntity.ok(kafkaAdminService.listTopics());
    }

    @PostMapping("/topics")
    public ResponseEntity<Map<String, Object>> createTopic(@Valid @RequestBody KafkaTopicRequestDto dto) {
        boolean created = kafkaAdminService.createTopic(dto);
        Map<String, Object> resp = new HashMap<>();
        if (created) {
            resp.put("success", true);
            resp.put("message", "Tópico '" + dto.getName() + "' creado exitosamente en el clúster Kafka");
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } else {
            resp.put("success", false);
            resp.put("message", "No se pudo crear el tópico '" + dto.getName() + "'. Verifique el clúster o si ya existe.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        }
    }

    @DeleteMapping("/topics/{name}")
    public ResponseEntity<Map<String, Object>> deleteTopic(@PathVariable String name) {
        boolean deleted = kafkaAdminService.deleteTopic(name);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", deleted);
        resp.put("message", deleted ? "Tópico eliminado" : "Error al eliminar tópico");
        return ResponseEntity.ok(resp);
    }

    /**
     * Endpoint para simulación y pruebas de streaming: publica un evento manual a Kafka.
     */
    @PostMapping("/stream/simular")
    public ResponseEntity<Map<String, Object>> simularEventoAnalitica(
            @RequestParam(defaultValue = "Tomas") String cliente,
            @RequestParam(defaultValue = "55000.0") Double monto) {

        PedidoAnaliticaEvent event = PedidoAnaliticaEvent.builder()
                .pedidoId(System.currentTimeMillis())
                .numeroOrden("STREAM-" + (int)(Math.random() * 9000 + 1000))
                .cliente(cliente)
                .monto(monto)
                .estado("STREAMING_INGESTED")
                .origen("API_SIMULATOR")
                .categoria("ELECTRONICA")
                .timestamp(LocalDateTime.now())
                .build();

        kafkaEventProducer.enviarEventoAnalitica(event);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "Evento de analítica emitido hacia Apache Kafka");
        resp.put("data", event);
        return ResponseEntity.ok(resp);
    }
}
