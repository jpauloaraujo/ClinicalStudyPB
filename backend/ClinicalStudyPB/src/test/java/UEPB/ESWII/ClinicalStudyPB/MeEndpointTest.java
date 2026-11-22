package UEPB.ESWII.ClinicalStudyPB;

import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.enviar;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;

/**
 * T109: teste de fumaça de {@code /api/me/estudos} e {@code /api/me/estudos/{estudoId}} com o banco e tokens reais:
 * só os estudos da própria conta, 404 para estudo de outro participante, lista vazia sem vínculo, 401/403 por perfil,
 * resumo cortado e nenhum dado de candidato na resposta. Os testes completos são da T113.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MeEndpointTest {

    private static final String COORDENADOR_ID = "22222222-2222-4222-8222-222222222222";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;

    private String participante;
    private UUID participanteId;

    @BeforeEach
    void entrar() throws Exception {
        participante = TokenDeTeste.bearer(mvc, TokenDeTeste.PARTICIPANTE);
        participanteId = usuarioId(TokenDeTeste.PARTICIPANTE);
    }

    // ---- montagem do cenário, direto no banco ----

    private UUID usuarioId(String email) {
        return UUID.fromString(jdbc.queryForObject("SELECT CAST(id AS VARCHAR) FROM usuarios WHERE email = ?",
                String.class, email));
    }

    private UUID criarOutraConta() {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO usuarios (id, nome, email, senha, perfil, ativo) VALUES (CAST(? AS UUID), "
                + "'Outro Participante (Ficticio)', ?, 'x', 'PARTICIPANTE', TRUE)", id.toString(),
                "outro." + id + "@clinicalstudy.test");
        return id;
    }

    private UUID criarEstudo(String titulo, String objetivo, String status) {
        UUID estudo = UUID.randomUUID();
        jdbc.update("INSERT INTO estudos (id, titulo, objetivo, responsavel_id, data_inicio, data_fim, "
                + "limite_participantes, status) SELECT CAST(? AS UUID), ?, ?, id, DATE '2026-11-01', "
                + "DATE '2027-05-01', 6, ? FROM usuarios WHERE email = 'pesquisador@clinicalstudy.test'",
                estudo.toString(), titulo, objetivo, status);
        return estudo;
    }

    /** Inclui um candidato no estudo, vinculado à conta (ou sem conta, se {@code usuario} for nulo). */
    private UUID incluir(UUID estudo, UUID usuario, String codigo, int diasAtras) {
        UUID candidato = UUID.randomUUID();
        UUID participacao = UUID.randomUUID();
        jdbc.update("INSERT INTO candidatos (id, estudo_id, codigo, nome_ficticio, data_nascimento, usuario_id, "
                + "situacao) VALUES (CAST(? AS UUID), CAST(? AS UUID), ?, 'Pessoa Sigilosa (Ficticia)', "
                + "DATE '1990-01-01', CAST(? AS UUID), 'INCLUIDO')", candidato.toString(), estudo.toString(), codigo,
                usuario == null ? null : usuario.toString());
        jdbc.update("INSERT INTO participacoes (id, candidato_id, estudo_id, data_inclusao, incluido_por_id) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), CAST(? AS UUID), CURRENT_DATE - INTERVAL '" + diasAtras
                + "' DAY, CAST(? AS UUID))", participacao.toString(), candidato.toString(), estudo.toString(),
                COORDENADOR_ID);
        return participacao;
    }

    // ---- lista ----

    @Test
    @DisplayName("200: lista só os estudos da conta, do mais recente para o mais antigo, sem dados de candidato")
    void listaSoOsEstudosDaConta() throws Exception {
        UUID antigo = criarEstudo("Estudo antigo T109", "Objetivo antigo.", "EXECUCAO");
        UUID recente = criarEstudo("Estudo recente T109", "Objetivo recente.", "RECRUTAMENTO");
        UUID deOutro = criarEstudo("Estudo de outro T109", "Objetivo de outro.", "EXECUCAO");
        UUID participacaoAntiga = incluir(antigo, participanteId, "CAND-901", 30);
        UUID participacaoRecente = incluir(recente, participanteId, "CAND-902", 2);
        incluir(deOutro, criarOutraConta(), "CAND-903", 5);

        enviar(mvc, "GET", "/api/me/estudos", participante, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].participacaoId").value(participacaoRecente.toString()))
                .andExpect(jsonPath("$[0].estudoId").value(recente.toString()))
                .andExpect(jsonPath("$[0].titulo").value("Estudo recente T109"))
                .andExpect(jsonPath("$[0].resumo").value("Objetivo recente."))
                .andExpect(jsonPath("$[0].statusEstudo").value("RECRUTAMENTO"))
                .andExpect(jsonPath("$[0].dataInclusao").isNotEmpty())
                .andExpect(jsonPath("$[0].situacaoParticipacao").value("ATIVO"))
                .andExpect(jsonPath("$[1].participacaoId").value(participacaoAntiga.toString()))
                .andExpect(jsonPath("$[1].statusEstudo").value("EXECUCAO"))
                .andExpect(jsonPath("$[0].codigo").doesNotExist())
                .andExpect(jsonPath("$[0].nomeFicticio").doesNotExist())
                .andExpect(jsonPath("$[0].candidatoId").doesNotExist());
    }

    @Test
    @DisplayName("RQ-05: conta sem candidato vinculado recebe 200 com lista vazia")
    void contaSemVinculoRecebeListaVazia() throws Exception {
        enviar(mvc, "GET", "/api/me/estudos", participante, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("O id do participante não é aceito por parâmetro: ?participanteId de outra conta é ignorado")
    void idPorParametroEhIgnorado() throws Exception {
        UUID outra = criarOutraConta();
        incluir(criarEstudo("Estudo de outro T109", "Objetivo.", "EXECUCAO"), outra, "CAND-904", 3);

        enviar(mvc, "GET", "/api/me/estudos?participanteId=" + outra + "&usuarioId=" + outra, participante, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ---- detalhe ----

    @Test
    @DisplayName("200: detalhe com objetivo completo, resumo cortado em 160, datas, responsável e retirada nula")
    void detalheDoEstudoDaConta() throws Exception {
        String objetivo = "Objetivo longo. ".repeat(20);
        UUID estudo = criarEstudo("Estudo detalhado T109", objetivo, "EXECUCAO");
        UUID participacao = incluir(estudo, participanteId, "CAND-905", 10);

        enviar(mvc, "GET", "/api/me/estudos/" + estudo, participante, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participacaoId").value(participacao.toString()))
                .andExpect(jsonPath("$.estudoId").value(estudo.toString()))
                .andExpect(jsonPath("$.titulo").value("Estudo detalhado T109"))
                .andExpect(jsonPath("$.objetivo").value(objetivo))
                .andExpect(jsonPath("$.resumo").value(endsWith("…")))
                .andExpect(jsonPath("$.resumo").value(matchesPattern("(?s).{1,160}")))
                .andExpect(jsonPath("$.statusEstudo").value("EXECUCAO"))
                .andExpect(jsonPath("$.dataInicio").value("2026-11-01"))
                .andExpect(jsonPath("$.dataFim").value("2027-05-01"))
                .andExpect(jsonPath("$.situacaoParticipacao").value("ATIVO"))
                .andExpect(jsonPath("$.responsavelNome").isNotEmpty())
                .andExpect(jsonPath("$.retirada").value(nullValue()))
                .andExpect(jsonPath("$.codigo").doesNotExist())
                .andExpect(jsonPath("$.nomeFicticio").doesNotExist());
    }

    @Test
    @DisplayName("404: estudo que existe mas é de outro participante, com a mesma resposta de um estudo inexistente")
    void estudoDeOutroParticipanteRecebe404() throws Exception {
        UUID deOutro = criarEstudo("Estudo de outro T109", "Objetivo.", "EXECUCAO");
        incluir(deOutro, criarOutraConta(), "CAND-906", 5);
        String corpoDeOutro = enviar(mvc, "GET", "/api/me/estudos/" + deOutro, participante, null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"))
                .andReturn().getResponse().getContentAsString();
        String corpoInexistente = enviar(mvc, "GET", "/api/me/estudos/" + UUID.randomUUID(), participante, null)
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        org.assertj.core.api.Assertions.assertThat(corpoDeOutro).as("não revela que o estudo existe")
                .isEqualTo(corpoInexistente);
    }

    @Test
    @DisplayName("404: estudo existente sem nenhuma participação da conta (conta sem vínculo)")
    void contaSemVinculoRecebe404NoDetalhe() throws Exception {
        UUID estudo = criarEstudo("Estudo qualquer T109", "Objetivo.", "EXECUCAO");

        enviar(mvc, "GET", "/api/me/estudos/" + estudo, participante, null).andExpect(status().isNotFound());
    }

    // ---- autorização ----

    @ParameterizedTest(name = "403: {0} não acessa /api/me/**")
    @ValueSource(strings = {TokenDeTeste.ADMIN, TokenDeTeste.PESQUISADOR, TokenDeTeste.COORDENADOR})
    @DisplayName("Os demais perfis recebem 403 em /api/me/**")
    void demaisPerfisRecebem403(String email) throws Exception {
        String token = TokenDeTeste.bearer(mvc, email);
        UUID estudo = criarEstudo("Estudo T109", "Objetivo.", "EXECUCAO");

        enviar(mvc, "GET", "/api/me/estudos", token, null).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));
        enviar(mvc, "GET", "/api/me/estudos/" + estudo, token, null).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("401: sem token")
    void semTokenRecebe401() throws Exception {
        enviar(mvc, "GET", "/api/me/estudos", null, null).andExpect(status().isUnauthorized());
        enviar(mvc, "GET", "/api/me/estudos/" + UUID.randomUUID(), null, null).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Somente leitura: POST em /api/me/estudos não é aceito")
    void somenteLeitura() throws Exception {
        enviar(mvc, "POST", "/api/me/estudos", participante, "{}").andExpect(status().isMethodNotAllowed());
    }
}
