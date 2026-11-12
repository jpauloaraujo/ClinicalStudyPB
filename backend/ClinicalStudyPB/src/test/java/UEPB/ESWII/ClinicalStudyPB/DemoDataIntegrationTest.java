package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** T63: migrations reais em bancos isolados, sem alterar o PostgreSQL local. */
class DemoDataIntegrationTest {

    private final List<SingleConnectionDataSource> bancos = new ArrayList<>();

    @AfterEach
    void fecharBancos() {
        bancos.forEach(SingleConnectionDataSource::destroy);
    }

    private static final String ESTUDO_PLANEJAMENTO = "d6300000-0000-4000-8000-000000000101";
    private static final String ESTUDO_RECRUTAMENTO = "d6300000-0000-4000-8000-000000000102";
    private static final String ESTUDO_ACOMPANHAMENTO = "d6300000-0000-4000-8000-000000001901";
    private static final String CANDIDATO_PRONTO = "d6300000-0000-4000-8000-000000000504";
    private static final String[] TABELAS = {"usuarios", "estudos", "criterios", "visitas_planejadas", "acoes",
            "candidatos", "avaliacoes_elegibilidade", "respostas_avaliacao", "consentimentos", "participacoes",
            "historico_status_estudo", "visitas_agendadas"};

    @Test
    @DisplayName("T63: o perfil normal nao executa a carga adicional de demonstracao")
    void perfilNormalNaoCarregaDemo() throws Exception {
        SingleConnectionDataSource banco = novoBanco();
        Properties padrao = PropertiesLoaderUtils.loadProperties(new ClassPathResource("application.properties"));
        Flyway flyway = Flyway.configure().dataSource(banco)
                .locations(padrao.getProperty("spring.flyway.locations").split(",")).load();
        flyway.migrate();

        assertThat(new JdbcTemplate(banco).queryForObject(
                "select count(*) from usuarios where email like 'demo.%'", Integer.class)).isZero();
        assertThat(flyway.info().applied()).noneMatch(info -> "dados demonstracao".equals(info.getDescription()));
    }

    @Test
    @DisplayName("T63: perfil demo cria perfis, planejamento e recrutamento com uma ultima vaga")
    void demoCarregaCenarioConsistente() throws Exception {
        SingleConnectionDataSource banco = novoBanco();
        Flyway flyway = migrarDemo(banco);
        JdbcTemplate jdbc = new JdbcTemplate(banco);

        assertThat(flyway.info().applied()).anyMatch(info -> info.getVersion() == null);
        assertThat(jdbc.queryForList("select perfil from usuarios where email like 'demo.%' order by perfil", String.class))
                .containsExactly("ADMINISTRADOR", "COORDENADOR", "PARTICIPANTE", "PESQUISADOR");
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        assertThat(jdbc.queryForList("select senha from usuarios where email like 'demo.%'", String.class))
                .allSatisfy(hash -> assertThat(encoder.matches("senha-de-teste", hash)).isTrue());
        assertThat(jdbc.queryForObject("select status from estudos where id = cast(? as uuid)", String.class,
                ESTUDO_PLANEJAMENTO)).isEqualTo("PLANEJAMENTO");
        assertThat(jdbc.queryForObject("select status from estudos where id = cast(? as uuid)", String.class,
                ESTUDO_RECRUTAMENTO)).isEqualTo("RECRUTAMENTO");
        assertThat(jdbc.queryForObject("select count(*) from criterios where estudo_id = cast(? as uuid)",
                Integer.class, ESTUDO_PLANEJAMENTO)).isEqualTo(2);
        assertThat(jdbc.queryForObject("select count(*) from visitas_planejadas where estudo_id = cast(? as uuid)",
                Integer.class, ESTUDO_PLANEJAMENTO)).isEqualTo(2);
        assertThat(jdbc.queryForList("select situacao from candidatos where estudo_id = cast(? as uuid) order by codigo",
                String.class, ESTUDO_RECRUTAMENTO))
                .containsExactly("CADASTRADO", "NAO_ELEGIVEL", "ELEGIVEL", "CONSENTIDO", "INCLUIDO", "CONSENTIDO");
        assertThat(jdbc.queryForObject("""
                select e.limite_participantes - (select count(*) from participacoes p where p.estudo_id = e.id)
                from estudos e where e.id = cast(? as uuid)
                """, Integer.class, ESTUDO_RECRUTAMENTO)).isEqualTo(1);
        // Duas pessoas prontas disputam a ultima vaga no roteiro de demonstracao.
        assertThat(jdbc.queryForObject("""
                select count(*) from candidatos c
                join avaliacoes_elegibilidade a on a.candidato_id = c.id and a.vigente = true
                join consentimentos co on co.candidato_id = c.id
                where c.estudo_id = cast(? as uuid) and c.situacao = 'CONSENTIDO' and a.resultado = 'APROVADO'
                """, Integer.class, ESTUDO_RECRUTAMENTO)).isEqualTo(2);
        assertThat(jdbc.queryForObject("""
                select count(*) from avaliacoes_elegibilidade a where a.candidato_id in
                (select id from candidatos where estudo_id = cast(? as uuid)) and
                (select count(*) from respostas_avaliacao r where r.avaliacao_id = a.id) <> 2
                """, Integer.class, ESTUDO_RECRUTAMENTO)).isZero();
    }

    @Test
    @DisplayName("T63: executar novamente nao duplica dados nem desfaz alteracoes do usuario")
    void cargaPodeSerRepetidaSemSobrescreverDados() throws Exception {
        SingleConnectionDataSource banco = novoBanco();
        Flyway flyway = migrarDemo(banco);
        JdbcTemplate jdbc = new JdbcTemplate(banco);
        Map<String, Integer> antes = contagens(jdbc);

        jdbc.update("update estudos set titulo = 'Titulo alterado na demonstracao' where id = cast(? as uuid)",
                ESTUDO_PLANEJAMENTO);
        jdbc.update("update candidatos set contato = 'contato ficticio alterado' where id = cast(? as uuid)",
                CANDIDATO_PRONTO);
        // O Flyway normalmente nao repete um R__ sem mudanca de checksum. A execucao
        // direta duas vezes tambem prova idempotencia se o arquivo for atualizado.
        try (Connection conexao = banco.getConnection()) {
            ClassPathResource script = new ClassPathResource("db/demo/R__dados_demonstracao.sql");
            ScriptUtils.executeSqlScript(conexao, script);
            ScriptUtils.executeSqlScript(conexao, script);
        }
        flyway.migrate();

        assertThat(contagens(jdbc)).isEqualTo(antes);
        assertThat(jdbc.queryForObject("select titulo from estudos where id = cast(? as uuid)", String.class,
                ESTUDO_PLANEJAMENTO)).isEqualTo("Titulo alterado na demonstracao");
        assertThat(jdbc.queryForObject("select contato from candidatos where id = cast(? as uuid)", String.class,
                CANDIDATO_PRONTO)).isEqualTo("contato ficticio alterado");
    }

    @Test
    @DisplayName("T74: o estudo de acompanhamento tem 3 participantes e visitas agendadas em situacoes diferentes, sem mexer no estudo da T63")
    void demoCarregaEstudoDeAcompanhamento() throws Exception {
        SingleConnectionDataSource banco = novoBanco();
        migrarDemo(banco);
        JdbcTemplate jdbc = new JdbcTemplate(banco);

        assertThat(jdbc.queryForObject("select status from estudos where id = cast(? as uuid)", String.class,
                ESTUDO_ACOMPANHAMENTO)).isEqualTo("EXECUCAO");
        assertThat(jdbc.queryForObject("select limite_participantes from estudos where id = cast(? as uuid)",
                Integer.class, ESTUDO_ACOMPANHAMENTO)).isEqualTo(6);
        assertThat(jdbc.queryForObject("select count(*) from criterios where estudo_id = cast(? as uuid) and tipo = 'INCLUSAO'",
                Integer.class, ESTUDO_ACOMPANHAMENTO)).isEqualTo(1);
        assertThat(jdbc.queryForList("select nome || ':' || dia_previsto || ':' || tolerancia from visitas_planejadas "
                + "where estudo_id = cast(? as uuid) order by ordem", String.class, ESTUDO_ACOMPANHAMENTO))
                .containsExactly("Triagem:0:3", "Retorno 30 dias:30:5", "Retorno 60 dias:60:7");
        assertThat(jdbc.queryForObject("select count(*) from acoes a join visitas_planejadas v on v.id = a.visita_id "
                + "where v.estudo_id = cast(? as uuid)", Integer.class, ESTUDO_ACOMPANHAMENTO)).isEqualTo(6);
        assertThat(jdbc.queryForObject("select count(*) from acoes a join visitas_planejadas v on v.id = a.visita_id "
                + "where v.estudo_id = cast(? as uuid) and a.categoria = 'AVALIACAO_CLINICA'", Integer.class,
                ESTUDO_ACOMPANHAMENTO)).isEqualTo(3);
        assertThat(jdbc.queryForList("select estado_novo from historico_status_estudo where estudo_id = cast(? as uuid) "
                + "order by data_hora", String.class, ESTUDO_ACOMPANHAMENTO)).containsExactly("EXECUCAO");

        // Tres participantes incluidos, cada um com avaliacao aprovada e consentimento, na data de inclusao esperada (D-20, D-3, D-1)
        assertThat(jdbc.queryForList("select c.codigo || ':' || datediff('DAY', p.data_inclusao, current_date) "
                + "from participacoes p join candidatos c on c.id = p.candidato_id where p.estudo_id = cast(? as uuid) "
                + "order by c.codigo", String.class, ESTUDO_ACOMPANHAMENTO))
                .containsExactly("CAND-001:20", "CAND-002:3", "CAND-003:1");
        assertThat(jdbc.queryForObject("select count(*) from candidatos c "
                + "join avaliacoes_elegibilidade a on a.candidato_id = c.id and a.vigente = true and a.resultado = 'APROVADO' "
                + "join consentimentos co on co.candidato_id = c.id "
                + "where c.estudo_id = cast(? as uuid) and c.situacao = 'INCLUIDO'", Integer.class, ESTUDO_ACOMPANHAMENTO))
                .isEqualTo(3);

        // Visitas agendadas: P1 Triagem REALIZADA e Retorno 30 AGENDADA em D+10 09:30; P2 Triagem AGENDADA em D 15:00; P3 nenhuma
        assertThat(jdbc.queryForList("select c.codigo || ':' || vp.nome || ':' || va.situacao from visitas_agendadas va "
                + "join participacoes p on p.id = va.participacao_id join candidatos c on c.id = p.candidato_id "
                + "join visitas_planejadas vp on vp.id = va.visita_planejada_id order by c.codigo, vp.ordem", String.class))
                .containsExactly("CAND-001:Triagem:REALIZADA", "CAND-001:Retorno 30 dias:AGENDADA", "CAND-002:Triagem:AGENDADA");
        assertThat(jdbc.queryForObject("select count(*) from visitas_agendadas va join participacoes p on p.id = va.participacao_id "
                + "join candidatos c on c.id = p.candidato_id where c.codigo = 'CAND-003' and p.estudo_id = cast(? as uuid)",
                Integer.class, ESTUDO_ACOMPANHAMENTO)).isZero();
        assertThat(jdbc.queryForObject("select data_hora_realizada is not null from visitas_agendadas "
                + "where id = cast('d6300000-0000-4000-8000-000000001981' as uuid)", Boolean.class)).isTrue();
        assertThat(jdbc.queryForObject("select datediff('DAY', current_date, cast(data_hora_agendada as date)) "
                + "from visitas_agendadas where id = cast('d6300000-0000-4000-8000-000000001982' as uuid)", Integer.class))
                .isEqualTo(10);

        // O estudo de recrutamento da T63 segue intacto
        assertThat(jdbc.queryForObject("select status from estudos where id = cast(? as uuid)", String.class,
                ESTUDO_RECRUTAMENTO)).isEqualTo("RECRUTAMENTO");
        assertThat(jdbc.queryForObject("select count(*) from participacoes where estudo_id = cast(? as uuid)",
                Integer.class, ESTUDO_RECRUTAMENTO)).isEqualTo(1);
    }

    @Test
    @DisplayName("T74: duas cargas do script de acompanhamento nao duplicam nada nem desfazem alteracoes do usuario")
    void cargaDoAcompanhamentoEIdempotente() throws Exception {
        SingleConnectionDataSource banco = novoBanco();
        migrarDemo(banco);
        JdbcTemplate jdbc = new JdbcTemplate(banco);
        Map<String, Integer> antes = contagens(jdbc);
        jdbc.update("update visitas_agendadas set observacao = 'alterada na demonstracao' "
                + "where id = cast('d6300000-0000-4000-8000-000000001983' as uuid)");

        try (Connection conexao = banco.getConnection()) {
            ClassPathResource script = new ClassPathResource("db/demo/R__demo_19_visitas_agendadas.sql");
            ScriptUtils.executeSqlScript(conexao, script);
            ScriptUtils.executeSqlScript(conexao, script);
        }

        assertThat(contagens(jdbc)).isEqualTo(antes);
        assertThat(antes.get("visitas_agendadas")).isEqualTo(3);
        assertThat(jdbc.queryForObject("select observacao from visitas_agendadas "
                + "where id = cast('d6300000-0000-4000-8000-000000001983' as uuid)", String.class))
                .isEqualTo("alterada na demonstracao");
    }

    private static Flyway migrarDemo(SingleConnectionDataSource banco) throws Exception {
        Properties perfil = PropertiesLoaderUtils.loadProperties(new ClassPathResource("application-demo.properties"));
        Flyway flyway = Flyway.configure().dataSource(banco)
                .locations(perfil.getProperty("spring.flyway.locations").split(",")).load();
        flyway.migrate();
        return flyway;
    }

    private SingleConnectionDataSource novoBanco() {
        // Conserva a sessao fisica usada para criar/avaliar os CHECKs do H2;
        // os closes logicos do Flyway e do JdbcTemplate nao a encerram antes do fim do teste.
        SingleConnectionDataSource banco = new SingleConnectionDataSource("jdbc:h2:mem:demo_" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE", "sa", "", true);
        bancos.add(banco);
        return banco;
    }

    private static Map<String, Integer> contagens(JdbcTemplate jdbc) {
        Map<String, Integer> contagens = new LinkedHashMap<>();
        for (String tabela : TABELAS) {
            contagens.put(tabela, jdbc.queryForObject("select count(*) from " + tabela, Integer.class));
        }
        return contagens;
    }
}
