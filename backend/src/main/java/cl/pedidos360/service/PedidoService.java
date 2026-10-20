package cl.pedidos360.service;

import cl.pedidos360.messaging.producer.PedidoEventProducer;
import cl.pedidos360.model.Pedido;
import cl.pedidos360.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoEventProducer pedidoEventProducer;

    public PedidoService(PedidoRepository pedidoRepository, PedidoEventProducer pedidoEventProducer) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoEventProducer = pedidoEventProducer;
    }

    public List<Pedido> findAll() {
        return pedidoRepository.findAll();
    }

    public Optional<Pedido> findById(Long id) {
        return pedidoRepository.findById(id);
    }

    public Pedido save(Pedido pedido) {
        if (pedido.getFechaCreacion() == null) {
            pedido.setFechaCreacion(LocalDateTime.now());
        }
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        // Publicación del evento asíncrono sin acoplar detalles de RabbitMQ
        pedidoEventProducer.publicarPedidoCreado(pedidoGuardado);

        return pedidoGuardado;
    }

    public Optional<Pedido> update(Long id, Pedido pedidoDetails) {
        return pedidoRepository.findById(id).map(existing -> {
            if (pedidoDetails.getNumeroOrden() != null) {
                existing.setNumeroOrden(pedidoDetails.getNumeroOrden());
            }
            if (pedidoDetails.getCliente() != null) {
                existing.setCliente(pedidoDetails.getCliente());
            }
            if (pedidoDetails.getDescripcion() != null) {
                existing.setDescripcion(pedidoDetails.getDescripcion());
            }
            if (pedidoDetails.getMonto() != null) {
                existing.setMonto(pedidoDetails.getMonto());
            }
            if (pedidoDetails.getEstado() != null) {
                existing.setEstado(pedidoDetails.getEstado());
            }
            return pedidoRepository.save(existing);
        });
    }

    public boolean deleteById(Long id) {
        if (pedidoRepository.existsById(id)) {
            pedidoRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
