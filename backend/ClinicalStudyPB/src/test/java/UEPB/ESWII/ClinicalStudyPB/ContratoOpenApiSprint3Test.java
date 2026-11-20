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

import UEPB.ESWII.ClinicalStudyPB.controller.dto.AtividadeComResultadoResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.AgendaItemResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.DesfechoRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.ParticipanteAgendaResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.HistoricoParticipanteResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.ResultadoAtividadeResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.ResultadoRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.ResultadosDaVisitaResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.VisitaAgendadaRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.VisitaAgendadaResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.VisitaDoParticipanteResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.VisitaReagendamentoRequest;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoNoHistorico;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.TipoDesfecho;

/**
 * Contrato da Sprint 3 ({@code docs/api/openapi-sprint3.yaml}). Cada PBI acrescenta suas operações ao YAML e
 * atualiza a contagem esperada. Além de conferir os caminhos, confere que os schemas do histórico (T95) têm
 * exatamente os mesmos campos dos DTOs do backend, para o contrato e o código não divergirem, e que os schemas dos
 * resultados (T88) declaram os limites e os campos anuláveis que a tela e o serviço (T89) usam.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ContratoOpenApiSprint3Test {

    private static final Path CONTRATO = Path.of("..", "..", "docs", "api", "openapi-sprint3.yaml");
    private static final Set<String> METODOS = Set.of("get", "post", "put", "delete", "patch");

    /**
     * Quantas operações o YAML tem agora: 5 do agendamento (T74), 1 do histórico (T95), 2 dos resultados (T88), 2
     * do desfecho (T80 e T82: o POST do desfecho e o GET da visita), 2 dos estudos do participante (T109) e 2 das
     * visitas do participante (T115). Cada PBI só atualiza este número.
     */
    private static final int OPERACOES_ESPERADAS = 14;

    /** Operações do app do participante (T109): os estudos do participante logado, implementadas no {@code MeController}. */
    private static final Set<String> OPERACOES_ME = Set.of(
            "GET /api/me/estudos",
            "GET /api/me/estudos/{estudoId}");

    /**
     * Operações do app do participante descritas na T115 (próximas visitas e histórico de comparecimento): o contrato
     * vem antes; a implementação é da T116, que liga os casos {@code @Disabled("T116")} e tira estas operações da
     * lista de pendentes de {@link #contratoNaoPrometeEndpointInexistente()}.
     */
    private static final Set<String> OPERACOES_ME_VISITAS = Set.of(
            "GET /api/me/visitas/proximas",
            "GET /api/me/visitas/historico");

    private static final String PACOTE_DTO = "UEPB.ESWII.ClinicalStudyPB.controller.dto.";

    /** Operações do desfecho da visita (T80 e T82), implementadas na T82. */
    private static final Set<String> OPERACOES_DESFECHO = Set.of(
            "POST /api/visitas-agendadas/{visitaAgendadaId}/desfecho",
            "GET /api/visitas-agendadas/{visitaAgendadaId}");

    /** Operações dos resultados das avaliações (T88), implementadas na T90 ({@code ResultadoAtividadeController}). */
    private static final Set<String> OPERACOES_RESULTADOS = Set.of(
            "GET /api/visitas-agendadas/{visitaAgendadaId}/resultados",
            "PUT /api/visitas-agendadas/{visitaAgendadaId}/resultados/{acaoId}");

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
    @DisplayName("O contrato da Sprint 3 tem as operações previstas até aqui (agendamento da T74, histórico da T95, resultados da T88 e desfecho da T80/T82)")
    void contratoTemAsOperacoesEsperadas() throws Exception {
        assertThat(operacoesDoContrato()).hasSize(OPERACOES_ESPERADAS).containsAll(OPERACOES_RESULTADOS)
                .containsAll(OPERACOES_DESFECHO).containsAll(OPERACOES_ME).containsAll(OPERACOES_ME_VISITAS).contains(
                "GET /api/participacoes/{participacaoId}/visitas",
                "POST /api/participacoes/{participacaoId}/visitas-agendadas",
                "PUT /api/visitas-agendadas/{visitaAgendadaId}",
                "GET /api/estudos/{estudoId}/agenda",
                "GET /api/estudos/{estudoId}/agenda/participantes",
                "GET /api/participacoes/{participacaoId}/historico");
    }

    @Test
    @DisplayName("Todo endpoint do agendamento do contrato existe na API publicada")
    void contratoNaoPrometeEndpointInexistente() throws Exception {
        Set<String> faltandoNaApi = new TreeSet<>(operacoesDoContrato());
        faltandoNaApi.removeIf(operacao -> operacao.endsWith("/participacoes/{participacaoId}/historico"));
        faltandoNaApi.removeAll(OPERACOES_RESULTADOS);
        faltandoNaApi.removeAll(OPERACOES_DESFECHO);
        // T115: as visitas do participante estão no contrato, mas só a T116 as implementa (apague esta linha lá)
        faltandoNaApi.removeAll(OPERACOES_ME_VISITAS);
        faltandoNaApi.removeAll(operacoesDaApi());

        assertThat(faltandoNaApi).as("operações do contrato que a API não tem").isEmpty();
    }

    @Test
    @Disabled("T116: ligar quando as consultas de visitas do participante existirem no backend")
    @DisplayName("T115: as consultas de visitas do participante do contrato existem na API publicada")
    void meVisitasDoContratoExistemNaApi() throws Exception {
        Set<String> faltandoNaApi = new TreeSet<>(OPERACOES_ME_VISITAS);
        faltandoNaApi.removeAll(operacoesDaApi());

        assertThat(faltandoNaApi).as("operações /me/visitas que a API não tem").isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Disabled("T116: ligar quando os DTOs das consultas de visitas do participante existirem no backend")
    @ParameterizedTest(name = "T115: o schema {0} tem os mesmos campos do DTO")
    @ValueSource(strings = {"MinhaVisitaResponse", "ResumoComparecimentoResponse", "HistoricoMinhasVisitasResponse"})
    void schemaMeVisitasTemOsCamposDoDto(String nome) throws Exception {
        Class<?> dto = Class.forName(PACOTE_DTO + nome);
        Set<String> camposDoDto = new TreeSet<>();
        Arrays.stream(dto.getRecordComponents()).forEach(c -> camposDoDto.add(c.getName()));
        Set<String> camposDoContrato = new TreeSet<>(((Map<String, Object>) schema(nome).get("properties")).keySet());

        assertThat(camposDoContrato).as("campos do schema " + nome).isEqualTo(camposDoDto);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("T115: o contrato descreve as duas consultas do participante: sem id, só PARTICIPANTE, paginação e erros")
    void contratoDescreveAsConsultasDoParticipante() throws Exception {
        Map<String, Map<String, Object>> caminhos = (Map<String, Map<String, Object>>) documento().get("paths");
        Map<String, Object> proximas = (Map<String, Object>) caminhos.get("/api/me/visitas/proximas").get("get");
        Map<String, Object> historico = (Map<String, Object>) caminhos.get("/api/me/visitas/historico").get("get");

        // RQ-T115-03: nenhuma recebe o id do participante (nem no caminho nem como parâmetro); o dono é o token
        for (Map<String, Object> operacao : List.of(proximas, historico)) {
            assertThat(nomesDosParametros(operacao)).doesNotContain("participanteId", "participacaoId", "usuarioId");
            assertThat(((List<String>) operacao.get("tags"))).containsExactly("Participante (me)");
            assertThat((String) operacao.get("description")).contains("só PARTICIPANTE").contains("Não traz resultados");
            // RQ-T115-08: 400 (PARAMETRO_INVALIDO), 401 e 403; nunca 404 (estudoId alheio devolve lista vazia)
            assertThat(((Map<Object, Object>) operacao.get("responses")).keySet().stream().map(Object::toString))
                    .containsExactlyInAnyOrder("200", "400", "401", "403");
        }
        assertThat(nomesDosParametros(proximas)).containsExactly("estudoId");
        // RQ-T115-02: estudoId, pagina e tamanho (padrão 0 e 20, máximo 100)
        assertThat(nomesDosParametros(historico)).containsExactly("estudoId", "pagina", "tamanho");
        Map<String, Object> tamanho = ((List<Map<String, Object>>) historico.get("parameters")).get(2);
        Map<String, Object> esquemaTamanho = (Map<String, Object>) tamanho.get("schema");
        assertThat(esquemaTamanho.get("default")).isEqualTo(20);
        assertThat(esquemaTamanho.get("minimum")).isEqualTo(1);
        assertThat(esquemaTamanho.get("maximum")).isEqualTo(100);
        Map<String, Object> pagina = ((List<Map<String, Object>>) historico.get("parameters")).get(1);
        assertThat(((Map<String, Object>) pagina.get("schema")).get("default")).isEqualTo(0);
    }

    @SuppressWarnings("unchecked")
    private static List<String> nomesDosParametros(Map<String, Object> operacao) throws IOException {
        Map<String, Object> parametros = (Map<String, Object>) ((Map<String, Object>) documento().get("components"))
                .get("parameters");
        return ((List<Map<String, Object>>) operacao.get("parameters")).stream().map(parametro -> {
            String referencia = (String) parametro.get("$ref");
            Map<String, Object> real = referencia == null ? parametro
                    : (Map<String, Object>) parametros.get(referencia.substring(referencia.lastIndexOf('/') + 1));
            return (String) real.get("name");
        }).toList();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("T115: MinhaVisitaResponse e o histórico seguem o formato do contrato e não têm resultado de avaliação")
    void schemasDasVisitasDoParticipante() throws Exception {
        Map<String, Object> visita = schema("MinhaVisitaResponse");
        Map<String, Map<String, Object>> campos = (Map<String, Map<String, Object>>) visita.get("properties");

        // RQ-T115-04: os campos exatos, na ordem da convenção
        assertThat(campos.keySet()).containsExactly("id", "participacaoId", "estudoId", "estudoTitulo", "visitaNome",
                "dataHora", "situacao", "dataHoraRealizada", "motivo", "atividades");
        assertThat((List<String>) visita.get("required")).containsExactly("id", "participacaoId", "estudoId",
                "estudoTitulo", "visitaNome", "dataHora", "situacao", "atividades");
        assertThat(campos.get("dataHora").get("format")).isEqualTo("date-time");
        assertThat(campos.get("dataHoraRealizada")).containsEntry("nullable", true).containsEntry("format", "date-time");
        assertThat(campos.get("motivo")).containsEntry("nullable", true);
        assertThat(campos.get("situacao")).containsEntry("$ref", "#/components/schemas/SituacaoVisita");
        assertThat(campos.get("atividades")).containsEntry("type", "array");
        // RQ-T115-05: decisão de privacidade e de escopo: nenhum campo de resultado nem de candidato
        assertThat(campos.keySet()).noneMatch(nome -> nome.toLowerCase().contains("resultado") && !nome.equals("dataHoraRealizada")
                || nome.toLowerCase().contains("candidato"));

        // RQ-T115-06: o resumo é independente da página
        Map<String, Object> resumo = schema("ResumoComparecimentoResponse");
        assertThat(((Map<String, Object>) resumo.get("properties")).keySet())
                .containsExactly("realizadas", "ausentes", "canceladas");
        Map<String, Object> historico = schema("HistoricoMinhasVisitasResponse");
        assertThat(((Map<String, Object>) historico.get("properties")).keySet())
                .containsExactly("resumo", "conteudo", "pagina", "tamanho", "totalElementos", "totalPaginas");
        assertThat((List<String>) historico.get("required"))
                .containsExactly("resumo", "conteudo", "pagina", "tamanho", "totalElementos", "totalPaginas");
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("T115: os exemplos têm visita agendada, realizada, ausente e cancelada com o motivo 'retirada do estudo'")
    void exemplosDasVisitasDoParticipante() throws Exception {
        Map<String, Map<String, Object>> caminhos = (Map<String, Map<String, Object>>) documento().get("paths");
        List<Map<String, Object>> proximas = exemplo(caminhos, "/api/me/visitas/proximas", false);
        Map<String, Object> historico = exemplo(caminhos, "/api/me/visitas/historico", true).get(0);
        List<Map<String, Object>> visitasDoHistorico = (List<Map<String, Object>>) historico.get("conteudo");

        assertThat(proximas).isNotEmpty().allSatisfy(v -> assertThat(v.get("situacao")).isEqualTo("AGENDADA"));
        assertThat(visitasDoHistorico).extracting(v -> v.get("situacao"))
                .containsExactlyInAnyOrder("REALIZADA", "AUSENTE", "CANCELADA");
        assertThat(visitasDoHistorico).filteredOn(v -> "CANCELADA".equals(v.get("situacao")))
                .singleElement().satisfies(v -> assertThat(v.get("motivo")).isEqualTo("retirada do estudo"));
        // o resumo do exemplo bate com o conteúdo do exemplo
        assertThat(historico.get("resumo")).isEqualTo(Map.of("realizadas", 1, "ausentes", 1, "canceladas", 1));
        // dataHora com o deslocamento do fuso (ISO 8601), como a API devolve
        assertThat(proximas.get(0).get("dataHora").toString()).matches(".*[+-]\\d{2}:\\d{2}$");
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> exemplo(Map<String, Map<String, Object>> caminhos, String caminho,
            boolean objeto) {
        Map<String, Object> get = (Map<String, Object>) caminhos.get(caminho).get("get");
        Map<String, Object> conteudo = (Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>)
                ((Map<String, Object>) get.get("responses")).get("200")).get("content")).get("application/json");
        Object exemplo = conteudo.get("example");
        return objeto ? List.of((Map<String, Object>) exemplo) : (List<Map<String, Object>>) exemplo;
    }

    @Test
    @DisplayName("T90: os endpoints dos resultados do contrato existem na API publicada")
    void resultadosDoContratoExistemNaApi() throws Exception {
        Set<String> faltandoNaApi = new TreeSet<>(OPERACOES_RESULTADOS);
        faltandoNaApi.removeAll(operacoesDaApi());

        assertThat(faltandoNaApi).as("operações dos resultados que a API não tem").isEmpty();
    }

    @Test
    @DisplayName("T109: os endpoints /me do contrato existem na API publicada")
    void meDoContratoExistemNaApi() throws Exception {
        Set<String> faltandoNaApi = new TreeSet<>(OPERACOES_ME);
        faltandoNaApi.removeAll(operacoesDaApi());

        assertThat(faltandoNaApi).as("operações /me que a API não tem").isEmpty();
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest(name = "T109: o schema {0} tem os mesmos campos do DTO")
    @ValueSource(strings = {"MeuEstudoResponse", "MeuEstudoDetalheResponse", "RetiradaDoMeuEstudo"})
    void schemaMeTemOsCamposDoDto(String nome) throws Exception {
        Class<?> dto = switch (nome) {
            case "MeuEstudoResponse" -> UEPB.ESWII.ClinicalStudyPB.controller.dto.MeuEstudoResponse.class;
            case "MeuEstudoDetalheResponse" -> UEPB.ESWII.ClinicalStudyPB.controller.dto.MeuEstudoDetalheResponse.class;
            default -> UEPB.ESWII.ClinicalStudyPB.controller.dto.RetiradaDoMeuEstudo.class;
        };
        Set<String> camposDoDto = new TreeSet<>();
        Arrays.stream(dto.getRecordComponents()).forEach(c -> camposDoDto.add(c.getName()));
        Set<String> camposDoContrato = new TreeSet<>(((Map<String, Object>) schema(nome).get("properties")).keySet());

        assertThat(camposDoContrato).as("campos do schema " + nome).isEqualTo(camposDoDto);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("T109: o enum SituacaoParticipacao do contrato tem os mesmos valores do enum do backend")
    void situacaoParticipacaoDoContratoIgualAoDoBackend() throws Exception {
        List<String> valores = (List<String>) schema("SituacaoParticipacao").get("enum");

        assertThat(valores).containsExactly(Arrays.stream(
                UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoParticipacao.values()).map(Enum::name)
                .toArray(String[]::new));
    }

    @Test
    @DisplayName("O endpoint do histórico do contrato existe na API publicada")
    void historicoDoContratoExisteNaApi() throws Exception {
        Set<String> historico = new TreeSet<>(operacoesDoContrato());
        historico.removeIf(operacao -> !operacao.endsWith("/participacoes/{participacaoId}/historico"));
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
            assertThat(respostas).as(metodo + " " + caminho).contains("401", "403");
            // D-T109-01: /api/me/** é só leitura do próprio participante, sem regra de negócio (nada de 409), e a
            // lista não recebe id (nada de 404). O 404 continua exigido em toda operação com parâmetro de caminho.
            boolean doMe = caminho.startsWith("/api/me/");
            if (!doMe || caminho.contains("{")) {
                assertThat(respostas).as(metodo + " " + caminho).contains("404");
            }
            if (!doMe) {
                assertThat(respostas).as(metodo + " " + caminho).contains("409");
            }
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

    @ParameterizedTest(name = "T76: o schema {0} tem os mesmos campos do DTO")
    @ValueSource(strings = {"VisitaDoParticipanteResponse", "VisitaAgendadaRequest", "VisitaReagendamentoRequest",
            "VisitaAgendadaResponse", "AgendaItemResponse", "ParticipanteAgendaResponse"})
    void schemaDoAgendamentoTemOsCamposDoDto(String nome) throws Exception {
        Class<?> dto = switch (nome) {
            case "VisitaDoParticipanteResponse" -> VisitaDoParticipanteResponse.class;
            case "VisitaAgendadaRequest" -> VisitaAgendadaRequest.class;
            case "VisitaReagendamentoRequest" -> VisitaReagendamentoRequest.class;
            case "VisitaAgendadaResponse" -> VisitaAgendadaResponse.class;
            case "AgendaItemResponse" -> AgendaItemResponse.class;
            default -> ParticipanteAgendaResponse.class;
        };
        Set<String> camposDoDto = new TreeSet<>();
        Arrays.stream(dto.getRecordComponents()).forEach(c -> camposDoDto.add(c.getName()));
        Map<String, Object> schema = schema(nome);
        Set<String> camposDoContrato = new TreeSet<>(((Map<String, Object>) schema.get("properties")).keySet());

        assertThat(camposDoContrato).as("campos do schema " + nome).isEqualTo(camposDoDto);
    }

    @Test
    @DisplayName("T76: o Swagger documenta as operações e parâmetros do contrato de agenda")
    void swaggerDocumentaPaginacaoFiltrosETags() throws Exception {
        String json = mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode caminhos = new ObjectMapper().readTree(json).get("paths");
        JsonNode agenda = caminhos.get("/api/estudos/{estudoId}/agenda").get("get");
        Set<String> parametros = new TreeSet<>();
        agenda.get("parameters").forEach(parametro -> parametros.add(parametro.get("name").asText()));
        Set<String> respostas = new TreeSet<>();
        agenda.get("responses").fieldNames().forEachRemaining(respostas::add);

        assertThat(agenda.get("tags").get(0).asText()).isEqualTo("Agenda");
        assertThat(parametros).contains("de", "ate", "situacao", "pagina", "tamanho");
        assertThat(respostas).contains("200", "400", "401", "403", "404", "409");
        assertThat(caminhos.get("/api/participacoes/{participacaoId}/visitas-agendadas")
                .get("post").get("tags").get(0).asText()).isEqualTo("Agendamento");
    }

    @Test
    @DisplayName("T82: o desfecho e a consulta da visita do contrato existem na API publicada")
    void desfechoEConsultaDoContratoExistemNaApi() throws Exception {
        Set<String> faltandoNaApi = new TreeSet<>(OPERACOES_DESFECHO);
        faltandoNaApi.removeAll(operacoesDaApi());

        assertThat(faltandoNaApi).as("operações do desfecho que a API não tem").isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("T80: o enum TipoDesfecho do contrato tem os mesmos valores do enum do backend")
    void tipoDesfechoDoContratoIgualAoDoBackend() throws Exception {
        List<String> valores = (List<String>) schema("TipoDesfecho").get("enum");

        assertThat(valores).containsExactly(Arrays.stream(TipoDesfecho.values()).map(Enum::name).toArray(String[]::new));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("T80: DesfechoRequest exige só o tipo, limita texto a 500 caracteres e traz um exemplo por tipo")
    void desfechoRequestDoContrato() throws Exception {
        Map<String, Object> request = schema("DesfechoRequest");
        Map<String, Map<String, Object>> propriedades = (Map<String, Map<String, Object>>) request.get("properties");

        assertThat(propriedades.keySet()).containsExactlyInAnyOrder("tipo", "dataHoraRealizada", "motivo", "observacao");
        assertThat((List<String>) request.get("required")).containsExactly("tipo");
        assertThat(propriedades.get("motivo").get("maxLength")).isEqualTo(500);
        assertThat(propriedades.get("observacao").get("maxLength")).isEqualTo(500);

        Map<String, Map<String, Object>> caminhos = (Map<String, Map<String, Object>>) documento().get("paths");
        Map<String, Object> post = (Map<String, Object>) caminhos
                .get("/api/visitas-agendadas/{visitaAgendadaId}/desfecho").get("post");
        Map<String, Object> exemplos = (Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>)
                ((Map<String, Object>) post.get("requestBody")).get("content")).get("application/json")).get("examples");
        assertThat(exemplos.keySet()).containsExactlyInAnyOrder("realizada", "ausente", "cancelada");
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("T82: DesfechoRequest tem os mesmos campos do DTO, e os campos obrigatórios do contrato existem")
    void desfechoRequestTemOsCamposDoDto() throws Exception {
        Set<String> camposDoDto = new TreeSet<>();
        Arrays.stream(DesfechoRequest.class.getRecordComponents()).forEach(c -> camposDoDto.add(c.getName()));

        Map<String, Object> schema = schema("DesfechoRequest");
        Set<String> camposDoContrato = new TreeSet<>(((Map<String, Object>) schema.get("properties")).keySet());

        assertThat(camposDoContrato).as("campos do schema DesfechoRequest").isEqualTo(camposDoDto);
        assertThat(camposDoContrato).containsAll((List<String>) schema.get("required"));
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

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("T88: o contrato declara os schemas dos resultados, com bloqueio e resultado anuláveis")
    void contratoDeclaraOsSchemasDosResultados() throws Exception {
        Map<String, Object> resultados = (Map<String, Object>) schema("ResultadosDaVisitaResponse").get("properties");
        Map<String, Object> atividade = (Map<String, Object>) schema("AtividadeComResultado").get("properties");

        assertThat(resultados.keySet()).containsExactlyInAnyOrder("visitaAgendadaId", "situacaoVisita", "bloqueio",
                "total", "registrados", "pendentes", "atividades");
        assertThat(((Map<String, Object>) resultados.get("bloqueio")).get("nullable")).isEqualTo(true);
        assertThat(((Map<String, Object>) atividade.get("resultado")).get("nullable")).isEqualTo(true);
        assertThat((List<String>) schema("BloqueioResultados").get("enum"))
                .containsExactly("VISITA_NAO_REALIZADA", "ESTUDO_ENCERRADO", "PARTICIPANTE_RETIRADO");
        assertThat(((Map<String, Object>) schema("ResultadoAtividadeResponse").get("properties")).keySet())
                .containsExactlyInAnyOrder("acaoId", "valor", "unidade", "observacao", "registradoPor",
                        "registradoEm", "alteradoPor", "alteradoEm");
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("T88: o ResultadoRequest tem os mesmos limites da tabela resultados_atividade (T87)")
    void resultadoRequestTemOsLimitesDaTabela() throws Exception {
        Map<String, Object> request = schema("ResultadoRequest");
        Map<String, Map<String, Object>> campos = (Map<String, Map<String, Object>>) request.get("properties");

        assertThat((List<String>) request.get("required")).containsExactly("valor");
        assertThat(campos.get("valor")).containsEntry("minLength", 1).containsEntry("maxLength", 200);
        assertThat(campos.get("unidade")).containsEntry("maxLength", 30);
        assertThat(campos.get("observacao")).containsEntry("maxLength", 500);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("T88: o registro do resultado responde 201 no primeiro registro e 200 na edição")
    void registroDoResultadoDeclara201E200() throws Exception {
        Map<String, Map<String, Object>> caminhos = (Map<String, Map<String, Object>>) documento().get("paths");
        Map<String, Object> put = (Map<String, Object>) caminhos
                .get("/api/visitas-agendadas/{visitaAgendadaId}/resultados/{acaoId}").get("put");
        Set<String> respostas = ((Map<Object, Object>) put.get("responses")).keySet().stream()
                .map(Object::toString).collect(java.util.stream.Collectors.toSet());

        assertThat(respostas).contains("200", "201");
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest(name = "T90: o schema {0} tem os mesmos campos do DTO do backend")
    @ValueSource(strings = {"ResultadosDaVisitaResponse", "AtividadeComResultado", "ResultadoAtividadeResponse",
            "ResultadoRequest"})
    void schemaDosResultadosTemOsCamposDoDto(String nome) throws Exception {
        Class<?> dto = switch (nome) {
            case "ResultadosDaVisitaResponse" -> ResultadosDaVisitaResponse.class;
            case "AtividadeComResultado" -> AtividadeComResultadoResponse.class;
            case "ResultadoAtividadeResponse" -> ResultadoAtividadeResponse.class;
            default -> ResultadoRequest.class;
        };
        Set<String> camposDoDto = new TreeSet<>();
        Arrays.stream(dto.getRecordComponents()).forEach(c -> camposDoDto.add(c.getName()));

        Set<String> camposDoContrato = new TreeSet<>(((Map<String, Object>) schema(nome).get("properties")).keySet());

        assertThat(camposDoContrato).as("campos do schema " + nome).isEqualTo(camposDoDto);
    }

    @Test
    @DisplayName("T90: o Swagger publica as operações dos resultados com a tag do contrato")
    void swaggerPublicaOsResultadosComATagDoContrato() throws Exception {
        String json = mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode caminhos = new ObjectMapper().readTree(json).get("paths");

        JsonNode consulta = caminhos.get("/api/visitas-agendadas/{visitaAgendadaId}/resultados").get("get");
        JsonNode registro = caminhos.get("/api/visitas-agendadas/{visitaAgendadaId}/resultados/{acaoId}").get("put");
        assertThat(consulta.get("tags").get(0).asText()).isEqualTo("Resultados das avaliações");
        assertThat(registro.get("tags").get(0).asText()).isEqualTo("Resultados das avaliações");
        assertThat(registro.get("responses").has("200")).isTrue();
        assertThat(registro.get("responses").has("201")).isTrue();
    }
}
