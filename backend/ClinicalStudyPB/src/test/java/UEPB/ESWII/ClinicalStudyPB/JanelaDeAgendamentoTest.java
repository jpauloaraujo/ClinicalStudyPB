package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaPlanejada;
import UEPB.ESWII.ClinicalStudyPB.domain.regra.JanelaDeAgendamento;

/** T78 (RF12, RQ-T78-02): limites da janela de agendamento, sem Spring. */
class JanelaDeAgendamentoTest {

    private static final LocalDate INCLUSAO = LocalDate.of(2026, 11, 10);

    private static VisitaPlanejada visita(int dia, int tolerancia) {
        VisitaPlanejada visita = new VisitaPlanejada();
        visita.setDiaPrevisto(dia);
        visita.setTolerancia(tolerancia);
        return visita;
    }

    @Test
    @DisplayName("A data base é a inclusão mais o dia previsto, e a janela é a base mais ou menos a tolerância")
    void calculaBaseEJanela() {
        JanelaDeAgendamento janela = JanelaDeAgendamento.de(visita(30, 5), INCLUSAO);

        assertThat(janela.dataBase()).isEqualTo(LocalDate.of(2026, 12, 10));
        assertThat(janela.dataInicio()).isEqualTo(LocalDate.of(2026, 12, 5));
        assertThat(janela.dataFim()).isEqualTo(LocalDate.of(2026, 12, 15));
    }

    @Test
    @DisplayName("Tolerância 0: a janela tem um dia só, o da data base")
    void toleranciaZeroTemUmDia() {
        JanelaDeAgendamento janela = JanelaDeAgendamento.de(visita(7, 0), INCLUSAO);

        assertThat(janela.dataInicio()).isEqualTo(janela.dataFim()).isEqualTo(janela.dataBase());
        assertThat(janela.contem(janela.dataBase())).isTrue();
        assertThat(janela.contem(janela.dataBase().minusDays(1))).isFalse();
        assertThat(janela.contem(janela.dataBase().plusDays(1))).isFalse();
    }

    @Test
    @DisplayName("Dia previsto 0: a data base é o próprio dia da inclusão")
    void diaZeroEhODiaDaInclusao() {
        assertThat(JanelaDeAgendamento.de(visita(0, 0), INCLUSAO).dataBase()).isEqualTo(INCLUSAO);
    }

    @ParameterizedTest(name = "{0} dia(s) a partir do início da janela: dentro = {1}")
    @CsvSource({ "-1,false", "0,true", "1,true", "5,true", "10,true", "11,false" })
    @DisplayName("Os dois limites da janela são inclusivos; um dia antes ou depois está fora")
    void limitesInclusivos(int deslocamento, boolean dentro) {
        JanelaDeAgendamento janela = JanelaDeAgendamento.de(visita(30, 5), INCLUSAO);

        assertThat(janela.contem(janela.dataInicio().plusDays(deslocamento))).isEqualTo(dentro);
    }
}
