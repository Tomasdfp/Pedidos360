package cl.pedidos360.kafka.service;

import cl.pedidos360.kafka.dto.KafkaTopicRequestDto;
import cl.pedidos360.kafka.dto.KafkaTopicSummaryDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.TopicPartitionInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Servicio administrativo para la gestión de Apache Kafka mediante AdminClient.
 * Permite crear, listar, inspeccionar y eliminar tópicos en caliente.
 */
@Service
@Slf4j
public class KafkaAdminService {

    private final AdminClient adminClient;

    public KafkaAdminService(@Value("${spring.kafka.bootstrap-servers:localhost:9092,localhost:9093,localhost:9094}") String bootstrapServers) {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000");
        this.adminClient = AdminClient.create(props);
    }

    /**
     * Lista todos los tópicos del clúster con su metadata de particiones y réplicas.
     */
    public List<KafkaTopicSummaryDto> listTopics() {
        try {
            ListTopicsResult listTopicsResult = adminClient.listTopics(new ListTopicsOptions().listInternal(false));
            Set<String> topicNames = listTopicsResult.names().get(5, TimeUnit.SECONDS);

            DescribeTopicsResult describeResult = adminClient.describeTopics(topicNames);
            Map<String, TopicDescription> descriptions = describeResult.allTopicNames().get(5, TimeUnit.SECONDS);

            return descriptions.values().stream().map(desc -> {
                int partitions = desc.partitions().size();
                int repFactor = desc.partitions().isEmpty() ? 0 : desc.partitions().get(0).replicas().size();
                List<Integer> pIds = desc.partitions().stream().map(TopicPartitionInfo::partition).collect(Collectors.toList());

                return KafkaTopicSummaryDto.builder()
                        .name(desc.name())
                        .partitions(partitions)
                        .replicationFactor(repFactor)
                        .internal(desc.isInternal())
                        .partitionIds(pIds)
                        .build();
            }).collect(Collectors.toList());

        } catch (Exception ex) {
            log.error("[KAFKA ADMIN] Error al listar tópicos: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Crea un nuevo tópico en el clúster con particiones y réplicas configuradas.
     */
    public boolean createTopic(KafkaTopicRequestDto dto) {
        try {
            NewTopic newTopic = new NewTopic(dto.getName(), dto.getPartitions(), dto.getReplicationFactor());
            if (dto.getConfigs() != null && !dto.getConfigs().isEmpty()) {
                newTopic.configs(dto.getConfigs());
            }

            CreateTopicsResult result = adminClient.createTopics(Collections.singleton(newTopic));
            result.all().get(5, TimeUnit.SECONDS);
            log.info("[KAFKA ADMIN] Tópico '{}' creado con {} particiones y {} réplicas",
                    dto.getName(), dto.getPartitions(), dto.getReplicationFactor());
            return true;
        } catch (Exception ex) {
            log.error("[KAFKA ADMIN] Error al crear tópico '{}': {}", dto.getName(), ex.getMessage());
            return false;
        }
    }

    /**
     * Elimina un tópico del clúster.
     */
    public boolean deleteTopic(String topicName) {
        try {
            DeleteTopicsResult result = adminClient.deleteTopics(Collections.singleton(topicName));
            result.all().get(5, TimeUnit.SECONDS);
            log.info("[KAFKA ADMIN] Tópico '{}' eliminado exitosamente", topicName);
            return true;
        } catch (Exception ex) {
            log.error("[KAFKA ADMIN] Error al eliminar tópico '{}': {}", topicName, ex.getMessage());
            return false;
        }
    }
}
