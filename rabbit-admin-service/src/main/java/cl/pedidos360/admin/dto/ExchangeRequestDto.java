package cl.pedidos360.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRequestDto {

    @NotBlank(message = "El nombre del exchange no puede estar vacío")
    @Size(min = 3, max = 255, message = "El nombre del exchange debe tener entre 3 y 255 caracteres")
    @Pattern(
        regexp = "^[a-zA-Z0-9._-]+$",
        message = "El nombre del exchange solo puede contener letras, números, puntos (.), guiones (-) y guiones bajos (_)"
    )
    private String name;

    @NotBlank(message = "El tipo de exchange es obligatorio (DIRECT, TOPIC, FANOUT, HEADERS)")
    @Pattern(
        regexp = "^(?i)(DIRECT|TOPIC|FANOUT|HEADERS)$",
        message = "El tipo de exchange debe ser DIRECT, TOPIC, FANOUT o HEADERS"
    )
    private String type;

    @Builder.Default
    private boolean durable = true;

    @Builder.Default
    private boolean autoDelete = false;
}
