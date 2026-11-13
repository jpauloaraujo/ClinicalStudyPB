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
}
