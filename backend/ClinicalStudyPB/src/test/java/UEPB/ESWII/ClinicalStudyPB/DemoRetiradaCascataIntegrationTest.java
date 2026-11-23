package UEPB.ESWII.ClinicalStudyPB;

import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.enviar;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Connection;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;

/**
 * T105 (RQ-T105-06 e RQ-T105-07): conferência pela API real (agenda, histórico, retirada e vagas) de que o cenário de
 * demonstração de P6 ({@code R__demo_23_retiradas.sql}) e uma retirada real executada via {@code POST .../retirada}
 * produzem exatamente o mesmo estado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DemoRetiradaCascataIntegrationTest {

    private static final String ESTUDO_ACOMPANHAMENTO = "d6300000-0000-4000-8000-000000001901";
    private static final String PARTICIPACAO_P6 = "d6300000-0000-4000-8000-000000002321";
    private static final String COORDENADOR_ID = "22222222-2222-4222-8222-222222222222";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private Clock clock;

    private String coordenador;
    private String pesquisador;

    @BeforeEach
    void preparar() throws Exception {
        coordenador = TokenDeTeste.bearer(mvc, TokenDeTeste.COORDENADOR);
        pesquisador = TokenDeTeste.bearer(mvc, TokenDeTeste.PESQUISADOR);
    }

    private void carregarScriptsDemo() throws Exception {
        Connection conexao = DataSourceUtils.getConnection(dataSource);
        for (String caminho : new String[] {
                "db/demo/R__dados_demonstracao.sql",
                "db/demo/R__demo_19_visitas_agendadas.sql",
                "db/demo/R__demo_20_desfechos.sql",
                "db/demo/R__demo_23_retiradas.sql",
                "db/demo/R__demo_24_conta_participante.sql"}) {
            ScriptUtils.executeSqlScript(conexao, new ClassPathResource(caminho));
        }
    }

    @Test
    @DisplayName("RQ-T105-06: P6 aparece retirado no histórico, na agenda com Retorno 30 cancelado por 'retirada do estudo' e com 6/6 vagas")
    void conferenciaDemoP6NaAgendaNoHistoricoENasVagas() throws Exception {
        carregarScriptsDemo();

        // 6 de 6 vagas ocupadas (a retirada de P6 nao libera vaga)
        enviar(mvc, "GET", "/api/estudos/" + ESTUDO_ACOMPANHAMENTO + "/participantes/vagas", coordenador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incluidos").value(6))
                .andExpect(jsonPath("$.limite").value(6))
                .andExpect(jsonPath("$.restantes").value(0));

        // Andamento da agenda: P6 marcado como RETIRADO com 2 de 3 visitas agendadas
        enviar(mvc, "GET", "/api/estudos/" + ESTUDO_ACOMPANHAMENTO + "/agenda/participantes", pesquisador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo=='CAND-006')].situacaoParticipacao", hasItem("RETIRADO")))
                .andExpect(jsonPath("$[?(@.codigo=='CAND-006')].visitasPrevistas", hasItem(3)))
                .andExpect(jsonPath("$[?(@.codigo=='CAND-006')].visitasAgendadas", hasItem(2)));

        // Agenda filtrada por CANCELADA: mostra Retorno 30 dias de P6
        enviar(mvc, "GET", "/api/estudos/" + ESTUDO_ACOMPANHAMENTO + "/agenda?situacao=CANCELADA", pesquisador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo[?(@.participanteCodigo=='CAND-006')].visitaNome",
                        hasItem("Retorno 30 dias")))
                .andExpect(jsonPath("$.conteudo[?(@.participanteCodigo=='CAND-006')].situacao", hasItem("CANCELADA")));

        // Consulta da retirada: 1 visita cancelada pelo motivo da retirada
        enviar(mvc, "GET", "/api/participacoes/" + PARTICIPACAO_P6 + "/retirada", pesquisador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participacaoId").value(PARTICIPACAO_P6))
                .andExpect(jsonPath("$.motivo").value("Mudança de cidade"))
                .andExpect(jsonPath("$.solicitante").value("PARTICIPANTE"))
                .andExpect(jsonPath("$.visitasCanceladas").value(1));

        // Histórico de P6: Triagem REALIZADA preservada, Retorno 30 CANCELADA com "retirada do estudo", Retorno 60 NAO_AGENDADA
        enviar(mvc, "GET", "/api/participacoes/" + PARTICIPACAO_P6 + "/historico", pesquisador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participante.codigo").value("CAND-006"))
                .andExpect(jsonPath("$.participante.situacao").value("RETIRADO"))
                .andExpect(jsonPath("$.participante.retirada.motivo").value("Mudança de cidade"))
                .andExpect(jsonPath("$.participante.retirada.solicitante").value("PARTICIPANTE"))
                .andExpect(jsonPath("$.resumo.previstas").value(3))
                .andExpect(jsonPath("$.resumo.realizadas").value(1))
                .andExpect(jsonPath("$.resumo.ausentes").value(0))
                .andExpect(jsonPath("$.resumo.canceladas").value(1))
                .andExpect(jsonPath("$.resumo.agendadas").value(0))
                .andExpect(jsonPath("$.resumo.naoAgendadas").value(1))
                .andExpect(jsonPath("$.resumo.pendentes").value(1))
                .andExpect(jsonPath("$.visitas", hasSize(3)))
                .andExpect(jsonPath("$.visitas[0].nome").value("Triagem"))
                .andExpect(jsonPath("$.visitas[0].situacao").value("REALIZADA"))
                .andExpect(jsonPath("$.visitas[0].dataHoraRealizada").isNotEmpty())
                .andExpect(jsonPath("$.visitas[1].nome").value("Retorno 30 dias"))
                .andExpect(jsonPath("$.visitas[1].situacao").value("CANCELADA"))
                .andExpect(jsonPath("$.visitas[1].motivo").value("retirada do estudo"))
                .andExpect(jsonPath("$.visitas[2].nome").value("Retorno 60 dias"))
                .andExpect(jsonPath("$.visitas[2].situacao").value("NAO_AGENDADA"))
                .andExpect(jsonPath("$.visitas[2].visitaAgendadaId", nullValue()));
    }

    @Test
    @DisplayName("RQ-T105-07: uma retirada real via API produz o mesmo estado final do script de demonstração")
    void retiradaRealProduzOMesmoEstadoDoScriptDemo() throws Exception {
        UUID estudo = UUID.randomUUID();
        UUID triagem = UUID.randomUUID();
        UUID retorno30 = UUID.randomUUID();
        UUID retorno60 = UUID.randomUUID();
        UUID candidato = UUID.randomUUID();
        UUID participacao = UUID.randomUUID();

        jdbc.update("INSERT INTO estudos (id, titulo, objetivo, responsavel_id, data_inicio, data_fim, "
                + "limite_participantes, status) SELECT CAST(? AS UUID), ?, 'Conferir equivalencia T105.', id, "
                + "DATE '2026-01-01', DATE '2028-01-01', 1, 'EXECUCAO' FROM usuarios "
                + "WHERE email = 'pesquisador@clinicalstudy.test'", estudo.toString(), "Estudo T105 " + estudo);
        jdbc.update("INSERT INTO visitas_planejadas (id, estudo_id, nome, dia_previsto, tolerancia, ordem) VALUES "
                + "(CAST(? AS UUID), CAST(? AS UUID), 'Triagem', 0, 3, 1), "
                + "(CAST(? AS UUID), CAST(? AS UUID), 'Retorno 30 dias', 30, 5, 2), "
                + "(CAST(? AS UUID), CAST(? AS UUID), 'Retorno 60 dias', 60, 7, 3)",
                triagem.toString(), estudo.toString(), retorno30.toString(), estudo.toString(),
                retorno60.toString(), estudo.toString());
        jdbc.update("INSERT INTO candidatos (id, estudo_id, codigo, nome_ficticio, data_nascimento, situacao) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), 'CAND-REAL', 'Pessoa Real (Ficticia)', DATE '1990-01-01', 'INCLUIDO')",
                candidato.toString(), estudo.toString());
        LocalDate hoje = LocalDate.now(clock);
        jdbc.update("INSERT INTO participacoes (id, candidato_id, estudo_id, data_inclusao, incluido_por_id, situacao) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), CAST(? AS UUID), ?, CAST(? AS UUID), 'ATIVO')",
                participacao.toString(), candidato.toString(), estudo.toString(), java.sql.Date.valueOf(hoje.minusDays(15)),
                COORDENADOR_ID);

        LocalDateTime agora = LocalDateTime.now(clock);
        Timestamp triagemAgendada = Timestamp.valueOf(hoje.minusDays(15).atTime(9, 0));
        Timestamp triagemRealizada = Timestamp.valueOf(hoje.minusDays(15).atTime(9, 10));
        Timestamp retorno30Futuro = Timestamp.valueOf(hoje.plusDays(15).atTime(9, 30));
        Timestamp criacao = Timestamp.valueOf(agora.minusDays(15));

        jdbc.update("INSERT INTO visitas_agendadas (id, participacao_id, visita_planejada_id, data_hora_agendada, "
                + "situacao, data_hora_realizada, motivo, observacao, versao, criado_por_id, criado_em, alterado_por_id, alterado_em) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), CAST(? AS UUID), ?, 'REALIZADA', ?, NULL, NULL, 0, "
                + "CAST(? AS UUID), ?, CAST(? AS UUID), ?)",
                UUID.randomUUID().toString(), participacao.toString(), triagem.toString(), triagemAgendada,
                triagemRealizada, COORDENADOR_ID, criacao, COORDENADOR_ID, criacao);
        jdbc.update("INSERT INTO visitas_agendadas (id, participacao_id, visita_planejada_id, data_hora_agendada, "
                + "situacao, data_hora_realizada, motivo, observacao, versao, criado_por_id, criado_em, alterado_por_id, alterado_em) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), CAST(? AS UUID), ?, 'AGENDADA', NULL, NULL, NULL, 0, "
                + "CAST(? AS UUID), ?, CAST(? AS UUID), ?)",
                UUID.randomUUID().toString(), participacao.toString(), retorno30.toString(), retorno30Futuro,
                COORDENADOR_ID, criacao, COORDENADOR_ID, criacao);

        String corpo = """
                {
                  "motivo": "Mudança de cidade",
                  "solicitante": "PARTICIPANTE",
                  "dataRetirada": "%s"
                }
                """.formatted(hoje.minusDays(2));

        enviar(mvc, "POST", "/api/participacoes/" + participacao + "/retirada", coordenador, corpo)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.motivo").value("Mudança de cidade"))
                .andExpect(jsonPath("$.solicitante").value("PARTICIPANTE"))
                .andExpect(jsonPath("$.visitasCanceladas").value(1));

        // Mesmo estado do script demo no histórico, na agenda e nas vagas
        enviar(mvc, "GET", "/api/participacoes/" + participacao + "/historico", pesquisador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participante.situacao").value("RETIRADO"))
                .andExpect(jsonPath("$.participante.retirada.motivo").value("Mudança de cidade"))
                .andExpect(jsonPath("$.participante.retirada.solicitante").value("PARTICIPANTE"))
                .andExpect(jsonPath("$.resumo.previstas").value(3))
                .andExpect(jsonPath("$.resumo.realizadas").value(1))
                .andExpect(jsonPath("$.resumo.canceladas").value(1))
                .andExpect(jsonPath("$.resumo.agendadas").value(0))
                .andExpect(jsonPath("$.resumo.naoAgendadas").value(1))
                .andExpect(jsonPath("$.resumo.pendentes").value(1))
                .andExpect(jsonPath("$.visitas[0].situacao").value("REALIZADA"))
                .andExpect(jsonPath("$.visitas[1].situacao").value("CANCELADA"))
                .andExpect(jsonPath("$.visitas[1].motivo").value("retirada do estudo"))
                .andExpect(jsonPath("$.visitas[2].situacao").value("NAO_AGENDADA"));

        enviar(mvc, "GET", "/api/estudos/" + estudo + "/participantes/vagas", coordenador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incluidos").value(1))
                .andExpect(jsonPath("$.limite").value(1))
                .andExpect(jsonPath("$.restantes").value(0));
    }
}
