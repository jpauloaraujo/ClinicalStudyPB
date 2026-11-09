package UEPB.ESWII.ClinicalStudyPB;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

/**
 * T67: o CORS libera só as origens configuradas, por padrão a web (5173) e o Expo web (8082),
 * e aceita outra lista por CORS_ALLOWED_ORIGINS (propriedade app.cors.allowed-origins).
 */
class CorsIntegrationTest {

    private static final String URL = "/api/auth/login";

    private static org.springframework.test.web.servlet.ResultActions preflight(MockMvc mockMvc, String origem)
            throws Exception {
        return mockMvc.perform(options(URL)
                .header(HttpHeaders.ORIGIN, origem)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type"));
    }

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @DisplayName("Com as origens padrão")
    class OrigensPadrao {

        @Autowired
        private MockMvc mockMvc;

        @Test
        @DisplayName("libera a web em http://localhost:5173")
        void liberaWeb() throws Exception {
            preflight(mockMvc, "http://localhost:5173")
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
        }

        @Test
        @DisplayName("libera o Expo web em http://localhost:8082")
        void liberaExpoWeb() throws Exception {
            preflight(mockMvc, "http://localhost:8082")
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:8082"));
        }

        @Test
        @DisplayName("recusa uma origem desconhecida")
        void recusaOrigemDesconhecida() throws Exception {
            preflight(mockMvc, "http://evil.example")
                    .andExpect(status().isForbidden())
                    .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        }
    }

    @Nested
    @SpringBootTest(properties = "app.cors.allowed-origins=http://localhost:3000, http://192.168.0.10:5173")
    @AutoConfigureMockMvc
    @DisplayName("Com origens configuradas por variável de ambiente")
    class OrigensConfiguradas {

        @Autowired
        private MockMvc mockMvc;

        @Test
        @DisplayName("libera cada origem da lista, ignorando espaços")
        void liberaOrigensDaLista() throws Exception {
            preflight(mockMvc, "http://localhost:3000")
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"));
            preflight(mockMvc, "http://192.168.0.10:5173")
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://192.168.0.10:5173"));
        }

        @Test
        @DisplayName("deixa de liberar a origem padrão que saiu da lista")
        void recusaOrigemForaDaLista() throws Exception {
            preflight(mockMvc, "http://localhost:5173").andExpect(status().isForbidden());
        }
    }
}
