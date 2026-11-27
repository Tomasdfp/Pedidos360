# SLIDE 6: DEMOSTRACIÓN TÉCNICA MEDIANTE CAPTURAS DE EVIDENCIA
## Evaluación Parcial N° 6 — DSY1107 Desarrollo Cloud Native I
### Alternativa sin demostración en vivo (100% basada en capturas de pantalla)

---

## 📸 Las 4 Capturas que debes poner en tu presentación

| # | Título de la Captura | Dónde sacarla (URL / Comando) | Elementos clave que debe mostrar |
|:---:|---|---|---|
| **1** | **Clúster de 3 Brokers en Kafka UI** | `http://localhost:8085` (Dashboard Principal) | 3 Brokers en estado **Online**, 0 Offline, 0 Under-replicated partitions. |
| **2** | **Tópico Particionado y Replicado (3P / 3R)** | `http://localhost:8085/ui/clusters/pedidos360-kafka-cluster/all-topics/pedidos.analitica.v1` | 3 Particiones (0, 1, 2) con líderes en brokers distintos y réplicas ISR [1, 2, 3]. |
| **3** | **Trazabilidad en Logs (Productor y Consumidores)** | Terminal PowerShell de Spring Boot (:8081) | Logs de `[KAFKA PRODUCER OK]`, `[KAFKA ANALYTICS CONSUMER]` y partición asignada. |
| **4** | **Consumer Groups y Métrica de Consumer Lag = 0** | `http://localhost:8085/ui/clusters/pedidos360-kafka-cluster/consumer-groups` | Grupos `pedidos-analitica-group` y `pedidos-auditoria-group` con **Lag en 0**. |

---

## 🛠️ Paso a Paso: Dónde y Cómo obtener cada Captura

### Captura 1: Estado del Clúster (Kafka UI Dashboard)
1. **Paso previo:** Asegúrate de levantar el clúster con:
   ```powershell
   cd c:\Users\Home\Documents\Pedidos360
   docker compose -f docker-compose-kafka.yml up -d
   ```
2. Entra en tu navegador a: **`http://localhost:8085`**
3. En la pantalla principal (**Dashboard / Clusters**), toma la captura.
4. **Lo que debe verse claramente:**
   * Cluster Name: `pedidos360-kafka-cluster`.
   * **Brokers:** `3 Online / 0 Offline`.
   * **Zookeeper status:** Conectado.

---

### Captura 2: Detalle del Tópico con 3 Particiones y 3 Réplicas
1. En Kafka UI (`http://localhost:8085`), ve al menú lateral izquierdo y haz clic en **Topics**.
2. Haz clic sobre el tópico **`pedidos.analitica.v1`** (o `pedidos.auditoria.v1`).
3. Ve a la pestaña **Partitions**. Toma la captura de la tabla.
4. **Lo que debe verse claramente:**
   * Partición `0`, `1` y `2`.
   * Columna **Leader:** Broker 1, Broker 2 y Broker 3 (distribución balanceada).
   * Columna **Replicas:** `[1, 2, 3]` en cada partición.
   * Columna **In-Sync Replicas (ISR):** `[1, 2, 3]` (todas las réplicas saludables).

---

### Captura 3: Publicación y Consumo en Tiempo Real (Logs en Terminal)
1. Con tu backend de Spring Boot corriendo (`.\mvnw spring-boot:run` en `:8081`), crea un pedido o simula un evento con este comando en PowerShell:
   ```powershell
   Invoke-RestMethod -Uri "http://localhost:8081/api/kafka/stream/simular?cliente=Tomas&monto=45000" -Method POST
   ```
2. En la ventana de la terminal de Spring Boot verás las líneas de log del productor y del consumidor.
3. Toma una captura de esas líneas en la consola negra/azul.
4. **Lo que debe verse claramente:**
   ```text
   [KAFKA PRODUCER OK] Evento Analítica enviado a partición=1 con offset=...
   [KAFKA ANALYTICS CONSUMER] Evento recibido. Partition=1, Key='STREAM-...', Monto=45000.0
   [KAFKA ANALYTICS OK] Métricas actualizadas para pedido #...
   ```

---

### Captura 4: Monitoreo de Consumer Groups y Lag Cero
1. En Kafka UI (`http://localhost:8085`), haz clic en el menú lateral en **Consumers** (o **Consumer Groups**).
2. Toma una captura de la tabla donde aparecen los grupos.
3. **Lo que debe verse claramente:**
   * Consumer Group: `pedidos-analitica-group` y `pedidos-auditoria-group`.
   * Num of Members: 1 o más consumidores activos.
   * Columna **Consumer Lag:** **`0`** (evidencia de que no hay cuello de botella).

---

## 🎙️ Guion Verbal para exponer esta Slide con las Capturas (2 minutos)

> *(Muestras la diapositiva con las 4 capturas organizadas en un cuadrante o secuenciales)*

### Al señalar la Captura 1 (Dashboard Kafka UI):
> *"A continuación presentamos las evidencias de operación de nuestro clúster Kafka. En la primera captura observamos el dashboard de Kafka UI: los **tres brokers del clúster se encuentran en estado Online** y sin particiones degradadas, garantizando alta disponibilidad a través del ensamble de tres nodos Zookeeper."*

### Al señalar la Captura 2 (Tópico con 3 Particiones y 3 Réplicas):
> *"En la segunda captura inspeccionamos la topología del tópico `pedidos.analitica.v1`. Demostramos la configuración exigida en la pauta: **tres particiones distribuidas equitativamente**, donde cada broker asume el liderazgo de una partición y mantiene tres réplicas en sincronía (ISR). Esto permite balancear la carga de escritura y soportar la pérdida de un broker sin interrupción del servicio."*

### Al señalar la Captura 3 (Logs de Producción y Consumo):
> *"En la tercera captura evidenciamos el procesamiento en tiempo real en los logs de Spring Boot. Cuando se genera un evento, el productor utiliza la clave de partición `numeroOrden` para preservar el orden secuencial. Vemos cómo el listener recibe el evento inmediatamente en la partición asignada y actualiza las métricas del dominio sin interferir con las notificaciones de RabbitMQ."*

### Al señalar la Captura 4 (Consumer Groups y Lag = 0):
> *"Finalmente, en la cuarta captura supervisamos la salud operativa de nuestros dos Consumer Groups: `pedidos-analitica-group` y `pedidos-auditoria-group`. Cada grupo mantiene sus punteros de offset de forma independiente. Lo más relevante es que el **Consumer Lag se encuentra en cero**, lo que prueba que el sistema procesa el flujo en tiempo real sin acumulación de mensajes ni cuellos de botella."*

---

### 💡 Ventajas de este formato ante el evaluador:
* **Cero riesgo técnico:** No dependes de que se caiga el wifi, Docker o se cierre una ventana en plena presentación.
* **100% de cumplimiento de la pauta:** Cada captura responde exactamente a lo que pide la pauta de evaluación de la EP6 (Brokers, 3 particiones, 3 réplicas, logs de streaming y métricas de lag).
* **Control total del tiempo:** Te toma exactamente **2 minutos** explicar las 4 capturas con fluidez y precisión técnica.
