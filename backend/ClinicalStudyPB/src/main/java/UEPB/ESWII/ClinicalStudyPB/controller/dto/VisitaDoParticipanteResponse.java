package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.time.LocalDate;
import java.util.UUID;

import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaPlanejada;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Visita prevista do estudo, com sua janela e eventual agendamento")
public record VisitaDoParticipanteResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID visitaPlanejadaId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String nome,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int ordem,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int diaPrevisto,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int tolerancia,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate dataSugerida,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate janelaInicio,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate janelaFim,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        VisitaAgendadaResponse agendamento) {

    public static VisitaDoParticipanteResponse de(VisitaPlanejada visita, Participacao participacao,
            VisitaAgendadaResponse agendamento) {
        LocalDate dataInclusao = participacao.getDataInclusao();
        return new VisitaDoParticipanteResponse(visita.getId(), visita.getNome(), visita.getOrdem(),
                visita.getDiaPrevisto(), visita.getTolerancia(), visita.dataPrevista(dataInclusao),
                visita.inicioDaJanela(dataInclusao), visita.fimDaJanela(dataInclusao), agendamento);
    }
}
