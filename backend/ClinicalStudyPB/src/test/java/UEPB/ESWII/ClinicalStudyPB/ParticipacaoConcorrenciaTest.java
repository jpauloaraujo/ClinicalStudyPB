package UEPB.ESWII.ClinicalStudyPB;

import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.avaliarAprovado;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.consentir;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.criarCandidato;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.enviar;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.estudoEmRecrutamento;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;

/**
 * T62 (RF11): concorrência na inclusão de participantes. Várias requisições simultâneas disputam as últimas vagas
 * de um estudo e o bloqueio pessimista do estudo ({@code buscarComBloqueio}) tem de serializá-las: nunca pode haver
 * mais participantes que o limite, nem o mesmo candidato incluído duas vezes.
 *
 * <p>Este teste não usa {@code @Transactional}: cada requisição precisa da própria transação para que o bloqueio
 * funcione de verdade. Os dados ficam no H2 em memória (cada execução cria estudo e candidatos próprios).</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class ParticipacaoConcorrenciaTest {

    private static final int CANDIDATOS = 6;
    private static final int VAGAS = 2;

    @Autowired
    private MockMvc mvc;

    private String pesquisador;
    private String coordenador;

    @BeforeEach
    void preparar() throws Exception {
        pesquisador = TokenDeTeste.bearer(mvc, TokenDeTeste.PESQUISADOR);
        coordenador = TokenDeTeste.bearer(mvc, TokenDeTeste.COORDENADOR);
    }

    private String candidatoPronto(String estudo, String nome) throws Exception {
        String candidato = criarCandidato(mvc, coordenador, estudo, nome);
        avaliarAprovado(mvc, coordenador, candidato, estudo);
        consentir(mvc, coordenador, candidato).andExpect(status().isCreated());
        return candidato;
    }

    /** Dispara todas as tarefas ao mesmo tempo e devolve o status HTTP e o corpo de cada resposta. */
    private List<int[]> dispararJuntas(List<Callable<Object[]>> tarefas) throws Exception {
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
        List<int[]> resultados = new ArrayList<>();
        for (Future<Object[]> futuro : futuros) {
            Object[] resposta = futuro.get(60, TimeUnit.SECONDS);
            resultados.add(new int[] { (Integer) resposta[0] });
            respostas.add((String) resposta[1]);
        }
        pool.shutdown();
        return resultados;
    }

    private final List<String> respostas = java.util.Collections.synchronizedList(new ArrayList<>());

    private Callable<Object[]> incluir(String estudo, String candidato) {
        return () -> {
            var resposta = enviar(mvc, "POST", "/api/estudos/" + estudo + "/participantes", coordenador,
                    "{\"candidatoId\":\"" + candidato + "\"}").andReturn().getResponse();
            return new Object[] { resposta.getStatus(), resposta.getContentAsString() };
        };
    }

    @Test
    @DisplayName("Seis inclusões simultâneas para 2 vagas: exatamente 2 dão 201 e as 4 restantes dão 409 VAGAS_ESGOTADAS")
    void disputaPelasUltimasVagas() throws Exception {
        String estudo = estudoEmRecrutamento(mvc, pesquisador, VAGAS);
        List<Callable<Object[]>> tarefas = new ArrayList<>();
        for (int i = 0; i < CANDIDATOS; i++) {
            tarefas.add(incluir(estudo, candidatoPronto(estudo, "Candidato Concorrente " + i)));
        }

        List<int[]> resultados = dispararJuntas(tarefas);

        long criados = resultados.stream().filter(r -> r[0] == 201).count();
        long esgotados = resultados.stream().filter(r -> r[0] == 409).count();
        assertThat(criados).as("inclusões aceitas").isEqualTo(VAGAS);
        assertThat(esgotados).as("inclusões recusadas por falta de vaga").isEqualTo(CANDIDATOS - VAGAS);
        assertThat(respostas).filteredOn(corpo -> corpo.contains("VAGAS_ESGOTADAS")).hasSize(CANDIDATOS - VAGAS);

        enviar(mvc, "GET", "/api/estudos/" + estudo + "/participantes/vagas", coordenador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incluidos").value(VAGAS))
                .andExpect(jsonPath("$.restantes").value(0));
        String lista = enviar(mvc, "GET", "/api/estudos/" + estudo + "/participantes", coordenador, null)
                .andReturn().getResponse().getContentAsString();
        assertThat(JsonPath.<List<Object>>read(lista, "$")).hasSize(VAGAS);
    }

    @Test
    @DisplayName("O mesmo candidato incluído por várias requisições ao mesmo tempo vira um único participante")
    void mesmoCandidatoVariasVezes() throws Exception {
        String estudo = estudoEmRecrutamento(mvc, pesquisador, 5);
        String candidato = candidatoPronto(estudo, "Candidata Repetida");
        List<Callable<Object[]>> tarefas = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            tarefas.add(incluir(estudo, candidato));
        }

        List<int[]> resultados = dispararJuntas(tarefas);

        assertThat(resultados.stream().filter(r -> r[0] == 201).count()).isEqualTo(1);
        assertThat(resultados.stream().filter(r -> r[0] == 409).count()).isEqualTo(3);
        assertThat(respostas).filteredOn(corpo -> corpo.contains("CANDIDATO_JA_INCLUIDO")).hasSize(3);

        enviar(mvc, "GET", "/api/estudos/" + estudo + "/participantes/vagas", coordenador, null)
                .andExpect(jsonPath("$.incluidos").value(1));
    }

    @Test
    @DisplayName("Com vagas sobrando, inclusões simultâneas de candidatos diferentes são todas aceitas")
    void semDisputaTodasEntram() throws Exception {
        String estudo = estudoEmRecrutamento(mvc, pesquisador, 5);
        List<Callable<Object[]>> tarefas = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            tarefas.add(incluir(estudo, candidatoPronto(estudo, "Candidato Livre " + i)));
        }

        List<int[]> resultados = dispararJuntas(tarefas);

        assertThat(resultados).allSatisfy(r -> assertThat(r[0]).isEqualTo(201));
        enviar(mvc, "GET", "/api/estudos/" + estudo + "/participantes/vagas", coordenador, null)
                .andExpect(jsonPath("$.incluidos").value(4))
                .andExpect(jsonPath("$.restantes").value(1));
    }
}
