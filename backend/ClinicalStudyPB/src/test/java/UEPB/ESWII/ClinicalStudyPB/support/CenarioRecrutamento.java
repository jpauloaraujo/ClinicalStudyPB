package UEPB.ESWII.ClinicalStudyPB.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.jayway.jsonpath.JsonPath;

/**
 * Auxiliares compartilhados pelos testes de integração do recrutamento (T50, T53, T56, T59 e T62): preparam
 * estudo, critérios, visitas, candidatos, avaliação e consentimento pela própria API, com tokens reais.
 * Todos os métodos recebem o {@link MockMvc} e o cabeçalho {@code Authorization} do perfil que age
 * (veja {@link TokenDeTeste#bearer}); o estado do estudo muda só pela transição, nunca pelo {@code PUT}.
 */
public final class CenarioRecrutamento {

    /** Pesquisador do seed V2, usado como responsável dos estudos criados. */
    public static final String PESQUISADOR_ID = "22222222-2222-4222-8222-222222222222";

    private CenarioRecrutamento() {
    }

    public static ResultActions enviar(MockMvc mvc, String metodo, String url, String token, String corpo)
            throws Exception {
        var requisicao = switch (metodo) {
            case "PUT" -> org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(url);
            case "DELETE" -> org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(url);
            case "GET" -> get(url);
            default -> post(url);
        };
        if (token != null) {
            requisicao.header("Authorization", token);
        }
        if (corpo != null) {
            requisicao.contentType(MediaType.APPLICATION_JSON).content(corpo);
        }
        return mvc.perform(requisicao);
    }

    public static String id(ResultActions resposta) throws Exception {
        return JsonPath.read(resposta.andReturn().getResponse().getContentAsString(), "$.id");
    }

    /** Estudo novo em planejamento, com o limite de participantes pedido. */
    public static String criarEstudo(MockMvc mvc, String tokenPesquisador, int limite) throws Exception {
        return id(enviar(mvc, "POST", "/api/estudos", tokenPesquisador, "{\"titulo\":\"Estudo de Teste\","
                + "\"objetivo\":\"Objetivo fictício\",\"responsavelId\":\"" + PESQUISADOR_ID
                + "\",\"limiteParticipantes\":" + limite + ",\"status\":\"PLANEJAMENTO\"}")
                .andExpect(status().isCreated()));
    }

    public static String criarCriterio(MockMvc mvc, String token, String estudoId, String tipo, String titulo,
            String descricao) throws Exception {
        return id(enviar(mvc, "POST", "/api/estudos/" + estudoId + "/criterios", token,
                "{\"tipo\":\"" + tipo + "\",\"titulo\":\"" + titulo + "\",\"descricao\":\"" + descricao + "\"}")
                .andExpect(status().isCreated()));
    }

    public static String criarVisita(MockMvc mvc, String token, String estudoId, String nome, int dia)
            throws Exception {
        return id(enviar(mvc, "POST", "/api/estudos/" + estudoId + "/visitas", token,
                "{\"nome\":\"" + nome + "\",\"diaPrevisto\":" + dia + ",\"tolerancia\":0}")
                .andExpect(status().isCreated()));
    }

    public static ResultActions avancar(MockMvc mvc, String token, String estudoId, String destino) throws Exception {
        return enviar(mvc, "POST", "/api/estudos/" + estudoId + "/transicoes", token,
                "{\"destino\":\"" + destino + "\"}");
    }

    /**
     * Estudo em recrutamento com um critério de inclusão ("Idade adulta"), um de exclusão ("Gestante") e uma
     * visita no dia 0. Os ids dos critérios podem ser lidos depois pela listagem de critérios.
     */
    public static String estudoEmRecrutamento(MockMvc mvc, String tokenPesquisador, int limite) throws Exception {
        String estudo = criarEstudo(mvc, tokenPesquisador, limite);
        criarCriterio(mvc, tokenPesquisador, estudo, "INCLUSAO", "Idade adulta", "Maior de 18 anos");
        criarCriterio(mvc, tokenPesquisador, estudo, "EXCLUSAO", "Gestante", "Em gestação");
        criarVisita(mvc, tokenPesquisador, estudo, "Triagem", 0);
        avancar(mvc, tokenPesquisador, estudo, "RECRUTAMENTO").andExpect(status().isOk());
        return estudo;
    }

    public static String criarCandidato(MockMvc mvc, String tokenCoordenador, String estudoId, String nome)
            throws Exception {
        return id(enviar(mvc, "POST", "/api/estudos/" + estudoId + "/candidatos", tokenCoordenador,
                "{\"nomeFicticio\":\"" + nome + "\",\"dataNascimento\":\"1990-04-12\"}")
                .andExpect(status().isCreated()));
    }

    /** Avalia o candidato como aprovado: inclusões "se aplica" e exclusões "não se aplica". */
    public static void avaliarAprovado(MockMvc mvc, String tokenCoordenador, String candidatoId, String estudoId)
            throws Exception {
        String lista = enviar(mvc, "GET", "/api/estudos/" + estudoId + "/criterios", tokenCoordenador, null)
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        int total = JsonPath.<java.util.List<Object>>read(lista, "$").size();
        StringBuilder respostas = new StringBuilder();
        for (int i = 0; i < total; i++) {
            String criterioId = JsonPath.read(lista, "$[" + i + "].id");
            String tipo = JsonPath.read(lista, "$[" + i + "].tipo");
            if (i > 0) {
                respostas.append(',');
            }
            respostas.append("{\"criterioId\":\"").append(criterioId).append("\",\"aplica\":")
                    .append("INCLUSAO".equals(tipo)).append('}');
        }
        enviar(mvc, "POST", "/api/candidatos/" + candidatoId + "/avaliacoes", tokenCoordenador,
                "{\"respostas\":[" + respostas + "]}").andExpect(status().isCreated());
    }

    public static ResultActions consentir(MockMvc mvc, String tokenCoordenador, String candidatoId) throws Exception {
        return enviar(mvc, "POST", "/api/candidatos/" + candidatoId + "/consentimento", tokenCoordenador, "{}");
    }

    /** Registra o desfecho de uma visita agendada (T81/T82): {@code POST /api/visitas-agendadas/{id}/desfecho}. */
    public static ResultActions registrarDesfecho(MockMvc mvc, String token, String visitaAgendadaId, String corpo)
            throws Exception {
        return enviar(mvc, "POST", "/api/visitas-agendadas/" + visitaAgendadaId + "/desfecho", token, corpo);
    }

    /**
     * Cadastra uma atividade (ação) na visita prevista e devolve o id. O estudo precisa estar em planejamento, então
     * chame antes de avançar para o recrutamento.
     */
    public static String criarAcao(MockMvc mvc, String tokenPesquisador, String visitaPlanejadaId, String nome,
            String categoria) throws Exception {
        return id(enviar(mvc, "POST", "/api/visitas/" + visitaPlanejadaId + "/acoes", tokenPesquisador,
                "{\"nome\":\"" + nome + "\",\"descricao\":\"Atividade fictícia de teste\",\"categoria\":\"" + categoria
                        + "\"}")
                .andExpect(status().isCreated()));
    }

    /** T92 (RF14): atividade da categoria {@code AVALIACAO_CLINICA}, a única que recebe resultado. */
    public static String acaoDeAvaliacao(MockMvc mvc, String tokenPesquisador, String visitaPlanejadaId, String nome)
            throws Exception {
        return criarAcao(mvc, tokenPesquisador, visitaPlanejadaId, nome, "AVALIACAO_CLINICA");
    }

    /** Inclui um candidato novo no estudo (avaliado como aprovado e com consentimento) e devolve o id da participação. */
    public static String incluirParticipante(MockMvc mvc, String tokenCoordenador, String estudoId, String nome)
            throws Exception {
        String candidato = criarCandidato(mvc, tokenCoordenador, estudoId, nome);
        avaliarAprovado(mvc, tokenCoordenador, candidato, estudoId);
        consentir(mvc, tokenCoordenador, candidato).andExpect(status().isCreated());
        return id(enviar(mvc, "POST", "/api/estudos/" + estudoId + "/participantes", tokenCoordenador,
                "{\"candidatoId\":\"" + candidato + "\"}").andExpect(status().isCreated()));
    }

    /**
     * Agenda pela API (T76) a visita prevista da participação para o dia informado, à meia-noite, e devolve o id da
     * visita agendada ({@code AGENDADA}). O dia precisa estar na janela da visita prevista e não pode ser passado.
     */
    public static String agendarVisita(MockMvc mvc, String tokenCoordenador, String participacaoId,
            String visitaPlanejadaId, LocalDate dia) throws Exception {
        return id(enviar(mvc, "POST", "/api/participacoes/" + participacaoId + "/visitas-agendadas", tokenCoordenador,
                "{\"visitaPlanejadaId\":\"" + visitaPlanejadaId + "\",\"dataHora\":\"" + dia + "T00:00\"}")
                .andExpect(status().isCreated()));
    }

    /**
     * T92 (RF14): visita {@code REALIZADA} montada só pela API. Agenda a visita prevista para {@code hoje} (o dia do
     * {@code Clock} da aplicação, nunca uma data fixa) e registra o desfecho realizado (T82) sem data, que assume
     * agora. Exige uma visita prevista do dia 0 com tolerância 0 e participação incluída hoje.
     */
    public static String visitaRealizada(MockMvc mvc, String tokenCoordenador, String participacaoId,
            String visitaPlanejadaId, LocalDate hoje) throws Exception {
        String visitaAgendada = agendarVisita(mvc, tokenCoordenador, participacaoId, visitaPlanejadaId, hoje);
        registrarDesfecho(mvc, tokenCoordenador, visitaAgendada, "{\"tipo\":\"REALIZADA\"}")
                .andExpect(status().isOk());
        return visitaAgendada;
    }
}
