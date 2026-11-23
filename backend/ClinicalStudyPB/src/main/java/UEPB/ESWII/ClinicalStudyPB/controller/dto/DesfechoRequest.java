package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.time.LocalDateTime;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.TipoDesfecho;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Desfecho de uma visita agendada (RF13). As regras de motivo e de data ficam na {@code MaquinaEstadosVisita}
 * (T80); aqui só o que o Bean Validation resolve: o tipo é obrigatório e os textos têm até 500 caracteres. Campos
 * que não pertencem ao tipo escolhido são ignorados, sem erro.
 */
@Schema(description = "Desfecho da visita; os campos que não pertencem ao tipo são ignorados")
public record DesfechoRequest(
        @Schema(description = "REALIZADA, AUSENTE ou CANCELADA", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "O tipo do desfecho é obrigatório")
        TipoDesfecho tipo,

        @Schema(description = "Só para REALIZADA: data e hora reais, locais e sem fuso; não pode ser futura; "
                + "omitida assume agora", example = "2026-11-19T09:40")
        LocalDateTime dataHoraRealizada,

        @Schema(description = "Só para CANCELADA, onde é obrigatório (5 a 500 caracteres)", maxLength = 500)
        @Size(max = 500, message = "O motivo deve ter no máximo 500 caracteres")
        String motivo,

        @Schema(description = "Opcional nos três tipos", maxLength = 500)
        @Size(max = 500, message = "A observação deve ter no máximo 500 caracteres")
        String observacao) {
}
