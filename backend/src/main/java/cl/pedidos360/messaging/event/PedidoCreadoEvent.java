package cl.pedidos360.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Evento de dominio emitido cuando un pedido es creado exitosamente en el sistema.
 * Se transmite a través de RabbitMQ hacia los consumidores interesados (ej. Notificaciones).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoCreadoEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String numeroOrden;
    private String cliente;
    private String descripcion;
    private Double monto;
    private String estado;
    private LocalDateTime fechaCreacion;

    /**
     * Metadatos del evento
     */
    private String eventId;
    private String tipoEvento;
    private LocalDateTime timestamp;
}
