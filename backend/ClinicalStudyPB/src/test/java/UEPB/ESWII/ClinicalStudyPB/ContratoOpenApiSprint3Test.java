package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.HistoricoParticipanteResponse;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoNoHistorico;

/**
 * Contrato da Sprint 3 ({@code docs/api/openapi-sprint3.yaml}). Cada PBI acrescenta suas operações ao YAML e
 * atualiza a contagem esperada. Além de conferir os caminhos, confere que os schemas do histórico (T95) têm
 * exatamente os mesmos campos dos DTOs do backend, para o contrato e o código não divergirem.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ContratoOpenApiSprint3Test {

    private static final Path CONTRATO = Path.of("..", "..", "docs", "api", "openapi-sprint3.yaml");
    private static final Set<String> METODOS = Set.of("get", "post", "put", "delete", "patch");

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("unchecked")
    private static Map<String, Object> documento() throws IOException {
        return new Yaml().load(Files.readString(CONTRATO));
    }

    @SuppressWarnings("unchecked")
    private static Set<String> operacoesDoContrato() throws IOException {
        Map<String, Map<String, Object>> caminhos = (Map<String, Map<String, Object>>) documento().get("paths");
        Set<String> operacoes = new TreeSet<>();
        caminhos.forEach((caminho, metodos) -> metodos.keySet().stream().filter(METODOS::contains)
                .forEach(metodo -> operacoes.add(metodo.toUpperCase() + " " + caminho)));
        return operacoes;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> schema(String nome) throws IOException {
        Map<String, Object> componentes = (Map<String, Object>) documento().get("components");
        return ((Map<String, Map<String, Object>>) componentes.get("schemas")).get(nome);
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
    @DisplayName("T95: o contrato descreve o histórico do participante (1 operação até aqui; a T74 acrescenta as do agendamento)")
    void contratoTemAsOperacoesEsperadas() throws Exception {
        assertThat(operacoesDoContrato()).hasSize(1).contains("GET /api/participacoes/{participacaoId}/historico");
    }

    @Test
    @Disabled("T96: ligar quando o endpoint do histórico existir no backend")
    @DisplayName("Todo endpoint do contrato existe na API publicada")
    void contratoNaoPrometeEndpointInexistente() throws Exception {
        Set<String> faltandoNaApi = new TreeSet<>(operacoesDoContrato());
        faltandoNaApi.removeAll(operacoesDaApi());

        assertThat(faltandoNaApi).as("operações do contrato que a API não tem").isEmpty();
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest(name = "T95: o schema {0} tem os mesmos campos do DTO")
    @ValueSource(strings = {"HistoricoParticipanteResponse", "ParticipanteDoHistorico", "ResumoDoHistorico",
            "VisitaDoHistorico", "ResultadoDoHistorico"})
    void schemaTemOsCamposDoDto(String nome) throws Exception {
        Class<?> dto = nome.equals("HistoricoParticipanteResponse") ? HistoricoParticipanteResponse.class
                : Arrays.stream(HistoricoParticipanteResponse.class.getDeclaredClasses())
                        .filter(c -> c.getSimpleName().equals(nome)).findFirst().orElseThrow();
        Set<String> camposDoDto = new TreeSet<>();
        Arrays.stream(dto.getRecordComponents()).forEach(c -> camposDoDto.add(c.getName()));

        Map<String, Object> schema = schema(nome);
        Set<String> camposDoContrato = new TreeSet<>(((Map<String, Object>) schema.get("properties")).keySet());
        List<String> obrigatorios = (List<String>) schema.get("required");

        assertThat(camposDoContrato).as("campos do schema " + nome).isEqualTo(camposDoDto);
        assertThat(camposDoContrato).as("campos obrigatórios de " + nome).containsAll(obrigatorios);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("T95: o enum SituacaoNoHistorico do contrato tem os mesmos valores do enum do backend")
    void enumDoContratoIgualAoDoBackend() throws Exception {
        List<String> valores = (List<String>) schema("SituacaoNoHistorico").get("enum");

        assertThat(valores).containsExactly(Arrays.stream(SituacaoNoHistorico.values()).map(Enum::name).toArray(String[]::new));
    }
}
