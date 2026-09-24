package cl.pedidos360.config;

import cl.pedidos360.model.Pedido;
import cl.pedidos360.repository.PedidoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner initDatabase(PedidoRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.saveAll(List.of(
                    Pedido.builder()
                        .numeroOrden("ORD-001")
                        .cliente("Tomas Del Fierro")
                        .descripcion("Notebook Lenovo ThinkPad")
                        .monto(850000.0)
                        .estado("PENDIENTE")
                        .fechaCreacion(LocalDateTime.now().minusDays(4))
                        .build(),
                    Pedido.builder()
                        .numeroOrden("ORD-002")
                        .cliente("Maria Gonzalez")
                        .descripcion("Monitor Samsung 27 pulgadas")
                        .monto(220000.0)
                        .estado("COMPLETADO")
                        .fechaCreacion(LocalDateTime.now().minusDays(6))
                        .build(),
                    Pedido.builder()
                        .numeroOrden("ORD-003")
                        .cliente("Pedro Soto")
                        .descripcion("Teclado mecánico Logitech")
                        .monto(75000.0)
                        .estado("EN_PROCESO")
                        .fechaCreacion(LocalDateTime.now().minusDays(2))
                        .build()
                ));
            }
        };
    }
}
