package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Totais de comparecimento do participante (e do estudo, quando o app filtra por {@code estudoId}), calculados no
 * servidor e <b>independentes da página</b> pedida (T116).
 */
@Schema(description = "Totais de comparecimento do participante, independentes da página")
public record ResumoComparecimentoResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long realizadas,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long ausentes,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long canceladas) {

    /** Sem visitas finalizadas: participante sem vínculo ou estudo que não é dele. */
    public static ResumoComparecimentoResponse zerado() {
        return new ResumoComparecimentoResponse(0, 0, 0);
    }
}
