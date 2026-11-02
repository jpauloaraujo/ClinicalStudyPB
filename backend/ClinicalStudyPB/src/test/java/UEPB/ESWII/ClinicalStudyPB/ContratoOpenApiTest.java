package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * T37 e T48: o contrato {@code docs/api/openapi-sprint2.yaml} e a API publicada em {@code /v3/api-docs}
 * não podem divergir. Todo caminho e método do contrato existe na API, e todo endpoint de recrutamento
 * da API aparece no contrato. Se este teste falhar, corrija o YAML no mesmo Pull Request do código.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ContratoOpenApiTest {

    private static final Path CONTRATO = Path.of("..", "..", "docs", "api", "openapi-sprint2.yaml");
    private static final Set<String> METODOS = Set.of("get", "post", "put", "delete", "patch");

    // Endpoints da Sprint 2 (recrutamento e estado do estudo); os da Sprint 1 não fazem parte deste contrato
    private static final Pattern RECRUTAMENTO = Pattern.compile(
            "^/api/(estudos/\\{estudoId}/(transicoes|historico-status|criterios|visitas|candidatos|participantes)(/.*)?"
                    + "|visitas/.*|candidatos/.*)$");

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("unchecked")
    private static Set<String> operacoesDoContrato() throws IOException {
        Map<String, Object> documento = new Yaml().load(Files.readString(CONTRATO));
        Map<String, Map<String, Object>> caminhos = (Map<String, Map<String, Object>>) documento.get("paths");
        Set<String> operacoes = new TreeSet<>();
        caminhos.forEach((caminho, metodos) -> metodos.keySet().stream().filter(METODOS::contains)
                .forEach(metodo -> operacoes.add(metodo.toUpperCase() + " " + caminho)));
        return operacoes;
    }

    private Set<String> operacoesDaApi() throws Exception {
        String json = mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode caminhos = new ObjectMapper().readTree(json).get("paths");
        Set<String> operacoes = new TreeSet<>();
        caminhos.fields().forEachRemaining(entrada -> entrada.getValue().fieldNames().forEachRemaining(metodo -> {
            if (METODOS.contains(metodo)) {
                operacoes.add(metodo.toUpperCase() + " " + entrada.getKey());
            }
        }));
        return operacoes;
    }

    @Test
    @DisplayName("Todo endpoint do contrato existe na API publicada")
    void contratoNaoPrometeEndpointInexistente() throws Exception {
        Set<String> faltandoNaApi = new TreeSet<>(operacoesDoContrato());
        faltandoNaApi.removeAll(operacoesDaApi());

        assertThat(faltandoNaApi).as("operações do contrato que a API não tem").isEmpty();
    }

    @Test
    @DisplayName("Todo endpoint de recrutamento da API aparece no contrato")
    void apiNaoTemEndpointForaDoContrato() throws Exception {
        Set<String> faltandoNoContrato = new TreeSet<>();
        for (String operacao : operacoesDaApi()) {
            String caminho = operacao.substring(operacao.indexOf(' ') + 1);
            if (RECRUTAMENTO.matcher(caminho).matches()) {
                faltandoNoContrato.add(operacao);
            }
        }
        faltandoNoContrato.removeAll(operacoesDoContrato());

        assertThat(faltandoNoContrato).as("operações da API que o contrato não descreve").isEmpty();
    }

    @Test
    @DisplayName("O contrato cobre as 24 operações previstas da Sprint 2")
    void contratoTemAsOperacoesEsperadas() throws Exception {
        assertThat(operacoesDoContrato()).hasSize(24).contains(
                "POST /api/estudos/{estudoId}/transicoes", "GET /api/estudos/{estudoId}/historico-status",
                "POST /api/estudos/{estudoId}/criterios", "POST /api/visitas/{visitaId}/acoes",
                "GET /api/estudos/{estudoId}/candidatos", "POST /api/candidatos/{candidatoId}/avaliacoes",
                "POST /api/candidatos/{candidatoId}/consentimento", "POST /api/estudos/{estudoId}/participantes",
                "GET /api/estudos/{estudoId}/participantes/vagas");
    }
}
