# GUION Y ESTRUCTURA DE PRESENTACIÓN (10 MINUTOS)
## Evaluación Parcial N° 6 — DSY1107 Desarrollo Cloud Native I
### Defensa Técnica: Arquitectura Cloud Native Híbrida (IDaaS + RabbitMQ + Clúster Apache Kafka + Streaming)

---

## ⏱️ Distribución Estratégica del Tiempo (Total: 9:30 a 10:00 min)

| Bloque | Tiempo | Tema Central | Soporte Visual |
|---|:---:|---|---|
| **Bloque 1** | **0:00 - 1:30** | Introducción, Arquitectura Global y Convivencia RabbitMQ vs Kafka | Slide 1 y 2 (Diagrama de Arquitectura) |
| **Bloque 2** | **1:30 - 3:00** | Seguridad IDaaS (Entra ID), JWT y API Management | Slide 3 + Login en Vivo |
| **Bloque 3** | **3:00 - 4:45** | Clúster RabbitMQ, Enrutamiento Direct/Topic y Tolerancia a Fallos (DLQ) | Slide 4 + Panel RabbitMQ Management |
| **Bloque 4** | **4:45 - 7:15** | Clúster Apache Kafka (3 ZK + 3 Brokers), Tópicos (3P/3R) y Streaming | Slide 5 + Kafka UI en Vivo |
| **Bloque 5** | **7:15 - 8:45** | Demostración Funcional End-to-End: Creación de Pedido y Métricas (Offsets/Lag) | Live Demo Navegador + Terminal |
| **Bloque 6** | **8:45 - 9:45** | Conclusiones, Justificación de Diseño y Cierre | Slide 6 |
| **Cierre** | **9:45 - 10:00** | Paso a Preguntas de la Comisión | - |

---

## 📋 Estructura Diapositiva por Diapositiva y Guion Verbal

---

### 🖥️ Slide 1: Portada y Presentación del Proyecto (0:00 - 0:40)
* **Elementos en pantalla:** Título del Proyecto (*Pedidos360: Plataforma Cloud Native de Gestión de Pedidos y Event Streaming*), Nombres de los integrantes, Asignatura DSY1107.
* **Guion Verbal:**
> *"Buenos días profesor y comisión. Hoy presentamos la evolución de **Pedidos360**, una solución Cloud Native empresarial que integra seguridad federada, microservicios en Spring Boot 3 y Angular, y una arquitectura híbrida de mensajería asíncrona. El objetivo de este proyecto es demostrar cómo resolver dos problemas críticos en sistemas distribuidos: la comunicación transaccional confiable y la ingesta masiva de eventos para analítica en tiempo real, garantizando alta disponibilidad y tolerancia a fallos."*

---

### 🖥️ Slide 2: Arquitectura Híbrida: RabbitMQ vs Apache Kafka (0:40 - 1:30)
* **Elementos en pantalla:** Diagrama que muestra el Frontend Angular, Gateway/API Manager, Backend de Pedidos, el Broker RabbitMQ y el Clúster de Kafka.
* **Guion Verbal:**
> *"Uno de los desafíos principales fue incorporar Apache Kafka sin alterar la lógica de negocio preexistente con RabbitMQ. Adoptamos un enfoque donde cada tecnología cumple su rol natural:*
> - ***RabbitMQ*** *actúa como nuestro broker transaccional: maneja colas de tareas, notificaciones al cliente y contingencias mediante colas de mensajes muertos (DLQ).*
> - ***Apache Kafka*** *actúa como nuestra plataforma de streaming distribuido: opera como un log inmutable de eventos de alto rendimiento, orientado a la analítica de ventas y auditoría en tiempo real.*
> *Esta división desacopla el flujo crítico del cliente de las tareas de procesamiento analítico."*

---

### 🖥️ Slide 3: Seguridad Perimetral: IDaaS, OAuth 2.0 y Validación JWT (1:30 - 3:00)
* **Elementos en pantalla:** Flujo OIDC con Microsoft Entra ID (Azure), Tenant, Scopes (`pedidos.read`, `pedidos.write`), captura de rechazo `401 Unauthorized` vs éxito `201 Created`.
* **Guion Verbal:**
> *"En el ámbito de la seguridad, la solución implementa autenticación federada mediante un proveedor de identidad en la nube (IDaaS), en nuestro caso Microsoft Entra ID. 
> El frontend en Angular utiliza la biblioteca MSAL con flujo Authorization Code con PKCE. Cuando el usuario inicia sesión, obtiene un Access Token JWT firmado criptográficamente. 
> Nuestro backend actúa como un OAuth2 Resource Server: valida el emisor (*issuer*), la firma contra el `jwk-set-uri` del IDaaS y los scopes requeridos mediante `@PreAuthorize`. Cualquier petición sin token o con token manipulado es rechazada de inmediato con un HTTP 401, protegiendo las capas internas del negocio."*

---

### 🖥️ Slide 4: Infraestructura RabbitMQ: Enrutamiento y Políticas DLQ (3:00 - 4:45)
* **Elementos en pantalla:** Captura o panel web de RabbitMQ mostrando los 2 nodos en clúster, los exchanges `pedidos.exchange` y `pedidos.dlx`, y las colas vinculadas.
* **Guion Verbal:**
> *"Para la mensajería transaccional, contamos con un clúster de RabbitMQ con exchanges de tipo `direct` y `topic`. Configuramos confirmación manual de mensajes (`AcknowledgeMode.MANUAL`). 
> Cuando se procesa una notificación con éxito, el consumidor emite un `basicAck`. Sin embargo, si ocurre una falla técnica o de negocio, emitimos un `basicNack` con reencolamiento en falso (`requeue=false`). 
> Esto activa los argumentos de Dead Letter Exchange (`x-dead-letter-exchange`), desviando el mensaje fallido a la cola `notificacion.dlq` o `ticket.dlq`, donde un consumidor de contingencia audita el incidente sin bloquear la cola de producción."*

---

### 🖥️ Slide 5: Clúster Apache Kafka (3 ZK + 3 Brokers) y Tópicos (4:45 - 6:00)
* **Elementos en pantalla:** Diagrama de los 3 nodos Zookeeper y 3 Brokers Kafka desplegados en Docker Compose, tabla de configuración de los 2 tópicos.
* **Guion Verbal:**
> *"Pasando al núcleo de streaming, desplegamos mediante Docker Compose un clúster de alta disponibilidad compuesto por **tres nodos Zookeeper** en quórum y **tres brokers Kafka**.
> En el clúster definimos centralizadamente dos tópicos estratégicos:
> 1. `pedidos.analitica.v1`: con **3 particiones y factor de replicación 3**, con retención de 7 días para análisis de demanda y KPIs.
> 2. `pedidos.auditoria.v1`: también con **3 particiones y 3 réplicas**, con retención de 30 días para trazabilidad inmutable de cambios de estado.
> Al tener 3 particiones y 3 réplicas con `min.insync.replicas: 2`, el clúster tolera la pérdida de un broker sin degradación del servicio ni pérdida de eventos."*

---

### 🖥️ Slide 6: Demostración en Vivo: Streaming, Kafka UI y Métricas (6:00 - 8:45) ⭐ PARTE CLAVE
*(En esta sección cambias la pantalla al navegador: Kafka UI y consola)*
* **Demostración en vivo:**
1. **Mostrar Kafka UI (`http://localhost:8085`):**
   > *"Aquí observamos Kafka UI conectado a nuestro clúster: los 3 brokers están en estado Online y sin particiones sub-replicadas. Si abrimos el tópico `pedidos.analitica.v1`, vemos las 3 particiones balanceadas equitativamente entre los brokers 1, 2 y 3."*
2. **Generar un Pedido / Evento de Streaming:**
   > *"Emitimos un nuevo pedido desde el sistema. Inmediatamente vemos en los logs del microservicio cómo se publica el evento con clave de partición `numeroOrden` para preservar el orden secuencial. 
   > Al mismo tiempo, el evento viaja a RabbitMQ para la notificación síncrona y a Kafka para analítica."*
3. **Mostrar Consumer Groups, Offsets y Lag:**
   > *"En la pestaña de **Consumer Groups** de Kafka UI vemos a `pedidos-analitica-group` y `pedidos-auditoria-group`. Cada grupo mantiene su propio offset de lectura independiente. Podemos observar que el **Consumer Lag es cero**, lo que demuestra que los consumidores procesan el flujo en tiempo real sin acumulación ni cuellos de botella."*

---

### 🖥️ Slide 7: Conclusiones y Cierre (8:45 - 9:45)
* **Elementos en pantalla:** Puntos clave: Alta Disponibilidad, Desacoplamiento Híbrido, Tolerancia a Fallos y Observabilidad.
* **Guion Verbal:**
> *"En conclusión, esta solución demuestra la aplicación práctica de patrones Cloud Native avanzados:
> 1. **Coexistencia eficiente:** RabbitMQ resuelve la mensajería transaccional con control estricto de fallas mediante DLQ, mientras Kafka resuelve el streaming masivo.
> 2. **Tolerancia a fallos:** El clúster de 3 brokers garantiza continuidad operativa frente a caídas de nodos.
> 3. **Observabilidad:** La combinación de métricas de offset y lag en Kafka UI permite anticipar problemas de capacidad antes de que afecten al usuario final.
> Muchas gracias por su atención, quedamos atentos a sus preguntas."*

---

## 🛡️ Banco de Preguntas Típicas de la Comisión (y cómo responderlas)

### ❓ Pregunta 1: "¿Por qué utilizaron 3 particiones y factor de replicación 3 en Kafka?"
> **Respuesta sugerida:**  
> *"Elegimos 3 particiones porque tenemos 3 brokers en el clúster. Esto permite que cada broker sea líder de una partición, distribuyendo la carga de escritura y lectura equitativamente entre todos los nodos de cómputo y permitiendo que hasta 3 instancias de consumidores trabajen en paralelo.  
> El factor de replicación 3 garantiza que cada mensaje exista en los 3 brokers. Combinado con `min.insync.replicas: 2` y `acks=all`, el sistema asegura que aunque un broker falle por completo, el quórum de réplicas en sincronía mantiene el servicio disponible sin pérdida de datos."*

### ❓ Pregunta 2: "¿Cuál es la diferencia entre el Consumer Group de analítica y el de auditoría?"
> **Respuesta sugerida:**  
> *"En Kafka, los Consumer Groups permiten implementar el patrón Publish-Subscribe de forma desacoplada. `pedidos-analitica-group` y `pedidos-auditoria-group` leen del mismo flujo pero tienen diferentes identificadores de grupo. Esto significa que cada uno mantiene su propio puntero (*offset*) de avance de lectura de manera independiente. Si el servicio de analítica necesita reprocesar datos históricos o se detiene por mantenimiento, el servicio de auditoría sigue consumiendo en tiempo real sin verse afectado."*

### ❓ Pregunta 3: "¿Qué es el 'Consumer Lag' y por qué es una métrica crítica?"
> **Respuesta sugerida:**  
> *"El Consumer Lag es la diferencia aritmética entre el último offset escrito por los productores en una partición (*Log End Offset*) y el último offset confirmado por el grupo de consumidores (*Current Offset*).  
> Representa cuántos mensajes están pendientes de ser procesados. Un lag creciente es un indicador temprano de que los consumidores están saturados o que la lógica de negocio tiene cuellos de botella, lo que permite tomar decisiones operativas como escalar horizontalmente más consumidores o aumentar particiones."*

### ❓ Pregunta 4: "¿Por qué no usaron solo RabbitMQ o solo Kafka para todo?"
> **Respuesta sugerida:**  
> *"Porque responden a paradigmas distintos. RabbitMQ es un gestor de colas transaccional (*Queue broker*) excelente para ruteo complejo y entrega garantizada con eliminación posterior del mensaje; pero no está optimizado para retención histórica de petabytes ni replay de eventos.  
> Kafka, por otro lado, es un registro inmutable append-only (*Log streaming*) optimizado para alta tasa de transferencia y reprocesamiento analítico, pero carece de enrutamiento por headers dinámicos o ACK granular mensaje por mensaje como RabbitMQ. Usar ambos entrega lo mejor de cada mundo sin sobrecargar ninguna plataforma."*
