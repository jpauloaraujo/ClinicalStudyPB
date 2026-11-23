package UEPB.ESWII.ClinicalStudyPB;

import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.agendar;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.agendarVisita;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.avancar;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.enviar;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.estudoParaAgendamento;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.id;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.participanteIncluido;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.registrarDesfecho;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.EstudoParaAgendamento;
import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;

/**
 * T78 (RF12, RQ-T78-10 a 12): os endpoints do agendamento ({@code POST}, {@code PUT} e {@code GET .../visitas}) com o
 * banco e os tokens reais dos perfis. O estado de partida (estudo em recrutamento, participante incluído hoje, triagem
 * no dia 0 e retorno no dia 5 ± 2, com janela de hoje + 3 a hoje + 7) é montado pela própria API e desfeito no fim de
 * cada teste. Não depende de comportamento exclusivo do H2: roda também no job do PostgreSQL.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AgendamentoRegrasEndpointTest {

    private static final String ISO_COM_FUSO =
            "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2}(\\.\\d+)?)?([+-]\\d{2}:\\d{2}|Z)";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private Clock clock;

    private String coordenador;
    private String pesquisador;
    private String administrador;
    private String participante;
    private LocalDate hoje;
    private EstudoParaAgendamento estudo;
    private String participacao;

    @BeforeEach
    void montar() throws Exception {
        coordenador = TokenDeTeste.bearer(mvc, TokenDeTeste.COORDENADOR);
        pesquisador = TokenDeTeste.bearer(mvc, TokenDeTeste.PESQUISADOR);
        administrador = TokenDeTeste.bearer(mvc, TokenDeTeste.ADMIN);
        participante = TokenDeTeste.bearer(mvc, TokenDeTeste.PARTICIPANTE);
        hoje = LocalDate.now(clock);
        estudo = estudoParaAgendamento(mvc, pesquisador, 5);
        participacao = participanteIncluido(mvc, coordenador, estudo.estudoId(), "Pessoa Agenda (Ficticia)");
    }

    private String dia(int deslocamento) {
        return hoje.plusDays(deslocamento) + "T09:30";
    }

    private String agendarRetorno(int deslocamento) throws Exception {
        return id(agendar(mvc, coordenador, participacao, estudo.retornoId(), dia(deslocamento))
                .andExpect(status().isCreated()));
    }

    // ---- POST: sucesso ----

    @Test
    @DisplayName("POST 201: devolve Location e o corpo completo do agendamento, com a janela e a auditoria")
    void agendaERetornaOCorpoCompleto() throws Exception {
        agendar(mvc, coordenador, participacao, estudo.retornoId(), dia(5))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/visitas-agendadas/")))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.participacaoId").value(participacao))
                .andExpect(jsonPath("$.estudoId").value(estudo.estudoId()))
                .andExpect(jsonPath("$.visitaPlanejadaId").value(estudo.retornoId()))
                .andExpect(jsonPath("$.visitaNome").value("Retorno"))
                .andExpect(jsonPath("$.situacao").value("AGENDADA"))
                .andExpect(jsonPath("$.dataHora").value(org.hamcrest.Matchers.matchesPattern(ISO_COM_FUSO)))
                .andExpect(jsonPath("$.janelaInicio").value(hoje.plusDays(3).toString()))
                .andExpect(jsonPath("$.janelaFim").value(hoje.plusDays(7).toString()))
                .andExpect(jsonPath("$.dataHoraRealizada").value(nullValue()))
                .andExpect(jsonPath("$.criadoPor").isNotEmpty())
                .andExpect(jsonPath("$.versao").value(0));
    }

    // ---- POST: erros ----

    @Test
    @DisplayName("POST 400: campos ausentes, JSON malformado, data malformada e data no passado")
    void rejeitaPedidosInvalidos() throws Exception {
        String url = "/api/participacoes/" + participacao + "/visitas-agendadas";

        enviar(mvc, "POST", url, coordenador, "{\"dataHora\":\"" + dia(5) + "\"}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errosCampo[0].campo").value("visitaPlanejadaId"));
        enviar(mvc, "POST", url, coordenador, "{\"visitaPlanejadaId\":\"" + estudo.retornoId() + "\"}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errosCampo[0].campo").value("dataHora"));
        enviar(mvc, "POST", url, coordenador, "{ nao e json")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("CORPO_INVALIDO"));
        enviar(mvc, "POST", url, coordenador,
                "{\"visitaPlanejadaId\":\"" + estudo.retornoId() + "\",\"dataHora\":\"amanha de manha\"}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("CORPO_INVALIDO"));
        agendar(mvc, coordenador, participacao, estudo.retornoId(), hoje.minusDays(1) + "T09:30")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("DATA_NO_PASSADO"));
    }

    @ParameterizedTest(name = "POST 403: {0} não agenda")
    @ValueSource(strings = { TokenDeTeste.PESQUISADOR, TokenDeTeste.PARTICIPANTE, TokenDeTeste.ADMIN })
    @DisplayName("A escrita é exclusiva do coordenador")
    void soOCoordenadorAgenda(String email) throws Exception {
        String token = TokenDeTeste.bearer(mvc, email);

        agendar(mvc, token, participacao, estudo.retornoId(), dia(5))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));
    }

    @Test
    @DisplayName("POST 401: sem token")
    void semToken() throws Exception {
        agendar(mvc, null, participacao, estudo.retornoId(), dia(5)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST 404: participação inexistente e visita prevista inexistente")
    void recursosInexistentes() throws Exception {
        agendar(mvc, coordenador, UUID.randomUUID().toString(), estudo.retornoId(), dia(5))
                .andExpect(status().isNotFound());
        agendar(mvc, coordenador, participacao, UUID.randomUUID().toString(), dia(5))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST 409: data fora da janela traz a janela válida; nada é gravado")
    void dataForaDaJanela() throws Exception {
        agendar(mvc, coordenador, participacao, estudo.retornoId(), dia(2))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("DATA_FORA_DA_JANELA"))
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString(
                        hoje.plusDays(3).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")))));
        agendar(mvc, coordenador, participacao, estudo.retornoId(), dia(8)).andExpect(status().isConflict());

        enviar(mvc, "GET", "/api/participacoes/" + participacao + "/visitas", coordenador, null)
                .andExpect(jsonPath("$[1].agendamento").value(nullValue()));
    }

    @Test
    @DisplayName("POST 409: o mesmo par visita prevista e participação só pode ser agendado uma vez")
    void visitaJaAgendada() throws Exception {
        agendarRetorno(5);

        agendar(mvc, coordenador, participacao, estudo.retornoId(), dia(6))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("VISITA_JA_AGENDADA"));
    }

    @Test
    @DisplayName("POST 409: estudo fora de recrutamento ou execução")
    void estudoEncerrado() throws Exception {
        avancar(mvc, pesquisador, estudo.estudoId(), "EXECUCAO").andExpect(status().isOk());
        agendarRetorno(5);
        avancar(mvc, pesquisador, estudo.estudoId(), "ENCERRADO").andExpect(status().isOk());

        agendar(mvc, coordenador, participacao, estudo.triagemId(), dia(0))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO"));
    }

    @Test
    @DisplayName("POST: o estudo em execução também aceita o agendamento")
    void estudoEmExecucaoAceita() throws Exception {
        avancar(mvc, pesquisador, estudo.estudoId(), "EXECUCAO").andExpect(status().isOk());

        agendar(mvc, coordenador, participacao, estudo.retornoId(), dia(5)).andExpect(status().isCreated());
    }

    // ---- PUT ----

    @Test
    @DisplayName("PUT 200: reagenda para outro dia da janela e devolve a nova data")
    void reagenda() throws Exception {
        String visita = agendarRetorno(5);

        enviar(mvc, "PUT", "/api/visitas-agendadas/" + visita, coordenador, "{\"dataHora\":\"" + dia(7) + "\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(visita))
                .andExpect(jsonPath("$.situacao").value("AGENDADA"))
                .andExpect(jsonPath("$.dataHora").value(org.hamcrest.Matchers.startsWith(hoje.plusDays(7) + "T09:30")));
    }

    @Test
    @DisplayName("PUT 409: visita que já tem desfecho não pode ser reagendada")
    void reagendarVisitaComDesfecho() throws Exception {
        String triagem = agendarVisita(mvc, coordenador, participacao, estudo.triagemId(), hoje);
        registrarDesfecho(mvc, coordenador, triagem, "{\"tipo\":\"REALIZADA\"}").andExpect(status().isOk());

        enviar(mvc, "PUT", "/api/visitas-agendadas/" + triagem, coordenador, "{\"dataHora\":\"" + hoje + "T10:00\"}")
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("VISITA_NAO_ESTA_AGENDADA"));
    }

    @Test
    @DisplayName("PUT 409: reagendar fora da janela; PUT 400: data no passado e corpo sem data")
    void reagendarComDadosInvalidos() throws Exception {
        String visita = agendarRetorno(5);
        String url = "/api/visitas-agendadas/" + visita;

        enviar(mvc, "PUT", url, coordenador, "{\"dataHora\":\"" + dia(9) + "\"}")
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("DATA_FORA_DA_JANELA"));
        enviar(mvc, "PUT", url, coordenador, "{\"dataHora\":\"" + hoje.minusDays(1) + "T09:00\"}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("DATA_NO_PASSADO"));
        enviar(mvc, "PUT", url, coordenador, "{}").andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "PUT 403: {0} não reagenda")
    @ValueSource(strings = { TokenDeTeste.PESQUISADOR, TokenDeTeste.PARTICIPANTE, TokenDeTeste.ADMIN })
    @DisplayName("O reagendamento é exclusivo do coordenador")
    void soOCoordenadorReagenda(String email) throws Exception {
        String visita = agendarRetorno(5);
        String token = TokenDeTeste.bearer(mvc, email);

        enviar(mvc, "PUT", "/api/visitas-agendadas/" + visita, token, "{\"dataHora\":\"" + dia(6) + "\"}")
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT 404: agendamento inexistente; PUT 401: sem token")
    void reagendarInexistenteESemToken() throws Exception {
        enviar(mvc, "PUT", "/api/visitas-agendadas/" + UUID.randomUUID(), coordenador, "{\"dataHora\":\"" + dia(5) + "\"}")
                .andExpect(status().isNotFound());
        enviar(mvc, "PUT", "/api/visitas-agendadas/" + UUID.randomUUID(), null, "{\"dataHora\":\"" + dia(5) + "\"}")
                .andExpect(status().isUnauthorized());
    }

    // ---- GET .../visitas ----

    @Test
    @DisplayName("GET visitas: todas as visitas previstas com janela e sugestão; o agendamento só nas agendadas")
    void listaAsVisitasDoParticipante() throws Exception {
        agendarRetorno(5);

        enviar(mvc, "GET", "/api/participacoes/" + participacao + "/visitas", coordenador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome").value("Triagem"))
                .andExpect(jsonPath("$[0].dataSugerida").value(hoje.toString()))
                .andExpect(jsonPath("$[0].janelaInicio").value(hoje.toString()))
                .andExpect(jsonPath("$[0].janelaFim").value(hoje.toString()))
                .andExpect(jsonPath("$[0].agendamento").value(nullValue()))
                .andExpect(jsonPath("$[1].nome").value("Retorno"))
                .andExpect(jsonPath("$[1].dataSugerida").value(hoje.plusDays(5).toString()))
                .andExpect(jsonPath("$[1].janelaInicio").value(hoje.plusDays(3).toString()))
                .andExpect(jsonPath("$[1].janelaFim").value(hoje.plusDays(7).toString()))
                .andExpect(jsonPath("$[1].agendamento.situacao").value("AGENDADA"));
    }

    @Test
    @DisplayName("GET visitas: o pesquisador lê; administrador e participante recebem 403; sem token 401; inexistente 404")
    void permissoesDaLeitura() throws Exception {
        String url = "/api/participacoes/" + participacao + "/visitas";

        enviar(mvc, "GET", url, pesquisador, null).andExpect(status().isOk());
        enviar(mvc, "GET", url, administrador, null).andExpect(status().isForbidden());
        enviar(mvc, "GET", url, participante, null).andExpect(status().isForbidden());
        enviar(mvc, "GET", url, null, null).andExpect(status().isUnauthorized());
        enviar(mvc, "GET", "/api/participacoes/" + UUID.randomUUID() + "/visitas", coordenador, null)
                .andExpect(status().isNotFound());
    }
}
