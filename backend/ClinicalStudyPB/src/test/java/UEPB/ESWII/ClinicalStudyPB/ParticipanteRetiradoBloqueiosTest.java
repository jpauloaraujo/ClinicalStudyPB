package UEPB.ESWII.ClinicalStudyPB;

import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.enviar;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;

/**
 * T106 (RQ-T106-16 e RQ-T106-17): bloqueios impostos a participantes retirados nos serviços dos outros PBIs —
 * agendamento, reagendamento, desfecho e resultados (registro, edição e consulta com {@code bloqueio}) — e a
 * precedência do estado do estudo sobre a situação do participante.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ParticipanteRetiradoBloqueiosTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private Clock clock;

    private String coordenadorToken;
    private String pesquisadorToken;
    private UUID coordenadorId;
    private UUID pesquisadorId;

    private record Cenario(
            UUID estudoId,
            UUID participacaoId,
            UUID vpTriagem,
            UUID vpRetorno30,
            UUID vpRetorno60,
            UUID vaRealizada,
            UUID vaVencidaAgendada,
            UUID acaoPendente,
            UUID acaoJaRegistrada) {
    }

    @BeforeEach
    void preparar() throws Exception {
        coordenadorToken = TokenDeTeste.bearer(mvc, TokenDeTeste.COORDENADOR);
        pesquisadorToken = TokenDeTeste.bearer(mvc, TokenDeTeste.PESQUISADOR);
        coordenadorId = jdbc.queryForObject("SELECT id FROM usuarios WHERE email = ?", UUID.class,
                "coordenador@clinicalstudy.test");
        pesquisadorId = jdbc.queryForObject("SELECT id FROM usuarios WHERE email = ?", UUID.class,
                "pesquisador@clinicalstudy.test");
    }

    /** Monta estudo no status pedido com participante RETIRADO, Triagem REALIZADA (2 avaliações) e Retorno 30 vencido AGENDADA. */
    private Cenario montarParticipanteRetirado(String statusEstudo) {
        LocalDate hoje = LocalDate.now(clock);
        LocalDateTime agora = LocalDateTime.now(clock);
        UUID estudoId = UUID.randomUUID();
        UUID vpTriagem = UUID.randomUUID();
        UUID vpRetorno30 = UUID.randomUUID();
        UUID vpRetorno60 = UUID.randomUUID();
        UUID acaoPendente = UUID.randomUUID();
        UUID acaoJaRegistrada = UUID.randomUUID();
        UUID candidatoId = UUID.randomUUID();
        UUID participacaoId = UUID.randomUUID();
        UUID vaRealizada = UUID.randomUUID();
        UUID vaVencidaAgendada = UUID.randomUUID();

        jdbc.update("INSERT INTO estudos (id, titulo, objetivo, responsavel_id, data_inicio, data_fim, "
                + "limite_participantes, status) VALUES (CAST(? AS UUID), ?, 'Bloqueios T106', CAST(? AS UUID), "
                + "?, ?, 6, ?)", estudoId.toString(), "Bloqueios " + estudoId, pesquisadorId.toString(),
                Date.valueOf(hoje.minusDays(60)), Date.valueOf(hoje.plusDays(180)), statusEstudo);

        jdbc.update("INSERT INTO visitas_planejadas (id, estudo_id, nome, dia_previsto, tolerancia, ordem) VALUES "
                + "(CAST(? AS UUID), CAST(? AS UUID), 'Triagem', 0, 3, 1), "
                + "(CAST(? AS UUID), CAST(? AS UUID), 'Retorno 30 dias', 30, 5, 2), "
                + "(CAST(? AS UUID), CAST(? AS UUID), 'Retorno 60 dias', 60, 7, 3)",
                vpTriagem.toString(), estudoId.toString(), vpRetorno30.toString(), estudoId.toString(),
                vpRetorno60.toString(), estudoId.toString());

        jdbc.update("INSERT INTO acoes (id, visita_id, nome, descricao, categoria) VALUES "
                + "(CAST(? AS UUID), CAST(? AS UUID), 'Aferir peso', 'Peso ficticio', 'AVALIACAO_CLINICA'), "
                + "(CAST(? AS UUID), CAST(? AS UUID), 'Aferir pressão arterial', 'PA ficticia', 'AVALIACAO_CLINICA')",
                acaoPendente.toString(), vpTriagem.toString(), acaoJaRegistrada.toString(), vpTriagem.toString());

        jdbc.update("INSERT INTO candidatos (id, estudo_id, codigo, nome_ficticio, data_nascimento, situacao) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), 'CAND-BLQ', 'Pessoa Bloqueio (Ficticia)', DATE '1990-01-01', 'INCLUIDO')",
                candidatoId.toString(), estudoId.toString());

        // Inclusão em D-32: Triagem D-32, Retorno 30 sugerido D-2 (janela D-7 a D+3), Retorno 60 sugerido D+28 (janela D+21 a D+35)
        jdbc.update("INSERT INTO participacoes (id, candidato_id, estudo_id, data_inclusao, incluido_por_id, situacao) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), CAST(? AS UUID), ?, CAST(? AS UUID), 'RETIRADO')",
                participacaoId.toString(), candidatoId.toString(), estudoId.toString(),
                Date.valueOf(hoje.minusDays(32)), coordenadorId.toString());

        jdbc.update("INSERT INTO retiradas_participacao (id, participacao_id, data_retirada, motivo, solicitante, "
                + "registrado_por_id, registrado_em) VALUES (CAST(? AS UUID), CAST(? AS UUID), ?, 'Mudança de cidade', "
                + "'PARTICIPANTE', CAST(? AS UUID), ?)",
                UUID.randomUUID().toString(), participacaoId.toString(), Date.valueOf(hoje.minusDays(1)),
                coordenadorId.toString(), Timestamp.valueOf(agora.minusDays(1)));

        Timestamp tRealizada = Timestamp.valueOf(hoje.minusDays(32).atTime(9, 10));
        jdbc.update("INSERT INTO visitas_agendadas (id, participacao_id, visita_planejada_id, data_hora_agendada, "
                + "situacao, data_hora_realizada, motivo, observacao, versao, criado_por_id, criado_em, alterado_por_id, alterado_em) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), CAST(? AS UUID), ?, 'REALIZADA', ?, NULL, NULL, 0, "
                + "CAST(? AS UUID), ?, CAST(? AS UUID), ?)",
                vaRealizada.toString(), participacaoId.toString(), vpTriagem.toString(),
                Timestamp.valueOf(hoje.minusDays(32).atTime(9, 0)), tRealizada,
                coordenadorId.toString(), tRealizada, coordenadorId.toString(), tRealizada);

        // Resultado já registrado antes da retirada (para testar bloqueio de edição e preservação na consulta)
        jdbc.update("INSERT INTO resultados_atividade (id, visita_agendada_id, acao_id, valor, unidade, observacao, "
                + "registrado_por_id, registrado_em, alterado_por_id, alterado_em) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), CAST(? AS UUID), '120/80', 'mmHg', NULL, "
                + "CAST(? AS UUID), ?, CAST(? AS UUID), ?)",
                UUID.randomUUID().toString(), vaRealizada.toString(), acaoJaRegistrada.toString(),
                pesquisadorId.toString(), tRealizada, pesquisadorId.toString(), tRealizada);

        // Visita vencida (D-2) ainda AGENDADA: válida para testar reagendamento e desfecho bloqueados por retirada
        Timestamp tVencida = Timestamp.valueOf(hoje.minusDays(2).atTime(10, 0));
        jdbc.update("INSERT INTO visitas_agendadas (id, participacao_id, visita_planejada_id, data_hora_agendada, "
                + "situacao, data_hora_realizada, motivo, observacao, versao, criado_por_id, criado_em, alterado_por_id, alterado_em) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), CAST(? AS UUID), ?, 'AGENDADA', NULL, NULL, NULL, 0, "
                + "CAST(? AS UUID), ?, CAST(? AS UUID), ?)",
                vaVencidaAgendada.toString(), participacaoId.toString(), vpRetorno30.toString(), tVencida,
                coordenadorId.toString(), tRealizada, coordenadorId.toString(), tRealizada);

        return new Cenario(estudoId, participacaoId, vpTriagem, vpRetorno30, vpRetorno60,
                vaRealizada, vaVencidaAgendada, acaoPendente, acaoJaRegistrada);
    }

    @Test
    @DisplayName("RQ-T106-16: agendar visita para participante retirado devolve 409 PARTICIPANTE_RETIRADO")
    void agendarParaRetiradoRecusado() throws Exception {
        Cenario c = montarParticipanteRetirado("EXECUCAO");
        String dataValida = LocalDate.now(clock).plusDays(28).atTime(9, 0).toString();

        enviar(mvc, "POST", "/api/participacoes/" + c.participacaoId() + "/visitas-agendadas", coordenadorToken,
                "{\"visitaPlanejadaId\":\"" + c.vpRetorno60() + "\",\"dataHora\":\"" + dataValida + "\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("PARTICIPANTE_RETIRADO"));
    }

    @Test
    @DisplayName("RQ-T106-16: reagendar visita de participante retirado devolve 409 PARTICIPANTE_RETIRADO")
    void reagendarParaRetiradoRecusado() throws Exception {
        Cenario c = montarParticipanteRetirado("EXECUCAO");
        String novaDataValida = LocalDate.now(clock).plusDays(1).atTime(10, 0).toString();

        enviar(mvc, "PUT", "/api/visitas-agendadas/" + c.vaVencidaAgendada(), coordenadorToken,
                "{\"dataHora\":\"" + novaDataValida + "\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("PARTICIPANTE_RETIRADO"));
    }

    @Test
    @DisplayName("RQ-T106-16: registrar desfecho em visita de participante retirado devolve 409 PARTICIPANTE_RETIRADO")
    void registrarDesfechoParaRetiradoRecusado() throws Exception {
        Cenario c = montarParticipanteRetirado("EXECUCAO");

        enviar(mvc, "POST", "/api/visitas-agendadas/" + c.vaVencidaAgendada() + "/desfecho", coordenadorToken,
                "{\"tipo\":\"AUSENTE\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("PARTICIPANTE_RETIRADO"));
    }

    @Test
    @DisplayName("RQ-T106-16: registrar novo resultado em visita de participante retirado devolve 409 PARTICIPANTE_RETIRADO")
    void registrarResultadoParaRetiradoRecusado() throws Exception {
        Cenario c = montarParticipanteRetirado("EXECUCAO");

        enviar(mvc, "PUT", "/api/visitas-agendadas/" + c.vaRealizada() + "/resultados/" + c.acaoPendente(),
                pesquisadorToken, "{\"valor\":\"72\",\"unidade\":\"kg\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("PARTICIPANTE_RETIRADO"));
    }

    @Test
    @DisplayName("RQ-T106-16: editar resultado já existente de participante retirado devolve 409 PARTICIPANTE_RETIRADO e preserva valor")
    void editarResultadoParaRetiradoRecusado() throws Exception {
        Cenario c = montarParticipanteRetirado("EXECUCAO");

        enviar(mvc, "PUT", "/api/visitas-agendadas/" + c.vaRealizada() + "/resultados/" + c.acaoJaRegistrada(),
                pesquisadorToken, "{\"valor\":\"140/90\",\"unidade\":\"mmHg\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("PARTICIPANTE_RETIRADO"));

        assertThat(jdbc.queryForObject(
                "SELECT valor FROM resultados_atividade WHERE visita_agendada_id = CAST(? AS UUID) AND acao_id = CAST(? AS UUID)",
                String.class, c.vaRealizada().toString(), c.acaoJaRegistrada().toString())).isEqualTo("120/80");
    }

    @Test
    @DisplayName("RQ-T106-16: consultar resultados de participante retirado devolve 200 com bloqueio PARTICIPANTE_RETIRADO")
    void consultarResultadosDeRetiradoDevolveBloqueio() throws Exception {
        Cenario c = montarParticipanteRetirado("EXECUCAO");

        enviar(mvc, "GET", "/api/visitas-agendadas/" + c.vaRealizada() + "/resultados", pesquisadorToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bloqueio").value("PARTICIPANTE_RETIRADO"))
                .andExpect(jsonPath("$.registrados").value(1))
                .andExpect(jsonPath("$.pendentes").value(1));
    }

    @Test
    @DisplayName("RQ-T106-17: ordem das verificações — estado do estudo encerrado vence a situação de participante retirado")
    void estadoDoEstudoVenceSituacaoDoParticipante() throws Exception {
        Cenario c = montarParticipanteRetirado("ENCERRADO");
        String dataAgendar = LocalDate.now(clock).plusDays(28).atTime(9, 0).toString();
        String dataReagendar = LocalDate.now(clock).plusDays(1).atTime(10, 0).toString();

        // Agendar
        enviar(mvc, "POST", "/api/participacoes/" + c.participacaoId() + "/visitas-agendadas", coordenadorToken,
                "{\"visitaPlanejadaId\":\"" + c.vpRetorno60() + "\",\"dataHora\":\"" + dataAgendar + "\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO"));

        // Reagendar
        enviar(mvc, "PUT", "/api/visitas-agendadas/" + c.vaVencidaAgendada(), coordenadorToken,
                "{\"dataHora\":\"" + dataReagendar + "\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO"));

        // Desfecho
        enviar(mvc, "POST", "/api/visitas-agendadas/" + c.vaVencidaAgendada() + "/desfecho", coordenadorToken,
                "{\"tipo\":\"AUSENTE\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO"));

        // Resultados: escrita e consulta
        enviar(mvc, "PUT", "/api/visitas-agendadas/" + c.vaRealizada() + "/resultados/" + c.acaoPendente(),
                pesquisadorToken, "{\"valor\":\"72\",\"unidade\":\"kg\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESTUDO_ENCERRADO"));

        enviar(mvc, "GET", "/api/visitas-agendadas/" + c.vaRealizada() + "/resultados", pesquisadorToken, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bloqueio").value("ESTUDO_ENCERRADO"));
    }
}
