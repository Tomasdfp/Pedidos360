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
public class PedidoAnaliticaEvent implements Serializable {
    private Long pedidoId;
    private String numeroOrden;
    private String cliente;
    private Double monto;
    private String estado;
    private String origen;
    private String categoria;
    private LocalDateTime timestamp;
}
