package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.HistoricoStatusEstudo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Registro de uma mudança de estado do estudo")
public record HistoricoStatusResponse(
        UUID id,
        UUID estudoId,
        StatusEstudo estadoAnterior,
        StatusEstudo estadoNovo,
        @Schema(description = "Nome de quem avançou o estudo") String usuario,
        LocalDateTime dataHora) {

    public static HistoricoStatusResponse de(HistoricoStatusEstudo registro) {
        return new HistoricoStatusResponse(registro.getId(), registro.getEstudo().getId(),
                registro.getEstadoAnterior(), registro.getEstadoNovo(), registro.getUsuario().getNome(),
                registro.getDataHora());
    }
}
