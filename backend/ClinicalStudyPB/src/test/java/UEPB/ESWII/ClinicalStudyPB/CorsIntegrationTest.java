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
 * T67: com a configuração padrão, o CORS libera só a web (5173) e o Expo web (8082).
 * Origens configuradas por variável de ambiente: {@link CorsOrigensConfiguradasIntegrationTest}.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CorsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions preflight(String origem) throws Exception {
        return mockMvc.perform(options("/api/auth/login")
                .header(HttpHeaders.ORIGIN, origem)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type"));
    }

    @Test
    @DisplayName("libera a web em http://localhost:5173")
    void liberaWeb() throws Exception {
        preflight("http://localhost:5173")
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
    }

    @Test
    @DisplayName("libera o Expo web em http://localhost:8082")
    void liberaExpoWeb() throws Exception {
        preflight("http://localhost:8082")
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:8082"));
    }

    @Test
    @DisplayName("recusa uma origem desconhecida")
    void recusaOrigemDesconhecida() throws Exception {
        preflight("http://evil.example")
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
