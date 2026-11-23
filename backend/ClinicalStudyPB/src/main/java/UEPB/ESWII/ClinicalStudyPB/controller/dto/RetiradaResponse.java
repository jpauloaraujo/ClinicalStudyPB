package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SolicitanteRetirada;
import UEPB.ESWII.ClinicalStudyPB.domain.model.RetiradaParticipacao;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Registro formal da retirada de um participante do estudo (RF16, T103), com o total de visitas futuras
 * canceladas pelo motivo {@code "retirada do estudo"}. {@code registradoEm} sai com o fuso de
 * {@code app.fuso-horario}.
 */
@Schema(description = "Registro formal da retirada de um participante do estudo.")
public record RetiradaResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID participacaoId,
        @Schema(example = "Mudança de cidade", requiredMode = Schema.RequiredMode.REQUIRED) String motivo,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) SolicitanteRetirada solicitante,
        @Schema(example = "2026-11-18", requiredMode = Schema.RequiredMode.REQUIRED) LocalDate dataRetirada,
        @Schema(example = "Coordenador Demo (Ficticio)", requiredMode = Schema.RequiredMode.REQUIRED) String registradoPor,
        @Schema(example = "2026-11-18T14:30:00-03:00", requiredMode = Schema.RequiredMode.REQUIRED) OffsetDateTime registradoEm,
        @Schema(description = "Quantidade de visitas futuras AGENDADA canceladas com o motivo \"retirada do estudo\".",
                example = "2", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED) int visitasCanceladas) {

    public static RetiradaResponse de(RetiradaParticipacao retirada, int visitasCanceladas, ZoneId fuso) {
        return new RetiradaResponse(
                retirada.getId(),
                retirada.getParticipacao().getId(),
                retirada.getMotivo(),
                retirada.getSolicitante(),
                retirada.getDataRetirada(),
                retirada.getRegistradoPor().getNome(),
                comFuso(retirada.getRegistradoEm(), fuso),
                visitasCanceladas);
    }

    private static OffsetDateTime comFuso(LocalDateTime dataHora, ZoneId fuso) {
        return dataHora == null ? null : dataHora.atZone(fuso).toOffsetDateTime();
    }
}
