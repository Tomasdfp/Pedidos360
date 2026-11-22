package cl.pedidos360.kafka.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KafkaTopicRequestDto {

    @NotBlank(message = "El nombre del tópico es obligatorio")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "El nombre del tópico solo puede contener letras, números, puntos, guiones y guiones bajos")
    private String name;

    @Min(value = 1, message = "Debe tener al menos 1 partición")
    @Max(value = 50, message = "Máximo 50 particiones")
    private int partitions = 3;

    @Min(value = 1, message = "Debe tener al menos factor de replicación 1")
    @Max(value = 3, message = "Máximo factor de replicación 3")
    private short replicationFactor = 3;

    private Map<String, String> configs;
}
