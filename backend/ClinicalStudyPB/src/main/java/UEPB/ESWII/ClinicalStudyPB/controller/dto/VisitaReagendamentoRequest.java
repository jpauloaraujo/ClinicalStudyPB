package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Pedido para reagendar uma visita")
public record VisitaReagendamentoRequest(
        @NotNull(message = "A data e hora são obrigatórias")
        @Schema(description = "Nova data e hora locais, sem fuso", example = "2026-11-20T10:00",
                format = "date-time", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime dataHora) {
}
