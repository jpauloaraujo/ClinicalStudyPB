package UEPB.ESWII.ClinicalStudyPB;

import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.enviar;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SolicitanteRetirada;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaAgendadaRepository;
import UEPB.ESWII.ClinicalStudyPB.service.RetiradaService;
import UEPB.ESWII.ClinicalStudyPB.service.RetiradaService.ComandoRetirada;
import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;

/**
 * T106 (RQ-T106-06 a RQ-T106-10): integração com banco real provando o cancelamento em cascata apenas das visitas
 * futuras {@code AGENDADA}, a preservação de vencidas, finalizadas e resultados no histórico, e o rollback completo da
 * transação quando ocorre uma falha no meio do cancelamento.
 *
 * <p>Não usa {@code @Transactional} na classe para que a transação de {@link RetiradaService#registrar} faça commit ou
 * rollback real no banco; os dados são removidos no {@code @AfterEach}.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class RetiradaCascataIntegracaoTest {

    private static final String COORDENADOR_EMAIL = "coordenador@clinicalstudy.test";
    private static final String PESQUISADOR_EMAIL = "pesquisador@clinicalstudy.test";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private RetiradaService retiradaService;
    @Autowired
    private Clock clock;

    @MockitoSpyBean
    private VisitaAgendadaRepository visitasAgendadas;

    private final List<UUID> estudosCriados = new ArrayList<>();
    private String coordenadorToken;
    private String pesquisadorToken;
    private UUID coordenadorId;
    private UUID pesquisadorId;

    @BeforeEach
    void preparar() throws Exception {
        coordenadorToken = TokenDeTeste.bearer(mvc, TokenDeTeste.COORDENADOR);
        pesquisadorToken = TokenDeTeste.bearer(mvc, TokenDeTeste.PESQUISADOR);
        coordenadorId = jdbc.queryForObject("SELECT id FROM usuarios WHERE email = ?", UUID.class, COORDENADOR_EMAIL);
        pesquisadorId = jdbc.queryForObject("SELECT id FROM usuarios WHERE email = ?", UUID.class, PESQUISADOR_EMAIL);
    }

    @AfterEach
    void limpar() {
        reset(visitasAgendadas);
        for (UUID estudoId : estudosCriados) {
            String e = estudoId.toString();
            String participacoes = "(SELECT id FROM participacoes WHERE estudo_id = CAST(? AS UUID))";
            String visitasAg = "(SELECT id FROM visitas_agendadas WHERE participacao_id IN " + participacoes + ")";
            String candidatos = "(SELECT id FROM candidatos WHERE estudo_id = CAST(? AS UUID))";
            String visitasPl = "(SELECT id FROM visitas_planejadas WHERE estudo_id = CAST(? AS UUID))";
            jdbc.update("DELETE FROM resultados_atividade WHERE visita_agendada_id IN " + visitasAg, e);
            jdbc.update("DELETE FROM retiradas_participacao WHERE participacao_id IN " + participacoes, e);
            jdbc.update("DELETE FROM visitas_agendadas WHERE participacao_id IN " + participacoes, e);
            jdbc.update("DELETE FROM participacoes WHERE estudo_id = CAST(? AS UUID)", e);
            jdbc.update("DELETE FROM candidatos WHERE estudo_id = CAST(? AS UUID)", e);
            jdbc.update("DELETE FROM acoes WHERE visita_id IN " + visitasPl, e);
            jdbc.update("DELETE FROM visitas_planejadas WHERE estudo_id = CAST(? AS UUID)", e);
            jdbc.update("DELETE FROM estudos WHERE id = CAST(? AS UUID)", e);
        }
        estudosCriados.clear();
    }

    private UUID criarEstudo() {
        UUID estudoId = UUID.randomUUID();
        estudosCriados.add(estudoId);
        LocalDate hoje = LocalDate.now(clock);
        jdbc.update("INSERT INTO estudos (id, titulo, objetivo, responsavel_id, data_inicio, data_fim, "
                + "limite_participantes, status) VALUES (CAST(? AS UUID), ?, 'Testar cascata T106', CAST(? AS UUID), "
                + "?, ?, 6, 'EXECUCAO')", estudoId.toString(), "Cascata T106 " + estudoId, pesquisadorId.toString(),
                Date.valueOf(hoje.minusDays(60)), Date.valueOf(hoje.plusDays(180)));
        return estudoId;
    }

    private UUID criarVisitaPlanejada(UUID estudoId, String nome, int dia, int ordem) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO visitas_planejadas (id, estudo_id, nome, dia_previsto, tolerancia, ordem) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), ?, ?, 5, ?)",
                id.toString(), estudoId.toString(), nome, dia, ordem);
        return id;
    }

    private UUID criarParticipacao(UUID estudoId) {
        UUID candidatoId = UUID.randomUUID();
        UUID participacaoId = UUID.randomUUID();
        jdbc.update("INSERT INTO candidatos (id, estudo_id, codigo, nome_ficticio, data_nascimento, situacao) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), 'CAND-C01', 'Pessoa Cascata (Ficticia)', DATE '1990-01-01', 'INCLUIDO')",
                candidatoId.toString(), estudoId.toString());
        jdbc.update("INSERT INTO participacoes (id, candidato_id, estudo_id, data_inclusao, incluido_por_id, situacao) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), CAST(? AS UUID), ?, CAST(? AS UUID), 'ATIVO')",
                participacaoId.toString(), candidatoId.toString(), estudoId.toString(),
                Date.valueOf(LocalDate.now(clock).minusDays(40)), coordenadorId.toString());
        return participacaoId;
    }

    private UUID criarVisitaAgendada(UUID participacaoId, UUID visitaPlanejadaId, LocalDateTime dataHora,
            String situacao, LocalDateTime realizada, String motivo, UUID autorInicial, LocalDateTime alteradoEm) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO visitas_agendadas (id, participacao_id, visita_planejada_id, data_hora_agendada, "
                + "situacao, data_hora_realizada, motivo, observacao, versao, criado_por_id, criado_em, "
                + "alterado_por_id, alterado_em) VALUES (CAST(? AS UUID), CAST(? AS UUID), CAST(? AS UUID), ?, ?, ?, "
                + "?, NULL, 0, CAST(? AS UUID), ?, CAST(? AS UUID), ?)",
                id.toString(), participacaoId.toString(), visitaPlanejadaId.toString(), Timestamp.valueOf(dataHora),
                situacao, realizada == null ? null : Timestamp.valueOf(realizada), motivo,
                autorInicial.toString(), Timestamp.valueOf(alteradoEm), autorInicial.toString(),
                Timestamp.valueOf(alteradoEm));
        return id;
    }

    private void registrarResultado(UUID visitaPlanejadaId, UUID visitaAgendadaId, String nomeAcao, String valor) {
        UUID acaoId = UUID.randomUUID();
        Timestamp ts = Timestamp.valueOf(LocalDateTime.now(clock).minusDays(30));
        jdbc.update("INSERT INTO acoes (id, visita_id, nome, descricao, categoria) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), ?, 'Avaliacao ficticia', 'AVALIACAO_CLINICA')",
                acaoId.toString(), visitaPlanejadaId.toString(), nomeAcao);
        jdbc.update("INSERT INTO resultados_atividade (id, visita_agendada_id, acao_id, valor, unidade, observacao, "
                + "registrado_por_id, registrado_em, alterado_por_id, alterado_em) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), CAST(? AS UUID), ?, 'mmHg', NULL, "
                + "CAST(? AS UUID), ?, CAST(? AS UUID), ?)",
                UUID.randomUUID().toString(), visitaAgendadaId.toString(), acaoId.toString(), valor,
                pesquisadorId.toString(), ts, pesquisadorId.toString(), ts);
    }

    @Test
    @DisplayName("RQ-T106-06/07/08/10: cancela apenas futuras AGENDADA com auditoria, preserva vencidas, finalizadas, motivo original e histórico")
    void cancelaFuturasEPreservaVencidasFinalizadasEHistorico() throws Exception {
        UUID estudoId = criarEstudo();
        UUID vp1 = criarVisitaPlanejada(estudoId, "V1 Realizada", 0, 1);
        UUID vp2 = criarVisitaPlanejada(estudoId, "V2 Ausente", 10, 2);
        UUID vp3 = criarVisitaPlanejada(estudoId, "V3 Ja Cancelada", 20, 3);
        UUID vp4 = criarVisitaPlanejada(estudoId, "V4 Vencida", 30, 4);
        UUID vp5 = criarVisitaPlanejada(estudoId, "V5 Futura 1", 50, 5);
        UUID vp6 = criarVisitaPlanejada(estudoId, "V6 Futura 2", 70, 6);
        UUID participacaoId = criarParticipacao(estudoId);

        LocalDateTime agora = LocalDateTime.now(clock);
        LocalDateTime criacaoAntiga = agora.minusDays(35);

        UUID vaRealizada = criarVisitaAgendada(participacaoId, vp1, agora.minusDays(35), "REALIZADA",
                agora.minusDays(35).plusMinutes(15), null, pesquisadorId, criacaoAntiga);
        registrarResultado(vp1, vaRealizada, "Aferir pressão arterial", "120/80");

        UUID vaAusente = criarVisitaAgendada(participacaoId, vp2, agora.minusDays(25), "AUSENTE",
                null, null, pesquisadorId, criacaoAntiga);
        UUID vaJaCancelada = criarVisitaAgendada(participacaoId, vp3, agora.minusDays(15), "CANCELADA",
                null, "Participante em viagem", pesquisadorId, criacaoAntiga);
        UUID vaVencida = criarVisitaAgendada(participacaoId, vp4, agora.minusDays(2), "AGENDADA",
                null, null, pesquisadorId, criacaoAntiga);
        UUID vaFutura1 = criarVisitaAgendada(participacaoId, vp5, agora.plusDays(10), "AGENDADA",
                null, null, pesquisadorId, criacaoAntiga);
        UUID vaFutura2 = criarVisitaAgendada(participacaoId, vp6, agora.plusDays(25), "AGENDADA",
                null, null, pesquisadorId, criacaoAntiga);

        String corpo = "{\"motivo\":\"Mudança de cidade\",\"solicitante\":\"PARTICIPANTE\",\"dataRetirada\":\""
                + LocalDate.now(clock) + "\"}";

        enviar(mvc, "POST", "/api/participacoes/" + participacaoId + "/retirada", coordenadorToken, corpo)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.visitasCanceladas").value(2));

        // GET .../retirada bate com as 2 visitas efetivamente canceladas pela retirada
        enviar(mvc, "GET", "/api/participacoes/" + participacaoId + "/retirada", pesquisadorToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visitasCanceladas").value(2));

        // Confere cada visita diretamente no banco (situacao, motivo e auditoria)
        assertThat(situacaoDe(vaRealizada)).isEqualTo("REALIZADA");
        assertThat(situacaoDe(vaAusente)).isEqualTo("AUSENTE");
        assertThat(situacaoDe(vaJaCancelada)).isEqualTo("CANCELADA");
        assertThat(motivoDe(vaJaCancelada)).isEqualTo("Participante em viagem");
        assertThat(situacaoDe(vaVencida)).isEqualTo("AGENDADA");
        assertThat(motivoDe(vaVencida)).isNull();

        for (UUID futura : List.of(vaFutura1, vaFutura2)) {
            assertThat(situacaoDe(futura)).isEqualTo("CANCELADA");
            assertThat(motivoDe(futura)).isEqualTo("retirada do estudo");
            UUID alteradoPor = jdbc.queryForObject(
                    "SELECT alterado_por_id FROM visitas_agendadas WHERE id = CAST(? AS UUID)", UUID.class,
                    futura.toString());
            LocalDateTime alteradoEm = jdbc.queryForObject(
                    "SELECT alterado_em FROM visitas_agendadas WHERE id = CAST(? AS UUID)", Timestamp.class,
                    futura.toString()).toLocalDateTime();
            assertThat(alteradoPor).isEqualTo(coordenadorId);
            assertThat(alteradoEm).isAfter(criacaoAntiga);
        }

        // Histórico preservado em leitura (visitas + resultado)
        enviar(mvc, "GET", "/api/participacoes/" + participacaoId + "/historico", pesquisadorToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participante.situacao").value("RETIRADO"))
                .andExpect(jsonPath("$.participante.retirada.motivo").value("Mudança de cidade"))
                .andExpect(jsonPath("$.resumo.previstas").value(6))
                .andExpect(jsonPath("$.resumo.realizadas").value(1))
                .andExpect(jsonPath("$.resumo.ausentes").value(1))
                .andExpect(jsonPath("$.resumo.canceladas").value(3))
                .andExpect(jsonPath("$.resumo.agendadas").value(1))
                .andExpect(jsonPath("$.visitas", hasSize(6)))
                .andExpect(jsonPath("$.visitas[0].resultados", hasSize(1)))
                .andExpect(jsonPath("$.visitas[0].resultados[0].valor").value("120/80"));
    }

    @Test
    @DisplayName("RQ-T106-09: falha ao salvar a segunda visita desfaz toda a transação (nada gravado pela metade)")
    void falhaAoSalvarSegundaVisitaDesfazTodaARetirada() {
        UUID estudoId = criarEstudo();
        UUID vp1 = criarVisitaPlanejada(estudoId, "V1 Futura", 10, 1);
        UUID vp2 = criarVisitaPlanejada(estudoId, "V2 Futura", 20, 2);
        UUID participacaoId = criarParticipacao(estudoId);

        LocalDateTime agora = LocalDateTime.now(clock);
        UUID va1 = criarVisitaAgendada(participacaoId, vp1, agora.plusDays(5), "AGENDADA",
                null, null, coordenadorId, agora.minusDays(1));
        UUID va2 = criarVisitaAgendada(participacaoId, vp2, agora.plusDays(15), "AGENDADA",
                null, null, coordenadorId, agora.minusDays(1));

        // A 1ª visita (va1, D+5) passa pelo repositório real e faz flush na transação; a 2ª (va2, D+15) falha
        doThrow(new RuntimeException("Falha simulada ao cancelar a segunda visita"))
                .when(visitasAgendadas)
                .saveAndFlush(argThat(v -> v != null && va2.equals(v.getId())));

        ComandoRetirada comando = new ComandoRetirada("Mudança de cidade", SolicitanteRetirada.PARTICIPANTE,
                LocalDate.now(clock));

        assertThatThrownBy(() -> retiradaService.registrar(participacaoId, comando, coordenadorId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha simulada");

        // Confirma que a 1ª visita chegou a ser salva antes da falha na 2ª
        verify(visitasAgendadas).saveAndFlush(argThat(v -> v != null && va1.equals(v.getId())));

        // Prova de rollback real no banco: participação continua ATIVO, 0 retiradas e ambas as visitas AGENDADA
        assertThat(jdbc.queryForObject("SELECT situacao FROM participacoes WHERE id = CAST(? AS UUID)",
                String.class, participacaoId.toString())).isEqualTo("ATIVO");
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM retiradas_participacao WHERE participacao_id = CAST(? AS UUID)",
                Integer.class, participacaoId.toString())).isZero();
        assertThat(situacaoDe(va1)).isEqualTo("AGENDADA");
        assertThat(motivoDe(va1)).isNull();
        assertThat(situacaoDe(va2)).isEqualTo("AGENDADA");
        assertThat(motivoDe(va2)).isNull();
    }

    private String situacaoDe(UUID visitaId) {
        return jdbc.queryForObject("SELECT situacao FROM visitas_agendadas WHERE id = CAST(? AS UUID)",
                String.class, visitaId.toString());
    }

    private String motivoDe(UUID visitaId) {
        return jdbc.queryForObject("SELECT motivo FROM visitas_agendadas WHERE id = CAST(? AS UUID)",
                String.class, visitaId.toString());
    }
}
