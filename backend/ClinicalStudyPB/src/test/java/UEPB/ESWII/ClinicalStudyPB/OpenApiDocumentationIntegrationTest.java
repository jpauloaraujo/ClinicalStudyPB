package UEPB.ESWII.ClinicalStudyPB;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Testes de integração da documentação OpenAPI 3 / Swagger UI (T10).
 */
@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("T10 - Deve disponibilizar a especificação OpenAPI 3 em /v3/api-docs com metadados e segurança JWT")
    void deveRetornarEspecificacaoOpenApiCompleta() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").value("ClinicalStudyPB API"))
                .andExpect(jsonPath("$.info.version").value("v1.0.0"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"))
                .andExpect(jsonPath("$.paths['/api/usuarios']").exists())
                .andExpect(jsonPath("$.paths['/api/usuarios/{id}']").exists())
                .andExpect(jsonPath("$.paths['/api/estudos']").exists())
                .andExpect(jsonPath("$.paths['/api/estudos/{id}']").exists());
    }

    @Test
    @DisplayName("T10 - Deve permitir acesso público à interface interativa do Swagger UI")
    void devePermitirAcessoAoSwaggerUi() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
