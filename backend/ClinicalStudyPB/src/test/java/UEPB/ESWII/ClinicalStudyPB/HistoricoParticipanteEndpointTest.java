package UEPB.ESWII.ClinicalStudyPB;

import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.enviar;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
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

import UEPB.ESWII.ClinicalStudyPB.service.HistoricoParticipanteService;
import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;
import jakarta.persistence.EntityManagerFactory;

/**
 * T96 (RF15): teste de fumaça do endpoint do histórico com o banco e tokens reais: junção das visitas previstas, dos
 * agendamentos e dos resultados; resumo; ordem pelo dia previsto; 200/401/403/404/405; estudo em qualquer estado; o
 * P4 e o P3 do demonstração; e a contagem de consultas, que não cresce com o número de visitas (sem N+1).
 */
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureMockMvc
@Transactional
class HistoricoParticipanteEndpointTest {

    private static final String COORDENADOR_ID = "22222222-2222-4222-8222-222222222222";
    private static final String TITULO_DEMO = "Demo - Acompanhamento de participantes";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @Autowired
    private HistoricoParticipanteService service;

    private String pesquisador;
    private String coordenador;

    @BeforeEach
    void entrar() throws Exception {
        pesquisador = TokenDeTeste.bearer(mvc, TokenDeTeste.PESQUISADOR);
        coordenador = TokenDeTeste.bearer(mvc, TokenDeTeste.COORDENADOR);
    }

    // ---- montagem do cenário, direto no banco ----

    private UUID criarEstudo(String titulo, String status) {
        UUID estudo = UUID.randomUUID();
        jdbc.update("INSERT INTO estudos (id, titulo, objetivo, responsavel_id, data_inicio, data_fim, "
                + "limite_participantes, status) SELECT CAST(? AS UUID), ?, 'Testar o historico.', id, "
                + "DATE '2026-11-01', DATE '2027-05-01', 6, ? FROM usuarios WHERE email = 'pesquisador@clinicalstudy.test'",
                estudo.toString(), titulo, status);
        return estudo;
    }

    private UUID criarVisitaPlanejada(UUID estudo, String nome, int dia, int ordem) {
        UUID visita = UUID.randomUUID();
        jdbc.update("INSERT INTO visitas_planejadas (id, estudo_id, nome, dia_previsto, tolerancia, ordem) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), ?, ?, 3, ?)", visita.toString(), estudo.toString(), nome,
                dia, ordem);
        return visita;
    }

    private UUID incluirParticipante(UUID estudo, String codigo, String nome, int diasAtras) {
        UUID candidato = UUID.randomUUID();
        UUID participacao = UUID.randomUUID();
        jdbc.update("INSERT INTO candidatos (id, estudo_id, codigo, nome_ficticio, data_nascimento, situacao) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), ?, ?, DATE '1990-01-01', 'INCLUIDO')",
                candidato.toString(), estudo.toString(), codigo, nome);
        jdbc.update("INSERT INTO participacoes (id, candidato_id, estudo_id, data_inclusao, incluido_por_id) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), CAST(? AS UUID), CURRENT_DATE - INTERVAL '" + diasAtras
                + "' DAY, CAST(? AS UUID))", participacao.toString(), candidato.toString(), estudo.toString(),
                COORDENADOR_ID);
        return participacao;
    }

    private UUID agendar(UUID participacao, UUID visita, String situacao, LocalDateTime realizada, String motivo,
            String observacao) {
        Timestamp agora = Timestamp.valueOf(LocalDateTime.of(2026, 11, 20, 9, 30));
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO visitas_agendadas (id, participacao_id, visita_planejada_id, data_hora_agendada, "
                + "situacao, data_hora_realizada, motivo, observacao, versao, criado_por_id, criado_em, "
                + "alterado_por_id, alterado_em) VALUES (CAST(? AS UUID), CAST(? AS UUID), CAST(? AS UUID), ?, ?, ?, "
                + "?, ?, 0, CAST(? AS UUID), ?, CAST(? AS UUID), ?)", id.toString(), participacao.toString(),
                visita.toString(), agora, situacao, realizada == null ? null : Timestamp.valueOf(realizada), motivo,
                observacao, COORDENADOR_ID, agora, COORDENADOR_ID, agora);
        return id;
    }

    private void registrarResultado(UUID visitaPrevista, UUID visitaAgendada, String atividade, String valor) {
        UUID acao = UUID.randomUUID();
        jdbc.update("INSERT INTO acoes (id, visita_id, nome, descricao, categoria) VALUES (CAST(? AS UUID), "
                + "CAST(? AS UUID), ?, 'Atividade ficticia', 'AVALIACAO_CLINICA')", acao.toString(),
                visitaPrevista.toString(), atividade);
        Timestamp agora = Timestamp.valueOf(LocalDateTime.of(2026, 11, 20, 10, 0));
        jdbc.update("INSERT INTO resultados_atividade (id, visita_agendada_id, acao_id, valor, unidade, observacao, "
                + "registrado_por_id, registrado_em, alterado_por_id, alterado_em) VALUES (CAST(? AS UUID), "
                + "CAST(? AS UUID), CAST(? AS UUID), ?, 'mmHg', NULL, CAST(? AS UUID), ?, CAST(? AS UUID), ?)",
                UUID.randomUUID().toString(), visitaAgendada.toString(), acao.toString(), valor, COORDENADOR_ID, agora,
                COORDENADOR_ID, agora);
    }

    /** Estudo em execução com 3 visitas (inseridas fora de ordem): Triagem realizada, Retorno 30 agendado e Retorno 60 sem agendamento. */
    private UUID participacaoComTresVisitas(String status) {
        UUID estudo = criarEstudo("Historico T96 " + UUID.randomUUID(), status);
        UUID retorno60 = criarVisitaPlanejada(estudo, "Retorno 60 dias", 60, 3);
        UUID triagem = criarVisitaPlanejada(estudo, "Triagem", 0, 1);
        UUID retorno30 = criarVisitaPlanejada(estudo, "Retorno 30 dias", 30, 2);
        UUID participacao = incluirParticipante(estudo, "CAND-901", "Pessoa Historico (Ficticia)", 35);
        UUID daTriagem = agendar(participacao, triagem, "REALIZADA", LocalDateTime.of(2026, 11, 20, 10, 5), null,
                "Tudo certo");
        agendar(participacao, retorno30, "AGENDADA", null, null, null);
        registrarResultado(triagem, daTriagem, "Escala de dor", "3");
        registrarResultado(triagem, daTriagem, "Aferir pressão arterial", "120/80");
        assertThat(retorno60).isNotNull();
        return participacao;
    }

    private void esperarFalha(int status, String metodo, String url, String token) throws Exception {
        enviar(mvc, metodo, url, token, null).andExpect(status().is(status));
    }

    // ---- endpoint ----

    @Test
    @DisplayName("200: junta previstas, agendamentos e resultados, ordenados pelo dia previsto, com o resumo calculado")
    void historicoCompleto() throws Exception {
        UUID participacao = participacaoComTresVisitas("EXECUCAO");

        enviar(mvc, "GET", "/api/participacoes/" + participacao + "/historico", pesquisador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participante.participacaoId").value(participacao.toString()))
                .andExpect(jsonPath("$.participante.codigo").value("CAND-901"))
                .andExpect(jsonPath("$.participante.nomeFicticio").value("Pessoa Historico (Ficticia)"))
                .andExpect(jsonPath("$.participante.dataInclusao").isNotEmpty())
                .andExpect(jsonPath("$.resumo.previstas").value(3))
                .andExpect(jsonPath("$.resumo.realizadas").value(1))
                .andExpect(jsonPath("$.resumo.ausentes").value(0))
                .andExpect(jsonPath("$.resumo.canceladas").value(0))
                .andExpect(jsonPath("$.resumo.agendadas").value(1))
                .andExpect(jsonPath("$.resumo.naoAgendadas").value(1))
                .andExpect(jsonPath("$.resumo.pendentes").value(2))
                .andExpect(jsonPath("$.visitas", hasSize(3)))
                .andExpect(jsonPath("$.visitas[0].nome").value("Triagem"))
                .andExpect(jsonPath("$.visitas[0].diaPrevisto").value(0))
                .andExpect(jsonPath("$.visitas[0].situacao").value("REALIZADA"))
                .andExpect(jsonPath("$.visitas[0].observacao").value("Tudo certo"))
                .andExpect(jsonPath("$.visitas[0].dataHoraAgendada", matchesPattern("2026-11-20T09:30:00-03:00")))
                .andExpect(jsonPath("$.visitas[0].dataHoraRealizada", matchesPattern("2026-11-20T10:05:00-03:00")))
                .andExpect(jsonPath("$.visitas[0].resultados", hasSize(2)))
                .andExpect(jsonPath("$.visitas[0].resultados[0].nome").value("Aferir pressão arterial"))
                .andExpect(jsonPath("$.visitas[0].resultados[0].valor").value("120/80"))
                .andExpect(jsonPath("$.visitas[0].resultados[0].unidade").value("mmHg"))
                .andExpect(jsonPath("$.visitas[0].resultados[1].nome").value("Escala de dor"))
                .andExpect(jsonPath("$.visitas[1].nome").value("Retorno 30 dias"))
                .andExpect(jsonPath("$.visitas[1].situacao").value("AGENDADA"))
                .andExpect(jsonPath("$.visitas[1].resultados", hasSize(0)))
                .andExpect(jsonPath("$.visitas[2].nome").value("Retorno 60 dias"))
                .andExpect(jsonPath("$.visitas[2].situacao").value("NAO_AGENDADA"))
                .andExpect(jsonPath("$.visitas[2].visitaAgendadaId", nullValue()))
                .andExpect(jsonPath("$.visitas[2].dataHoraAgendada", nullValue()))
                .andExpect(jsonPath("$.visitas[2].dataHoraRealizada", nullValue()))
                .andExpect(jsonPath("$.visitas[2].motivo", nullValue()))
                .andExpect(jsonPath("$.visitas[2].observacao", nullValue()))
                .andExpect(jsonPath("$.visitas[2].resultados", hasSize(0)));
    }

    @Test
    @DisplayName("O coordenador também lê o histórico")
    void coordenadorLe() throws Exception {
        UUID participacao = participacaoComTresVisitas("EXECUCAO");

        enviar(mvc, "GET", "/api/participacoes/" + participacao + "/historico", coordenador, null)
                .andExpect(status().isOk()).andExpect(jsonPath("$.resumo.previstas").value(3));
    }

    @Test
    @DisplayName("Funciona com o estudo em qualquer estado, inclusive encerrado")
    void qualquerEstadoDoEstudo() throws Exception {
        for (String estado : new String[] {"PLANEJAMENTO", "RECRUTAMENTO", "EXECUCAO", "ENCERRADO"}) {
            UUID participacao = participacaoComTresVisitas(estado);

            enviar(mvc, "GET", "/api/participacoes/" + participacao + "/historico", pesquisador, null)
                    .andExpect(status().isOk()).andExpect(jsonPath("$.visitas", hasSize(3)));
        }
    }

    @Test
    @DisplayName("401 sem token e 403 para administrador e participante")
    void autorizacao() throws Exception {
        UUID participacao = participacaoComTresVisitas("EXECUCAO");
        String url = "/api/participacoes/" + participacao + "/historico";

        esperarFalha(401, "GET", url, null);
        esperarFalha(403, "GET", url, TokenDeTeste.bearer(mvc, TokenDeTeste.ADMIN));
        esperarFalha(403, "GET", url, TokenDeTeste.bearer(mvc, TokenDeTeste.PARTICIPANTE));
    }

    @Test
    @DisplayName("404 RECURSO_NAO_ENCONTRADO para participação inexistente, no formato único de erro")
    void participacaoInexistente() throws Exception {
        enviar(mvc, "GET", "/api/participacoes/" + UUID.randomUUID() + "/historico", pesquisador, null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigoRegra").value("RECURSO_NAO_ENCONTRADO"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test
    @DisplayName("Somente leitura: POST, PUT e DELETE no recurso do histórico não são aceitos")
    void somenteLeitura() throws Exception {
        UUID participacao = participacaoComTresVisitas("EXECUCAO");
        String url = "/api/participacoes/" + participacao + "/historico";

        for (String metodo : new String[] {"POST", "PUT", "DELETE"}) {
            int status = enviar(mvc, metodo, url, pesquisador, null).andReturn().getResponse().getStatus();
            assertThat(status).as(metodo).isIn(404, 405);
        }
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM visitas_agendadas WHERE participacao_id = CAST(? AS UUID)",
                Integer.class, participacao.toString())).isEqualTo(2);
    }

    // ---- T98: casos de borda do histórico (RQ-T98-01, 02 e 04) ----

    @Test
    @DisplayName("RQ-T98-01: sem nenhuma visita agendada, todas as previstas saem NAO_AGENDADA e pendentes = previstas")
    void semNenhumaVisitaAgendada() throws Exception {
        UUID estudo = criarEstudo("Historico sem agenda " + UUID.randomUUID(), "EXECUCAO");
        criarVisitaPlanejada(estudo, "Retorno 60 dias", 60, 3);
        criarVisitaPlanejada(estudo, "Triagem", 0, 1);
        criarVisitaPlanejada(estudo, "Retorno 30 dias", 30, 2);
        UUID participacao = incluirParticipante(estudo, "CAND-T98A", "Pessoa Sem Agenda (Ficticia)", 5);

        enviar(mvc, "GET", "/api/participacoes/" + participacao + "/historico", pesquisador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.previstas").value(3))
                .andExpect(jsonPath("$.resumo.naoAgendadas").value(3))
                .andExpect(jsonPath("$.resumo.pendentes").value(3))
                .andExpect(jsonPath("$.resumo.realizadas").value(0))
                .andExpect(jsonPath("$.resumo.ausentes").value(0))
                .andExpect(jsonPath("$.resumo.canceladas").value(0))
                .andExpect(jsonPath("$.resumo.agendadas").value(0))
                .andExpect(jsonPath("$.visitas[0].nome").value("Triagem"))
                .andExpect(jsonPath("$.visitas[1].nome").value("Retorno 30 dias"))
                .andExpect(jsonPath("$.visitas[2].nome").value("Retorno 60 dias"))
                .andExpect(jsonPath("$.visitas[*].situacao", org.hamcrest.Matchers.everyItem(
                        org.hamcrest.Matchers.is("NAO_AGENDADA"))));
    }

    @Test
    @DisplayName("RQ-T98-02: com visitas em todas as situações, cada uma sai certa e o invariante do resumo vale")
    void todasAsSituacoesEInvariante() throws Exception {
        UUID estudo = criarEstudo("Historico todas as situacoes " + UUID.randomUUID(), "EXECUCAO");
        UUID v1 = criarVisitaPlanejada(estudo, "V1 realizada", 0, 1);
        UUID v2 = criarVisitaPlanejada(estudo, "V2 ausente", 10, 2);
        UUID v3 = criarVisitaPlanejada(estudo, "V3 cancelada", 20, 3);
        UUID v4 = criarVisitaPlanejada(estudo, "V4 agendada", 30, 4);
        criarVisitaPlanejada(estudo, "V5 sem agenda", 40, 5);
        UUID participacao = incluirParticipante(estudo, "CAND-T98B", "Pessoa Todas (Ficticia)", 50);
        UUID realizada = agendar(participacao, v1, "REALIZADA", LocalDateTime.of(2026, 11, 20, 10, 5), null, "Ok");
        registrarResultado(v1, realizada, "Escala de dor", "2");
        agendar(participacao, v2, "AUSENTE", null, "Nao compareceu", null);
        agendar(participacao, v3, "CANCELADA", null, "Participante em viagem", null);
        agendar(participacao, v4, "AGENDADA", null, null, null);

        enviar(mvc, "GET", "/api/participacoes/" + participacao + "/historico", coordenador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visitas", hasSize(5)))
                .andExpect(jsonPath("$.visitas[0].situacao").value("REALIZADA"))
                .andExpect(jsonPath("$.visitas[0].dataHoraAgendada").isNotEmpty())
                .andExpect(jsonPath("$.visitas[0].dataHoraRealizada").isNotEmpty())
                .andExpect(jsonPath("$.visitas[0].resultados", hasSize(1)))
                .andExpect(jsonPath("$.visitas[1].situacao").value("AUSENTE"))
                .andExpect(jsonPath("$.visitas[1].motivo").value("Nao compareceu"))
                .andExpect(jsonPath("$.visitas[2].situacao").value("CANCELADA"))
                .andExpect(jsonPath("$.visitas[2].motivo").value("Participante em viagem"))
                .andExpect(jsonPath("$.visitas[3].situacao").value("AGENDADA"))
                .andExpect(jsonPath("$.visitas[4].situacao").value("NAO_AGENDADA"))
                .andExpect(jsonPath("$.resumo.previstas").value(5))
                .andExpect(jsonPath("$.resumo.realizadas").value(1))
                .andExpect(jsonPath("$.resumo.ausentes").value(1))
                .andExpect(jsonPath("$.resumo.canceladas").value(1))
                .andExpect(jsonPath("$.resumo.agendadas").value(1))
                .andExpect(jsonPath("$.resumo.naoAgendadas").value(1))
                .andExpect(jsonPath("$.resumo.pendentes").value(2));
    }

    @Test
    @DisplayName("RQ-T98-04: estudo sem visitas previstas devolve 200 com lista vazia e resumo zerado")
    void estudoSemVisitasPrevistas() throws Exception {
        UUID estudo = criarEstudo("Historico sem previstas " + UUID.randomUUID(), "RECRUTAMENTO");
        UUID participacao = incluirParticipante(estudo, "CAND-T98C", "Pessoa Vazia (Ficticia)", 1);

        enviar(mvc, "GET", "/api/participacoes/" + participacao + "/historico", pesquisador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visitas", hasSize(0)))
                .andExpect(jsonPath("$.resumo.previstas").value(0))
                .andExpect(jsonPath("$.resumo.pendentes").value(0))
                .andExpect(jsonPath("$.resumo.realizadas").value(0));
    }

    // ---- demonstração: P4 e P3 ----

    private void executarScriptDeDemonstracao() throws Exception {
        Connection conexao = DataSourceUtils.getConnection(dataSource);
        ScriptUtils.executeSqlScript(conexao, new ClassPathResource("db/demo/R__demo_22_historico.sql"));
    }

    private UUID participacaoDoDemo(String codigo) {
        return jdbc.queryForObject("SELECT p.id FROM participacoes p JOIN candidatos c ON c.id = p.candidato_id "
                + "JOIN estudos e ON e.id = p.estudo_id WHERE c.codigo = ? AND e.titulo = ?", UUID.class, codigo,
                TITULO_DEMO);
    }

    @Test
    @DisplayName("Demonstração: o P4 fecha em 1 realizada, 1 ausente e 1 cancelada (0 pendentes); o P3 tem as 3 visitas pendentes")
    void demonstracaoP4EP3() throws Exception {
        jdbc.update("INSERT INTO usuarios (id, nome, email, senha, perfil, ativo) VALUES "
                + "(CAST(? AS UUID), 'Coordenador Demo (Ficticio)', 'demo.coordenador@clinicalstudy.test', 'x', "
                + "'COORDENADOR', TRUE)", UUID.randomUUID().toString());
        UUID estudo = criarEstudo(TITULO_DEMO, "EXECUCAO");
        criarVisitaPlanejada(estudo, "Triagem", 0, 1);
        criarVisitaPlanejada(estudo, "Retorno 30 dias", 30, 2);
        criarVisitaPlanejada(estudo, "Retorno 60 dias", 60, 3);
        incluirParticipante(estudo, "CAND-003", "Pessoa Kappa (Ficticia)", 1);
        incluirParticipante(estudo, "CAND-004", "Pessoa Lambda (Ficticia)", 35);
        executarScriptDeDemonstracao();

        enviar(mvc, "GET", "/api/participacoes/" + participacaoDoDemo("CAND-004") + "/historico", pesquisador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.previstas").value(3))
                .andExpect(jsonPath("$.resumo.realizadas").value(1))
                .andExpect(jsonPath("$.resumo.ausentes").value(1))
                .andExpect(jsonPath("$.resumo.canceladas").value(1))
                .andExpect(jsonPath("$.resumo.agendadas").value(0))
                .andExpect(jsonPath("$.resumo.naoAgendadas").value(0))
                .andExpect(jsonPath("$.resumo.pendentes").value(0))
                .andExpect(jsonPath("$.visitas[0].situacao").value("REALIZADA"))
                .andExpect(jsonPath("$.visitas[1].situacao").value("AUSENTE"))
                .andExpect(jsonPath("$.visitas[2].situacao").value("CANCELADA"))
                .andExpect(jsonPath("$.visitas[2].motivo").value("Participante em viagem"));

        enviar(mvc, "GET", "/api/participacoes/" + participacaoDoDemo("CAND-003") + "/historico", coordenador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.previstas").value(3))
                .andExpect(jsonPath("$.resumo.naoAgendadas").value(3))
                .andExpect(jsonPath("$.resumo.pendentes").value(3))
                .andExpect(jsonPath("$.visitas[0].situacao").value("NAO_AGENDADA"))
                .andExpect(jsonPath("$.visitas[2].situacao").value("NAO_AGENDADA"));
    }

    // ---- sem N+1 ----

    /** Comandos SQL que o serviço envia ao banco para montar o histórico de um estudo com {@code n} visitas previstas. */
    private long comandosParaMontarHistorico(int n) {
        UUID estudo = criarEstudo("Historico N+1 " + n + " " + UUID.randomUUID(), "EXECUCAO");
        UUID participacao = incluirParticipante(estudo, "CAND-N" + n, "Pessoa N+1 (Ficticia)", 200);
        for (int i = 0; i < n; i++) {
            UUID prevista = criarVisitaPlanejada(estudo, "Visita " + i, i * 10, i + 1);
            UUID agendada = agendar(participacao, prevista, "REALIZADA", LocalDateTime.of(2026, 11, 20, 10, 5), null,
                    null);
            registrarResultado(prevista, agendada, "Atividade A" + i, "1");
            registrarResultado(prevista, agendada, "Atividade B" + i, "2");
        }
        Statistics estatisticas = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        estatisticas.clear();

        var historico = service.historico(participacao);

        assertThat(historico.visitas()).hasSize(n);
        assertThat(historico.visitas()).allSatisfy(v -> assertThat(v.resultados()).hasSize(2));
        return estatisticas.getPrepareStatementCount();
    }

    @Test
    @DisplayName("Sem N+1: poucas consultas, e o mesmo número com 3 ou com 15 visitas previstas")
    void semNMaisUm() {
        long comTres = comandosParaMontarHistorico(3);
        long comQuinze = comandosParaMontarHistorico(15);

        assertThat(comTres).as("participação + previstas + agendamentos + resultados").isLessThanOrEqualTo(4);
        assertThat(comQuinze).isEqualTo(comTres);
    }
}
