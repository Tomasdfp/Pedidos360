package cl.pedidos360.admin.controller;

import cl.pedidos360.admin.dto.*;
import cl.pedidos360.admin.service.RabbitAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para la administración de RabbitMQ y envío de Tickets.
 * Expone endpoints con rutas y verbos estandarizados (POST /queues, DELETE /queues/{name}, etc.)
 * con validación estricta de parámetros mediante DTOs.
 */
@RestController
@RequestMapping({"/api/rabbit", ""})
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
public class RabbitAdminController {

    private final RabbitAdminService adminService;

    public RabbitAdminController(RabbitAdminService adminService) {
        this.adminService = adminService;
    }

    // =========================================================================
    // Endpoints de Colas (Queues)
    // =========================================================================

    @PostMapping("/queues")
    public ResponseEntity<ApiResponseDto<QueueInfoDto>> createQueue(@Valid @RequestBody QueueRequestDto dto) {
        QueueInfoDto info = adminService.createQueue(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.ok("Cola '" + dto.getName() + "' creada exitosamente", info));
    }

    @DeleteMapping("/queues/{name}")
    public ResponseEntity<ApiResponseDto<Boolean>> deleteQueue(@PathVariable String name) {
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("El nombre de la cola no puede estar vacío"));
        }
        boolean deleted = adminService.deleteQueue(name);
        if (deleted) {
            return ResponseEntity.ok(ApiResponseDto.ok("Cola '" + name + "' eliminada correctamente", true));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error("No se pudo eliminar la cola '" + name + "' o no existe"));
        }
    }

    @GetMapping("/queues/{name}")
    public ResponseEntity<ApiResponseDto<QueueInfoDto>> getQueueInfo(@PathVariable String name) {
        QueueInfoDto info = adminService.getQueueInfo(name);
        return ResponseEntity.ok(ApiResponseDto.ok("Estado de la cola obtenido", info));
    }

    // =========================================================================
    // Endpoints de Exchanges
    // =========================================================================

    @PostMapping("/exchanges")
    public ResponseEntity<ApiResponseDto<String>> createExchange(@Valid @RequestBody ExchangeRequestDto dto) {
        adminService.createExchange(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.ok("Exchange '" + dto.getName() + "' creado exitosamente", dto.getName()));
    }

    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<ApiResponseDto<Boolean>> deleteExchange(@PathVariable String name) {
        boolean deleted = adminService.deleteExchange(name);
        return ResponseEntity.ok(ApiResponseDto.ok("Exchange '" + name + "' eliminado", deleted));
    }

    // =========================================================================
    // Endpoints de Bindings
    // =========================================================================

    @PostMapping("/bindings")
    public ResponseEntity<ApiResponseDto<String>> createBinding(@Valid @RequestBody BindingRequestDto dto) {
        adminService.createBinding(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.ok("Binding creado entre cola '" + dto.getQueueName() + "' y exchange '" + dto.getExchangeName() + "'", null));
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<ApiResponseDto<String>> removeBinding(@Valid @RequestBody BindingRequestDto dto) {
        adminService.removeBinding(dto);
        return ResponseEntity.ok(ApiResponseDto.ok("Binding eliminado", null));
    }

    // =========================================================================
    // Endpoint para emitir Tickets (Demostración de flujo del consumidor)
    // =========================================================================

    @PostMapping("/tickets")
    public ResponseEntity<ApiResponseDto<TicketEvent>> createTicket(@Valid @RequestBody TicketEvent ticket) {
        if (ticket.getId() == null) {
            ticket.setId(System.currentTimeMillis());
        }
        adminService.publishTicket(ticket);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponseDto.ok("Ticket publicado exitosamente para procesamiento asíncrono", ticket));
    }

    // =========================================================================
    // Manejo de Errores de Validación (400 Bad Request)
    // =========================================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(ApiResponseDto.<Map<String, String>>builder()
                        .success(false)
                        .message("Parámetros de entrada inválidos")
                        .data(errors)
                        .build());
    }
}
