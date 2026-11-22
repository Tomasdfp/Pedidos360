package cl.pedidos360.kafka.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración centralizada de tópicos de Apache Kafka para Analítica y Auditoría.
 * Declara 2 tópicos con 3 particiones y 3 réplicas cada uno, cumpliendo con
 * la arquitectura de alta disponibilidad sobre el clúster de 3 brokers.
 */
@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092,localhost:9093,localhost:9094}")
    private String bootstrapServers;

    @Value("${app.kafka.topics.pedidos-analitica.name:pedidos.analitica.v1}")
    private String analiticaTopicName;

    @Value("${app.kafka.topics.pedidos-analitica.partitions:3}")
    private int analiticaPartitions;

    @Value("${app.kafka.topics.pedidos-analitica.replicas:3}")
    private int analiticaReplicas;

    @Value("${app.kafka.topics.pedidos-analitica.retention-ms:604800000}")
    private String analiticaRetentionMs;

    @Value("${app.kafka.topics.pedidos-auditoria.name:pedidos.auditoria.v1}")
    private String auditoriaTopicName;

    @Value("${app.kafka.topics.pedidos-auditoria.partitions:3}")
    private int auditoriaPartitions;

    @Value("${app.kafka.topics.pedidos-auditoria.replicas:3}")
    private int auditoriaReplicas;

    @Value("${app.kafka.topics.pedidos-auditoria.retention-ms:2592000000}")
    private String auditoriaRetentionMs;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    /**
     * Tópico 1: Streaming de eventos de pedidos para analítica en tiempo real.
     * 3 particiones para balanceo y paralelismo, 3 réplicas para tolerancia a fallos.
     */
    @Bean
    public NewTopic pedidosAnaliticaTopic() {
        return TopicBuilder.name(analiticaTopicName)
                .partitions(analiticaPartitions)
                .replicas(analiticaReplicas)
                .config("retention.ms", analiticaRetentionMs)
                .config("cleanup.policy", "delete")
                .config("min.insync.replicas", "2")
                .build();
    }

    /**
     * Tópico 2: Trazabilidad inmutable y auditoría de cambios en pedidos.
     * 3 particiones y 3 réplicas con retención extendida (30 días).
     */
    @Bean
    public NewTopic pedidosAuditoriaTopic() {
        return TopicBuilder.name(auditoriaTopicName)
                .partitions(auditoriaPartitions)
                .replicas(auditoriaReplicas)
                .config("retention.ms", auditoriaRetentionMs)
                .config("cleanup.policy", "delete")
                .config("min.insync.replicas", "2")
                .build();
    }
}
