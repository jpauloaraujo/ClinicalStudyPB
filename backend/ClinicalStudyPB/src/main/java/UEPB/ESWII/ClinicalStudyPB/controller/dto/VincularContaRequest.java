package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Conta de participante a vincular ao participante do estudo (RF17)")
public record VincularContaRequest(
        @Schema(description = "Identificador da conta de perfil PARTICIPANTE, ativa e ainda sem vínculo")
        @NotNull(message = "Informe a conta do participante")
        UUID usuarioId) {
}
