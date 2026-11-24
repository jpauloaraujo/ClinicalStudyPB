package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.PerfilUsuario;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.repository.EstudoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.UsuarioRepository;
import UEPB.ESWII.ClinicalStudyPB.support.CenarioVisitaAgendada;

/**
 * Testes de integração das migrations iniciais do Flyway (T09).
 */
@SpringBootTest
@Transactional
class FlywayMigrationIntegrationTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EstudoRepository estudoRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    @DisplayName("T09 - Deve aplicar com sucesso as migrations iniciais V1 e V2 do Flyway")
    void deveAplicarMigrationsIniciaisDoFlyway() {
        MigrationInfo[] aplicadas = flyway.info().applied();

        assertThat(aplicadas)
                .hasSizeGreaterThanOrEqualTo(2)
                .extracting(info -> info.getVersion().getVersion())
                .contains("1", "2");
    }

    @Test
    @DisplayName("T09 - Deve carregar os quatro perfis fictícios no seed inicial com senha em BCrypt")
    void deveCarregarUsuariosDeSeedComQuatroPerfisESenhaValida() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        Usuario admin = usuarioRepository.findByEmail("admin@clinicalstudy.test").orElseThrow();
        assertThat(admin.getPerfil()).isEqualTo(PerfilUsuario.ADMINISTRADOR);
        assertThat(admin.isAtivo()).isTrue();
        assertThat(encoder.matches("senha-de-teste", admin.getSenha())).isTrue();

        Usuario pesquisador = usuarioRepository.findByEmail("pesquisador@clinicalstudy.test").orElseThrow();
        assertThat(pesquisador.getPerfil()).isEqualTo(PerfilUsuario.PESQUISADOR);

        Usuario coordenador = usuarioRepository.findByEmail("coordenador@clinicalstudy.test").orElseThrow();
        assertThat(coordenador.getPerfil()).isEqualTo(PerfilUsuario.COORDENADOR);

        Usuario participante = usuarioRepository.findByEmail("participante@clinicalstudy.test").orElseThrow();
        assertThat(participante.getPerfil()).isEqualTo(PerfilUsuario.PARTICIPANTE);
    }

    @Test
    @DisplayName("T09 - Deve carregar os estudos clínicos fictícios vinculados ao pesquisador responsável")
    void deveCarregarEstudosClinicosIniciaisDeDemonstracao() {
        List<Estudo> estudos = estudoRepository.findAll();

        assertThat(estudos).hasSizeGreaterThanOrEqualTo(2);

        Estudo estudoPlanejamento = estudoRepository
                .findById(UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa"))
                .orElseThrow();

        assertThat(estudoPlanejamento.getStatus()).isEqualTo(StatusEstudo.PLANEJAMENTO);
        assertThat(estudoPlanejamento.getLimiteParticipantes()).isEqualTo(50);
        assertThat(estudoPlanejamento.getResponsavel().getEmail()).isEqualTo("pesquisador@clinicalstudy.test");
    }

    @Test
    @DisplayName("T73 - A tabela visitas_agendadas existe e aceita uma visita AGENDADA")
    void deveCriarTabelaDeVisitasAgendadas() {
        var ids = CenarioVisitaAgendada.criar(jdbc);

        CenarioVisitaAgendada.inserirVisitaAgendada(jdbc, ids, ids.visita(), "AGENDADA", null, null);

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM visitas_agendadas WHERE participacao_id = CAST(? AS UUID)",
                Integer.class, ids.participacao().toString())).isEqualTo(1);
    }

    @Test
    @DisplayName("T73 - O UNIQUE recusa a segunda visita agendada do mesmo par participação e visita prevista")
    void uniqueRecusaSegundoAgendamentoDoMesmoPar() {
        var ids = CenarioVisitaAgendada.criar(jdbc);
        CenarioVisitaAgendada.inserirVisitaAgendada(jdbc, ids, ids.visita(), "AGENDADA", null, null);

        recusadoPeloBanco(() -> CenarioVisitaAgendada.inserirVisitaAgendada(jdbc, ids, ids.visita(), "AGENDADA", null, null));
    }

    @Test
    @DisplayName("T73 - O CHECK recusa uma situação inválida")
    void checkRecusaSituacaoInvalida() {
        var ids = CenarioVisitaAgendada.criar(jdbc);

        recusadoPeloBanco(() -> CenarioVisitaAgendada.inserirVisitaAgendada(jdbc, ids, ids.visita(), "EM_ANDAMENTO", null, null));
    }

    @Test
    @DisplayName("T73 - O CHECK recusa REALIZADA sem data real e CANCELADA sem motivo, e aceita os dois completos")
    void checkExigeCamposPorSituacao() {
        var ids = CenarioVisitaAgendada.criar(jdbc);

        recusadoPeloBanco(() -> CenarioVisitaAgendada.inserirVisitaAgendada(jdbc, ids, ids.visita(), "REALIZADA", null, null));
        recusadoPeloBanco(() -> CenarioVisitaAgendada.inserirVisitaAgendada(jdbc, ids, ids.outraVisita(), "CANCELADA", null, null));

        CenarioVisitaAgendada.inserirVisitaAgendada(jdbc, ids, ids.visita(), "REALIZADA", LocalDateTime.now(), null);
        CenarioVisitaAgendada.inserirVisitaAgendada(jdbc, ids, ids.outraVisita(), "CANCELADA", null, "Participante viajou");
    }

    @Test
    @DisplayName("T73 - Não há cascata: apagar a participação com visita agendada é recusado, preservando o histórico")
    void apagarParticipacaoComVisitaAgendadaERecusado() {
        var ids = CenarioVisitaAgendada.criar(jdbc);
        CenarioVisitaAgendada.inserirVisitaAgendada(jdbc, ids, ids.visita(), "AGENDADA", null, null);

        recusadoPeloBanco(() -> jdbc.update("DELETE FROM participacoes WHERE id = CAST(? AS UUID)",
                ids.participacao().toString()));
    }

    @Test
    @DisplayName("T87 - A tabela resultados_atividade existe e aceita um resultado de uma atividade da visita")
    void deveCriarTabelaDeResultadosAtividade() {
        var ids = CenarioVisitaAgendada.criar(jdbc);
        UUID visitaAgendada = CenarioVisitaAgendada.inserirVisitaAgendada(jdbc, ids, ids.visita(), "REALIZADA",
                LocalDateTime.now(), null);
        UUID acao = CenarioVisitaAgendada.inserirAcao(jdbc, ids.visita(), "Aferir pressão arterial", "AVALIACAO_CLINICA");

        inserirResultado(visitaAgendada, acao, ids.usuario());

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM resultados_atividade WHERE visita_agendada_id = CAST(? AS UUID)",
                Integer.class, visitaAgendada.toString())).isEqualTo(1);
    }

    @Test
    @DisplayName("T87 - O UNIQUE recusa o segundo resultado da mesma atividade na mesma visita agendada")
    void uniqueRecusaSegundoResultadoDaMesmaAtividade() {
        var ids = CenarioVisitaAgendada.criar(jdbc);
        UUID visitaAgendada = CenarioVisitaAgendada.inserirVisitaAgendada(jdbc, ids, ids.visita(), "REALIZADA",
                LocalDateTime.now(), null);
        UUID acao = CenarioVisitaAgendada.inserirAcao(jdbc, ids.visita(), "Aferir pressão arterial", "AVALIACAO_CLINICA");
        UUID outraAcao = CenarioVisitaAgendada.inserirAcao(jdbc, ids.visita(), "Escala de dor", "AVALIACAO_CLINICA");
        inserirResultado(visitaAgendada, acao, ids.usuario());

        recusadoPeloBanco(() -> inserirResultado(visitaAgendada, acao, ids.usuario()));
        inserirResultado(visitaAgendada, outraAcao, ids.usuario());
    }

    @Test
    @DisplayName("T87 - As chaves estrangeiras recusam visita agendada, atividade e usuário inexistentes")
    void fksRecusamReferenciasInexistentes() {
        var ids = CenarioVisitaAgendada.criar(jdbc);
        UUID visitaAgendada = CenarioVisitaAgendada.inserirVisitaAgendada(jdbc, ids, ids.visita(), "REALIZADA",
                LocalDateTime.now(), null);
        UUID acao = CenarioVisitaAgendada.inserirAcao(jdbc, ids.visita(), "Aferir pressão arterial", "AVALIACAO_CLINICA");

        recusadoPeloBanco(() -> inserirResultado(UUID.randomUUID(), acao, ids.usuario()));
        recusadoPeloBanco(() -> inserirResultado(visitaAgendada, UUID.randomUUID(), ids.usuario()));
        recusadoPeloBanco(() -> inserirResultado(visitaAgendada, acao, UUID.randomUUID()));
    }

    @Test
    @DisplayName("T87 - Não há cascata: apagar a visita agendada ou a atividade com resultado é recusado")
    void apagarVisitaOuAtividadeComResultadoERecusado() {
        var ids = CenarioVisitaAgendada.criar(jdbc);
        UUID visitaAgendada = CenarioVisitaAgendada.inserirVisitaAgendada(jdbc, ids, ids.visita(), "REALIZADA",
                LocalDateTime.now(), null);
        UUID acao = CenarioVisitaAgendada.inserirAcao(jdbc, ids.visita(), "Aferir pressão arterial", "AVALIACAO_CLINICA");
        inserirResultado(visitaAgendada, acao, ids.usuario());

        recusadoPeloBanco(() -> jdbc.update("DELETE FROM visitas_agendadas WHERE id = CAST(? AS UUID)",
                visitaAgendada.toString()));
        recusadoPeloBanco(() -> jdbc.update("DELETE FROM acoes WHERE id = CAST(? AS UUID)",
                acao.toString()));
    }

    @Test
    @DisplayName("T111 - O índice único de candidatos.usuario_id existe, aceita vários candidatos sem conta e recusa a mesma conta duas vezes")
    void uniqueDaContaDoCandidato() {
        UUID estudo = inserirEstudoParaConta();
        UUID conta = inserirContaParticipante();

        assertThat(indicesUnicosDeCandidatos()).as("índices únicos de candidatos").contains("uk_candidatos_usuario");

        inserirCandidatoComConta(estudo, null, "T111-A");
        inserirCandidatoComConta(estudo, null, "T111-B");
        inserirCandidatoComConta(estudo, conta, "T111-C");

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM candidatos WHERE estudo_id = CAST(? AS UUID) "
                + "AND usuario_id IS NULL", Integer.class, estudo.toString())).isEqualTo(2);
        recusadoPeloBanco(() -> inserirCandidatoComConta(estudo, conta, "T111-D"));
    }

    @Test
    @DisplayName("T111 - Contas diferentes em candidatos diferentes convivem; o índice não atrapalha o um para um")
    void contasDiferentesConvivem() {
        UUID estudo = inserirEstudoParaConta();

        inserirCandidatoComConta(estudo, inserirContaParticipante(), "T111-E");
        inserirCandidatoComConta(estudo, inserirContaParticipante(), "T111-F");

        assertThat(jdbc.queryForObject("SELECT COUNT(DISTINCT usuario_id) FROM candidatos WHERE estudo_id = CAST(? AS UUID)",
                Integer.class, estudo.toString())).isEqualTo(2);
    }

    /**
     * Nomes (em minúsculas) dos índices únicos da tabela candidatos, pelo {@code DatabaseMetaData} do JDBC: a consulta
     * funciona no H2 e no PostgreSQL (D-T111-06: {@code information_schema.indexes} só existe no H2).
     */
    private List<String> indicesUnicosDeCandidatos() {
        return jdbc.execute((java.sql.Connection conexao) -> {
            java.util.Set<String> nomes = new java.util.TreeSet<>();
            // o H2 e o PostgreSQL guardam o nome da tabela em caixas diferentes
            for (String tabela : new String[] { "candidatos", "CANDIDATOS" }) {
                try (java.sql.ResultSet indices = conexao.getMetaData().getIndexInfo(null, null, tabela, true, false)) {
                    while (indices.next()) {
                        String nome = indices.getString("INDEX_NAME");
                        if (nome != null) {
                            nomes.add(nome.toLowerCase());
                        }
                    }
                }
            }
            return new java.util.ArrayList<>(nomes);
        });
    }

    private UUID inserirEstudoParaConta() {
        UUID estudo = UUID.randomUUID();
        jdbc.update("INSERT INTO estudos (id, titulo, objetivo, responsavel_id, data_inicio, data_fim, "
                + "limite_participantes, status) SELECT CAST(? AS UUID), 'Estudo T111', 'Objetivo.', id, "
                + "DATE '2026-11-01', DATE '2027-05-01', 6, 'RECRUTAMENTO' FROM usuarios "
                + "WHERE email = 'pesquisador@clinicalstudy.test'", estudo.toString());
        return estudo;
    }

    private UUID inserirContaParticipante() {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO usuarios (id, nome, email, senha, perfil, ativo) VALUES (CAST(? AS UUID), "
                + "'Conta T111 (Ficticia)', ?, 'x', 'PARTICIPANTE', TRUE)", id.toString(),
                "t111." + id + "@clinicalstudy.test");
        return id;
    }

    private void inserirCandidatoComConta(UUID estudo, UUID conta, String codigo) {
        jdbc.update("INSERT INTO candidatos (id, estudo_id, codigo, nome_ficticio, data_nascimento, usuario_id, "
                + "situacao) VALUES (CAST(? AS UUID), CAST(? AS UUID), ?, ?, DATE '1990-01-01', "
                + "CAST(? AS UUID), 'CONSENTIDO')", UUID.randomUUID().toString(), estudo.toString(), codigo,
                // nome único por candidato: o índice da Sprint 2 recusa o mesmo nome e data no mesmo estudo (D-T111-01)
                "Pessoa " + codigo + " (Ficticia)", conta == null ? null : conta.toString());
    }

    @Test
    @DisplayName("T101 - A coluna participacoes.situacao nasce com padrão ATIVO e o CHECK recusa situação inválida")
    void participacaoNasceAtivaECheckRecusaInvalida() {
        var ids = CenarioVisitaAgendada.criar(jdbc);

        String situacao = jdbc.queryForObject(
                "SELECT situacao FROM participacoes WHERE id = CAST(? AS UUID)",
                String.class, ids.participacao().toString());
        assertThat(situacao).isEqualTo("ATIVO");

        jdbc.update("UPDATE participacoes SET situacao = 'RETIRADO' WHERE id = CAST(? AS UUID)",
                ids.participacao().toString());
        assertThat(jdbc.queryForObject("SELECT situacao FROM participacoes WHERE id = CAST(? AS UUID)",
                String.class, ids.participacao().toString())).isEqualTo("RETIRADO");

        recusadoPeloBanco(() -> jdbc.update("UPDATE participacoes SET situacao = 'SUSPENSO' WHERE id = CAST(? AS UUID)",
                ids.participacao().toString()));
    }

    @Test
    @DisplayName("T101 - A tabela retiradas_participacao aceita retirada válida e recusa duplicidade (UNIQUE)")
    void retiradaUnicaPorParticipacao() {
        var ids = CenarioVisitaAgendada.criar(jdbc);

        inserirRetirada(ids.participacao(), "PARTICIPANTE", ids.usuario());

        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM retiradas_participacao WHERE participacao_id = CAST(? AS UUID)",
                Integer.class, ids.participacao().toString())).isEqualTo(1);

        recusadoPeloBanco(() -> inserirRetirada(ids.participacao(), "COORDENACAO", ids.usuario()));
    }

    @Test
    @DisplayName("T101 - O CHECK de solicitante recusa valor fora de PARTICIPANTE, PESQUISADOR e COORDENACAO")
    void checkRecusaSolicitanteInvalido() {
        var ids = CenarioVisitaAgendada.criar(jdbc);

        recusadoPeloBanco(() -> inserirRetirada(ids.participacao(), "ADMINISTRADOR", ids.usuario()));
    }

    @Test
    @DisplayName("T101 - Chaves estrangeiras da retirada recusam referências inexistentes e impedem apagar participação")
    void fksDaRetiradaPreservamHistorico() {
        var ids = CenarioVisitaAgendada.criar(jdbc);

        recusadoPeloBanco(() -> inserirRetirada(UUID.randomUUID(), "PARTICIPANTE", ids.usuario()));
        recusadoPeloBanco(() -> inserirRetirada(ids.participacao(), "PARTICIPANTE", UUID.randomUUID()));

        inserirRetirada(ids.participacao(), "PESQUISADOR", ids.usuario());
        recusadoPeloBanco(() -> jdbc.update("DELETE FROM participacoes WHERE id = CAST(? AS UUID)",
                ids.participacao().toString()));
    }

    /**
     * Confere que o banco recusa o comando com {@link DataIntegrityViolationException}, dentro de um savepoint: no
     * PostgreSQL um erro aborta a transação inteira do teste, e o savepoint deixa os comandos seguintes rodarem.
     */
    private void recusadoPeloBanco(Runnable comando) {
        jdbc.execute("SAVEPOINT recusa_esperada");
        try {
            assertThatThrownBy(comando::run).isInstanceOf(DataIntegrityViolationException.class);
        } finally {
            jdbc.execute("ROLLBACK TO SAVEPOINT recusa_esperada");
        }
    }

    private void inserirResultado(UUID visitaAgendada, UUID acao, UUID usuario) {
        java.sql.Timestamp agora = java.sql.Timestamp.valueOf(LocalDateTime.now());
        jdbc.update("INSERT INTO resultados_atividade (id, visita_agendada_id, acao_id, valor, unidade, observacao, "
                + "registrado_por_id, registrado_em, alterado_por_id, alterado_em) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), CAST(? AS UUID), '120/80', 'mmHg', NULL, "
                + "CAST(? AS UUID), ?, CAST(? AS UUID), ?)",
                UUID.randomUUID().toString(), visitaAgendada.toString(), acao.toString(),
                usuario.toString(), agora, usuario.toString(), agora);
    }

    private void inserirRetirada(UUID participacao, String solicitante, UUID usuario) {
        java.sql.Timestamp agora = java.sql.Timestamp.valueOf(LocalDateTime.now());
        jdbc.update("INSERT INTO retiradas_participacao (id, participacao_id, motivo, solicitante, data_retirada, "
                + "registrado_por_id, registrado_em) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), 'Mudança de cidade', ?, CURRENT_DATE, CAST(? AS UUID), ?)",
                UUID.randomUUID().toString(), participacao.toString(), solicitante, usuario.toString(), agora);
    }
}
