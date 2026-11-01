package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.NivelAtendimento;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.TipoCriterio;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Criterio;
import UEPB.ESWII.ClinicalStudyPB.domain.model.CriterioExclusao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.CriterioInclusao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaPlanejada;
import UEPB.ESWII.ClinicalStudyPB.domain.regra.RegraElegibilidade;

/** Regras de domínio do recrutamento, isoladas de banco e Spring. */
class DominioRecrutamentoTest {

    private static CriterioInclusao inclusao(float peso, boolean determinante) {
        CriterioInclusao criterio = new CriterioInclusao();
        criterio.setId(UUID.randomUUID());
        criterio.setTitulo("Inclusão");
        criterio.setPeso(peso);
        criterio.setDeterminante(determinante);
        return criterio;
    }

    private static CriterioExclusao exclusao() {
        CriterioExclusao criterio = new CriterioExclusao();
        criterio.setId(UUID.randomUUID());
        criterio.setTitulo("Exclusão");
        return criterio;
    }

    @Test
    @DisplayName("Critério de inclusão devolve 0, parcial ou o peso, conforme o validador")
    void inclusaoDevolveNotaEntreZeroEPeso() {
        CriterioInclusao criterio = inclusao(4f, false);

        assertThat(criterio.validarCriterio(() -> 0f)).isZero();
        assertThat(criterio.validarCriterio(() -> 2.5f)).isEqualTo(2.5f);
        assertThat(criterio.validarCriterio(() -> 4f)).isEqualTo(4f);
        assertThat(criterio.getTipo()).isEqualTo(TipoCriterio.INCLUSAO);
    }

    @Test
    @DisplayName("A nota é limitada ao intervalo de 0 até o peso")
    void notaFicaLimitadaAoPeso() {
        CriterioInclusao criterio = inclusao(3f, true);

        assertThat(criterio.validarCriterio(() -> 10f)).isEqualTo(3f);
        assertThat(criterio.validarCriterio(() -> -1f)).isZero();
        assertThat(criterio.validarCriterio(() -> null)).isZero();
    }

    @Test
    @DisplayName("Classifica a nota como não atendido, parcial ou atendido")
    void classificaONivelDeAtendimento() {
        CriterioInclusao criterio = inclusao(4f, false);

        assertThat(criterio.classificar(0f)).isEqualTo(NivelAtendimento.NAO_ATENDIDO);
        assertThat(criterio.classificar(1f)).isEqualTo(NivelAtendimento.PARCIALMENTE_ATENDIDO);
        assertThat(criterio.classificar(4f)).isEqualTo(NivelAtendimento.ATENDIDO);
    }

    @Test
    @DisplayName("Critério de exclusão devolve só se foi satisfeito")
    void exclusaoDevolveBoolean() {
        CriterioExclusao criterio = exclusao();

        assertThat(criterio.validarCriterio(() -> true)).isTrue();
        assertThat(criterio.validarCriterio(() -> false)).isFalse();
        assertThat(criterio.getTipo()).isEqualTo(TipoCriterio.EXCLUSAO);
    }

    @Test
    @DisplayName("Elegível: inclusão determinante atendida e nenhuma exclusão satisfeita")
    void elegivelQuandoTudoDeterminanteEAtendido() {
        CriterioInclusao idade = inclusao(1f, true);
        CriterioExclusao fumante = exclusao();

        var resultado = RegraElegibilidade.avaliar(List.of(idade, fumante),
                Map.of(idade.getId(), true, fumante.getId(), false));

        assertThat(resultado.elegivel()).isTrue();
        assertThat(resultado.falhas()).isEmpty();
        assertThat(resultado.pontuacao()).isEqualTo(1f);
    }

    @Test
    @DisplayName("Inclusão determinante não atendida reprova o candidato")
    void inclusaoDeterminanteNaoAtendidaReprova() {
        CriterioInclusao idade = inclusao(1f, true);

        var resultado = RegraElegibilidade.avaliar(List.of(idade), Map.of(idade.getId(), false));

        assertThat(resultado.elegivel()).isFalse();
        assertThat(resultado.falhas()).containsExactly(idade);
        assertThat(resultado.pontuacao()).isZero();
    }

    @Test
    @DisplayName("Inclusão não determinante não atendida não reprova, só deixa de pontuar")
    void inclusaoNaoDeterminanteNaoReprova() {
        CriterioInclusao obrigatorio = inclusao(2f, true);
        CriterioInclusao desejavel = inclusao(3f, false);

        var resultado = RegraElegibilidade.avaliar(List.of(obrigatorio, desejavel),
                Map.of(obrigatorio.getId(), true, desejavel.getId(), false));

        assertThat(resultado.elegivel()).isTrue();
        assertThat(resultado.pontuacao()).isEqualTo(2f);
    }

    @Test
    @DisplayName("Exclusão satisfeita reprova; várias falhas aparecem juntas")
    void exclusaoSatisfeitaReprovaEAcumulaFalhas() {
        CriterioInclusao idade = inclusao(1f, true);
        CriterioExclusao fumante = exclusao();
        CriterioExclusao gestante = exclusao();

        var resultado = RegraElegibilidade.avaliar(List.<Criterio>of(idade, fumante, gestante),
                Map.of(idade.getId(), false, fumante.getId(), true, gestante.getId(), false));

        assertThat(resultado.elegivel()).isFalse();
        assertThat(resultado.falhas()).containsExactlyInAnyOrder(idade, fumante);
    }

    @Test
    @DisplayName("Estudo sem critérios deixa o candidato elegível")
    void semCriteriosEElegivel() {
        var resultado = RegraElegibilidade.avaliar(List.of(), Map.of());

        assertThat(resultado.elegivel()).isTrue();
        assertThat(resultado.pontuacao()).isZero();
    }

    @Test
    @DisplayName("Visita planejada calcula a data prevista e a janela a partir da inclusão (dia 0)")
    void visitaCalculaDatasAPartirDaInclusao() {
        VisitaPlanejada visita = new VisitaPlanejada();
        visita.setDiaPrevisto(30);
        visita.setTolerancia(5);
        LocalDate inclusao = LocalDate.of(2026, 11, 1);

        assertThat(visita.dataPrevista(inclusao)).isEqualTo(LocalDate.of(2026, 12, 1));
        assertThat(visita.inicioDaJanela(inclusao)).isEqualTo(LocalDate.of(2026, 11, 26));
        assertThat(visita.fimDaJanela(inclusao)).isEqualTo(LocalDate.of(2026, 12, 6));
        assertThat(visita.estaNaJanela(inclusao, LocalDate.of(2026, 11, 25))).isFalse();
        assertThat(visita.estaNaJanela(inclusao, LocalDate.of(2026, 11, 26))).isTrue();
        assertThat(visita.estaNaJanela(inclusao, LocalDate.of(2026, 12, 6))).isTrue();
        assertThat(visita.estaNaJanela(inclusao, LocalDate.of(2026, 12, 7))).isFalse();
    }
}
