package cl.pedidos360.admin.dto;

import jakarta.validation.constraints.NotBlank;
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
public class TicketEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "El título del ticket es obligatorio")
    private String titulo;

    @NotBlank(message = "La descripción del ticket es obligatoria")
    private String descripcion;

    @Builder.Default
    private String prioridad = "MEDIA";

    private String solicitante;

    @Builder.Default
    private String estado = "PENDIENTE";

    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    private boolean debeFallar;
}
