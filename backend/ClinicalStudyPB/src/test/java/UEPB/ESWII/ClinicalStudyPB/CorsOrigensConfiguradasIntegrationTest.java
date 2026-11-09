package UEPB.ESWII.ClinicalStudyPB;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * T67 (B-T67-01): a lista de origens do CORS vem de app.cors.allowed-origins (variável CORS_ALLOWED_ORIGINS).
 */
@SpringBootTest(properties = "app.cors.allowed-origins=http://localhost:3000, http://192.168.0.10:5173")
@AutoConfigureMockMvc
class CorsOrigensConfiguradasIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions preflight(String origem) throws Exception {
        return mockMvc.perform(options("/api/auth/login")
                .header(HttpHeaders.ORIGIN, origem)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type"));
    }

    @Test
    @DisplayName("libera cada origem da lista, ignorando espaços")
    void liberaOrigensDaLista() throws Exception {
        preflight("http://localhost:3000")
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"));
        preflight("http://192.168.0.10:5173")
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://192.168.0.10:5173"));
    }

    @Test
    @DisplayName("deixa de liberar a origem padrão que saiu da lista")
    void recusaOrigemForaDaLista() throws Exception {
        preflight("http://localhost:5173").andExpect(status().isForbidden());
    }
}
