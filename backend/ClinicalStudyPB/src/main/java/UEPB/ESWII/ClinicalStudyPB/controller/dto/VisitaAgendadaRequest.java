package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Pedido para agendar uma visita prevista")
public record VisitaAgendadaRequest(
        @NotNull(message = "A visita prevista é obrigatória")
        @Schema(description = "Identificador da visita prevista do estudo", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID visitaPlanejadaId,
        @NotNull(message = "A data e hora são obrigatórias")
        @Schema(description = "Data e hora locais, sem fuso", example = "2026-11-19T09:30",
                format = "date-time", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime dataHora) {
}
