package UEPB.ESWII.ClinicalStudyPB;

import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.enviar;
import static org.assertj.core.api.Assertions.assertThat;

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
 * T111 (RQ-T111-04): corrida no vínculo da conta. Várias requisições simultâneas tentam ligar a <b>mesma conta</b> a
 * participantes diferentes (e várias contas ao <b>mesmo participante</b>): o banco decide, e o resultado é sempre um
 * sucesso e o resto 409, nunca 500 nem dois vínculos. Sem {@code @Transactional}, para cada requisição ter a própria
 * transação; os dados criados são apagados no {@code @AfterEach}.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ContaParticipanteConcorrenciaTest {

    private static final String COORDENADOR_ID = "22222222-2222-4222-8222-222222222222";
    private static final int TENTATIVAS = 4;

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;

    private final List<UUID> estudos = new ArrayList<>();
    private final List<UUID> contas = new ArrayList<>();
    private String coordenador;

    @BeforeEach
    void preparar() throws Exception {
        coordenador = TokenDeTeste.bearer(mvc, TokenDeTeste.COORDENADOR);
    }

    @AfterEach
    void limpar() {
        for (UUID estudo : estudos) {
            jdbc.update("DELETE FROM participacoes WHERE estudo_id = CAST(? AS UUID)", estudo.toString());
            jdbc.update("DELETE FROM candidatos WHERE estudo_id = CAST(? AS UUID)", estudo.toString());
            jdbc.update("DELETE FROM estudos WHERE id = CAST(? AS UUID)", estudo.toString());
        }
        for (UUID conta : contas) {
            jdbc.update("DELETE FROM usuarios WHERE id = CAST(? AS UUID)", conta.toString());
        }
        estudos.clear();
        contas.clear();
    }

    private UUID novoEstudo() {
        UUID estudo = UUID.randomUUID();
        jdbc.update("INSERT INTO estudos (id, titulo, objetivo, responsavel_id, data_inicio, data_fim, "
                + "limite_participantes, status) SELECT CAST(? AS UUID), 'Estudo T111 concorrência', 'Objetivo.', id, "
                + "DATE '2026-11-01', DATE '2027-05-01', 20, 'RECRUTAMENTO' FROM usuarios "
                + "WHERE email = 'pesquisador@clinicalstudy.test'", estudo.toString());
        estudos.add(estudo);
        return estudo;
    }

    private UUID novaConta() {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO usuarios (id, nome, email, senha, perfil, ativo) VALUES (CAST(? AS UUID), "
                + "'Conta Concorrente (Ficticia)', ?, 'x', 'PARTICIPANTE', TRUE)", id.toString(),
                "t111.conc." + id + "@clinicalstudy.test");
        contas.add(id);
        return id;
    }

    private UUID novaParticipacao(UUID estudo, String codigo) {
        UUID candidato = UUID.randomUUID();
        UUID participacao = UUID.randomUUID();
        jdbc.update("INSERT INTO candidatos (id, estudo_id, codigo, nome_ficticio, data_nascimento, usuario_id, "
                + "situacao) VALUES (CAST(? AS UUID), CAST(? AS UUID), ?, ?, DATE '1990-01-01', "
                + "NULL, 'INCLUIDO')", candidato.toString(), estudo.toString(), codigo,
                // nome único por candidato: o índice da Sprint 2 recusa nome e data repetidos no estudo (D-T111-03)
                "Pessoa " + codigo + " (Ficticia)");
        jdbc.update("INSERT INTO participacoes (id, candidato_id, estudo_id, data_inclusao, incluido_por_id) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), CAST(? AS UUID), CURRENT_DATE, CAST(? AS UUID))",
                participacao.toString(), candidato.toString(), estudo.toString(), COORDENADOR_ID);
        return participacao;
    }

    private Callable<Object[]> vincular(UUID participacao, UUID conta) {
        return () -> {
            var resposta = enviar(mvc, "PUT", "/api/participacoes/" + participacao + "/conta", coordenador,
                    "{\"usuarioId\":\"" + conta + "\"}").andReturn().getResponse();
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
        List<Object[]> resultados = new ArrayList<>();
        for (Future<Object[]> futuro : futuros) {
            resultados.add(futuro.get(60, TimeUnit.SECONDS));
        }
        pool.shutdown();
        return resultados;
    }

    @Test
    @DisplayName("A mesma conta disputada por vários participantes: 1 sucesso, o resto 409 CONTA_JA_VINCULADA, nunca 500")
    void mesmaContaEmVariosParticipantes() throws Exception {
        UUID estudo = novoEstudo();
        UUID conta = novaConta();
        List<Callable<Object[]>> tarefas = new ArrayList<>();
        for (int i = 0; i < TENTATIVAS; i++) {
            tarefas.add(vincular(novaParticipacao(estudo, "T111-C" + i), conta));
        }

        List<Object[]> resultados = dispararJuntas(tarefas);

        assertThat(resultados).extracting(r -> r[0]).as("nenhum 500").doesNotContain(500);
        assertThat(resultados.stream().filter(r -> (int) r[0] == 200).count()).as("vínculos aceitos").isEqualTo(1);
        assertThat(resultados.stream().filter(r -> (int) r[0] == 409).count()).isEqualTo(TENTATIVAS - 1);
        assertThat(resultados.stream().filter(r -> (int) r[0] == 409)
                .allMatch(r -> ((String) r[1]).contains("CONTA_JA_VINCULADA"))).isTrue();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM candidatos WHERE usuario_id = CAST(? AS UUID)",
                Integer.class, conta.toString())).as("candidatos com a conta").isEqualTo(1);
    }

    @Test
    @DisplayName("Várias contas disputando o mesmo participante: 1 sucesso, o resto 409 PARTICIPACAO_JA_TEM_CONTA, nunca 500")
    void variasContasNoMesmoParticipante() throws Exception {
        UUID estudo = novoEstudo();
        UUID participacao = novaParticipacao(estudo, "T111-P0");
        List<Callable<Object[]>> tarefas = new ArrayList<>();
        for (int i = 0; i < TENTATIVAS; i++) {
            tarefas.add(vincular(participacao, novaConta()));
        }

        List<Object[]> resultados = dispararJuntas(tarefas);

        assertThat(resultados).extracting(r -> r[0]).as("nenhum 500").doesNotContain(500);
        assertThat(resultados.stream().filter(r -> (int) r[0] == 200).count()).as("vínculos aceitos").isEqualTo(1);
        assertThat(resultados.stream().filter(r -> (int) r[0] == 409)
                .allMatch(r -> ((String) r[1]).contains("PARTICIPACAO_JA_TEM_CONTA"))).isTrue();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM candidatos WHERE estudo_id = CAST(? AS UUID) "
                + "AND usuario_id IS NOT NULL", Integer.class, estudo.toString())).isEqualTo(1);
    }
}
