package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * T38: a migração é segura em um banco já populado. Aplica só as migrations da Sprint 1 (V1 a V3, que
 * incluem o seed de usuários e estudos) e então as da Sprint 2, conferindo que nada foi perdido.
 */
class MigracaoSeguraTest {

    private static final String URL = "jdbc:h2:mem:migracao_segura;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;"
            + "DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1";

    private static Flyway flyway(String alvo) {
        return Flyway.configure().dataSource(URL, "sa", "").locations("classpath:db/migration")
                .target(alvo).load();
    }

    private static long contar(Connection conexao, String tabela) throws SQLException {
        try (Statement st = conexao.createStatement(); ResultSet rs = st.executeQuery("select count(*) from " + tabela)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private static boolean tabelaExiste(Connection conexao, String tabela) throws SQLException {
        try (ResultSet rs = conexao.getMetaData().getTables(null, null, tabela, null)) {
            return rs.next();
        }
    }

    @Test
    @DisplayName("As migrations da Sprint 2 (V4 e V5) preservam os dados da Sprint 1 e criam as tabelas novas")
    void migracaoSobreBancoPopulado() throws Exception {
        // A conexão é aberta antes do Flyway para o banco em memória não ser descartado entre as etapas
        try (Connection conexao = DriverManager.getConnection(URL, "sa", "")) {
            flyway("3").migrate();
            assertThat(tabelaExiste(conexao, "candidatos")).isFalse();
            long usuariosAntes = contar(conexao, "usuarios");
            long estudosAntes = contar(conexao, "estudos");
            assertThat(usuariosAntes).isGreaterThanOrEqualTo(4);
            assertThat(estudosAntes).isGreaterThanOrEqualTo(2);

            var resultado = flyway("latest").migrate();

            assertThat(resultado.success).isTrue();
            assertThat(contar(conexao, "usuarios")).isEqualTo(usuariosAntes);
            assertThat(contar(conexao, "estudos")).isEqualTo(estudosAntes);
            List<String> novas = List.of("criterios", "visitas_planejadas", "acoes", "candidatos",
                    "avaliacoes_elegibilidade", "respostas_avaliacao", "consentimentos", "participacoes",
                    "historico_status_estudo");
            for (String tabela : novas) {
                assertThat(tabelaExiste(conexao, tabela)).as("tabela %s", tabela).isTrue();
            }
            assertThat(contar(conexao, "candidatos")).isZero();
        }
    }

    @Test
    @DisplayName("Em um banco vazio todas as migrations são aplicadas em sequência")
    void migracaoEmBancoVazio() throws Exception {
        String urlVazia = URL.replace("migracao_segura", "migracao_vazia");
        var resultado = Flyway.configure().dataSource(urlVazia, "sa", "").locations("classpath:db/migration")
                .load().migrate();

        assertThat(resultado.success).isTrue();
        assertThat(resultado.migrationsExecuted).isGreaterThanOrEqualTo(5);
    }
}
