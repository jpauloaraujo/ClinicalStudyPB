package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * T109: o script {@code R__demo_24_conta_participante.sql} vincula a conta de demonstração do participante ao P5 e
 * não faz nada quando o P5 não existe. O candidato é montado aqui, dentro de uma transação desfeita no fim, porque
 * o script que o cria (T84) é de outra task.
 */
@SpringBootTest
@Transactional
class DemoContaParticipanteTest {

    private static final String P5 = "d6300000-0000-4000-8000-000000002012";
    private static final String EMAIL_DEMO = "demo.participante@clinicalstudy.test";

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private DataSource dataSource;

    private void executarScript() throws Exception {
        Connection conexao = DataSourceUtils.getConnection(dataSource);
        ScriptUtils.executeSqlScript(conexao, new ClassPathResource("db/demo/R__demo_24_conta_participante.sql"));
    }

    private void criarContaDemo() {
        jdbc.update("INSERT INTO usuarios (id, nome, email, senha, perfil, ativo) VALUES (CAST(? AS UUID), "
                + "'Participante Demo (Ficticio)', ?, 'x', 'PARTICIPANTE', TRUE)", UUID.randomUUID().toString(),
                EMAIL_DEMO);
    }

    private void criarP5() {
        UUID estudo = UUID.randomUUID();
        jdbc.update("INSERT INTO estudos (id, titulo, objetivo, responsavel_id, data_inicio, data_fim, "
                + "limite_participantes, status) SELECT CAST(? AS UUID), 'Demo T109', 'Objetivo.', id, "
                + "DATE '2026-11-01', DATE '2027-05-01', 6, 'EXECUCAO' FROM usuarios "
                + "WHERE email = 'pesquisador@clinicalstudy.test'", estudo.toString());
        jdbc.update("INSERT INTO candidatos (id, estudo_id, codigo, nome_ficticio, data_nascimento, situacao) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), 'CAND-005', 'Pessoa Mi (Ficticia)', DATE '1990-01-01', "
                + "'INCLUIDO')", P5, estudo.toString());
    }

    private String contaDoP5() {
        return jdbc.queryForObject("SELECT CAST(usuario_id AS VARCHAR) FROM candidatos WHERE id = CAST(? AS UUID)",
                String.class, P5);
    }

    @Test
    @DisplayName("vincula a conta demo ao P5, e repetir a carga não muda nada")
    void vinculaEEhIdempotente() throws Exception {
        criarContaDemo();
        criarP5();

        executarScript();
        String vinculada = contaDoP5();
        executarScript();

        assertThat(vinculada).isNotNull();
        assertThat(contaDoP5()).isEqualTo(vinculada);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM candidatos WHERE usuario_id = CAST(? AS UUID)",
                Integer.class, vinculada)).isEqualTo(1);
    }

    @Test
    @DisplayName("sem o P5 (script da T84 não rodou) não faz nada e não falha")
    void semP5NaoFazNada() throws Exception {
        criarContaDemo();

        executarScript();

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM candidatos WHERE usuario_id IS NOT NULL", Integer.class))
                .isZero();
    }

    private static final String P1 = "d6300000-0000-4000-8000-000000001911";

    /** Cria o P1 (CAND-001) num estudo próprio, como o script da T74 faria. */
    private void criarP1() {
        UUID estudo = UUID.randomUUID();
        jdbc.update("INSERT INTO estudos (id, titulo, objetivo, responsavel_id, data_inicio, data_fim, "
                + "limite_participantes, status) SELECT CAST(? AS UUID), 'Demo T111', 'Objetivo.', id, "
                + "DATE '2026-11-01', DATE '2027-05-01', 6, 'EXECUCAO' FROM usuarios "
                + "WHERE email = 'pesquisador@clinicalstudy.test'", estudo.toString());
        jdbc.update("INSERT INTO candidatos (id, estudo_id, codigo, nome_ficticio, data_nascimento, situacao) "
                + "VALUES (CAST(? AS UUID), CAST(? AS UUID), 'CAND-001', 'Pessoa Teta (Ficticia)', DATE '1990-02-02', "
                + "'INCLUIDO')", P1, estudo.toString());
    }

    private String contaDe(String candidato) {
        return jdbc.queryForObject("SELECT CAST(usuario_id AS VARCHAR) FROM candidatos WHERE id = CAST(? AS UUID)",
                String.class, candidato);
    }

    private String idDaConta(String email) {
        return jdbc.queryForObject("SELECT CAST(id AS VARCHAR) FROM usuarios WHERE email = ?", String.class, email);
    }

    @Test
    @DisplayName("T111: vincula participante@ ao P1 e demo.participante@ ao P5; repetir a carga não duplica nem muda")
    void vinculaAsDuasContasEhIdempotente() throws Exception {
        criarContaDemo();
        criarP1();
        criarP5();

        executarScript();
        executarScript();

        assertThat(contaDe(P1)).isEqualTo(idDaConta("participante@clinicalstudy.test"));
        assertThat(contaDe(P5)).isEqualTo(idDaConta(EMAIL_DEMO));
        assertThat(jdbc.queryForObject("SELECT COUNT(usuario_id) FROM candidatos", Integer.class))
                .as("um vínculo por conta").isEqualTo(jdbc.queryForObject(
                        "SELECT COUNT(DISTINCT usuario_id) FROM candidatos", Integer.class));
    }

    @Test
    @DisplayName("T111: não toma a conta de quem já a tem (vínculo feito pela API) nem sobrescreve o candidato já vinculado")
    void naoMexeEmVinculosExistentes() throws Exception {
        criarContaDemo();
        criarP1();
        criarP5();
        // a conta demo já pertence a outro candidato (vinculada pela API): o script não pode movê-la para o P5
        UUID estudoOutro = UUID.randomUUID();
        jdbc.update("INSERT INTO estudos (id, titulo, objetivo, responsavel_id, data_inicio, data_fim, "
                + "limite_participantes, status) SELECT CAST(? AS UUID), 'Outro T111', 'Objetivo.', id, "
                + "DATE '2026-11-01', DATE '2027-05-01', 6, 'EXECUCAO' FROM usuarios "
                + "WHERE email = 'pesquisador@clinicalstudy.test'", estudoOutro.toString());
        String candidatoOutro = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO candidatos (id, estudo_id, codigo, nome_ficticio, data_nascimento, usuario_id, "
                + "situacao) VALUES (CAST(? AS UUID), CAST(? AS UUID), 'CAND-777', 'Pessoa Outra (Ficticia)', "
                + "DATE '1991-03-03', CAST(? AS UUID), 'INCLUIDO')", candidatoOutro, estudoOutro.toString(),
                idDaConta(EMAIL_DEMO));

        executarScript();

        assertThat(contaDe(candidatoOutro)).isEqualTo(idDaConta(EMAIL_DEMO));
        assertThat(contaDe(P5)).as("P5 continua sem conta").isNull();
    }

    @Test
    @DisplayName("a conta do seed V2 (participante@clinicalstudy.test) continua sem vínculo")
    void contaDoSeedFicaSemVinculo() throws Exception {
        criarContaDemo();
        criarP5();

        executarScript();

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM candidatos c JOIN usuarios u ON u.id = c.usuario_id "
                + "WHERE u.email = 'participante@clinicalstudy.test'", Integer.class)).isZero();
    }
}
