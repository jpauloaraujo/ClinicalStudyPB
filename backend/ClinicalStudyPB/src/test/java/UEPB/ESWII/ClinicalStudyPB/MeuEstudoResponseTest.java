package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.MeuEstudoResponse;

/** T109 (RQ-T109-10): o resumo é o objetivo cortado em 160 caracteres, com reticências quando cortado. */
class MeuEstudoResponseTest {

    @Test
    @DisplayName("objetivo com até 160 caracteres vai inteiro, sem reticências")
    void objetivoCurtoVaiInteiro() {
        String exato = "a".repeat(160);

        assertThat(MeuEstudoResponse.resumir("Objetivo curto.")).isEqualTo("Objetivo curto.");
        assertThat(MeuEstudoResponse.resumir(exato)).isEqualTo(exato);
    }

    @Test
    @DisplayName("objetivo com 161 caracteres é cortado em 160, terminando com reticências")
    void objetivoLongoEhCortado() {
        String resumo = MeuEstudoResponse.resumir("a".repeat(161));

        assertThat(resumo).hasSize(160).endsWith("…");
    }

    @Test
    @DisplayName("o corte não deixa espaço antes das reticências")
    void semEspacoAntesDasReticencias() {
        String objetivo = "a".repeat(158) + " " + "b".repeat(50);

        assertThat(MeuEstudoResponse.resumir(objetivo)).isEqualTo("a".repeat(158) + "…");
    }

    @Test
    @DisplayName("objetivo nulo vira texto vazio")
    void objetivoNulo() {
        assertThat(MeuEstudoResponse.resumir(null)).isEmpty();
    }
}
