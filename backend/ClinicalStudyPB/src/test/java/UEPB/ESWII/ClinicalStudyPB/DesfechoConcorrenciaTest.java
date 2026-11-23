package UEPB.ESWII.ClinicalStudyPB;

import static UEPB.ESWII.ClinicalStudyPB.support.CenarioDesfecho.estudoComVisita;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioDesfecho.visitaAgendada;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.enviar;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.registrarDesfecho;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import UEPB.ESWII.ClinicalStudyPB.support.CenarioDesfecho.EstudoComVisita;
import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;

/**
 * T85 (RF13): concorrência no desfecho. Requisições simultâneas para a mesma visita têm de resultar em exatamente
 * um desfecho: uma resposta 200 e as demais 409 ({@code TRANSICAO_VISITA_INVALIDA}, quando a requisição lenta já
 * encontra a visita finalizada, ou {@code VISITA_ALTERADA_CONCORRENTEMENTE}, quando a versão otimista é a barreira),
 * nunca dois desfechos nem erro 500.
 *
 * <p>Este teste não usa {@code @Transactional}: cada requisição precisa da própria transação para o bloqueio e o
 * controle de versão funcionarem de verdade. Os dados são apagados no {@code @AfterEach}.</p>
 *
 * <p>Para provar que o teste detecta a falha, troque temporariamente {@code buscarComBloqueio} por {@code findById}
 * no {@code DesfechoVisitaService} <b>e</b> comente o {@code @Version} de {@code VisitaAgendada}: o teste tem de
 * falhar (mais de um 200). Os dois mecanismos se protegem entre si; remover só um deles não faz o teste falhar.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class DesfechoConcorrenciaTest {

    private static final int REQUISICOES = 6;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    private final List<String> estudosCriados = new ArrayList<>();

    private String pesquisador;
    private String coordenador;

    @BeforeEach
    void preparar() throws Exception {
        pesquisador = TokenDeTeste.bearer(mvc, TokenDeTeste.PESQUISADOR);
        coordenador = TokenDeTeste.bearer(mvc, TokenDeTeste.COORDENADOR);
    }

    @AfterEach
    void limpar() {
        for (String estudo : estudosCriados) {
            String candidatos = "(SELECT id FROM candidatos WHERE estudo_id = CAST(? AS UUID))";
            String avaliacoes = "(SELECT id FROM avaliacoes_elegibilidade WHERE candidato_id IN " + candidatos + ")";
            String visitas = "(SELECT id FROM visitas_planejadas WHERE estudo_id = CAST(? AS UUID))";
            jdbc.update("DELETE FROM visitas_agendadas WHERE participacao_id IN "
                    + "(SELECT id FROM participacoes WHERE estudo_id = CAST(? AS UUID))", estudo);
            jdbc.update("DELETE FROM participacoes WHERE estudo_id = CAST(? AS UUID)", estudo);
            jdbc.update("DELETE FROM consentimentos WHERE candidato_id IN " + candidatos, estudo);
            jdbc.update("DELETE FROM respostas_avaliacao WHERE avaliacao_id IN " + avaliacoes, estudo);
            jdbc.update("DELETE FROM avaliacoes_elegibilidade WHERE candidato_id IN " + candidatos, estudo);
            jdbc.update("DELETE FROM candidatos WHERE estudo_id = CAST(? AS UUID)", estudo);
            jdbc.update("DELETE FROM acoes WHERE visita_id IN " + visitas, estudo);
            jdbc.update("DELETE FROM visitas_planejadas WHERE estudo_id = CAST(? AS UUID)", estudo);
            jdbc.update("DELETE FROM criterios WHERE estudo_id = CAST(? AS UUID)", estudo);
            jdbc.update("DELETE FROM historico_status_estudo WHERE estudo_id = CAST(? AS UUID)", estudo);
            jdbc.update("DELETE FROM estudos WHERE id = CAST(? AS UUID)", estudo);
        }
        estudosCriados.clear();
    }

    private EstudoComVisita novoEstudo(int limite) throws Exception {
        EstudoComVisita estudo = estudoComVisita(mvc, pesquisador, limite);
        estudosCriados.add(estudo.estudoId());
        return estudo;
    }

    private String visitaDeOntem(EstudoComVisita estudo) throws Exception {
        return visitaAgendada(mvc, jdbc, coordenador, estudo, LocalDateTime.now().minusDays(1));
    }

    private Callable<Object[]> desfecho(String visita, String corpo) {
        return () -> {
            var resposta = registrarDesfecho(mvc, coordenador, visita, corpo).andReturn().getResponse();
            return new Object[] { resposta.getStatus(), resposta.getContentAsString() };
        };
    }

    /** Dispara todas as tarefas ao mesmo tempo e devolve o status e o corpo de cada resposta. */
    private List<Object[]> dispararJuntas(List<Callable<Object[]>> tarefas) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(tarefas.size());
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<Object[]>> futuros = new ArrayList<>();
        for (Callable<Object[]> tarefa : tarefas) {
            futuros.add(pool.submit(() -> {
                largada.await();
                return tarefa.call();
            }));
        }
        largada.countDown();
        List<Object[]> respostas = new ArrayList<>();
        for (Future<Object[]> futuro : futuros) {
            respostas.add(futuro.get(60, TimeUnit.SECONDS));
        }
        pool.shutdown();
        return respostas;
    }

    private static long contar(List<Object[]> respostas, int status) {
        return respostas.stream().filter(r -> (Integer) r[0] == status).count();
    }

    @Test
    @DisplayName("Seis desfechos iguais ao mesmo tempo: exatamente 1 dá 200 e as outras 5 dão 409, nunca 500")
    void mesmoDesfechoVariasVezes() throws Exception {
        String visita = visitaDeOntem(novoEstudo(5));
        List<Callable<Object[]>> tarefas = new ArrayList<>();
        for (int i = 0; i < REQUISICOES; i++) {
            tarefas.add(desfecho(visita, "{\"tipo\":\"AUSENTE\"}"));
        }

        List<Object[]> respostas = dispararJuntas(tarefas);

        assertThat(contar(respostas, 200)).as("desfechos aceitos").isEqualTo(1);
        assertThat(contar(respostas, 409)).as("desfechos recusados").isEqualTo(REQUISICOES - 1);
        assertThat(respostas).filteredOn(r -> (Integer) r[0] == 409).allSatisfy(r -> assertThat((String) r[1])
                .containsAnyOf("TRANSICAO_VISITA_INVALIDA", "VISITA_ALTERADA_CONCORRENTEMENTE"));

        enviar(mvc, "GET", "/api/visitas-agendadas/" + visita, coordenador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("AUSENTE"))
                .andExpect(jsonPath("$.versao").value(1));
    }

    @Test
    @DisplayName("Realizada, ausente e cancelada ao mesmo tempo: só um desfecho vence e o estado final é coerente")
    void desfechosDiferentesNaMesmaVisita() throws Exception {
        String visita = visitaDeOntem(novoEstudo(5));
        List<Callable<Object[]>> tarefas = List.of(
                desfecho(visita, "{\"tipo\":\"REALIZADA\"}"),
                desfecho(visita, "{\"tipo\":\"AUSENTE\"}"),
                desfecho(visita, "{\"tipo\":\"CANCELADA\",\"motivo\":\"Participante em viagem\"}"),
                desfecho(visita, "{\"tipo\":\"REALIZADA\"}"),
                desfecho(visita, "{\"tipo\":\"CANCELADA\",\"motivo\":\"Remarcada pelo centro\"}"));

        List<Object[]> respostas = dispararJuntas(tarefas);

        assertThat(contar(respostas, 200)).as("desfechos aceitos").isEqualTo(1);
        assertThat(contar(respostas, 409)).as("desfechos recusados").isEqualTo(tarefas.size() - 1);

        String situacao = jsonDe(visita, "$.situacao");
        assertThat(situacao).isIn("REALIZADA", "AUSENTE", "CANCELADA");
        // Só o desfecho vencedor deixa marcas: data real só na realizada, motivo só na cancelada
        enviar(mvc, "GET", "/api/visitas-agendadas/" + visita, coordenador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versao").value(1))
                .andExpect(situacao.equals("REALIZADA")
                        ? jsonPath("$.dataHoraRealizada").isNotEmpty()
                        : jsonPath("$.dataHoraRealizada").doesNotExist())
                .andExpect(situacao.equals("CANCELADA")
                        ? jsonPath("$.motivo").isNotEmpty()
                        : jsonPath("$.motivo").doesNotExist());
    }

    @Test
    @DisplayName("Desfechos simultâneos em visitas diferentes não se atrapalham: todos dão 200")
    void visitasDiferentesNaoSeBloqueiam() throws Exception {
        EstudoComVisita estudo = novoEstudo(8);
        List<Callable<Object[]>> tarefas = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            tarefas.add(desfecho(visitaDeOntem(estudo), "{\"tipo\":\"AUSENTE\"}"));
        }

        List<Object[]> respostas = dispararJuntas(tarefas);

        assertThat(respostas).allSatisfy(r -> assertThat(r[0]).isEqualTo(200));
    }

    private String jsonDe(String visita, String caminho) throws Exception {
        String corpo = enviar(mvc, "GET", "/api/visitas-agendadas/" + visita, coordenador, null)
                .andReturn().getResponse().getContentAsString();
        return com.jayway.jsonpath.JsonPath.read(corpo, caminho);
    }
}
