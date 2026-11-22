package cl.pedidos360.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoAuditoriaEvent implements Serializable {
    private String idAuditoria;
    private Long pedidoId;
    private String numeroOrden;
    private String accion;       // CREACION, MODIFICACION, CANCELACION
    private String usuario;
    private String ipOrigen;
    private String detalle;
    private LocalDateTime timestamp;
}
