package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.BloqueioResultados;
import UEPB.ESWII.ClinicalStudyPB.service.ConflitoRegraException;
import UEPB.ESWII.ClinicalStudyPB.service.ResultadoAtividadeService;
import UEPB.ESWII.ClinicalStudyPB.service.ResultadoAtividadeService.ComandoResultado;
import UEPB.ESWII.ClinicalStudyPB.service.ResultadoAtividadeService.ResultadoRegistrado;
import UEPB.ESWII.ClinicalStudyPB.service.ResultadoAtividadeService.ResultadosDaVisita;

/**
 * T89: o serviço de resultados sobre os dados de demonstração das T63 e T74 (estudo "Demo - Acompanhamento de
 * participantes"). A Triagem do P1 está REALIZADA e tem uma atividade de avaliação ("Aferir pressão arterial") e uma
 * de exame laboratorial; o Retorno 30 dias do P1 está AGENDADO. Os scripts rodam dentro da transação do teste, que é
 * desfeita no fim. Somente dados fictícios.
 */
@SpringBootTest
@Transactional
class ResultadoAtividadeDemoTest {

    private static final UUID TRIAGEM_P1_REALIZADA = UUID.fromString("d6300000-0000-4000-8000-000000001981");
    private static final UUID RETORNO_30_P1_AGENDADO = UUID.fromString("d6300000-0000-4000-8000-000000001982");
    private static final UUID AFERIR_PRESSAO = UUID.fromString("d6300000-0000-4000-8000-000000001941");
    private static final UUID COLETAR_SANGUE = UUID.fromString("d6300000-0000-4000-8000-000000001942");

    @Autowired
    private ResultadoAtividadeService service;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private DataSource dataSource;

    private UUID pesquisador;

    @BeforeEach
    void carregarDemonstracao() throws Exception {
        Connection conexao = DataSourceUtils.getConnection(dataSource);
        ScriptUtils.executeSqlScript(conexao, new ClassPathResource("db/demo/R__dados_demonstracao.sql"));
        ScriptUtils.executeSqlScript(conexao, new ClassPathResource("db/demo/R__demo_19_visitas_agendadas.sql"));
        pesquisador = jdbc.queryForObject("SELECT id FROM usuarios WHERE email = 'demo.pesquisador@clinicalstudy.test'",
                UUID.class);
    }

    @Test
    @DisplayName("Triagem realizada do P1: uma avaliação pendente, depois registrada e editada")
    void registraEEditaNaTriagemRealizada() {
        ResultadosDaVisita antes = service.consultar(TRIAGEM_P1_REALIZADA);
        assertThat(antes.bloqueio()).isNull();
        assertThat(antes.atividades()).extracting(a -> a.acao().getNome()).containsExactly("Aferir pressão arterial");
        assertThat(antes.pendentes()).isEqualTo(1);

        ResultadoRegistrado criado = service.registrar(TRIAGEM_P1_REALIZADA, AFERIR_PRESSAO,
                new ComandoResultado("120/80", "mmHg", null), pesquisador);
        ResultadoRegistrado editado = service.registrar(TRIAGEM_P1_REALIZADA, AFERIR_PRESSAO,
                new ComandoResultado("130/85", "mmHg", "Repetida após 10 minutos"), pesquisador);

        assertThat(criado.criado()).isTrue();
        assertThat(editado.criado()).isFalse();
        ResultadosDaVisita depois = service.consultar(TRIAGEM_P1_REALIZADA);
        assertThat(depois.registrados()).isEqualTo(1);
        assertThat(depois.pendentes()).isZero();
        assertThat(depois.atividades().get(0).resultado().getValor()).isEqualTo("130/85");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM resultados_atividade WHERE visita_agendada_id = ?",
                Integer.class, TRIAGEM_P1_REALIZADA)).isEqualTo(1);
    }

    @Test
    @DisplayName("Coletar sangue (exame laboratorial) da Triagem não recebe resultado")
    void exameLaboratorialNaoRecebeResultado() {
        assertThatThrownBy(() -> service.registrar(TRIAGEM_P1_REALIZADA, COLETAR_SANGUE,
                new ComandoResultado("Coletado", null, null), pesquisador))
                .isInstanceOf(ConflitoRegraException.class)
                .extracting(e -> ((ConflitoRegraException) e).getCodigo())
                .isEqualTo("ATIVIDADE_FORA_DA_CATEGORIA_AVALIACAO");
    }

    @Test
    @DisplayName("Retorno 30 dias do P1 ainda agendado: consulta com bloqueio e registro recusado")
    void visitaAgendadaFicaSomenteLeitura() {
        assertThat(service.consultar(RETORNO_30_P1_AGENDADO).bloqueio())
                .isEqualTo(BloqueioResultados.VISITA_NAO_REALIZADA);
        assertThatThrownBy(() -> service.registrar(RETORNO_30_P1_AGENDADO, AFERIR_PRESSAO,
                new ComandoResultado("120/80", null, null), pesquisador))
                .isInstanceOf(ConflitoRegraException.class)
                .extracting(e -> ((ConflitoRegraException) e).getCodigo()).isEqualTo("VISITA_NAO_REALIZADA");
    }
}
