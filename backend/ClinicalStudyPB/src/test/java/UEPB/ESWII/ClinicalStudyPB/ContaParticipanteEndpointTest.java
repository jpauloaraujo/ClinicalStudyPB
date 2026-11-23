package UEPB.ESWII.ClinicalStudyPB;

import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.enviar;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
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

import com.jayway.jsonpath.JsonPath;

import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;

/**
 * T111 (RF17): endpoints do vínculo da conta do participante com o banco e tokens reais: matriz de perfis (401/403),
 * regras de negócio com cada código de conflito, ausência de dados de segurança e a lista de contas disponíveis.
 * A corrida entre dois vínculos fica em {@link ContaParticipanteConcorrenciaTest} (sem {@code @Transactional}).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ContaParticipanteEndpointTest {

    private static final String COORDENADOR_ID = "22222222-2222-4222-8222-222222222222";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;

    private String coordenador;
    private String pesquisador;
    private String administrador;
    private String participante;

    @BeforeEach
    void entrar() throws Exception {
        coordenador = TokenDeTeste.bearer(mvc, TokenDeTeste.COORDENADOR);
        pesquisador = TokenDeTeste.bearer(mvc, TokenDeTeste.PESQUISADOR);
        administrador = TokenDeTeste.bearer(mvc, TokenDeTeste.ADMIN);
        participante = TokenDeTeste.bearer(mvc, TokenDeTeste.PARTICIPANTE);
    }

    // ---- montagem do cenário, direto no banco ----

    private UUID criarConta(String nome, String perfil, boolean ativa) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO usuarios (id, nome, email, senha, perfil, ativo) VALUES (CAST(? AS UUID), ?, ?, 'x', ?, ?)",
                id.toString(), nome, "t111." + id + "@clinicalstudy.test", perfil, ativa);
        return id;
    }

    private UUID criarEstudo() {
        UUID estudo = UUID.randomUUID();
        jdbc.update("INSERT INTO estudos (id, titulo, objetivo, responsavel_id, data_inicio, data_fim, "
                + "limite_participantes, status) SELECT CAST(? AS UUID), 'Estudo T111', 'Objetivo.', id, "
                + "DATE '2026-11-01', DATE '2027-05-01', 6, 'RECRUTAMENTO' FROM usuarios "
                + "WHERE email = 'pesquisador@clinicalstudy.test'", estudo.toString());
        return estudo;
    }

    /** Participação de um candidato sem conta (ou com a conta informada). */
    private UUID criarParticipacao(UUID estudo, UUID conta, String codigo) {
        UUID candidato = UUID.randomUUID();
        UUID participacao = UUID.randomUUID();
        jdbc.update("INSERT INTO candidatos (id, estudo_id, codigo, nome_ficticio, data_nascimento, usuario_id, "
                + "situacao) VALUES (CAST(? AS UUID), CAST(? AS UUID), ?, ?, "
                + "DATE '1990-01-01', CAST(? AS UUID), 'INCLUIDO')", candidato.toString(), estudo.toString(), codigo,
                // nome único por candidato: o índice da Sprint 2 recusa nome e data repetidos no estudo (D-T111-03)
                "Pessoa " + codigo + " (Ficticia)", conta == null ? null : conta.toString());
        jdbc.update("INSERT INTO participacoes (id, candidato_id, estudo_id, data_inclusao, incluido_por_id) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), CAST(? AS UUID), CURRENT_DATE, CAST(? AS UUID))",
                participacao.toString(), candidato.toString(), estudo.toString(), COORDENADOR_ID);
        return participacao;
    }

    private static String corpo(UUID usuarioId) {
        return "{\"usuarioId\":\"" + usuarioId + "\"}";
    }

    private static String urlConta(UUID participacao) {
        return "/api/participacoes/" + participacao + "/conta";
    }

    // ---- matriz de perfis ----

    @Test
    @DisplayName("401: sem token, as três rotas recusam")
    void semToken() throws Exception {
        UUID participacao = UUID.randomUUID();
        enviar(mvc, "PUT", urlConta(participacao), null, corpo(UUID.randomUUID())).andExpect(status().isUnauthorized());
        enviar(mvc, "GET", urlConta(participacao), null, null).andExpect(status().isUnauthorized());
        enviar(mvc, "GET", "/api/contas-participante/disponiveis", null, null).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("403: pesquisador, administrador e participante não vinculam nem listam disponíveis")
    void soOCoordenadorEscreve() throws Exception {
        UUID participacao = criarParticipacao(criarEstudo(), null, "T111-01");
        UUID conta = criarConta("Conta Livre", "PARTICIPANTE", true);

        for (String token : List.of(pesquisador, administrador, participante)) {
            enviar(mvc, "PUT", urlConta(participacao), token, corpo(conta)).andExpect(status().isForbidden());
            enviar(mvc, "GET", "/api/contas-participante/disponiveis", token, null).andExpect(status().isForbidden());
        }
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM candidatos WHERE usuario_id = CAST(? AS UUID)",
                Integer.class, conta.toString())).as("nenhum vínculo gravado").isZero();
    }

    @Test
    @DisplayName("Leitura da conta: coordenador e pesquisador leem; administrador e participante recebem 403")
    void leituraDaConta() throws Exception {
        UUID conta = criarConta("Conta Vinculada", "PARTICIPANTE", true);
        UUID participacao = criarParticipacao(criarEstudo(), conta, "T111-02");

        for (String token : List.of(coordenador, pesquisador)) {
            enviar(mvc, "GET", urlConta(participacao), token, null).andExpect(status().isOk());
        }
        for (String token : List.of(administrador, participante)) {
            enviar(mvc, "GET", urlConta(participacao), token, null).andExpect(status().isForbidden());
        }
    }

    // ---- caminho feliz ----

    @Test
    @DisplayName("200: o coordenador vincula; a conta aparece na consulta, para os dois perfis, e some das disponíveis")
    void vinculaEConsulta() throws Exception {
        UUID conta = criarConta("Maria Vinculavel (Ficticia)", "PARTICIPANTE", true);
        UUID participacao = criarParticipacao(criarEstudo(), null, "T111-03");

        enviar(mvc, "GET", "/api/contas-participante/disponiveis", coordenador, null)
                .andExpect(jsonPath("$[?(@.usuarioId == '" + conta + "')]").isNotEmpty());

        enviar(mvc, "PUT", urlConta(participacao), coordenador, corpo(conta))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participacaoId").value(participacao.toString()))
                .andExpect(jsonPath("$.usuarioId").value(conta.toString()))
                .andExpect(jsonPath("$.nome").value("Maria Vinculavel (Ficticia)"))
                .andExpect(jsonPath("$.email").value("t111." + conta + "@clinicalstudy.test"));

        for (String token : List.of(coordenador, pesquisador)) {
            enviar(mvc, "GET", urlConta(participacao), token, null)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.usuarioId").value(conta.toString()));
        }
        enviar(mvc, "GET", "/api/contas-participante/disponiveis", coordenador, null)
                .andExpect(jsonPath("$[?(@.usuarioId == '" + conta + "')]").isEmpty());
    }

    @Test
    @DisplayName("A resposta nunca traz senha, hash, perfil nem situação da conta")
    void semDadosDeSeguranca() throws Exception {
        UUID conta = criarConta("Conta Segura", "PARTICIPANTE", true);
        UUID participacao = criarParticipacao(criarEstudo(), null, "T111-04");

        enviar(mvc, "PUT", urlConta(participacao), coordenador, corpo(conta))
                .andExpect(content().string(not(containsString("senha"))))
                .andExpect(content().string(not(containsString("hash"))))
                .andExpect(jsonPath("$.perfil").doesNotExist())
                .andExpect(jsonPath("$.ativo").doesNotExist());
        enviar(mvc, "GET", urlConta(participacao), coordenador, null)
                .andExpect(content().string(not(containsString("senha"))))
                .andExpect(jsonPath("$.perfil").doesNotExist());
        enviar(mvc, "GET", "/api/contas-participante/disponiveis", coordenador, null)
                .andExpect(content().string(not(containsString("senha"))))
                .andExpect(jsonPath("$[0].perfil").doesNotExist());
    }

    // ---- 400 e 404 ----

    @Test
    @DisplayName("400: corpo sem usuarioId")
    void corpoSemUsuario() throws Exception {
        UUID participacao = criarParticipacao(criarEstudo(), null, "T111-05");

        enviar(mvc, "PUT", urlConta(participacao), coordenador, "{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO_CAMPOS"));
    }

    @Test
    @DisplayName("404: participação inexistente e conta inexistente")
    void naoEncontrados() throws Exception {
        UUID participacao = criarParticipacao(criarEstudo(), null, "T111-06");
        UUID conta = criarConta("Conta Real", "PARTICIPANTE", true);

        enviar(mvc, "PUT", urlConta(UUID.randomUUID()), coordenador, corpo(conta))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"));
        enviar(mvc, "PUT", urlConta(participacao), coordenador, corpo(UUID.randomUUID()))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"));
        enviar(mvc, "GET", urlConta(UUID.randomUUID()), coordenador, null).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("404: consulta de participante sem conta vinculada")
    void consultaSemConta() throws Exception {
        UUID participacao = criarParticipacao(criarEstudo(), null, "T111-07");

        enviar(mvc, "GET", urlConta(participacao), coordenador, null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"));
    }

    // ---- 409, um código por regra ----

    @Test
    @DisplayName("409 CONTA_NAO_E_PARTICIPANTE: conta de outro perfil")
    void contaDeOutroPerfil() throws Exception {
        UUID participacao = criarParticipacao(criarEstudo(), null, "T111-08");
        UUID coordenadora = criarConta("Outra Coordenadora", "COORDENADOR", true);

        enviar(mvc, "PUT", urlConta(participacao), coordenador, corpo(coordenadora))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CONTA_NAO_E_PARTICIPANTE"));
    }

    @Test
    @DisplayName("409 CONTA_INATIVA: conta desativada")
    void contaInativa() throws Exception {
        UUID participacao = criarParticipacao(criarEstudo(), null, "T111-09");
        UUID inativa = criarConta("Conta Desativada", "PARTICIPANTE", false);

        enviar(mvc, "PUT", urlConta(participacao), coordenador, corpo(inativa))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CONTA_INATIVA"));
    }

    @Test
    @DisplayName("409 PARTICIPACAO_JA_TEM_CONTA: outra conta, e também a mesma conta reenviada (não é idempotente)")
    void participacaoJaTemConta() throws Exception {
        UUID participacao = criarParticipacao(criarEstudo(), null, "T111-10");
        UUID primeira = criarConta("Primeira Conta", "PARTICIPANTE", true);
        UUID segunda = criarConta("Segunda Conta", "PARTICIPANTE", true);
        enviar(mvc, "PUT", urlConta(participacao), coordenador, corpo(primeira)).andExpect(status().isOk());

        enviar(mvc, "PUT", urlConta(participacao), coordenador, corpo(segunda))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("PARTICIPACAO_JA_TEM_CONTA"));
        enviar(mvc, "PUT", urlConta(participacao), coordenador, corpo(primeira))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("PARTICIPACAO_JA_TEM_CONTA"));
        enviar(mvc, "GET", urlConta(participacao), coordenador, null)
                .andExpect(jsonPath("$.usuarioId").value(primeira.toString()));
    }

    @Test
    @DisplayName("409 CONTA_JA_VINCULADA: a conta já pertence a outro participante (um para um)")
    void contaJaVinculada() throws Exception {
        UUID estudo = criarEstudo();
        UUID conta = criarConta("Conta Disputada", "PARTICIPANTE", true);
        UUID dona = criarParticipacao(estudo, null, "T111-11");
        UUID outra = criarParticipacao(estudo, null, "T111-12");
        enviar(mvc, "PUT", urlConta(dona), coordenador, corpo(conta)).andExpect(status().isOk());

        enviar(mvc, "PUT", urlConta(outra), coordenador, corpo(conta))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CONTA_JA_VINCULADA"));
        enviar(mvc, "GET", urlConta(outra), coordenador, null).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Ordem das verificações: conta de outro perfil e inativa ao mesmo tempo devolve CONTA_NAO_E_PARTICIPANTE")
    void ordemPerfilAntesDeInativa() throws Exception {
        UUID participacao = criarParticipacao(criarEstudo(), null, "T111-13");
        UUID conta = criarConta("Pesquisador Inativo", "PESQUISADOR", false);

        enviar(mvc, "PUT", urlConta(participacao), coordenador, corpo(conta))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CONTA_NAO_E_PARTICIPANTE"));
    }

    @ParameterizedTest(name = "o vínculo não tem rota de desvincular nem de troca: {0} é recusado")
    @ValueSource(strings = {"DELETE", "POST"})
    void naoHaDesvincular(String metodo) throws Exception {
        UUID conta = criarConta("Conta Fixa", "PARTICIPANTE", true);
        UUID participacao = criarParticipacao(criarEstudo(), conta, "T111-14");

        enviar(mvc, metodo, urlConta(participacao), coordenador, "{}").andExpect(status().is4xxClientError());
        enviar(mvc, "GET", urlConta(participacao), coordenador, null)
                .andExpect(jsonPath("$.usuarioId").value(conta.toString()));
    }

    // ---- contas disponíveis ----

    @Test
    @DisplayName("Disponíveis: só contas ativas, de perfil PARTICIPANTE e livres, por nome")
    void disponiveis() throws Exception {
        UUID zelia = criarConta("Zélia Disponível T111", "PARTICIPANTE", true);
        UUID alice = criarConta("Alice Disponível T111", "PARTICIPANTE", true);
        UUID inativa = criarConta("Bia Inativa T111", "PARTICIPANTE", false);
        UUID outroPerfil = criarConta("Caio Pesquisador T111", "PESQUISADOR", true);
        UUID vinculada = criarConta("Dora Vinculada T111", "PARTICIPANTE", true);
        criarParticipacao(criarEstudo(), vinculada, "T111-15");

        String json = enviar(mvc, "GET", "/api/contas-participante/disponiveis", coordenador, null)
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<String> ids = JsonPath.read(json, "$[*].usuarioId");

        assertThat(ids).contains(alice.toString(), zelia.toString())
                .doesNotContain(inativa.toString(), outroPerfil.toString(), vinculada.toString());
        // A ordem exata depende da collation do banco; entre "Alice" e "Zélia" ela é a mesma em qualquer uma
        assertThat(ids.indexOf(alice.toString())).as("ordenadas por nome: Alice antes de Zélia")
                .isLessThan(ids.indexOf(zelia.toString()));
    }
}
