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
import java.util.regex.Pattern;

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
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;

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

    /** Quantas operações o YAML tem agora: 5 do agendamento (T74) e 1 do histórico (T95). Cada PBI só atualiza este número. */
    private static final int OPERACOES_ESPERADAS = 6;

    // Caminhos da Sprint 3 (nenhum colide com o regex do ContratoOpenApiTest da Sprint 2)
    private static final Pattern SPRINT3 = Pattern.compile(
            "^/api/(participacoes/.*|visitas-agendadas/.*|estudos/\\{estudoId}/agenda(/.*)?|me/.*|contas-participante/.*)$");

    @Autowired
    private MockMvc mockMvc;

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
    @DisplayName("O contrato da Sprint 3 tem as operações previstas até aqui (agendamento da T74 e histórico da T95)")
    void contratoTemAsOperacoesEsperadas() throws Exception {
        assertThat(operacoesDoContrato()).hasSize(OPERACOES_ESPERADAS).contains(
                "GET /api/participacoes/{participacaoId}/visitas",
                "POST /api/participacoes/{participacaoId}/visitas-agendadas",
                "PUT /api/visitas-agendadas/{visitaAgendadaId}",
                "GET /api/estudos/{estudoId}/agenda",
                "GET /api/estudos/{estudoId}/agenda/participantes",
                "GET /api/participacoes/{participacaoId}/historico");
    }

    @Test
    @Disabled("T76: ligar quando os endpoints do agendamento existirem no backend")
    @DisplayName("Todo endpoint do agendamento do contrato existe na API publicada")
    void contratoNaoPrometeEndpointInexistente() throws Exception {
        Set<String> faltandoNaApi = new TreeSet<>(operacoesDoContrato());
        faltandoNaApi.removeIf(operacao -> operacao.endsWith("/historico"));
        faltandoNaApi.removeAll(operacoesDaApi());

        assertThat(faltandoNaApi).as("operações do contrato que a API não tem").isEmpty();
    }

    @Test
    @Disabled("T96: ligar quando o endpoint do histórico existir no backend")
    @DisplayName("O endpoint do histórico do contrato existe na API publicada")
    void historicoDoContratoExisteNaApi() throws Exception {
        Set<String> historico = new TreeSet<>(operacoesDoContrato());
        historico.removeIf(operacao -> !operacao.endsWith("/historico"));
        historico.removeAll(operacoesDaApi());

        assertThat(historico).as("operações do histórico que a API não tem").isEmpty();
    }

    @Test
    @DisplayName("Todo endpoint da Sprint 3 que a API publicar tem de estar no contrato")
    void apiNaoTemEndpointDaSprint3ForaDoContrato() throws Exception {
        Set<String> faltandoNoContrato = new TreeSet<>();
        for (String operacao : operacoesDaApi()) {
            String caminho = operacao.substring(operacao.indexOf(' ') + 1);
            if (SPRINT3.matcher(caminho).matches()) {
                faltandoNoContrato.add(operacao);
            }
        }
        faltandoNoContrato.removeAll(operacoesDoContrato());

        assertThat(faltandoNoContrato).as("operações da API que o contrato não descreve").isEmpty();
    }

    @Test
    @DisplayName("Todo caminho do contrato casa com o regex dos caminhos da Sprint 3")
    void caminhosDoContratoSaoDaSprint3() throws Exception {
        assertThat(operacoesDoContrato()).allSatisfy(operacao ->
                assertThat(SPRINT3.matcher(operacao.substring(operacao.indexOf(' ') + 1)).matches()).isTrue());
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("T74: toda operação declara 401, 403, 404 e 409; as de escrita declaram também 400")
    void operacoesDeclaramAsRespostasDeErro() throws Exception {
        Map<String, Map<String, Object>> caminhos = (Map<String, Map<String, Object>>) documento().get("paths");
        caminhos.forEach((caminho, metodos) -> metodos.forEach((metodo, definicao) -> {
            if (!METODOS.contains(metodo)) {
                return;
            }
            Set<String> respostas = ((Map<Object, Object>) ((Map<String, Object>) definicao).get("responses"))
                    .keySet().stream().map(Object::toString).collect(java.util.stream.Collectors.toSet());
            assertThat(respostas).as(metodo + " " + caminho).contains("401", "403", "404", "409");
            if (!metodo.equals("get")) {
                assertThat(respostas).as(metodo + " " + caminho).contains("400");
            }
        }));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("T74: o contrato declara os schemas do agendamento")
    void contratoDeclaraOsSchemasDoAgendamento() throws Exception {
        Map<String, Object> componentes = (Map<String, Object>) documento().get("components");
        Map<String, Object> schemas = (Map<String, Object>) componentes.get("schemas");

        assertThat(schemas.keySet()).contains("VisitaDoParticipanteResponse", "VisitaAgendadaRequest",
                "VisitaReagendamentoRequest", "VisitaAgendadaResponse", "AgendaItemResponse",
                "ParticipanteAgendaResponse", "SituacaoVisita", "Erro");
        Map<String, Object> visita = (Map<String, Object>) ((Map<String, Object>) schema("VisitaDoParticipanteResponse")
                .get("properties")).get("agendamento");
        assertThat(visita.get("nullable")).isEqualTo(true);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("T74: o enum SituacaoVisita do contrato tem os mesmos valores do enum do backend")
    void situacaoVisitaDoContratoIgualAoDoBackend() throws Exception {
        List<String> valores = (List<String>) schema("SituacaoVisita").get("enum");

        assertThat(valores).containsExactly(Arrays.stream(SituacaoVisita.values()).map(Enum::name).toArray(String[]::new));
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
