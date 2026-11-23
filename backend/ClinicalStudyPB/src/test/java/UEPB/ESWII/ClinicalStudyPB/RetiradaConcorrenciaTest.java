package UEPB.ESWII.ClinicalStudyPB;

import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.enviar;
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
 * T106 (RF16, RQ-T106-15): concorrência no registro da retirada. Duas (ou mais) requisições simultâneas para retirar
 * o mesmo participante precisam resultar em exatamente um {@code 201} e os demais {@code 409 PARTICIPANTE_JA_RETIRADO},
 * nunca dois {@code 201} nem {@code 500}.
 *
 * <p>Não usa {@code @Transactional}: cada requisição roda em transação própria para que o bloqueio pessimista
 * ({@code buscarComBloqueio}) e a restrição única {@code uk_retiradas_participacao} atuem de verdade; os dados são
 * apagados no {@code @AfterEach}.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class RetiradaConcorrenciaTest {

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
            jdbc.update("DELETE FROM retiradas_participacao WHERE participacao_id IN " + participacoes, e);
            jdbc.update("DELETE FROM visitas_agendadas WHERE participacao_id IN " + participacoes, e);
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
                + "limite_participantes, status) VALUES (CAST(? AS UUID), ?, 'Concorrencia T106', CAST(? AS UUID), "
                + "?, ?, 6, 'EXECUCAO')", estudoId.toString(), "Concorrencia T106 " + estudoId, pesquisadorId.toString(),
                Date.valueOf(hoje.minusDays(30)), Date.valueOf(hoje.plusDays(180)));
        return estudoId;
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
                Date.valueOf(LocalDate.now(clock).minusDays(10)), coordenadorId.toString());
        return participacaoId;
    }

    private Callable<Object[]> retirar(UUID participacaoId, String motivo) {
        String corpo = "{\"motivo\":\"" + motivo + "\",\"solicitante\":\"PARTICIPANTE\",\"dataRetirada\":\""
                + LocalDate.now(clock) + "\"}";
        return () -> {
            var resposta = enviar(mvc, "POST", "/api/participacoes/" + participacaoId + "/retirada",
                    coordenadorToken, corpo).andReturn().getResponse();
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
    @DisplayName("RQ-T106-15: duas retiradas simultâneas do mesmo participante resultam em exatamente um 201 e um 409")
    void duasRetiradasSimultaneasDoMesmoParticipante() throws Exception {
        UUID estudoId = criarEstudo();
        UUID participacaoId = criarParticipacao(estudoId, "CAND-C1");

        List<Object[]> respostas = dispararJuntas(List.of(
                retirar(participacaoId, "Mudança de cidade"),
                retirar(participacaoId, "Pedido do participante")));

        assertThat(contar(respostas, 201)).as("retiradas aceitas").isEqualTo(1);
        assertThat(contar(respostas, 409)).as("retiradas recusadas").isEqualTo(1);
        assertThat(respostas).filteredOn(r -> (Integer) r[0] == 409)
                .allSatisfy(r -> assertThat((String) r[1]).contains("PARTICIPANTE_JA_RETIRADO"));

        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM retiradas_participacao WHERE participacao_id = CAST(? AS UUID)",
                Integer.class, participacaoId.toString())).isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "SELECT situacao FROM participacoes WHERE id = CAST(? AS UUID)",
                String.class, participacaoId.toString())).isEqualTo("RETIRADO");
    }

    @Test
    @DisplayName("Retiradas simultâneas de participantes diferentes no mesmo estudo não se bloqueiam (ambas dão 201)")
    void retiradasSimultaneasDeParticipantesDiferentes() throws Exception {
        UUID estudoId = criarEstudo();
        UUID participacao1 = criarParticipacao(estudoId, "CAND-D1");
        UUID participacao2 = criarParticipacao(estudoId, "CAND-D2");

        List<Object[]> respostas = dispararJuntas(List.of(
                retirar(participacao1, "Mudança de cidade"),
                retirar(participacao2, "Indisponibilidade de horário")));

        assertThat(respostas).allSatisfy(r -> assertThat(r[0]).isEqualTo(201));
    }
}
