# Pedidos360 🚀

Sistema de gestión de pedidos con arquitectura BFF (Backend-For-Frontend) y mensajería asíncrona desacoplada con **RabbitMQ**, integrado con **Microsoft Entra External ID (CIAM)**.

---

## 📁 Estructura del Proyecto

```
Pedidos360/
├── frontend/                 # SPA Angular 18 (MSAL Angular + Entra External ID)
│   ├── src/app/pages/home/   # Vista de Pedidos (Persona A)
│   └── src/app/pages/rabbit-admin/ # Vista de Administración RabbitMQ (Persona B)
├── backend/                  # Microservicio 1: Pedidos + Notificaciones (Spring Boot 3.3.5 en puerto 8081)
│   ├── src/main/java/cl/pedidos360/
│   │   ├── config/           # RabbitMQConfig, SecurityConfig, CorsConfig
│   │   ├── controller/       # PedidoController
│   │   ├── messaging/        # PedidoEventProducer, NotificacionConsumer (ACK/DLQ)
│   │   └── service/          # PedidoService
├── rabbit-admin-service/     # Microservicio 2: Administrador RabbitMQ (Spring Boot 3.3.5 en puerto 8082)
│   ├── src/main/java/cl/pedidos360/admin/
│   │   ├── config/           # RabbitAdminConfig (Beans base RabbitAdmin y Tickets)
│   │   ├── controller/       # RabbitAdminController (REST POST/DELETE queues, exchanges, bindings)
│   │   ├── dto/              # QueueRequestDto, ExchangeRequestDto, TicketEvent (Validaciones)
│   │   ├── consumer/         # TicketConsumer (ACK manual / NACK a ticket.dlq)
│   │   └── service/          # RabbitAdminService (Lógica de administración encapsulada)
├── docker-compose.yml        # Infraestructura local de RabbitMQ con Management UI
└── README.md
```

---

## 🛠️ Tecnologías Utilizadas

- **Frontend:** Angular 18, MSAL Angular / Browser, RxJS, Tailwind/CSS.
- **Backend:** Spring Boot 3.3.5, Java 21, Spring AMQP / RabbitMQ, Spring Security OAuth2 Resource Server, Spring Data JPA, H2 Database, Lombok.
- **Mensajería Asíncrona:** RabbitMQ 3 (Exchanges, Queues, Routing Keys, DLX / DLQ, ACK Manual).
- **Identidad y Autenticación:** Microsoft Entra External ID (CIAM).

---

## 📨 Arquitectura de Mensajería (RabbitMQ)

### 1. Configuración Centralizada (`application.properties` y `RabbitMQConfig.java`)
- **Exchange Principal:** `pedidos.exchange` (Direct Exchange).
- **Dead Letter Exchange (DLX):** `pedidos.dlx` (Direct Exchange).
- **Colas Principales:**
  - `pedidos.queue`: Enrutada mediante `pedido.creado`. Argumentos DLQ configurados:
    - `x-dead-letter-exchange: pedidos.dlx`
    - `x-dead-letter-routing-key: pedidos.dead-letter`
  - `notificacion.queue`: Enrutada mediante `pedido.creado`. Argumentos DLQ configurados:
    - `x-dead-letter-exchange: pedidos.dlx`
    - `x-dead-letter-routing-key: notificacion.dead-letter`
- **Colas de Mensajes No Entregados (DLQ):**
  - `pedidos.dlq`: Vinculada a `pedidos.dlx` con routing key `pedidos.dead-letter`.
  - `notificacion.dlq`: Vinculada a `pedidos.dlx` con routing key `notificacion.dead-letter`.

### 2. Flujo de Trabajo Asíncrono
1. **Creación de Pedido:** Al invocar `POST /api/pedidos`, el servicio persiste el pedido en la BD H2 y delega en `PedidoEventProducer` la publicación del evento `PedidoCreadoEvent`.
2. **Productor:** Publica el evento JSON en `pedidos.exchange` con la routing key `pedido.creado`.
3. **Consumidor (`NotificacionConsumer`):** Escucha `notificacion.queue` mediante `@RabbitListener`.
   - Procesa la notificación simulando alerta al cliente.
   - Si el proceso es exitoso, emite **ACK manual** (`channel.basicAck(deliveryTag, false)`).
   - Si ocurre una excepción o error de negocio, registra el fallo en logs y emite **NACK manual sin requeue** (`channel.basicNack(deliveryTag, false, false)`).
4. **Manejo de Errores y DLQ:** Al rechazar el mensaje sin requeue, RabbitMQ lo desvía automáticamente a `notificacion.dlq`. El componente `NotificacionDlqConsumer` audita y registra el evento fallido en logs.

---

## 🚀 Instrucciones de Ejecución

### 1. Iniciar RabbitMQ (Docker)
```bash
docker compose up -d
```
- **AMQP Broker:** `localhost:5672`
- **Management Console:** `http://localhost:15672` (Usuario: `guest` / Contraseña: `guest`)

### 2. Iniciar Microservicio de Pedidos (Backend 1)
```bash
cd backend
./mvnw spring-boot:run
```
- **Puerto:** `8081`
- **Consola H2:** `http://localhost:8081/h2-console`
- **Endpoints REST:** `http://localhost:8081/api/pedidos`

### 3. Iniciar Microservicio Administrador de RabbitMQ (Backend 2)
```bash
cd rabbit-admin-service
./mvnw spring-boot:run
```
- **Puerto:** `8082`
- **Endpoints REST:**
  - `POST /queues`: Crear cola (validado con DTO)
  - `DELETE /queues/{name}`: Eliminar cola
  - `GET /queues/{name}`: Consultar estado y conteo de mensajes
  - `POST /exchanges`: Crear exchange
  - `DELETE /exchanges/{name}`: Eliminar exchange
  - `POST /bindings`: Crear binding
  - `POST /tickets`: Publicar ticket de prueba

### 4. Iniciar Frontend (Angular)
```bash
cd frontend
npm install
npm start
```
- **Puerto:** `4200` (`http://localhost:4200`)
- **Vistas:**
  - `http://localhost:4200/`: Vista de Pedidos (Persona A)
  - `http://localhost:4200/admin-rabbit`: Administración RabbitMQ (Persona B)

---

## 🧪 Ejecutar Pruebas Automatizadas

### Pruebas Backend Pedidos
```bash
cd backend
./mvnw test
```
Resultados: **11/11 pruebas exitosas** (Seguridad, Productor, Consumidor ACK y DLQ).

### Pruebas Backend Rabbit Admin
```bash
cd rabbit-admin-service
./mvnw test
```
Resultados: **13/13 pruebas exitosas** (RabbitAdminService, Validación de DTOs, Endpoints REST, Consumidor Tickets ACK y DLQ).


