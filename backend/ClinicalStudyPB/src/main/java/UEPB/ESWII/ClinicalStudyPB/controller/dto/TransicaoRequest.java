package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/** Pedido de mudança do estado de um estudo (RF07). */
@Schema(description = "Estado de destino; precisa ser o estado seguinte ao atual (sem pular nem voltar)")
public record TransicaoRequest(
        @Schema(description = "Estado para o qual o estudo deve ir", example = "RECRUTAMENTO")
        @NotNull(message = "O estado de destino é obrigatório")
        StatusEstudo destino) {
}
