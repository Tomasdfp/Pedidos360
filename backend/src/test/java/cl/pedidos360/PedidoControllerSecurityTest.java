package cl.pedidos360;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PedidoControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("1. Petición sin token debe retornar 401 Unauthorized")
    public void whenNoToken_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("2. Petición con scope pedidos.read en GET debe retornar 200 OK")
    public void whenValidReadToken_thenOk() throws Exception {
        mockMvc.perform(get("/api/pedidos")
                .with(jwt().authorities(() -> "SCOPE_pedidos.read")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("3. Petición POST con scope solo pedidos.read debe retornar 403 Forbidden")
    public void whenReadTokenOnPost_thenForbidden() throws Exception {
        String newPedidoJson = """
            {
                "numeroOrden": "ORD-999",
                "cliente": "Test Client",
                "descripcion": "Test Product",
                "monto": 50000.0,
                "estado": "PENDIENTE"
            }
            """;

        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newPedidoJson)
                .with(jwt().authorities(() -> "SCOPE_pedidos.read")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("4. Petición POST con scope pedidos.write debe retornar 201 Created")
    public void whenWriteTokenOnPost_thenCreated() throws Exception {
        String newPedidoJson = """
            {
                "numeroOrden": "ORD-999",
                "cliente": "Test Client",
                "descripcion": "Test Product",
                "monto": 50000.0,
                "estado": "PENDIENTE"
            }
            """;

        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newPedidoJson)
                .with(jwt().authorities(() -> "SCOPE_pedidos.write")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.numeroOrden").value("ORD-999"));
    }

    @Test
    @DisplayName("5. Petición DELETE con scope pedidos.write debe retornar 204 No Content")
    public void whenWriteTokenOnDelete_thenNoContent() throws Exception {
        mockMvc.perform(delete("/api/pedidos/1")
                .with(jwt().authorities(() -> "SCOPE_pedidos.write")))
                .andExpect(status().isNoContent());
    }
}
