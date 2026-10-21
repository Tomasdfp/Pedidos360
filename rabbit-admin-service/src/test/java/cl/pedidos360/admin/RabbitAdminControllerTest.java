package cl.pedidos360.admin;

import cl.pedidos360.admin.controller.RabbitAdminController;
import cl.pedidos360.admin.dto.QueueInfoDto;
import cl.pedidos360.admin.dto.QueueRequestDto;
import cl.pedidos360.admin.service.RabbitAdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RabbitAdminController.class)
public class RabbitAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RabbitAdminService adminService;

    @Test
    @DisplayName("POST /queues con datos válidos debe retornar 201 Created")
    public void testCreateQueue_Success() throws Exception {
        QueueRequestDto dto = QueueRequestDto.builder()
                .name("facturas.queue")
                .durable(true)
                .autoDelete(false)
                .build();

        QueueInfoDto info = QueueInfoDto.builder()
                .name("facturas.queue")
                .messageCount(0)
                .consumerCount(0)
                .exists(true)
                .status("ACTIVA")
                .build();

        when(adminService.createQueue(any(QueueRequestDto.class))).thenReturn(info);

        mockMvc.perform(post("/queues")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("facturas.queue"));
    }

    @Test
    @DisplayName("POST /queues con nombre vacío debe retornar 400 Bad Request")
    public void testCreateQueue_InvalidName_BadRequest() throws Exception {
        QueueRequestDto dto = QueueRequestDto.builder()
                .name("") // Inválido
                .build();

        mockMvc.perform(post("/queues")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.name").exists());
    }

    @Test
    @DisplayName("DELETE /queues/{name} debe retornar 200 OK cuando se elimina")
    public void testDeleteQueue_Success() throws Exception {
        when(adminService.deleteQueue("facturas.queue")).thenReturn(true);

        mockMvc.perform(delete("/queues/facturas.queue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("GET /queues/{name} debe retornar estado y conteo de mensajes")
    public void testGetQueueInfo_Success() throws Exception {
        QueueInfoDto info = QueueInfoDto.builder()
                .name("ticket.queue")
                .messageCount(5)
                .consumerCount(1)
                .exists(true)
                .status("ACTIVA")
                .build();

        when(adminService.getQueueInfo("ticket.queue")).thenReturn(info);

        mockMvc.perform(get("/queues/ticket.queue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.messageCount").value(5));
    }
}
