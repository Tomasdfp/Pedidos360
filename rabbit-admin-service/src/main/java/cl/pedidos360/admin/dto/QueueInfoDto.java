package cl.pedidos360.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueInfoDto {

    private String name;
    private int messageCount;
    private int consumerCount;
    private boolean exists;
    private String status;
}
