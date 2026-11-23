package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

/**
 * T95: o script de demonstração {@code R__demo_22_historico.sql} completa o histórico do P4 e não faz nada quando o
 * estudo de acompanhamento ainda não existe. O estudo e os participantes dos PBIs 19 e 20 são montados aqui,
 * dentro de uma transação que é desfeita no fim, porque esses scripts de demonstração são de outras tasks.
 */
@SpringBootTest
@Transactional
class DemoHistoricoParticipanteTest {

    private static final String TITULO = "Demo - Acompanhamento de participantes";

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private DataSource dataSource;

    private void executarScript() throws Exception {
        Connection conexao = DataSourceUtils.getConnection(dataSource);
        ScriptUtils.executeSqlScript(conexao, new ClassPathResource("db/demo/R__demo_22_historico.sql"));
    }

    /** Estudo de demonstração com P4 (incluído há 35 dias) e P3 (incluído há 1 dia), sem visitas agendadas. */
    private UUID montarEstudoComP3EP4() {
        jdbc.update("INSERT INTO usuarios (id, nome, email, senha, perfil, ativo) VALUES "
                + "(CAST(? AS UUID), 'Coordenador Demo (Ficticio)', 'demo.coordenador@clinicalstudy.test', 'x', 'COORDENADOR', TRUE)",
                UUID.randomUUID().toString());
        UUID estudo = UUID.randomUUID();
        jdbc.update("INSERT INTO estudos (id, titulo, objetivo, responsavel_id, data_inicio, data_fim, limite_participantes, status) "
                + "SELECT CAST(? AS UUID), ?, 'Demonstrar o acompanhamento.', id, DATE '2026-11-01', DATE '2027-05-01', 6, 'EXECUCAO' "
                + "FROM usuarios WHERE email = 'pesquisador@clinicalstudy.test'", estudo.toString(), TITULO);
        String[][] visitas = {{"Triagem", "0", "3", "1"}, {"Retorno 30 dias", "30", "5", "2"}, {"Retorno 60 dias", "60", "7", "3"}};
        for (String[] v : visitas) {
            jdbc.update("INSERT INTO visitas_planejadas (id, estudo_id, nome, dia_previsto, tolerancia, ordem) "
                    + "VALUES (CAST(? AS UUID), CAST(? AS UUID), ?, ?, ?, ?)", UUID.randomUUID().toString(),
                    estudo.toString(), v[0], Integer.parseInt(v[1]), Integer.parseInt(v[2]), Integer.parseInt(v[3]));
        }
        incluir(estudo, "CAND-003", "Pessoa Kappa (Ficticia)", 1);
        incluir(estudo, "CAND-004", "Pessoa Lambda (Ficticia)", 35);
        return estudo;
    }

    private void incluir(UUID estudo, String codigo, String nome, int diasAtras) {
        UUID candidato = UUID.randomUUID();
        jdbc.update("INSERT INTO candidatos (id, estudo_id, codigo, nome_ficticio, data_nascimento, situacao) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), ?, ?, DATE '1990-01-01', 'INCLUIDO')",
                candidato.toString(), estudo.toString(), codigo, nome);
        jdbc.update("INSERT INTO participacoes (id, candidato_id, estudo_id, data_inclusao, incluido_por_id) "
                + "SELECT CAST(? AS UUID), CAST(? AS UUID), CAST(? AS UUID), CURRENT_DATE - INTERVAL '" + diasAtras + "' DAY, id "
                + "FROM usuarios WHERE email = 'coordenador@clinicalstudy.test'",
                UUID.randomUUID().toString(), candidato.toString(), estudo.toString());
    }

    private List<Map<String, Object>> visitasDe(String codigo) {
        return jdbc.queryForList("SELECT vp.nome, va.situacao, va.motivo, va.data_hora_realizada, va.data_hora_agendada "
                + "FROM visitas_agendadas va JOIN visitas_planejadas vp ON vp.id = va.visita_planejada_id "
                + "JOIN participacoes p ON p.id = va.participacao_id JOIN candidatos c ON c.id = p.candidato_id "
                + "WHERE c.codigo = ? AND vp.estudo_id IN (SELECT id FROM estudos WHERE titulo = ?) ORDER BY vp.ordem",
                codigo, TITULO);
    }

    @Test
    @DisplayName("O P4 fica com Triagem REALIZADA, Retorno 30 AUSENTE e Retorno 60 CANCELADA com motivo; o P3 segue sem agendamento")
    void completaOHistoricoDoP4() throws Exception {
        montarEstudoComP3EP4();

        executarScript();

        List<Map<String, Object>> p4 = visitasDe("CAND-004");
        assertThat(p4).extracting(l -> l.get("nome")).containsExactly("Triagem", "Retorno 30 dias", "Retorno 60 dias");
        assertThat(p4).extracting(l -> l.get("situacao")).containsExactly("REALIZADA", "AUSENTE", "CANCELADA");
        assertThat(p4.get(0).get("data_hora_realizada")).isNotNull();
        assertThat(p4.get(2).get("motivo")).isEqualTo("Participante em viagem");
        assertThat(visitasDe("CAND-003")).isEmpty();
    }

    @Test
    @DisplayName("Rodar o script de novo não duplica nem altera o que já existe")
    void ehRepetivel() throws Exception {
        montarEstudoComP3EP4();
        executarScript();
        jdbc.update("UPDATE visitas_agendadas SET observacao = 'editada pelo usuario' "
                + "WHERE id = CAST('d6300000-0000-4000-8000-000000002202' AS UUID)");

        executarScript();

        assertThat(visitasDe("CAND-004")).hasSize(3);
        assertThat(jdbc.queryForObject("SELECT observacao FROM visitas_agendadas "
                + "WHERE id = CAST('d6300000-0000-4000-8000-000000002202' AS UUID)", String.class))
                .isEqualTo("editada pelo usuario");
    }

    @Test
    @DisplayName("Sem o estudo de acompanhamento no banco, o script não insere nada e não dá erro")
    void semOEstudoNaoFazNada() throws Exception {
        int antes = jdbc.queryForObject("SELECT COUNT(*) FROM visitas_agendadas", Integer.class);

        executarScript();

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM visitas_agendadas", Integer.class)).isEqualTo(antes);
    }
}
