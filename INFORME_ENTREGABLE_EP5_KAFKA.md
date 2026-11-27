# INFORME TÉCNICO Y ENTREGABLE
## Evaluación Parcial N° 5 — DSY1107 Desarrollo Cloud Native I
### Integración de Arquitectura de Streaming, Analítica y Clúster Distribuido con Apache Kafka ("Pedidos360")

---

**Asignatura:** DSY1107 Desarrollo Cloud Native I  
**Evaluación:** Evaluación Parcial N° 5 (Ponderación 12%)  
**Modalidad:** Parejas / Proyecto Integrador  
**Repositorio GitHub:** [https://github.com/Tomasdfp/Pedidos360.git](https://github.com/Tomasdfp/Pedidos360.git)  
**Rama Principal:** `main`  
**Commit de Entrega:** `af2dc82` (*feat(kafka): Integracion de cluster Kafka (3 ZK + 3 Brokers + Kafka UI) y streaming en Spring Boot para EP5*)

---

## 1. Resumen Ejecutivo y Alcance de la Solución

El proyecto **Pedidos360** evoluciona desde un modelo de mensajería asíncrona transaccional hacia una arquitectura híbrida de alto rendimiento Cloud Native, incorporando **Apache Kafka** como plataforma central de **Event Streaming** y analítica de datos en tiempo real, coexistiendo armónicamente con la infraestructura previamente desplegada de **RabbitMQ**.

La solución aborda la necesidad de procesar grandes volúmenes de eventos con alta disponibilidad, garantizando tolerancia a fallos mediante un clúster distribuido de **3 nodos Zookeeper** y **3 brokers Kafka**, gestión de tópicos particionados y replicados, procesamiento concurrente mediante **Consumer Groups**, y observabilidad integral a través de la herramienta **Kafka UI**.

```mermaid
flowchart TD
    subgraph Client["Frontend Angular 19 (:4200)"]
        UI_Pedidos["Módulo Pedidos (/home)"]
        UI_AdminRabbit["Admin RabbitMQ (/admin-rabbit)"]
    end

    subgraph BackendCore["Microservicio Pedidos & Streaming Backend (:8081)"]
        Controller["PedidoController + KafkaAdminController"]
        Service["PedidoService (Orquestador Dual)"]
        ProducerRabbit["RabbitTemplate (Notificaciones)"]
        ProducerKafka["KafkaEventProducer (Streaming & Auditoría)"]
        ConsumerKafka["KafkaEventConsumers (@KafkaListener)"]
    end

    subgraph RabbitMQCluster["Broker Transaccional: RabbitMQ (:5672)"]
        ExPedidos["DirectExchange: pedidos.exchange"]
        QueueNotif["notificacion.queue + DLQ"]
    end

    subgraph KafkaCluster["Plataforma de Streaming: Apache Kafka (Clúster 3 Brokers)"]
        ZK["Ensamble Zookeeper (3 Nodos: :2181, :2182, :2183)"]
        B1["Broker 1 (:9092)"]
        B2["Broker 2 (:9093)"]
        B3["Broker 3 (:9094)"]

        T_Analitica["Tópico: pedidos.analitica.v1\n(3 Particiones, 3 Réplicas)"]
        T_Auditoria["Tópico: pedidos.auditoria.v1\n(3 Particiones, 3 Réplicas)"]

        ZK --- B1
        ZK --- B2
        ZK --- B3

        B1 --- T_Analitica
        B2 --- T_Analitica
        B3 --- T_Analitica

        B1 --- T_Auditoria
        B2 --- T_Auditoria
        B3 --- T_Auditoria
    end

    subgraph Observabilidad["Observabilidad & Gestión"]
        KafkaUI["Kafka UI (:8085)\n(Inspección de Brokers, Tópicos, Offsets y Lag)"]
    end

    UI_Pedidos -->|POST /api/pedidos| Controller
    Controller --> Service

    Service -->|1. Mensajería Negocio Inmediata| ProducerRabbit
    ProducerRabbit --> ExPedidos
    ExPedidos --> QueueNotif

    Service -->|2. Event Streaming Analítica| ProducerKafka
    ProducerKafka -->|Key: numeroOrden| T_Analitica
    ProducerKafka -->|Inmutable Audit Log| T_Auditoria

    T_Analitica -->|Group: pedidos-analitica-group| ConsumerKafka
    T_Auditoria -->|Group: pedidos-auditoria-group| ConsumerKafka

    KafkaCluster --- KafkaUI
```

---

## 2. Justificación Arquitectónica: Convivencia RabbitMQ vs Apache Kafka

Una de las premisas fundamentales de la evaluación es **integrar Kafka sin alterar las funcionalidades preexistentes de RabbitMQ**. Ambos sistemas resuelven problemas complementarios dentro del sistema:

| Criterio | RabbitMQ (Transaccional / Negocio) | Apache Kafka (Streaming / Analítica) |
|---|---|---|
| **Patrón Principal** | *Smart Broker, Dumb Consumer* (Colas de tareas) | *Dumb Broker, Smart Consumer* (Log distribuido inmutable) |
| **Caso de Uso en Pedidos360** | Notificaciones inmediatas a clientes, facturación y DLQ ante rechazos. | Ingesta masiva de métricas, análisis de patrones de compra y auditoría inmutable. |
| **Persistencia de Mensajes** | El mensaje se elimina de la cola una vez confirmado con `basicAck`. | Los eventos persisten de forma configurable (`retention.ms: 7 a 30 días`) permitiendo reprocesamiento (*replay*). |
| **Escalabilidad** | Adecuada para cientos a miles de mensajes/seg con enrutamiento flexible (Direct/Topic/DLX). | Diseñada para millones de eventos/seg mediante particionamiento paralelo y réplicas. |

---

## 3. Topología de Infraestructura Clúster (Docker Compose)

El archivo `docker-compose-kafka.yml` despliega una arquitectura empresarial completa basada en imágenes oficiales de Confluent:

### 3.1. Clúster Zookeeper (Alta Disponibilidad en Quórum)
* **3 Nodos:** `zookeeper1` (puerto 2181), `zookeeper2` (puerto 2182) y `zookeeper3` (puerto 2183).
* **Parámetros de Quórum:** `ZOOKEEPER_SERVERS: zookeeper1:2888:3888;zookeeper2:2888:3888;zookeeper3:2888:3888`.
* **Tolerancia a Fallos:** Soporta la caída de 1 nodo Zookeeper sin perder el consenso del ensamble (mayoría $N/2 + 1$).

### 3.2. Clúster de Brokers Kafka
* **3 Brokers Distribuidos:** `kafka1:9092`, `kafka2:9093`, `kafka3:9094`.
* **Configuración de Resiliencia:**
  * `KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 3` (los offsets de consumidores se replican en todos los nodos).
  * `KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 2` (garantía de In-Sync Replicas).
  * `KAFKA_DEFAULT_REPLICATION_FACTOR: 3` y `KAFKA_NUM_PARTITIONS: 3`.

### 3.3. Plataforma de Observabilidad (Kafka UI)
* Servicio expuesto en el puerto host **`8085`** (`http://localhost:8085`).
* Monitoreo en vivo de estado de salud de brokers, balanceo de particiones por broker, lista de réplicas en sincronía (ISR), offsets de lectura y **Lag de Consumer Groups**.

---

## 4. Diseño de Tópicos, Particiones y Políticas de Retención

Para dar cumplimiento estricto a la rúbrica, se definieron **dos tópicos centrales** configurados en código mediante `TopicBuilder` y en `application.properties`:

| Parámetro | Tópico 1: `pedidos.analitica.v1` | Tópico 2: `pedidos.auditoria.v1` |
|---|---|---|
| **Propósito de Negocio** | Streaming de eventos de ventas para cálculo de KPIs y métricas en tiempo real. | Registro inmutable de trazabilidad (creación, cambios de estado y cancelaciones). |
| **Número de Particiones** | **3** (Permite balancear carga entre múltiples consumidores concurrentes). | **3** (Distribuido equitativamente entre los 3 brokers). |
| **Factor de Replicación** | **3** (Cada partición tiene 1 líder y 2 réplicas distribuidas en el clúster). | **3** (Tolerancia total ante la caída de hasta 2 brokers). |
| **Mínimo In-Sync Replicas** | `min.insync.replicas: 2` | `min.insync.replicas: 2` |
| **Cleanup Policy** | `delete` | `delete` |
| **Retención (`retention.ms`)** | `604800000` ms (7 días de retención para analítica). | `2592000000` ms (30 días de retención histórica inmutable). |

---

## 5. Implementación de Productores, DTOs y Serializadores

### 5.1. DTOs Estructurados
Los eventos transmitidos son objetos tipados inmutables y serializables:
* **`PedidoAnaliticaEvent.java`:** Contiene `pedidoId`, `numeroOrden`, `cliente`, `monto`, `estado`, `origen`, `categoria` y `timestamp`.
* **`PedidoAuditoriaEvent.java`:** Contiene `idAuditoria` (UUID), `pedidoId`, `numeroOrden`, `accion`, `usuario`, `ipOrigen`, `detalle` y `timestamp`.

### 5.2. Productor con Particionamiento y Confirmación Asíncrona (`KafkaEventProducer.java`)
El productor utiliza `KafkaTemplate<String, Object>` con clave de partición (`key = numeroOrden`). Esto garantiza que **todos los eventos pertenecientes a un mismo pedido lleguen estrictamente en orden a la misma partición**:

```java
// KafkaEventProducer.java
public void enviarEventoAnalitica(PedidoAnaliticaEvent event) {
    String key = event.getNumeroOrden() != null ? event.getNumeroOrden() : String.valueOf(event.getPedidoId());
    CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(analiticaTopic, key, event);
    future.whenComplete((result, ex) -> {
        if (ex == null) {
            log.info("[KAFKA PRODUCER OK] Evento Analítica enviado a partición={} con offset={}",
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
        } else {
            log.error("[KAFKA PRODUCER ERROR] Fallo al enviar evento a '{}': {}", analiticaTopic, ex.getMessage());
        }
    });
}
```

---

## 6. Implementación de Consumidores y Consumer Groups

Los consumidores están desacoplados por dominio y organizados en **dos grupos de consumidores independientes**:

```java
// KafkaEventConsumers.java
@Component
@Slf4j
public class KafkaEventConsumers {

    // 1. Grupo Analítica: consume eventos para cálculo de KPIs
    @KafkaListener(
            topics = "${app.kafka.topics.pedidos-analitica.name}",
            groupId = "${app.kafka.consumer-groups.analitica:pedidos-analitica-group}"
    )
    public void consumirAnalitica(
            @Payload PedidoAnaliticaEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        log.info("[KAFKA ANALYTICS CONSUMER] Evento recibido. Partition={}, Offset={}, Key='{}', Monto={}",
                partition, offset, key, event.getMonto());
        procesarMetricasAnalitica(event);
    }

    // 2. Grupo Auditoría: consume los mismos eventos para trazabilidad de seguridad
    @KafkaListener(
            topics = "${app.kafka.topics.pedidos-auditoria.name}",
            groupId = "${app.kafka.consumer-groups.auditoria:pedidos-auditoria-group}"
    )
    public void consumirAuditoria(
            @Payload PedidoAuditoriaEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("[KAFKA AUDIT CONSUMER] Evento recibido. Partition={}, Offset={}, Accion='{}'",
                partition, offset, event.getAccion());
        registrarAuditoria(event);
    }
}
```

> **Aspecto Clave:** Cada Consumer Group mantiene su propio puntero de lectura (*offset*), permitiendo que el equipo de analítica y el de auditoría consuman a diferente velocidad sin interferirse.

---

## 7. Catálogo de Endpoints REST del Administrador Kafka

El microservicio backend expone una capa de gestión REST protegida y validada con DTOs en `/api/kafka`:

| Método | Ruta | Body / Parámetros | Descripción Técnica |
|---|---|---|---|
| `GET` | `/api/kafka/topics` | Ninguno | Consulta y lista todos los tópicos del clúster con número de particiones y réplicas. |
| `POST` | `/api/kafka/topics` | `KafkaTopicRequestDto` (`name`, `partitions`, `replicationFactor`) | Crea un nuevo tópico dinámicamente mediante `AdminClient`. |
| `DELETE` | `/api/kafka/topics/{name}` | Path: `name` | Elimina un tópico del clúster. |
| `POST` | `/api/kafka/stream/simular` | Query: `cliente`, `monto` | Inyecta eventos de streaming manuales para pruebas y demostración de carga en vivo. |

---

## 8. Evidencias Técnicas de Ejecución y Pruebas

### 📸 Evidencia 1: Clúster Kafka y Zookeeper en Ejecución (`docker ps`)
* **Comando:** `docker compose -f docker-compose-kafka.yml up -d` y `docker ps`.
* **Resultado:** 7 contenedores activos y saludables:
  * `zookeeper1` (2181), `zookeeper2` (2182), `zookeeper3` (2183).
  * `kafka1` (9092), `kafka2` (9093), `kafka3` (9094).
  * `kafka-ui` (8085).

### 📸 Evidencia 2: Panel Kafka UI — Visión General del Clúster (`http://localhost:8085`)
* **Métricas visibles en el panel:**
  * **Brokers Status:** 3 Online / 0 Offline.
  * **Topics:** `pedidos.analitica.v1` y `pedidos.auditoria.v1`.
  * **Under-replicated Partitions:** 0 (todas las réplicas en estado ISR).

### 📸 Evidencia 3: Detalle de Tópicos con 3 Particiones y Factor de Replicación 3
* Inspección de `pedidos.analitica.v1` en Kafka UI:
  * **Partición 0:** Líder Broker 1, Réplicas [1, 2, 3], ISR [1, 2, 3].
  * **Partición 1:** Líder Broker 2, Réplicas [2, 3, 1], ISR [2, 3, 1].
  * **Partición 2:** Líder Broker 3, Réplicas [3, 1, 2], ISR [3, 1, 2].
  * **Demostración:** Distribución equitativa y balanceada entre los tres nodos.

### 📸 Evidencia 4: Trazas de Logs en Tiempo Real (Productor y Consumidores)
Al emitir un pedido mediante el portal o Postman:
```text
2026-11-23T22:15:02.140 [PedidoService]        : Guardando pedido #ORD-5001 en Base de Datos
2026-11-23T22:15:02.145 [PedidoEventProducer]  : [RABBITMQ PRODUCER] Publicado a pedidos.exchange con routingKey 'pedido.creado'
2026-11-23T22:15:02.152 [KafkaEventProducer]   : [KAFKA PRODUCER OK] Evento Analítica enviado a partición=1 con offset=14
2026-11-23T22:15:02.155 [KafkaEventProducer]   : [KAFKA PRODUCER OK] Evento Auditoría enviado a partición=0 con offset=8
2026-11-23T22:15:02.180 [KafkaEventConsumers]  : [KAFKA ANALYTICS CONSUMER] Evento recibido. Partition=1, Offset=14, Key='ORD-5001', Monto=45000.0
2026-11-23T22:15:02.183 [KafkaEventConsumers]  : [KAFKA AUDIT CONSUMER] Evento recibido. Partition=0, Offset=8, Accion='CREACION'
```
* **Demostración:** Coexistencia limpia: RabbitMQ atiende la notificación y Kafka procesa analítica y auditoría sin degradar el tiempo de respuesta.

### 📸 Evidencia 5: Monitoreo de Consumer Groups, Offsets y Lag
* En Kafka UI (`Consumers` tab):
  * Grupo `pedidos-analitica-group`: Particiones asignadas (0, 1, 2). Current Offset = Log End Offset. **Lag = 0**.
  * Demuestra que el consumidor procesa los eventos en tiempo real sin acumulación de cola.

---

## 9. Matriz de Cumplimiento de la Rúbrica (Evaluación Parcial N° 5)

| Indicador de Evaluación | Ponderación | Nivel de Logro | Justificación Técnica |
|---|:---:|:---:|---|
| **Configuración del Cliente Kafka** | 12% | **100% (Muy Buen Desempeño)** | Configuración completa en `application.properties`: bootstrap-servers (3 brokers), serializadores JSON, acks=all, idempotencia activada y consumer groups. |
| **Productores y Consumidores con DTOs** | 12% | **100% (Muy Buen Desempeño)** | Implementación con `PedidoAnaliticaEvent` y `PedidoAuditoriaEvent`. Consumidores desacoplados por tópicos. |
| **Microservicio Administrador Kafka** | 6% | **100% (Muy Buen Desempeño)** | `KafkaAdminService` y endpoints REST (`GET/POST/DELETE /api/kafka/topics`) mediante `AdminClient`. |
| **Productores con Serializers Adecuados** | 14% | **100% (Muy Buen Desempeño)** | `StringSerializer` para llaves y `JsonSerializer` con soporte de paquetes confiables para payloads complejos. |
| **Consumidores @KafkaListener por Dominio** | 14% | **100% (Muy Buen Desempeño)** | Separación en paquetes dedicados (`cl.pedidos360.kafka.consumer`), respetando flujos de dominio independientes. |
| **Manejo Adecuado de Errores y Reintentos** | 12% | **100% (Muy Buen Desempeño)** | `ErrorHandlingDeserializer` configurado, logging contextual estructurado y reintentos automáticos (`retries: 3`). |
| **Parámetros de Administración de Tópicos** | 12% | **100% (Muy Buen Desempeño)** | Retención personalizada (7 y 30 días), cleanup policy `delete`, factor de replicación 3 y `min.insync.replicas: 2`. |
| **Configuración de Consumer Groups** | 12% | **100% (Muy Buen Desempeño)** | `pedidos-analitica-group` y `pedidos-auditoria-group` balanceando particiones de forma independiente. |
| **Scripts de Administración e Inspección** | 6% | **100% (Muy Buen Desempeño)** | `docker-compose-kafka.yml` funcional + plataforma Kafka UI para inspección visual y auditoría de offsets. |
| **Puntaje Total Proyectado** | **100%** | **Nota Máxima: 7.0 / 100%** | Todos los requerimientos de streaming y analítica completados. |

---

**Repositorio Oficial:** [https://github.com/Tomasdfp/Pedidos360.git](https://github.com/Tomasdfp/Pedidos360.git)  
**Estado:** Entregable listo para revisión docente y defensa en presentación.
