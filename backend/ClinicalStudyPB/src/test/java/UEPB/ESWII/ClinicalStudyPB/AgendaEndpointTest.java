package UEPB.ESWII.ClinicalStudyPB;

import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.agendar;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.agendarVisita;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.enviar;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.estudoParaAgendamento;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.participanteIncluido;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.registrarDesfecho;
import static org.hamcrest.Matchers.hasSize;
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
 * T78 (RF12, RQ-T78-13): a agenda do estudo ({@code GET /api/estudos/{id}/agenda}) e a lista de participantes com o
 * andamento do agendamento, com o banco e os tokens reais: ordenação por data, filtros {@code de}, {@code ate} e
 * {@code situacao}, paginação, parâmetros inválidos (400 {@code PARAMETRO_INVALIDO}), permissões por perfil e o
 * progresso por participante ({@code visitasPrevistas} e {@code visitasAgendadas}).
 *
 * <p>Cenário: três participantes (códigos em ordem) num estudo com a triagem (hoje) e o retorno (hoje + 3 a hoje + 7).
 * O retorno do primeiro vai para hoje + 7, o do segundo para hoje + 4 e o do terceiro para hoje + 5; a triagem do
 * primeiro é agendada e realizada.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AgendaEndpointTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private Clock clock;

    private String coordenador;
    private String pesquisador;
    private LocalDate hoje;
    private EstudoParaAgendamento estudo;
    private String agenda;
    private String primeiroNome = "Pessoa Um (Ficticia)";
    private String segundoNome = "Pessoa Dois (Ficticia)";
    private String terceiroNome = "Pessoa Tres (Ficticia)";

    @BeforeEach
    void montar() throws Exception {
        coordenador = TokenDeTeste.bearer(mvc, TokenDeTeste.COORDENADOR);
        pesquisador = TokenDeTeste.bearer(mvc, TokenDeTeste.PESQUISADOR);
        hoje = LocalDate.now(clock);
        estudo = estudoParaAgendamento(mvc, pesquisador, 5);
        agenda = "/api/estudos/" + estudo.estudoId() + "/agenda";

        String primeiro = participanteIncluido(mvc, coordenador, estudo.estudoId(), primeiroNome);
        String segundo = participanteIncluido(mvc, coordenador, estudo.estudoId(), segundoNome);
        String terceiro = participanteIncluido(mvc, coordenador, estudo.estudoId(), terceiroNome);

        String triagem = agendarVisita(mvc, coordenador, primeiro, estudo.triagemId(), hoje);
        registrarDesfecho(mvc, coordenador, triagem, "{\"tipo\":\"REALIZADA\"}").andExpect(status().isOk());
        agendar(mvc, coordenador, primeiro, estudo.retornoId(), hoje.plusDays(7) + "T09:00")
                .andExpect(status().isCreated());
        agendar(mvc, coordenador, segundo, estudo.retornoId(), hoje.plusDays(4) + "T08:00")
                .andExpect(status().isCreated());
        agendar(mvc, coordenador, terceiro, estudo.retornoId(), hoje.plusDays(5) + "T10:00")
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Ordena por data e hora, da mais próxima para a mais distante, com os dados do participante e da visita")
    void ordenaPorData() throws Exception {
        enviar(mvc, "GET", agenda + "?tamanho=100", coordenador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(4)))
                .andExpect(jsonPath("$.conteudo[0].visitaNome").value("Triagem"))
                .andExpect(jsonPath("$.conteudo[1].participanteNome").value(segundoNome))
                .andExpect(jsonPath("$.conteudo[2].participanteNome").value(terceiroNome))
                .andExpect(jsonPath("$.conteudo[3].participanteNome").value(primeiroNome))
                .andExpect(jsonPath("$.conteudo[3].visitaNome").value("Retorno"))
                .andExpect(jsonPath("$.conteudo[3].situacao").value("AGENDADA"))
                .andExpect(jsonPath("$.conteudo[3].participanteCodigo").isNotEmpty())
                .andExpect(jsonPath("$.conteudo[3].dataHora").isNotEmpty());
    }

    @Test
    @DisplayName("Os filtros de e ate são inclusivos e podem ser usados sozinhos ou juntos")
    void filtraPorPeriodo() throws Exception {
        enviar(mvc, "GET", agenda + "?de=" + hoje.plusDays(5) + "&ate=" + hoje.plusDays(5), coordenador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(1)))
                .andExpect(jsonPath("$.conteudo[0].participanteNome").value(terceiroNome));
        enviar(mvc, "GET", agenda + "?de=" + hoje.plusDays(5), coordenador, null)
                .andExpect(jsonPath("$.conteudo", hasSize(2)));
        enviar(mvc, "GET", agenda + "?ate=" + hoje.plusDays(4), coordenador, null)
                .andExpect(jsonPath("$.conteudo", hasSize(2)));
        enviar(mvc, "GET", agenda + "?de=" + hoje.plusDays(1) + "&ate=" + hoje.plusDays(3), coordenador, null)
                .andExpect(jsonPath("$.conteudo", hasSize(0)))
                .andExpect(jsonPath("$.totalElementos").value(0));
    }

    @Test
    @DisplayName("Filtra pela situação da visita")
    void filtraPorSituacao() throws Exception {
        enviar(mvc, "GET", agenda + "?situacao=AGENDADA", coordenador, null)
                .andExpect(jsonPath("$.conteudo", hasSize(3)));
        enviar(mvc, "GET", agenda + "?situacao=REALIZADA", coordenador, null)
                .andExpect(jsonPath("$.conteudo", hasSize(1)))
                .andExpect(jsonPath("$.conteudo[0].visitaNome").value("Triagem"));
        enviar(mvc, "GET", agenda + "?situacao=CANCELADA", coordenador, null)
                .andExpect(jsonPath("$.conteudo", hasSize(0)));
    }

    @Test
    @DisplayName("Pagina com totalElementos e totalPaginas, sem repetir itens entre as páginas")
    void paginacao() throws Exception {
        enviar(mvc, "GET", agenda + "?pagina=0&tamanho=3", coordenador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(3)))
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanho").value(3))
                .andExpect(jsonPath("$.totalElementos").value(4))
                .andExpect(jsonPath("$.totalPaginas").value(2));
        enviar(mvc, "GET", agenda + "?pagina=1&tamanho=3", coordenador, null)
                .andExpect(jsonPath("$.conteudo", hasSize(1)))
                .andExpect(jsonPath("$.conteudo[0].participanteNome").value(primeiroNome))
                .andExpect(jsonPath("$.pagina").value(1));
    }

    @ParameterizedTest(name = "400 PARAMETRO_INVALIDO: {0}")
    @ValueSource(strings = { "pagina=-1", "tamanho=0", "tamanho=101", "situacao=INEXISTENTE", "de=ontem",
            "de=2026-12-10&ate=2026-12-01" })
    @DisplayName("Parâmetros inválidos recebem 400")
    void parametrosInvalidos(String consulta) throws Exception {
        enviar(mvc, "GET", agenda + "?" + consulta, coordenador, null)
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("PARAMETRO_INVALIDO"));
    }

    @Test
    @DisplayName("Tamanho 100 é aceito")
    void tamanhoMaximo() throws Exception {
        enviar(mvc, "GET", agenda + "?tamanho=100", coordenador, null)
                .andExpect(status().isOk()).andExpect(jsonPath("$.tamanho").value(100));
    }

    @ParameterizedTest(name = "200: {0} lê a agenda e os participantes")
    @ValueSource(strings = { TokenDeTeste.COORDENADOR, TokenDeTeste.PESQUISADOR })
    @DisplayName("Coordenador e pesquisador leem")
    void perfisQueLeem(String email) throws Exception {
        String token = TokenDeTeste.bearer(mvc, email);

        enviar(mvc, "GET", agenda, token, null).andExpect(status().isOk());
        enviar(mvc, "GET", agenda + "/participantes", token, null).andExpect(status().isOk());
    }

    @ParameterizedTest(name = "403: {0} não lê a agenda")
    @ValueSource(strings = { TokenDeTeste.ADMIN, TokenDeTeste.PARTICIPANTE })
    @DisplayName("Administrador e participante recebem 403")
    void perfisSemPermissao(String email) throws Exception {
        String token = TokenDeTeste.bearer(mvc, email);

        enviar(mvc, "GET", agenda, token, null).andExpect(status().isForbidden());
        enviar(mvc, "GET", agenda + "/participantes", token, null).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("401 sem token e 404 para estudo inexistente")
    void semTokenEEstudoInexistente() throws Exception {
        enviar(mvc, "GET", agenda, null, null).andExpect(status().isUnauthorized());
        enviar(mvc, "GET", agenda + "/participantes", null, null).andExpect(status().isUnauthorized());
        enviar(mvc, "GET", "/api/estudos/" + UUID.randomUUID() + "/agenda", coordenador, null)
                .andExpect(status().isNotFound());
        enviar(mvc, "GET", "/api/estudos/" + UUID.randomUUID() + "/agenda/participantes", coordenador, null)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Participantes: por código, com as visitas previstas e quantas já têm agendamento")
    void progressoPorParticipante() throws Exception {
        enviar(mvc, "GET", agenda + "/participantes", coordenador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].nomeFicticio").value(primeiroNome))
                .andExpect(jsonPath("$[0].visitasPrevistas").value(2))
                .andExpect(jsonPath("$[0].visitasAgendadas").value(2))
                .andExpect(jsonPath("$[1].nomeFicticio").value(segundoNome))
                .andExpect(jsonPath("$[1].visitasAgendadas").value(1))
                .andExpect(jsonPath("$[2].nomeFicticio").value(terceiroNome))
                .andExpect(jsonPath("$[2].visitasAgendadas").value(1))
                .andExpect(jsonPath("$[0].situacaoParticipacao").value("ATIVO"))
                .andExpect(jsonPath("$[0].dataInclusao").value(hoje.toString()))
                .andExpect(jsonPath("$[0].codigo").isNotEmpty());
    }
}
