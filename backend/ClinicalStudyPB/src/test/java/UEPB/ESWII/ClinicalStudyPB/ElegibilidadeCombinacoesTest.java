package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import UEPB.ESWII.ClinicalStudyPB.domain.model.Criterio;
import UEPB.ESWII.ClinicalStudyPB.domain.model.CriterioExclusao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.CriterioInclusao;
import UEPB.ESWII.ClinicalStudyPB.domain.regra.RegraElegibilidade;

/**
 * T56 (RF09): todas as combinações da regra de elegibilidade com uma inclusão determinante (peso 2), uma
 * inclusão desejável (peso 3) e uma exclusão. A regra é função pura, então não há banco nem Spring.
 */
class ElegibilidadeCombinacoesTest {

    private final CriterioInclusao obrigatorio = inclusao(2f, true);
    private final CriterioInclusao desejavel = inclusao(3f, false);
    private final CriterioExclusao impeditivo = exclusao();

    private static CriterioInclusao inclusao(float peso, boolean determinante) {
        CriterioInclusao criterio = new CriterioInclusao();
        criterio.setId(UUID.randomUUID());
        criterio.setTitulo("Inclusão " + peso);
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

    @ParameterizedTest(name = "obrigatório={0}, desejável={1}, exclusão={2} → elegível={3}, pontos={4}, falhas={5}")
    @DisplayName("As oito combinações de respostas dão o resultado, a pontuação e o número de falhas esperados")
    @CsvSource({
            "true,  true,  false, true,  5, 0",
            "true,  false, false, true,  2, 0",
            "false, true,  false, false, 3, 1",
            "false, false, false, false, 0, 1",
            "true,  true,  true,  false, 5, 1",
            "true,  false, true,  false, 2, 1",
            "false, true,  true,  false, 3, 2",
            "false, false, true,  false, 0, 2",
    })
    void combinacoes(boolean aplicaObrigatorio, boolean aplicaDesejavel, boolean aplicaExclusao, boolean elegivel,
            float pontos, int falhas) {
        var resultado = RegraElegibilidade.avaliar(List.<Criterio>of(obrigatorio, desejavel, impeditivo),
                Map.of(obrigatorio.getId(), aplicaObrigatorio, desejavel.getId(), aplicaDesejavel,
                        impeditivo.getId(), aplicaExclusao));

        assertThat(resultado.elegivel()).isEqualTo(elegivel);
        assertThat(resultado.pontuacao()).isEqualTo(pontos);
        assertThat(resultado.falhas()).hasSize(falhas);
    }

    @Test
    @DisplayName("Só critérios de exclusão: elegível enquanto nenhum se aplica, pontuação sempre zero")
    void apenasExclusoes() {
        CriterioExclusao outra = exclusao();

        var nenhuma = RegraElegibilidade.avaliar(List.<Criterio>of(impeditivo, outra),
                Map.of(impeditivo.getId(), false, outra.getId(), false));
        var uma = RegraElegibilidade.avaliar(List.<Criterio>of(impeditivo, outra),
                Map.of(impeditivo.getId(), false, outra.getId(), true));

        assertThat(nenhuma.elegivel()).isTrue();
        assertThat(uma.elegivel()).isFalse();
        assertThat(uma.falhas()).containsExactly(outra);
        assertThat(nenhuma.pontuacao()).isZero();
        assertThat(uma.pontuacao()).isZero();
    }

    @Test
    @DisplayName("Só inclusões não determinantes: sempre elegível, e a pontuação soma só as que se aplicam")
    void apenasInclusoesDesejaveis() {
        CriterioInclusao outra = inclusao(1.5f, false);

        var resultado = RegraElegibilidade.avaliar(List.<Criterio>of(desejavel, outra),
                Map.of(desejavel.getId(), false, outra.getId(), true));

        assertThat(resultado.elegivel()).isTrue();
        assertThat(resultado.pontuacao()).isEqualTo(1.5f);
    }

    @Test
    @DisplayName("Critério sem resposta no mapa conta como \"não se aplica\"")
    void semRespostaContaComoNaoAplica() {
        var resultado = RegraElegibilidade.avaliar(List.<Criterio>of(obrigatorio, impeditivo), Map.of());

        assertThat(resultado.elegivel()).isFalse();
        assertThat(resultado.falhas()).containsExactly(obrigatorio);
    }
}
