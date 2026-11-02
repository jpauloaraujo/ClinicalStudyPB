package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Dados de uma visita planejada do protocolo (RF06). */
@Schema(description = "Payload de cadastro ou edição de uma visita planejada")
public record VisitaRequest(
        @Schema(description = "Nome da visita", example = "Retorno Mensal", maxLength = 100)
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @Schema(description = "Dia alvo da visita, contado a partir da inclusão (dia 0)", example = "30", minimum = "0")
        @NotNull(message = "O dia previsto é obrigatório")
        @Min(value = 0, message = "O dia previsto deve ser maior ou igual a zero")
        Integer diaPrevisto,

        @Schema(description = "Margem em dias, antes e depois do dia previsto (0 a 30)", example = "5", minimum = "0", maximum = "30")
        @NotNull(message = "A tolerância é obrigatória")
        @Min(value = 0, message = "A tolerância deve ser de no mínimo 0 dia")
        @Max(value = 30, message = "A tolerância deve ser de no máximo 30 dias")
        Integer tolerancia) {
}
