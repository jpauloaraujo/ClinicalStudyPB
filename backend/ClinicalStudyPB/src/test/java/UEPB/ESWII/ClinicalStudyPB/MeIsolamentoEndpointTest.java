package UEPB.ESWII.ClinicalStudyPB;

import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.enviar;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;

/**
 * T113 (RQ-T113-01, 03, 05 e 06): isolamento entre participantes com <b>duas contas que entram de verdade</b>
 * (login real), e o fluxo vínculo → login → lista. O teste mais importante da task: o participante A nunca vê o
 * estudo do B, nem o contrário. Complementa o {@link MeEndpointTest} (T109), que só entra com a conta do seed.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MeIsolamentoEndpointTest {

    private static final String COORDENADOR_ID = "22222222-2222-4222-8222-222222222222";
    private static final String SENHA = "senha-de-teste";
    /** Campos que identificam o candidato e nunca podem aparecer em {@code /api/me/**}. */
    private static final Set<String> CAMPOS_PROIBIDOS = Set.of("codigo", "nomeFicticio", "candidatoId", "candidato",
            "dataNascimento", "motivo", "motivoRetirada", "usuarioId", "senha");

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private PasswordEncoder codificador;

    private String coordenador;

    @BeforeEach
    void entrar() throws Exception {
        coordenador = TokenDeTeste.bearer(mvc, TokenDeTeste.COORDENADOR);
    }

    // ---- montagem do cenário, direto no banco ----

    /** Conta de participante com senha conhecida, para entrar de verdade. */
    private String criarConta(String sufixo) {
        String email = "t113." + sufixo + "." + UUID.randomUUID() + "@clinicalstudy.test";
        email = email.toLowerCase(java.util.Locale.ROOT); // o login normaliza o e-mail para minúsculas (D-T113-01)
        jdbc.update("INSERT INTO usuarios (id, nome, email, senha, perfil, ativo) VALUES (CAST(? AS UUID), ?, ?, ?, "
                + "'PARTICIPANTE', TRUE)", UUID.randomUUID().toString(), "Participante " + sufixo + " (Ficticio)",
                email, codificador.encode(SENHA));
        return email;
    }

    private UUID usuarioId(String email) {
        return UUID.fromString(jdbc.queryForObject("SELECT CAST(id AS VARCHAR) FROM usuarios WHERE email = ?",
                String.class, email));
    }

    private UUID criarEstudo(String titulo) {
        UUID estudo = UUID.randomUUID();
        jdbc.update("INSERT INTO estudos (id, titulo, objetivo, responsavel_id, data_inicio, data_fim, "
                + "limite_participantes, status) SELECT CAST(? AS UUID), ?, 'Objetivo do estudo.', id, "
                + "DATE '2026-11-01', DATE '2027-05-01', 6, 'EXECUCAO' FROM usuarios "
                + "WHERE email = 'pesquisador@clinicalstudy.test'", estudo.toString(), titulo);
        return estudo;
    }

    /** Inclui um candidato no estudo, com a conta (ou sem, se nula). Devolve o id da participação. */
    private UUID incluir(UUID estudo, UUID conta, String codigo) {
        UUID candidato = UUID.randomUUID();
        UUID participacao = UUID.randomUUID();
        jdbc.update("INSERT INTO candidatos (id, estudo_id, codigo, nome_ficticio, data_nascimento, usuario_id, "
                + "situacao) VALUES (CAST(? AS UUID), CAST(? AS UUID), ?, ?, DATE '1990-01-01', CAST(? AS UUID), "
                + "'INCLUIDO')", candidato.toString(), estudo.toString(), codigo, "Pessoa " + codigo + " (Ficticia)",
                conta == null ? null : conta.toString());
        jdbc.update("INSERT INTO participacoes (id, candidato_id, estudo_id, data_inclusao, incluido_por_id) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), CAST(? AS UUID), CURRENT_DATE, CAST(? AS UUID))",
                participacao.toString(), candidato.toString(), estudo.toString(), COORDENADOR_ID);
        return participacao;
    }

    /** Percorre o JSON inteiro e falha se achar alguma chave proibida. */
    private static void semCamposDoCandidato(String json) {
        Object raiz = JsonPath.parse(json).read("$");
        percorrer(raiz);
    }

    private static void percorrer(Object no) {
        if (no instanceof java.util.Map<?, ?> mapa) {
            mapa.forEach((chave, valor) -> {
                assertThat(CAMPOS_PROIBIDOS).as("campo proibido em /me: " + chave).doesNotContain(String.valueOf(chave));
                percorrer(valor);
            });
        } else if (no instanceof java.util.List<?> lista) {
            lista.forEach(MeIsolamentoEndpointTest::percorrer);
        }
    }

    // ---- RQ-01 e RQ-03: o participante A nunca vê o estudo do B ----

    @Test
    @DisplayName("RQ-01 e RQ-03: A e B, cada um com o seu estudo, só veem o próprio; o detalhe cruzado dá 404 nos dois sentidos")
    void aNuncaVeEstudoDeBENemBDeA() throws Exception {
        String emailA = criarConta("A");
        String emailB = criarConta("B");
        UUID estudoA = criarEstudo("Estudo exclusivo de A");
        UUID estudoB = criarEstudo("Estudo exclusivo de B");
        UUID participacaoA = incluir(estudoA, usuarioId(emailA), "T113-A");
        UUID participacaoB = incluir(estudoB, usuarioId(emailB), "T113-B");
        String tokenA = TokenDeTeste.bearer(mvc, emailA, SENHA);
        String tokenB = TokenDeTeste.bearer(mvc, emailB, SENHA);

        enviar(mvc, "GET", "/api/me/estudos", tokenA, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].estudoId").value(estudoA.toString()))
                .andExpect(jsonPath("$[0].participacaoId").value(participacaoA.toString()))
                .andExpect(jsonPath("$[?(@.estudoId == '" + estudoB + "')]").isEmpty());
        enviar(mvc, "GET", "/api/me/estudos", tokenB, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].estudoId").value(estudoB.toString()))
                .andExpect(jsonPath("$[0].participacaoId").value(participacaoB.toString()))
                .andExpect(jsonPath("$[?(@.estudoId == '" + estudoA + "')]").isEmpty());

        enviar(mvc, "GET", "/api/me/estudos/" + estudoA, tokenA, null).andExpect(status().isOk());
        enviar(mvc, "GET", "/api/me/estudos/" + estudoB, tokenB, null).andExpect(status().isOk());
        enviar(mvc, "GET", "/api/me/estudos/" + estudoB, tokenA, null).andExpect(status().isNotFound());
        enviar(mvc, "GET", "/api/me/estudos/" + estudoA, tokenB, null).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("RQ-01: dois participantes no MESMO estudo também só veem a própria participação")
    void doisParticipantesNoMesmoEstudo() throws Exception {
        String emailA = criarConta("A2");
        String emailB = criarConta("B2");
        UUID estudo = criarEstudo("Estudo compartilhado");
        UUID participacaoA = incluir(estudo, usuarioId(emailA), "T113-A2");
        UUID participacaoB = incluir(estudo, usuarioId(emailB), "T113-B2");

        enviar(mvc, "GET", "/api/me/estudos", TokenDeTeste.bearer(mvc, emailA, SENHA), null)
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].participacaoId").value(participacaoA.toString()));
        enviar(mvc, "GET", "/api/me/estudos/" + estudo, TokenDeTeste.bearer(mvc, emailB, SENHA), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participacaoId").value(participacaoB.toString()));
    }

    // ---- RQ-05: nenhum campo de candidato ----

    @Test
    @DisplayName("RQ-05: lista e detalhe não trazem nenhum campo de candidato, em nenhum nível do JSON")
    void respostaNaoTrazDadosDoCandidato() throws Exception {
        String email = criarConta("C");
        UUID estudo = criarEstudo("Estudo sem dados do candidato");
        incluir(estudo, usuarioId(email), "T113-C");
        String token = TokenDeTeste.bearer(mvc, email, SENHA);

        String lista = enviar(mvc, "GET", "/api/me/estudos", token, null).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String detalhe = enviar(mvc, "GET", "/api/me/estudos/" + estudo, token, null).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        semCamposDoCandidato(lista);
        semCamposDoCandidato(detalhe);
        assertThat(lista).doesNotContain("T113-C").doesNotContain("Pessoa T113-C");
        assertThat(detalhe).doesNotContain("T113-C").doesNotContain("Pessoa T113-C");
    }

    // ---- RQ-06: vínculo → login → lista ----

    @Test
    @DisplayName("RQ-06: depois que o coordenador vincula a conta, o participante entra e vê o estudo dele")
    void depoisDeVincularAContaVeOEstudo() throws Exception {
        String email = criarConta("D");
        UUID conta = usuarioId(email);
        UUID estudo = criarEstudo("Estudo do vínculo");
        UUID participacao = incluir(estudo, null, "T113-D");
        String token = TokenDeTeste.bearer(mvc, email, SENHA);

        enviar(mvc, "GET", "/api/me/estudos", token, null)
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        enviar(mvc, "GET", "/api/me/estudos/" + estudo, token, null).andExpect(status().isNotFound());

        enviar(mvc, "PUT", "/api/participacoes/" + participacao + "/conta", coordenador,
                "{\"usuarioId\":\"" + conta + "\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(conta.toString()));

        enviar(mvc, "GET", "/api/me/estudos", token, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].estudoId").value(estudo.toString()))
                .andExpect(jsonPath("$[0].participacaoId").value(participacao.toString()));
        enviar(mvc, "GET", "/api/me/estudos/" + estudo, token, null).andExpect(status().isOk());
        enviar(mvc, "GET", "/api/participacoes/" + participacao + "/conta", coordenador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(conta.toString()));
    }
}
