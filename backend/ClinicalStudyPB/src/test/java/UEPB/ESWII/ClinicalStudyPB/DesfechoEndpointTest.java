package UEPB.ESWII.ClinicalStudyPB;

import static UEPB.ESWII.ClinicalStudyPB.support.CenarioDesfecho.estudoComVisita;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioDesfecho.visitaAgendada;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.avancar;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.enviar;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.registrarDesfecho;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

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

import UEPB.ESWII.ClinicalStudyPB.support.CenarioDesfecho.EstudoComVisita;
import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;

/**
 * T85 (RF13): {@code POST /api/visitas-agendadas/{id}/desfecho} e {@code GET /api/visitas-agendadas/{id}} com
 * tokens reais. 200 de cada tipo, 400, 401, 403 por perfil, 404 e 409 de cada regra. As datas são relativas a hoje,
 * no fuso do {@code Clock} da aplicação: "ontem" já pode ter desfecho e "daqui a 5 dias" ainda não.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DesfechoEndpointTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private Clock relogio;

    // O teste é @Transactional: o flush grava a participação antes do INSERT por SQL da visita (ver CenarioDesfecho)
    @PersistenceContext
    private EntityManager em;

    private String pesquisador;
    private String coordenador;
    private EstudoComVisita estudo;
    private String visita;

    @BeforeEach
    void preparar() throws Exception {
        pesquisador = TokenDeTeste.bearer(mvc, TokenDeTeste.PESQUISADOR);
        coordenador = TokenDeTeste.bearer(mvc, TokenDeTeste.COORDENADOR);
        estudo = estudoComVisita(mvc, pesquisador, 10);
        visita = novaVisita(agora().minusDays(1));
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(relogio).truncatedTo(ChronoUnit.SECONDS);
    }

    private String novaVisita(LocalDateTime dataHoraAgendada) throws Exception {
        return visitaAgendada(mvc, jdbc, em, coordenador, estudo, dataHoraAgendada);
    }

    private String url(String visitaId) {
        return "/api/visitas-agendadas/" + visitaId;
    }

    // ---------------------------------------------------------------- 200

    @Test
    @DisplayName("200 REALIZADA: grava a data e hora reais, o autor e incrementa a versão")
    void realizada() throws Exception {
        LocalDateTime real = agora().minusHours(1);

        registrarDesfecho(mvc, coordenador, visita,
                "{\"tipo\":\"REALIZADA\",\"dataHoraRealizada\":\"" + real + "\",\"observacao\":\"Tudo certo\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(visita))
                .andExpect(jsonPath("$.situacao").value("REALIZADA"))
                .andExpect(jsonPath("$.dataHoraRealizada").isNotEmpty())
                .andExpect(jsonPath("$.observacao").value("Tudo certo"))
                .andExpect(jsonPath("$.motivo").value(nullValue()))
                .andExpect(jsonPath("$.alteradoPor").isNotEmpty())
                .andExpect(jsonPath("$.versao").value(1));
    }

    @Test
    @DisplayName("200 REALIZADA sem data: assume agora")
    void realizadaSemData() throws Exception {
        registrarDesfecho(mvc, coordenador, visita, "{\"tipo\":\"REALIZADA\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("REALIZADA"))
                .andExpect(jsonPath("$.dataHoraRealizada").isNotEmpty());
    }

    @Test
    @DisplayName("200 AUSENTE: guarda a observação e não guarda data real")
    void ausente() throws Exception {
        registrarDesfecho(mvc, coordenador, visita, "{\"tipo\":\"AUSENTE\",\"observacao\":\"Não atendeu\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("AUSENTE"))
                .andExpect(jsonPath("$.observacao").value("Não atendeu"))
                .andExpect(jsonPath("$.dataHoraRealizada").value(nullValue()));
    }

    @Test
    @DisplayName("200 CANCELADA: guarda o motivo e não guarda data real")
    void cancelada() throws Exception {
        registrarDesfecho(mvc, coordenador, visita, "{\"tipo\":\"CANCELADA\",\"motivo\":\"Participante em viagem\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("CANCELADA"))
                .andExpect(jsonPath("$.motivo").value("Participante em viagem"))
                .andExpect(jsonPath("$.dataHoraRealizada").value(nullValue()));
    }

    @Test
    @DisplayName("Campos que não pertencem ao tipo são ignorados, sem erro")
    void camposDeOutroTipoSaoIgnorados() throws Exception {
        registrarDesfecho(mvc, coordenador, visita,
                "{\"tipo\":\"AUSENTE\",\"motivo\":\"ignorado\",\"dataHoraRealizada\":\"" + agora().minusHours(2) + "\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.motivo").value(nullValue()))
                .andExpect(jsonPath("$.dataHoraRealizada").value(nullValue()));
    }

    @Test
    @DisplayName("Cancelar vale com a visita agendada para o futuro")
    void canceladaComDataFutura() throws Exception {
        String futura = novaVisita(agora().plusDays(5));

        registrarDesfecho(mvc, coordenador, futura, "{\"tipo\":\"CANCELADA\",\"motivo\":\"Remarcada pelo centro\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("CANCELADA"));
    }

    // ---------------------------------------------------------------- 400

    @Test
    @DisplayName("400 VALIDACAO_CAMPOS quando o tipo falta")
    void tipoAusente() throws Exception {
        registrarDesfecho(mvc, coordenador, visita, "{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO_CAMPOS"))
                .andExpect(jsonPath("$.errosCampo[0].campo").value("tipo"));
    }

    @ParameterizedTest(name = "corpo [{0}] responde 400 CORPO_INVALIDO")
    @ValueSource(strings = {"{\"tipo\":\"INEXISTENTE\"}", "{\"tipo\":\"AGENDADA\"}", "{tipo:", "nao-e-json"})
    @DisplayName("400 CORPO_INVALIDO para tipo inexistente e JSON malformado")
    void corpoInvalido(String corpo) throws Exception {
        registrarDesfecho(mvc, coordenador, visita, corpo)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("CORPO_INVALIDO"));
    }

    @Test
    @DisplayName("400 VALIDACAO_CAMPOS para observação com mais de 500 caracteres")
    void observacaoLonga() throws Exception {
        registrarDesfecho(mvc, coordenador, visita, "{\"tipo\":\"AUSENTE\",\"observacao\":\"" + "o".repeat(501) + "\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO_CAMPOS"));
    }

    @Test
    @DisplayName("400 DATA_REALIZACAO_FUTURA para data e hora reais no futuro")
    void dataRealizacaoFutura() throws Exception {
        registrarDesfecho(mvc, coordenador, visita,
                "{\"tipo\":\"REALIZADA\",\"dataHoraRealizada\":\"" + agora().plusDays(2) + "\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATA_REALIZACAO_FUTURA"))
                .andExpect(jsonPath("$.codigoRegra").value("DATA_REALIZACAO_FUTURA"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @ParameterizedTest(name = "cancelamento com corpo [{0}] responde 400 MOTIVO_OBRIGATORIO")
    @ValueSource(strings = {"{\"tipo\":\"CANCELADA\"}", "{\"tipo\":\"CANCELADA\",\"motivo\":\"   \"}",
            "{\"tipo\":\"CANCELADA\",\"motivo\":\"abcd\"}"})
    @DisplayName("400 MOTIVO_OBRIGATORIO para cancelamento sem motivo, em branco ou com 4 caracteres")
    void motivoObrigatorio(String corpo) throws Exception {
        registrarDesfecho(mvc, coordenador, visita, corpo)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("MOTIVO_OBRIGATORIO"));
    }

    @Test
    @DisplayName("Depois de um 400 nada foi gravado: a visita continua AGENDADA")
    void erroNaoGrava() throws Exception {
        registrarDesfecho(mvc, coordenador, visita, "{\"tipo\":\"CANCELADA\"}").andExpect(status().isBadRequest());

        enviar(mvc, "GET", url(visita), coordenador, null).andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("AGENDADA"))
                .andExpect(jsonPath("$.versao").value(0));
    }

    // ---------------------------------------------------------------- 401, 403, 404

    @Test
    @DisplayName("401 sem token")
    void semToken() throws Exception {
        registrarDesfecho(mvc, null, visita, "{\"tipo\":\"AUSENTE\"}").andExpect(status().isUnauthorized());
        enviar(mvc, "GET", url(visita), null, null).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "{0} recebe 403 ao registrar o desfecho")
    @ValueSource(strings = {TokenDeTeste.PESQUISADOR, TokenDeTeste.PARTICIPANTE, TokenDeTeste.ADMIN})
    @DisplayName("403 para pesquisador, participante e administrador no desfecho")
    void escritaSoDoCoordenador(String email) throws Exception {
        String token = TokenDeTeste.bearer(mvc, email);

        registrarDesfecho(mvc, token, visita, "{\"tipo\":\"AUSENTE\"}")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));
        enviar(mvc, "GET", url(visita), coordenador, null).andExpect(jsonPath("$.situacao").value("AGENDADA"));
    }

    @Test
    @DisplayName("404 para visita agendada inexistente, no desfecho e na consulta")
    void visitaInexistente() throws Exception {
        String fantasma = UUID.randomUUID().toString();

        registrarDesfecho(mvc, coordenador, fantasma, "{\"tipo\":\"AUSENTE\"}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"));
        enviar(mvc, "GET", url(fantasma), coordenador, null).andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------- 409

    @Test
    @DisplayName("409 TRANSICAO_VISITA_INVALIDA: visita que já tem desfecho não recebe outro")
    void transicaoInvalida() throws Exception {
        registrarDesfecho(mvc, coordenador, visita, "{\"tipo\":\"AUSENTE\"}").andExpect(status().isOk());

        for (String corpo : new String[] { "{\"tipo\":\"AUSENTE\"}", "{\"tipo\":\"REALIZADA\"}",
                "{\"tipo\":\"CANCELADA\",\"motivo\":\"Tentativa de desfazer\"}" }) {
            registrarDesfecho(mvc, coordenador, visita, corpo)
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.codigo").value("TRANSICAO_VISITA_INVALIDA"))
                    .andExpect(jsonPath("$.mensagem", containsString("não pode mais ser alterada")));
        }
        enviar(mvc, "GET", url(visita), coordenador, null).andExpect(jsonPath("$.situacao").value("AUSENTE"))
                .andExpect(jsonPath("$.versao").value(1));
    }

    @Test
    @DisplayName("409 ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO com o estudo encerrado")
    void estudoEncerrado() throws Exception {
        avancar(mvc, pesquisador, estudo.estudoId(), "EXECUCAO").andExpect(status().isOk());
        avancar(mvc, pesquisador, estudo.estudoId(), "ENCERRADO").andExpect(status().isOk());

        registrarDesfecho(mvc, coordenador, visita, "{\"tipo\":\"CANCELADA\",\"motivo\":\"Estudo encerrado\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO"));
    }

    @Test
    @DisplayName("O estudo em execução aceita o desfecho")
    void estudoEmExecucao() throws Exception {
        avancar(mvc, pesquisador, estudo.estudoId(), "EXECUCAO").andExpect(status().isOk());

        registrarDesfecho(mvc, coordenador, visita, "{\"tipo\":\"AUSENTE\"}").andExpect(status().isOk());
    }

    @ParameterizedTest(name = "{0} com a visita agendada para o futuro responde 409")
    @ValueSource(strings = {"REALIZADA", "AUSENTE"})
    @DisplayName("409 VISITA_AINDA_NAO_OCORREU para realizada e ausente com a data agendada futura")
    void visitaAindaNaoOcorreu(String tipo) throws Exception {
        String futura = novaVisita(agora().plusDays(5));

        registrarDesfecho(mvc, coordenador, futura, "{\"tipo\":\"" + tipo + "\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("VISITA_AINDA_NAO_OCORREU"));
    }

    @Test
    @DisplayName("A ordem vale na API: transição inválida vence a data futura")
    void transicaoVenceADataFutura() throws Exception {
        registrarDesfecho(mvc, coordenador, visita, "{\"tipo\":\"AUSENTE\"}").andExpect(status().isOk());

        registrarDesfecho(mvc, coordenador, visita,
                "{\"tipo\":\"REALIZADA\",\"dataHoraRealizada\":\"" + agora().plusDays(2) + "\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("TRANSICAO_VISITA_INVALIDA"));
    }

    // ---------------------------------------------------------------- GET

    @ParameterizedTest(name = "{0} consulta a visita")
    @ValueSource(strings = {TokenDeTeste.COORDENADOR, TokenDeTeste.PESQUISADOR})
    @DisplayName("GET 200 para coordenador e pesquisador, com o desfecho registrado")
    void consulta(String email) throws Exception {
        registrarDesfecho(mvc, coordenador, visita, "{\"tipo\":\"CANCELADA\",\"motivo\":\"Participante em viagem\"}")
                .andExpect(status().isOk());
        String token = TokenDeTeste.bearer(mvc, email);

        enviar(mvc, "GET", url(visita), token, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(visita))
                .andExpect(jsonPath("$.situacao").value("CANCELADA"))
                .andExpect(jsonPath("$.motivo").value("Participante em viagem"))
                .andExpect(jsonPath("$.visitaNome").value("Triagem"))
                .andExpect(jsonPath("$.participanteCodigo").isNotEmpty())
                .andExpect(jsonPath("$.dataHora", not(endsWith("Z"))))
                .andExpect(jsonPath("$.janelaInicio").isNotEmpty())
                .andExpect(jsonPath("$.janelaFim").isNotEmpty());
    }

    @ParameterizedTest(name = "{0} recebe 403 ao consultar a visita")
    @ValueSource(strings = {TokenDeTeste.ADMIN, TokenDeTeste.PARTICIPANTE})
    @DisplayName("GET 403 para administrador e participante")
    void consultaNegada(String email) throws Exception {
        enviar(mvc, "GET", url(visita), TokenDeTeste.bearer(mvc, email), null).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Depois do desfecho, o reagendamento responde 409 VISITA_NAO_ESTA_AGENDADA")
    void reagendarDepoisDoDesfecho() throws Exception {
        registrarDesfecho(mvc, coordenador, visita, "{\"tipo\":\"AUSENTE\"}").andExpect(status().isOk());

        enviar(mvc, "PUT", url(visita), coordenador, "{\"dataHora\":\"" + agora().plusDays(1) + "\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("VISITA_NAO_ESTA_AGENDADA"));
    }
}
