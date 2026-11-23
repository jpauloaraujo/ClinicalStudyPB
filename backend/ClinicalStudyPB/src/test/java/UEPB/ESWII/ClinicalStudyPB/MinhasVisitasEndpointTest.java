package UEPB.ESWII.ClinicalStudyPB;

import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.enviar;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.service.MinhasVisitasService;
import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;
import jakarta.persistence.EntityManagerFactory;

/**
 * T116 (RF18): teste de fumaça de {@code /api/me/visitas/proximas} e {@code /api/me/visitas/historico} com o banco e
 * tokens reais: só as visitas da própria conta, ordenação, paginação, resumo independente da página, filtro por
 * estudo (de outro devolve vazio), retirada (canceladas com motivo), nenhum resultado nem dado de candidato, 400/401/
 * 403 e a contagem de consultas, que não cresce com o número de visitas (sem N+1).
 */
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureMockMvc
@Transactional
class MinhasVisitasEndpointTest {

    private static final String COORDENADOR_ID = "22222222-2222-4222-8222-222222222222";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private Clock clock;
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @Autowired
    private MinhasVisitasService service;

    private String participante;
    private UUID participanteId;

    @BeforeEach
    void entrar() throws Exception {
        participante = TokenDeTeste.bearer(mvc, TokenDeTeste.PARTICIPANTE);
        participanteId = UUID.fromString(jdbc.queryForObject(
                "SELECT CAST(id AS VARCHAR) FROM usuarios WHERE email = ?", String.class, TokenDeTeste.PARTICIPANTE));
    }

    // ---- montagem do cenário, direto no banco ----

    private LocalDateTime agora() {
        return LocalDateTime.now(clock);
    }

    private UUID criarOutraConta() {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO usuarios (id, nome, email, senha, perfil, ativo) VALUES (CAST(? AS UUID), "
                + "'Outro Participante (Ficticio)', ?, 'x', 'PARTICIPANTE', TRUE)", id.toString(),
                "outro." + id + "@clinicalstudy.test");
        return id;
    }

    private UUID criarEstudo(String titulo) {
        UUID estudo = UUID.randomUUID();
        jdbc.update("INSERT INTO estudos (id, titulo, objetivo, responsavel_id, data_inicio, data_fim, "
                + "limite_participantes, status) SELECT CAST(? AS UUID), ?, 'Objetivo.', id, DATE '2026-11-01', "
                + "DATE '2027-05-01', 6, 'EXECUCAO' FROM usuarios WHERE email = 'pesquisador@clinicalstudy.test'",
                estudo.toString(), titulo);
        return estudo;
    }

    private UUID criarVisitaPlanejada(UUID estudo, String nome, int ordem) {
        UUID visita = UUID.randomUUID();
        jdbc.update("INSERT INTO visitas_planejadas (id, estudo_id, nome, dia_previsto, tolerancia, ordem) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), ?, ?, 3, ?)", visita.toString(), estudo.toString(), nome,
                ordem * 10, ordem);
        return visita;
    }

    private void criarAtividade(UUID visitaPlanejada, String nome) {
        jdbc.update("INSERT INTO acoes (id, visita_id, nome, descricao, categoria) VALUES (CAST(? AS UUID), "
                + "CAST(? AS UUID), ?, 'Atividade ficticia', 'AVALIACAO_CLINICA')", UUID.randomUUID().toString(),
                visitaPlanejada.toString(), nome);
    }

    private UUID incluir(UUID estudo, UUID usuario, String codigo) {
        UUID candidato = UUID.randomUUID();
        UUID participacao = UUID.randomUUID();
        jdbc.update("INSERT INTO candidatos (id, estudo_id, codigo, nome_ficticio, data_nascimento, usuario_id, "
                + "situacao) VALUES (CAST(? AS UUID), CAST(? AS UUID), ?, 'Pessoa Sigilosa (Ficticia)', "
                + "DATE '1990-01-01', CAST(? AS UUID), 'INCLUIDO')", candidato.toString(), estudo.toString(), codigo,
                usuario.toString());
        jdbc.update("INSERT INTO participacoes (id, candidato_id, estudo_id, data_inclusao, incluido_por_id) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), CAST(? AS UUID), CURRENT_DATE - INTERVAL '30' DAY, "
                + "CAST(? AS UUID))", participacao.toString(), candidato.toString(), estudo.toString(),
                COORDENADOR_ID);
        return participacao;
    }

    private UUID agendar(UUID participacao, UUID visita, LocalDateTime quando, String situacao,
            LocalDateTime realizada, String motivo) {
        Timestamp criado = Timestamp.valueOf(agora().minusDays(40));
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO visitas_agendadas (id, participacao_id, visita_planejada_id, data_hora_agendada, "
                + "situacao, data_hora_realizada, motivo, observacao, versao, criado_por_id, criado_em, "
                + "alterado_por_id, alterado_em) VALUES (CAST(? AS UUID), CAST(? AS UUID), CAST(? AS UUID), ?, ?, ?, "
                + "?, 'Observacao sigilosa', 0, CAST(? AS UUID), ?, CAST(? AS UUID), ?)", id.toString(),
                participacao.toString(), visita.toString(), Timestamp.valueOf(quando), situacao,
                realizada == null ? null : Timestamp.valueOf(realizada), motivo, COORDENADOR_ID, criado,
                COORDENADOR_ID, criado);
        return id;
    }

    // ---- próximas ----

    @Test
    @DisplayName("200: próximas só AGENDADA de agora em diante, da mais próxima à mais distante, com atividades")
    void proximasOrdenadasComAtividades() throws Exception {
        UUID estudo = criarEstudo("Estudo T116 proximas");
        UUID participacao = incluir(estudo, participanteId, "CAND-961");
        UUID v1 = criarVisitaPlanejada(estudo, "Retorno 30 dias", 1);
        UUID v2 = criarVisitaPlanejada(estudo, "Retorno 60 dias", 2);
        UUID v3 = criarVisitaPlanejada(estudo, "Retorno 90 dias", 3);
        UUID passada = criarVisitaPlanejada(estudo, "Visita passada", 4);
        criarAtividade(v1, "Pressao arterial");
        criarAtividade(v1, "Peso");
        UUID distante = agendar(participacao, v3, agora().plusDays(30), "AGENDADA", null, null);
        UUID proxima = agendar(participacao, v1, agora().plusDays(2), "AGENDADA", null, null);
        UUID meio = agendar(participacao, v2, agora().plusDays(10), "AGENDADA", null, null);
        agendar(participacao, passada, agora().minusDays(1), "AGENDADA", null, null);

        enviar(mvc, "GET", "/api/me/visitas/proximas", participante, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value(proxima.toString()))
                .andExpect(jsonPath("$[1].id").value(meio.toString()))
                .andExpect(jsonPath("$[2].id").value(distante.toString()))
                .andExpect(jsonPath("$[0].participacaoId").value(participacao.toString()))
                .andExpect(jsonPath("$[0].estudoId").value(estudo.toString()))
                .andExpect(jsonPath("$[0].estudoTitulo").value("Estudo T116 proximas"))
                .andExpect(jsonPath("$[0].visitaNome").value("Retorno 30 dias"))
                .andExpect(jsonPath("$[0].situacao").value("AGENDADA"))
                .andExpect(jsonPath("$[0].dataHora").value(org.hamcrest.Matchers.matchesPattern(
                        "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?([+-]\\d{2}:\\d{2}|Z)")))
                .andExpect(jsonPath("$[0].atividades[0]").value("Peso"))
                .andExpect(jsonPath("$[0].atividades[1]").value("Pressao arterial"))
                .andExpect(jsonPath("$[1].atividades", hasSize(0)))
                .andExpect(jsonPath("$[0].observacao").doesNotExist())
                .andExpect(jsonPath("$[0].resultados").doesNotExist())
                .andExpect(jsonPath("$[0].codigo").doesNotExist())
                .andExpect(jsonPath("$[0].nomeFicticio").doesNotExist());
    }

    @Test
    @DisplayName("Próximas: visita de outro participante e conta sem vínculo devolvem lista vazia (200)")
    void proximasSoDaConta() throws Exception {
        UUID estudo = criarEstudo("Estudo T116 de outro");
        UUID prevista = criarVisitaPlanejada(estudo, "Retorno", 1);
        UUID deOutro = incluir(estudo, criarOutraConta(), "CAND-962");
        agendar(deOutro, prevista, agora().plusDays(3), "AGENDADA", null, null);

        enviar(mvc, "GET", "/api/me/visitas/proximas", participante, null)
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        enviar(mvc, "GET", "/api/me/visitas/proximas?estudoId=" + estudo, participante, null)
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Próximas: filtro por estudoId devolve as visitas daquele estudo e vazio para estudo de outro participante")
    void proximasFiltraPorEstudo() throws Exception {
        // T111: a conta é um para um com o candidato (índice único), então o participante tem uma só participação
        UUID estudoA = criarEstudo("Estudo A T116");
        UUID estudoB = criarEstudo("Estudo B T116");
        UUID a = incluir(estudoA, participanteId, "CAND-963");
        UUID b = incluir(estudoB, criarOutraConta(), "CAND-964");
        UUID visitaA = agendar(a, criarVisitaPlanejada(estudoA, "Retorno A", 1), agora().plusDays(2), "AGENDADA",
                null, null);
        agendar(b, criarVisitaPlanejada(estudoB, "Retorno B", 1), agora().plusDays(4), "AGENDADA", null, null);

        enviar(mvc, "GET", "/api/me/visitas/proximas?estudoId=" + estudoA, participante, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(visitaA.toString()));
        enviar(mvc, "GET", "/api/me/visitas/proximas?estudoId=" + estudoB, participante, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        enviar(mvc, "GET", "/api/me/visitas/proximas", participante, null)
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // ---- histórico ----

    @Test
    @DisplayName("200: histórico REALIZADA/AUSENTE/CANCELADA, da mais recente à mais antiga, com motivo e resumo")
    void historicoComResumo() throws Exception {
        UUID estudo = criarEstudo("Estudo T116 historico");
        UUID participacao = incluir(estudo, participanteId, "CAND-965");
        UUID realizada = agendar(participacao, criarVisitaPlanejada(estudo, "V1", 1), agora().minusDays(20),
                "REALIZADA", agora().minusDays(20), null);
        UUID ausente = agendar(participacao, criarVisitaPlanejada(estudo, "V2", 2), agora().minusDays(10),
                "AUSENTE", null, null);
        UUID cancelada = agendar(participacao, criarVisitaPlanejada(estudo, "V3", 3), agora().minusDays(2),
                "CANCELADA", null, "retirada do estudo");
        agendar(participacao, criarVisitaPlanejada(estudo, "V4", 4), agora().plusDays(5), "AGENDADA", null, null);

        enviar(mvc, "GET", "/api/me/visitas/historico", participante, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.realizadas").value(1))
                .andExpect(jsonPath("$.resumo.ausentes").value(1))
                .andExpect(jsonPath("$.resumo.canceladas").value(1))
                .andExpect(jsonPath("$.conteudo", hasSize(3)))
                .andExpect(jsonPath("$.conteudo[0].id").value(cancelada.toString()))
                .andExpect(jsonPath("$.conteudo[0].motivo").value("retirada do estudo"))
                .andExpect(jsonPath("$.conteudo[1].id").value(ausente.toString()))
                .andExpect(jsonPath("$.conteudo[1].motivo").value(nullValue()))
                .andExpect(jsonPath("$.conteudo[2].id").value(realizada.toString()))
                .andExpect(jsonPath("$.conteudo[2].dataHoraRealizada").isNotEmpty())
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanho").value(20))
                .andExpect(jsonPath("$.totalElementos").value(3))
                .andExpect(jsonPath("$.totalPaginas").value(1))
                .andExpect(jsonPath("$.conteudo[0].observacao").doesNotExist())
                .andExpect(jsonPath("$.conteudo[0].resultados").doesNotExist());
    }

    @Test
    @DisplayName("Histórico: paginação, e o resumo não depende da página pedida")
    void historicoPaginadoComResumoIndependente() throws Exception {
        UUID estudo = criarEstudo("Estudo T116 paginas");
        UUID participacao = incluir(estudo, participanteId, "CAND-966");
        for (int i = 1; i <= 5; i++) {
            agendar(participacao, criarVisitaPlanejada(estudo, "V" + i, i), agora().minusDays(i), "REALIZADA",
                    agora().minusDays(i), null);
        }

        enviar(mvc, "GET", "/api/me/visitas/historico?pagina=1&tamanho=2", participante, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(2)))
                .andExpect(jsonPath("$.conteudo[0].visitaNome").value("V3"))
                .andExpect(jsonPath("$.conteudo[1].visitaNome").value("V4"))
                .andExpect(jsonPath("$.pagina").value(1))
                .andExpect(jsonPath("$.tamanho").value(2))
                .andExpect(jsonPath("$.totalElementos").value(5))
                .andExpect(jsonPath("$.totalPaginas").value(3))
                .andExpect(jsonPath("$.resumo.realizadas").value(5));
    }

    @Test
    @DisplayName("Histórico: estudoId de outro participante devolve vazio e resumo zerado; sem vínculo também")
    void historicoDeOutroOuSemVinculoVazio() throws Exception {
        UUID estudo = criarEstudo("Estudo T116 historico de outro");
        UUID deOutro = incluir(estudo, criarOutraConta(), "CAND-967");
        agendar(deOutro, criarVisitaPlanejada(estudo, "V1", 1), agora().minusDays(3), "REALIZADA",
                agora().minusDays(3), null);

        for (String url : new String[] {"/api/me/visitas/historico",
                "/api/me/visitas/historico?estudoId=" + estudo}) {
            enviar(mvc, "GET", url, participante, null)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.conteudo", hasSize(0)))
                    .andExpect(jsonPath("$.totalElementos").value(0))
                    .andExpect(jsonPath("$.resumo.realizadas").value(0))
                    .andExpect(jsonPath("$.resumo.ausentes").value(0))
                    .andExpect(jsonPath("$.resumo.canceladas").value(0));
        }
    }

    // ---- validação e autorização ----

    @ParameterizedTest(name = "400 PARAMETRO_INVALIDO: {0}")
    @ValueSource(strings = {"pagina=-1", "tamanho=0", "tamanho=101", "pagina=abc", "estudoId=nao-e-uuid"})
    @DisplayName("Parâmetros inválidos recebem 400 PARAMETRO_INVALIDO")
    void parametrosInvalidos(String consulta) throws Exception {
        enviar(mvc, "GET", "/api/me/visitas/historico?" + consulta, participante, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("PARAMETRO_INVALIDO"));
    }

    @Test
    @DisplayName("400: estudoId inválido também nas próximas")
    void estudoIdInvalidoNasProximas() throws Exception {
        enviar(mvc, "GET", "/api/me/visitas/proximas?estudoId=xyz", participante, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("PARAMETRO_INVALIDO"));
    }

    @ParameterizedTest(name = "403: {0} não acessa /api/me/visitas/**")
    @ValueSource(strings = {TokenDeTeste.ADMIN, TokenDeTeste.PESQUISADOR, TokenDeTeste.COORDENADOR})
    @DisplayName("Os demais perfis recebem 403")
    void demaisPerfisRecebem403(String email) throws Exception {
        String token = TokenDeTeste.bearer(mvc, email);

        enviar(mvc, "GET", "/api/me/visitas/proximas", token, null).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));
        enviar(mvc, "GET", "/api/me/visitas/historico", token, null).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("401: sem token; 405: escrita não é aceita")
    void semTokenESomenteLeitura() throws Exception {
        enviar(mvc, "GET", "/api/me/visitas/proximas", null, null).andExpect(status().isUnauthorized());
        enviar(mvc, "GET", "/api/me/visitas/historico", null, null).andExpect(status().isUnauthorized());
        enviar(mvc, "POST", "/api/me/visitas/proximas", participante, "{}").andExpect(status().isMethodNotAllowed());
    }

    // ---- sem N+1 ----

    /** Comandos SQL para montar próximas + histórico de um participante com {@code n} visitas de cada tipo. */
    private long comandosParaConsultar(int n) {
        UUID estudo = criarEstudo("Estudo N+1 " + n + " " + UUID.randomUUID());
        UUID conta = criarOutraConta();
        UUID participacao = incluir(estudo, conta, "CAND-N" + n);
        for (int i = 0; i < n; i++) {
            UUID prevista = criarVisitaPlanejada(estudo, "Futura " + i, i + 1);
            criarAtividade(prevista, "Atividade A" + i);
            criarAtividade(prevista, "Atividade B" + i);
            agendar(participacao, prevista, agora().plusDays(i + 1), "AGENDADA", null, null);
            UUID passada = criarVisitaPlanejada(estudo, "Passada " + i, n + i + 1);
            criarAtividade(passada, "Atividade C" + i);
            agendar(participacao, passada, agora().minusDays(i + 1), "REALIZADA", agora().minusDays(i + 1), null);
        }
        Statistics estatisticas = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        estatisticas.clear();

        var proximas = service.proximas(conta, null);
        var historico = service.historico(conta, null, 0, 100);

        assertThat(proximas).hasSize(n);
        assertThat(proximas).allSatisfy(v -> assertThat(v.atividades()).hasSize(2));
        assertThat(historico.conteudo()).hasSize(n);
        return estatisticas.getPrepareStatementCount();
    }

    @Test
    @DisplayName("Sem N+1: o mesmo número de consultas com 3 ou com 15 visitas de cada tipo")
    void semNMaisUm() {
        long comTres = comandosParaConsultar(3);
        long comQuinze = comandosParaConsultar(15);

        assertThat(comTres).as("participações + próximas + atividades + histórico + contagem + atividades")
                .isLessThanOrEqualTo(8);
        assertThat(comQuinze).isEqualTo(comTres);
    }
}
