package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Pedido de inclusão de um candidato como participante do estudo")
public record InclusaoRequest(
        @Schema(description = "UUID do candidato a incluir")
        @NotNull(message = "O candidato é obrigatório")
        UUID candidatoId) {
}
