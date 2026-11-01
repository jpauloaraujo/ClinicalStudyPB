package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Avaliação de elegibilidade: uma resposta para cada critério do estudo (RF09). */
@Schema(description = "Respostas da avaliação; deve haver exatamente uma por critério do estudo")
public record AvaliacaoRequest(
        @NotNull(message = "As respostas são obrigatórias")
        @Valid
        List<Resposta> respostas) {

    @Schema(description = "Resposta a um critério")
    public record Resposta(
            @NotNull(message = "O critério é obrigatório")
            UUID criterioId,

            @Schema(description = "Se a condição do critério se aplica ao candidato. Em critério de exclusão, \"se aplica\" reprova")
            @NotNull(message = "Informe se o critério se aplica")
            Boolean aplica,

            @Schema(description = "Justificativa opcional do avaliador", maxLength = 500)
            @Size(max = 500, message = "A observação deve ter no máximo 500 caracteres")
            String observacao) {
    }
}
