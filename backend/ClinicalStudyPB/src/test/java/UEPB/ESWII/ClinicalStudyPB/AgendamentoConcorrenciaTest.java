package UEPB.ESWII.ClinicalStudyPB;

import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.agendar;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Date;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;

/**
 * T78 (RF12, RQ-T78-15): concorrência no agendamento. Quatro requisições simultâneas para o mesmo par (participação e
 * visita prevista) precisam resultar em exatamente um {@code 201} e três {@code 409 VISITA_JA_AGENDADA}, com uma só
 * linha no banco, nunca dois {@code 201} nem {@code 500}.
 *
 * <p>Não usa {@code @Transactional}: cada requisição roda em transação própria para que a restrição única do par
 * atue de verdade; os dados são apagados no {@code @AfterEach}.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class AgendamentoConcorrenciaTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private Clock clock;

    private final List<UUID> estudosCriados = new ArrayList<>();
    private String coordenadorToken;
    private UUID coordenadorId;
    private UUID pesquisadorId;

    @BeforeEach
    void preparar() throws Exception {
        coordenadorToken = TokenDeTeste.bearer(mvc, TokenDeTeste.COORDENADOR);
        coordenadorId = jdbc.queryForObject("SELECT id FROM usuarios WHERE email = ?", UUID.class,
                "coordenador@clinicalstudy.test");
        pesquisadorId = jdbc.queryForObject("SELECT id FROM usuarios WHERE email = ?", UUID.class,
                "pesquisador@clinicalstudy.test");
    }

    @AfterEach
    void limpar() {
        for (UUID estudoId : estudosCriados) {
            String e = estudoId.toString();
            String participacoes = "(SELECT id FROM participacoes WHERE estudo_id = CAST(? AS UUID))";
            jdbc.update("DELETE FROM visitas_agendadas WHERE participacao_id IN " + participacoes, e);
            jdbc.update("DELETE FROM visitas_planejadas WHERE estudo_id = CAST(? AS UUID)", e);
            jdbc.update("DELETE FROM participacoes WHERE estudo_id = CAST(? AS UUID)", e);
            jdbc.update("DELETE FROM candidatos WHERE estudo_id = CAST(? AS UUID)", e);
            jdbc.update("DELETE FROM estudos WHERE id = CAST(? AS UUID)", e);
        }
        estudosCriados.clear();
    }

    private UUID criarEstudo() {
        UUID estudoId = UUID.randomUUID();
        estudosCriados.add(estudoId);
        LocalDate hoje = LocalDate.now(clock);
        jdbc.update("INSERT INTO estudos (id, titulo, objetivo, responsavel_id, data_inicio, data_fim, "
                + "limite_participantes, status) VALUES (CAST(? AS UUID), ?, 'Concorrencia T78', CAST(? AS UUID), "
                + "?, ?, 6, 'RECRUTAMENTO')", estudoId.toString(), "Concorrencia T78 " + estudoId,
                pesquisadorId.toString(), Date.valueOf(hoje.minusDays(30)), Date.valueOf(hoje.plusDays(180)));
        return estudoId;
    }

    /** Visita prevista no dia 0 com tolerância 0: para quem foi incluído hoje, a janela é só o dia de hoje. */
    private UUID criarVisitaPrevista(UUID estudoId) {
        UUID visitaId = UUID.randomUUID();
        jdbc.update("INSERT INTO visitas_planejadas (id, estudo_id, nome, dia_previsto, tolerancia, ordem) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), 'Triagem', 0, 0, 1)", visitaId.toString(),
                estudoId.toString());
        return visitaId;
    }

    private UUID criarParticipacao(UUID estudoId, String codigo) {
        UUID candidatoId = UUID.randomUUID();
        UUID participacaoId = UUID.randomUUID();
        jdbc.update("INSERT INTO candidatos (id, estudo_id, codigo, nome_ficticio, data_nascimento, situacao) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), ?, ?, DATE '1990-01-01', 'INCLUIDO')",
                candidatoId.toString(), estudoId.toString(), codigo, "Pessoa " + codigo + " (Ficticia)");
        jdbc.update("INSERT INTO participacoes (id, candidato_id, estudo_id, data_inclusao, incluido_por_id, situacao) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), CAST(? AS UUID), ?, CAST(? AS UUID), 'ATIVO')",
                participacaoId.toString(), candidatoId.toString(), estudoId.toString(),
                Date.valueOf(LocalDate.now(clock)), coordenadorId.toString());
        return participacaoId;
    }

    private Callable<Object[]> agendarEm(UUID participacaoId, UUID visitaId, String hora) {
        String dataHora = LocalDate.now(clock) + "T" + hora;
        return () -> {
            var resposta = agendar(mvc, coordenadorToken, participacaoId.toString(), visitaId.toString(), dataHora)
                    .andReturn().getResponse();
            return new Object[] { resposta.getStatus(), resposta.getContentAsString() };
        };
    }

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
    @DisplayName("RQ-T78-15: quatro agendamentos simultâneos do mesmo par resultam em um 201, três 409 e uma só linha")
    void quatroAgendamentosSimultaneosDoMesmoPar() throws Exception {
        UUID estudoId = criarEstudo();
        UUID visitaId = criarVisitaPrevista(estudoId);
        UUID participacaoId = criarParticipacao(estudoId, "CAND-A1");

        List<Object[]> respostas = dispararJuntas(List.of(
                agendarEm(participacaoId, visitaId, "08:00"), agendarEm(participacaoId, visitaId, "09:00"),
                agendarEm(participacaoId, visitaId, "10:00"), agendarEm(participacaoId, visitaId, "11:00")));

        assertThat(contar(respostas, 201)).as("agendamentos aceitos").isEqualTo(1);
        assertThat(contar(respostas, 409)).as("agendamentos recusados").isEqualTo(3);
        assertThat(contar(respostas, 500)).as("erros do servidor").isZero();
        assertThat(respostas).filteredOn(r -> (Integer) r[0] == 409)
                .allSatisfy(r -> assertThat((String) r[1]).contains("VISITA_JA_AGENDADA"));
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM visitas_agendadas WHERE participacao_id = CAST(? AS UUID)",
                Integer.class, participacaoId.toString())).isEqualTo(1);
    }

    @Test
    @DisplayName("Agendamentos simultâneos de participantes diferentes para a mesma visita prevista não se bloqueiam")
    void participantesDiferentesNaoSeBloqueiam() throws Exception {
        UUID estudoId = criarEstudo();
        UUID visitaId = criarVisitaPrevista(estudoId);
        UUID primeiro = criarParticipacao(estudoId, "CAND-B1");
        UUID segundo = criarParticipacao(estudoId, "CAND-B2");

        List<Object[]> respostas = dispararJuntas(List.of(
                agendarEm(primeiro, visitaId, "08:00"), agendarEm(segundo, visitaId, "09:00")));

        assertThat(respostas).allSatisfy(r -> assertThat(r[0]).isEqualTo(201));
    }
}
