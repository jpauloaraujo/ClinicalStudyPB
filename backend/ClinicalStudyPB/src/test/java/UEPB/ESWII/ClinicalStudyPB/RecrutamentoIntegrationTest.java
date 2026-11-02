package UEPB.ESWII.ClinicalStudyPB;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;

/**
 * Fluxo de recrutamento (RF05 a RF11) pela API, com tokens reais: o pesquisador prepara o estudo
 * em planejamento e o coordenador cadastra, avalia, colhe o consentimento e inclui candidatos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RecrutamentoIntegrationTest {

    private static final String PESQUISADOR_ID = "22222222-2222-4222-8222-222222222222";
    private static final String NASCIMENTO = "1990-04-12";
    private static final String NOME_COORDENADOR = "Carlos Eduardo — Coordenador (Fictício)";

    @Autowired
    private MockMvc mockMvc;

    private String pesquisador;
    private String coordenador;
    private String administrador;

    @BeforeEach
    void entrar() throws Exception {
        pesquisador = TokenDeTeste.bearer(mockMvc, TokenDeTeste.PESQUISADOR);
        coordenador = TokenDeTeste.bearer(mockMvc, TokenDeTeste.COORDENADOR);
        administrador = TokenDeTeste.bearer(mockMvc, TokenDeTeste.ADMIN);
    }

    // ---------- auxiliares ----------

    private ResultActions enviar(String metodo, String url, String token, String corpo) throws Exception {
        var requisicao = switch (metodo) {
            case "POST" -> post(url);
            case "PUT" -> put(url);
            case "DELETE" -> delete(url);
            default -> get(url);
        };
        requisicao.header("Authorization", token);
        if (corpo != null) {
            requisicao.contentType(MediaType.APPLICATION_JSON).content(corpo);
        }
        return mockMvc.perform(requisicao);
    }

    private String id(ResultActions resposta) throws Exception {
        return JsonPath.read(resposta.andReturn().getResponse().getContentAsString(), "$.id");
    }

    private static String corpoEstudo(int limite, String status) {
        return "{\"titulo\":\"Estudo de Recrutamento\",\"objetivo\":\"Objetivo fictício\",\"responsavelId\":\""
                + PESQUISADOR_ID + "\",\"limiteParticipantes\":" + limite + ",\"status\":\"" + status + "\"}";
    }

    private String criarEstudo(int limite) throws Exception {
        return id(enviar("POST", "/api/estudos", pesquisador, corpoEstudo(limite, "PLANEJAMENTO"))
                .andExpect(status().isCreated()));
    }

    private void mudarEstado(String estudoId, int limite, String estado) throws Exception {
        enviar("PUT", "/api/estudos/" + estudoId, pesquisador, corpoEstudo(limite, estado)).andExpect(status().isOk());
    }

    private String criarCriterio(String estudoId, String tipo, String titulo, String extra) throws Exception {
        return id(enviar("POST", "/api/estudos/" + estudoId + "/criterios", pesquisador,
                "{\"tipo\":\"" + tipo + "\",\"titulo\":\"" + titulo + "\",\"descricao\":\"Descrição do critério\""
                        + extra + "}").andExpect(status().isCreated()));
    }

    private String criarCandidato(String estudoId, String nome) throws Exception {
        return id(enviar("POST", "/api/estudos/" + estudoId + "/candidatos", coordenador,
                "{\"nomeFicticio\":\"" + nome + "\",\"dataNascimento\":\"" + NASCIMENTO + "\"}")
                .andExpect(status().isCreated()));
    }

    private ResultActions avaliar(String candidatoId, String criterioInclusao, boolean seAplicaInclusao,
            String criterioExclusao, boolean seAplicaExclusao) throws Exception {
        return enviar("POST", "/api/candidatos/" + candidatoId + "/avaliacoes", coordenador,
                "{\"respostas\":[{\"criterioId\":\"" + criterioInclusao + "\",\"aplica\":" + seAplicaInclusao
                        + ",\"observacao\":\"ok\"},{\"criterioId\":\"" + criterioExclusao + "\",\"aplica\":"
                        + seAplicaExclusao + "}]}");
    }

    private void consentir(String candidatoId) throws Exception {
        enviar("POST", "/api/candidatos/" + candidatoId + "/consentimento", coordenador, "{}")
                .andExpect(status().isCreated());
    }

    private ResultActions incluir(String estudoId, String candidatoId) throws Exception {
        return enviar("POST", "/api/estudos/" + estudoId + "/participantes", coordenador,
                "{\"candidatoId\":\"" + candidatoId + "\"}");
    }

    /** Estudo em recrutamento com um critério de inclusão e um de exclusão; devolve {estudoId, inclusaoId, exclusaoId}. */
    private String[] estudoEmRecrutamento(int limite) throws Exception {
        String estudo = criarEstudo(limite);
        String inclusao = criarCriterio(estudo, "INCLUSAO", "Idade adulta", "");
        String exclusao = criarCriterio(estudo, "EXCLUSAO", "Gestante", "");
        enviar("POST", "/api/estudos/" + estudo + "/visitas", pesquisador,
                "{\"nome\":\"Triagem\",\"diaPrevisto\":0,\"tolerancia\":0}").andExpect(status().isCreated());
        mudarEstado(estudo, limite, "RECRUTAMENTO");
        return new String[] {estudo, inclusao, exclusao};
    }

    /** Candidato elegível e com consentimento, pronto para ser incluído. */
    private String candidatoPronto(String[] estudo, String nome) throws Exception {
        String candidato = criarCandidato(estudo[0], nome);
        avaliar(candidato, estudo[1], true, estudo[2], false).andExpect(status().isCreated());
        consentir(candidato);
        return candidato;
    }

    // ---------- RF05 e RF06: critérios e visitas ----------

    @Test
    @DisplayName("Critérios: cria inclusão (padrões) e exclusão, lista e protege contra duplicidade e mudança de tipo")
    void criteriosDoEstudo() throws Exception {
        String estudo = criarEstudo(5);
        String url = "/api/estudos/" + estudo + "/criterios";

        String inclusao = criarCriterio(estudo, "INCLUSAO", "Idade adulta", "");
        enviar("GET", url, coordenador, null).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipo").value("INCLUSAO"))
                .andExpect(jsonPath("$[0].peso").value(1.0))
                .andExpect(jsonPath("$[0].determinante").value(true));

        enviar("POST", url, pesquisador,
                "{\"tipo\":\"EXCLUSAO\",\"titulo\":\"Gestante\",\"descricao\":\"Está grávida\",\"peso\":9}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.peso").doesNotExist())
                .andExpect(jsonPath("$.determinante").doesNotExist());

        enviar("POST", url, pesquisador,
                "{\"tipo\":\"INCLUSAO\",\"titulo\":\"IDADE ADULTA\",\"descricao\":\"Outra descrição\"}")
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CRITERIO_DUPLICADO"));
        enviar("POST", url, pesquisador, "{\"tipo\":\"INCLUSAO\",\"titulo\":\"X\",\"descricao\":\"abc\"}")
                .andExpect(status().isBadRequest());
        enviar("PUT", url + "/" + inclusao, pesquisador,
                "{\"tipo\":\"EXCLUSAO\",\"titulo\":\"Idade adulta\",\"descricao\":\"Descrição do critério\"}")
                .andExpect(status().isBadRequest());
        enviar("PUT", url + "/" + inclusao, pesquisador,
                "{\"tipo\":\"INCLUSAO\",\"titulo\":\"Idade adulta\",\"descricao\":\"Nova descrição\",\"peso\":2.5,\"determinante\":false}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.peso").value(2.5))
                .andExpect(jsonPath("$.determinante").value(false));
        enviar("DELETE", url + "/" + inclusao, pesquisador, null).andExpect(status().isNoContent());
        enviar("GET", url, pesquisador, null).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("Visitas: ordem derivada do dia, dia repetido recusado, ações e remoção em cascata")
    void visitasEAcoes() throws Exception {
        String estudo = criarEstudo(5);
        String url = "/api/estudos/" + estudo + "/visitas";

        enviar("POST", url, pesquisador, "{\"nome\":\"Retorno Mensal\",\"diaPrevisto\":30,\"tolerancia\":5}")
                .andExpect(status().isCreated()).andExpect(jsonPath("$.ordem").value(1));
        String inicial = id(enviar("POST", url, pesquisador, "{\"nome\":\"Inicial\",\"diaPrevisto\":0,\"tolerancia\":0}")
                .andExpect(status().isCreated()).andExpect(jsonPath("$.ordem").value(1)));

        enviar("GET", url, coordenador, null).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Inicial")).andExpect(jsonPath("$[0].ordem").value(1))
                .andExpect(jsonPath("$[1].nome").value("Retorno Mensal")).andExpect(jsonPath("$[1].ordem").value(2));

        enviar("POST", url, pesquisador, "{\"nome\":\"Repetida\",\"diaPrevisto\":30,\"tolerancia\":1}")
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("VISITA_DIA_DUPLICADO"));
        enviar("POST", url, pesquisador, "{\"nome\":\"Negativa\",\"diaPrevisto\":-1,\"tolerancia\":1}")
                .andExpect(status().isBadRequest());
        enviar("POST", url, pesquisador, "{\"nome\":\"Janela\",\"diaPrevisto\":10,\"tolerancia\":31}")
                .andExpect(status().isBadRequest());

        enviar("POST", "/api/visitas/" + inicial + "/acoes", pesquisador,
                "{\"nome\":\"Coleta de sangue\",\"categoria\":\"EXAME_LABORATORIAL\"}")
                .andExpect(status().isCreated()).andExpect(jsonPath("$.categoria").value("EXAME_LABORATORIAL"));
        enviar("POST", "/api/visitas/" + inicial + "/acoes", pesquisador,
                "{\"nome\":\"Inválida\",\"categoria\":\"INEXISTENTE\"}").andExpect(status().isBadRequest());
        enviar("GET", url, pesquisador, null).andExpect(jsonPath("$[0].acoes", hasSize(1)));

        enviar("DELETE", url + "/" + inicial, pesquisador, null).andExpect(status().isNoContent());
        enviar("GET", url, pesquisador, null).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ordem").value(1));
    }

    @Test
    @DisplayName("Critérios e visitas ficam somente leitura depois que o estudo sai de planejamento")
    void criteriosEVisitasSoEmPlanejamento() throws Exception {
        String[] estudo = estudoEmRecrutamento(5);

        enviar("POST", "/api/estudos/" + estudo[0] + "/criterios", pesquisador,
                "{\"tipo\":\"INCLUSAO\",\"titulo\":\"Novo\",\"descricao\":\"Descrição nova\"}")
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("ESTUDO_FORA_DE_PLANEJAMENTO"));
        enviar("DELETE", "/api/estudos/" + estudo[0] + "/criterios/" + estudo[1], pesquisador, null)
                .andExpect(status().isConflict());
        enviar("POST", "/api/estudos/" + estudo[0] + "/visitas", pesquisador,
                "{\"nome\":\"Nova\",\"diaPrevisto\":7,\"tolerancia\":1}").andExpect(status().isConflict());
    }

    // ---------- RF08 a RF11: fluxo de recrutamento ----------

    @Test
    @DisplayName("Fluxo completo: cadastra, reprova, reavalia, consente e inclui, com histórico e vagas")
    void fluxoCompleto() throws Exception {
        String[] estudo = estudoEmRecrutamento(2);

        String primeiro = criarCandidato(estudo[0], "J. S.");
        enviar("GET", "/api/candidatos/" + primeiro, coordenador, null)
                .andExpect(jsonPath("$.codigo").value("CAND-001"))
                .andExpect(jsonPath("$.situacao").value("CADASTRADO"))
                .andExpect(jsonPath("$.consentimentoRegistrado").value(false));
        enviar("POST", "/api/estudos/" + estudo[0] + "/candidatos", coordenador,
                "{\"nomeFicticio\":\"j. s.\",\"dataNascimento\":\"" + NASCIMENTO + "\"}")
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CANDIDATO_DUPLICADO"));
        enviar("POST", "/api/estudos/" + estudo[0] + "/candidatos", coordenador,
                "{\"nomeFicticio\":\"M. L.\",\"dataNascimento\":\"2999-01-01\"}").andExpect(status().isBadRequest());
        enviar("GET", "/api/candidatos/" + criarCandidato(estudo[0], "M. L."), coordenador, null)
                .andExpect(jsonPath("$.codigo").value("CAND-002"));

        // avaliação incompleta é recusada
        enviar("POST", "/api/candidatos/" + primeiro + "/avaliacoes", coordenador,
                "{\"respostas\":[{\"criterioId\":\"" + estudo[1] + "\",\"aplica\":true}]}")
                .andExpect(status().isBadRequest());

        // reprovado: critério de exclusão se aplica
        avaliar(primeiro, estudo[1], true, estudo[2], true).andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultado").value("REPROVADO"))
                .andExpect(jsonPath("$.criteriosReprovadores", hasSize(1)))
                .andExpect(jsonPath("$.criteriosReprovadores[0].titulo").value("Gestante"));
        enviar("GET", "/api/candidatos/" + primeiro, coordenador, null)
                .andExpect(jsonPath("$.situacao").value("NAO_ELEGIVEL"));
        incluir(estudo[0], primeiro).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CANDIDATO_NAO_ELEGIVEL"));

        // reavaliação aprovada passa a valer e a anterior vai para o histórico
        avaliar(primeiro, estudo[1], true, estudo[2], false).andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultado").value("APROVADO"))
                .andExpect(jsonPath("$.vigente").value(true));
        enviar("GET", "/api/candidatos/" + primeiro + "/avaliacoes", pesquisador, null)
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].vigente").value(true))
                .andExpect(jsonPath("$[1].vigente").value(false))
                .andExpect(jsonPath("$[1].resultado").value("REPROVADO"));
        enviar("GET", "/api/candidatos/" + primeiro, coordenador, null)
                .andExpect(jsonPath("$.situacao").value("ELEGIVEL"))
                .andExpect(jsonPath("$.resultadoAvaliacao").value("APROVADO"));

        // sem consentimento não inclui; com consentimento (um só, não futuro) inclui
        incluir(estudo[0], primeiro).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONSENTIMENTO_AUSENTE"));
        enviar("GET", "/api/candidatos/" + primeiro + "/consentimento", coordenador, null)
                .andExpect(status().isNotFound());
        enviar("POST", "/api/candidatos/" + primeiro + "/consentimento", coordenador,
                "{\"dataHora\":\"" + LocalDateTime.now().plusDays(1) + "\"}").andExpect(status().isBadRequest());
        enviar("POST", "/api/candidatos/" + primeiro + "/consentimento", coordenador, null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.versaoTermo").value("TCLE-SIMULADO v1"))
                .andExpect(jsonPath("$.responsavel").value(NOME_COORDENADOR));
        enviar("POST", "/api/candidatos/" + primeiro + "/consentimento", coordenador, "{}")
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CONSENTIMENTO_DUPLICADO"));
        enviar("GET", "/api/candidatos/" + primeiro, coordenador, null)
                .andExpect(jsonPath("$.situacao").value("CONSENTIDO"))
                .andExpect(jsonPath("$.consentimentoRegistrado").value(true));

        incluir(estudo[0], primeiro).andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("CAND-001"))
                .andExpect(jsonPath("$.incluidoPor").value(NOME_COORDENADOR));
        enviar("GET", "/api/candidatos/" + primeiro, coordenador, null)
                .andExpect(jsonPath("$.situacao").value("INCLUIDO"));
        enviar("GET", "/api/estudos/" + estudo[0] + "/participantes/vagas", pesquisador, null)
                .andExpect(jsonPath("$.incluidos").value(1)).andExpect(jsonPath("$.limite").value(2))
                .andExpect(jsonPath("$.restantes").value(1));
        enviar("GET", "/api/estudos/" + estudo[0] + "/participantes", pesquisador, null)
                .andExpect(jsonPath("$", hasSize(1)));

        incluir(estudo[0], primeiro).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CANDIDATO_JA_INCLUIDO"));
        enviar("PUT", "/api/candidatos/" + primeiro, coordenador,
                "{\"nomeFicticio\":\"Outro\",\"dataNascimento\":\"" + NASCIMENTO + "\"}")
                .andExpect(status().isConflict());
        avaliar(primeiro, estudo[1], true, estudo[2], false).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Consentimento antes da avaliação também libera a inclusão (a ordem não importa)")
    void consentimentoPodeVirAntesDaAvaliacao() throws Exception {
        String[] estudo = estudoEmRecrutamento(1);
        String candidato = criarCandidato(estudo[0], "A. B.");

        consentir(candidato);
        enviar("GET", "/api/candidatos/" + candidato, coordenador, null)
                .andExpect(jsonPath("$.situacao").value("CADASTRADO"));
        avaliar(candidato, estudo[1], true, estudo[2], false).andExpect(status().isCreated());
        enviar("GET", "/api/candidatos/" + candidato, coordenador, null)
                .andExpect(jsonPath("$.situacao").value("CONSENTIDO"));
        incluir(estudo[0], candidato).andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Vagas esgotadas: a segunda inclusão é recusada e o total não passa do limite")
    void vagasEsgotadas() throws Exception {
        String[] estudo = estudoEmRecrutamento(1);
        String primeiro = candidatoPronto(estudo, "C. D.");
        String segundo = candidatoPronto(estudo, "E. F.");

        incluir(estudo[0], primeiro).andExpect(status().isCreated());
        incluir(estudo[0], segundo).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("VAGAS_ESGOTADAS"));

        enviar("GET", "/api/estudos/" + estudo[0] + "/participantes/vagas", coordenador, null)
                .andExpect(jsonPath("$.incluidos").value(1)).andExpect(jsonPath("$.restantes").value(0));
        enviar("GET", "/api/estudos/" + estudo[0] + "/participantes", coordenador, null)
                .andExpect(jsonPath("$", hasSize(1)));
        enviar("GET", "/api/candidatos/" + segundo, coordenador, null)
                .andExpect(jsonPath("$.situacao").value("CONSENTIDO"));
    }

    @Test
    @DisplayName("Candidatos só existem com o estudo em recrutamento, e um candidato de outro estudo não é incluído")
    void estadoDoEstudoEVinculoDoCandidato() throws Exception {
        String planejamento = criarEstudo(3);
        enviar("POST", "/api/estudos/" + planejamento + "/candidatos", coordenador,
                "{\"nomeFicticio\":\"G. H.\",\"dataNascimento\":\"" + NASCIMENTO + "\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESTUDO_FORA_DE_RECRUTAMENTO"));

        String[] um = estudoEmRecrutamento(2);
        String[] outro = estudoEmRecrutamento(2);
        String candidato = candidatoPronto(um, "I. J.");
        incluir(outro[0], candidato).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Lista candidatos com paginação e filtro por situação")
    void listaPaginadaComFiltro() throws Exception {
        String[] estudo = estudoEmRecrutamento(5);
        candidatoPronto(estudo, "K. L.");
        criarCandidato(estudo[0], "M. N.");
        criarCandidato(estudo[0], "O. P.");

        String url = "/api/estudos/" + estudo[0] + "/candidatos";
        enviar("GET", url, coordenador, null).andExpect(jsonPath("$.totalElementos").value(3))
                .andExpect(jsonPath("$.conteudo[0].codigo").value("CAND-001"));
        enviar("GET", url + "?tamanho=2&pagina=1", pesquisador, null)
                .andExpect(jsonPath("$.conteudo", hasSize(1))).andExpect(jsonPath("$.totalPaginas").value(2));
        enviar("GET", url + "?situacao=CONSENTIDO", coordenador, null)
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.conteudo[0].nomeFicticio").value("K. L."));
        enviar("GET", url + "?situacao=CADASTRADO", coordenador, null)
                .andExpect(jsonPath("$.totalElementos").value(2));
        enviar("GET", url + "?situacao=INVALIDA", coordenador, null).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Estudo com candidatos não pode ser excluído")
    void estudoComCandidatosNaoEExcluido() throws Exception {
        String[] estudo = estudoEmRecrutamento(2);
        criarCandidato(estudo[0], "Q. R.");

        enviar("DELETE", "/api/estudos/" + estudo[0], pesquisador, null).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESTUDO_COM_CANDIDATOS"));
    }

    @Test
    @DisplayName("Critério já usado em avaliação não pode ser excluído")
    void criterioEmUsoNaoEExcluido() throws Exception {
        String[] estudo = estudoEmRecrutamento(2);
        String candidato = criarCandidato(estudo[0], "S. T.");
        avaliar(candidato, estudo[1], true, estudo[2], false).andExpect(status().isCreated());

        mudarEstado(estudo[0], 2, "PLANEJAMENTO");
        enviar("DELETE", "/api/estudos/" + estudo[0] + "/criterios/" + estudo[1], pesquisador, null)
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CRITERIO_EM_USO"));
    }

    // ---------- RF03: permissões ----------

    @Test
    @DisplayName("Permissões: pesquisador escreve planejamento, coordenador escreve recrutamento, administrador fica de fora")
    void permissoesPorPerfil() throws Exception {
        String[] estudo = estudoEmRecrutamento(2);
        String candidato = candidatoPronto(estudo, "U. V.");
        String criterio = "{\"tipo\":\"INCLUSAO\",\"titulo\":\"X\",\"descricao\":\"Descrição X\"}";
        String visita = "{\"nome\":\"X\",\"diaPrevisto\":99,\"tolerancia\":1}";
        String cand = "{\"nomeFicticio\":\"Z. Z.\",\"dataNascimento\":\"" + NASCIMENTO + "\"}";

        // coordenador lê, mas não escreve critérios e visitas
        enviar("GET", "/api/estudos/" + estudo[0] + "/criterios", coordenador, null).andExpect(status().isOk());
        enviar("POST", "/api/estudos/" + estudo[0] + "/criterios", coordenador, criterio).andExpect(status().isForbidden());
        enviar("POST", "/api/estudos/" + estudo[0] + "/visitas", coordenador, visita).andExpect(status().isForbidden());

        // pesquisador lê candidatos, mas não escreve recrutamento
        enviar("GET", "/api/candidatos/" + candidato, pesquisador, null).andExpect(status().isOk());
        enviar("POST", "/api/estudos/" + estudo[0] + "/candidatos", pesquisador, cand).andExpect(status().isForbidden());
        enviar("POST", "/api/candidatos/" + candidato + "/consentimento", pesquisador, "{}").andExpect(status().isForbidden());
        enviar("POST", "/api/estudos/" + estudo[0] + "/participantes", pesquisador,
                "{\"candidatoId\":\"" + candidato + "\"}").andExpect(status().isForbidden());

        // administrador não acessa nada do recrutamento
        for (String url : new String[] {"/api/estudos/" + estudo[0] + "/criterios",
                "/api/estudos/" + estudo[0] + "/visitas", "/api/estudos/" + estudo[0] + "/candidatos",
                "/api/candidatos/" + candidato, "/api/candidatos/" + candidato + "/avaliacoes",
                "/api/estudos/" + estudo[0] + "/participantes"}) {
            enviar("GET", url, administrador, null).andExpect(status().isForbidden());
        }
        mockMvc.perform(get("/api/estudos/" + estudo[0] + "/candidatos")).andExpect(status().isUnauthorized());
    }
}
