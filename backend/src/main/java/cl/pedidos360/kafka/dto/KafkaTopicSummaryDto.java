package cl.pedidos360.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KafkaTopicSummaryDto {
    private String name;
    private int partitions;
    private int replicationFactor;
    private boolean internal;
    private List<Integer> partitionIds;
    private Map<String, String> configs;
}
