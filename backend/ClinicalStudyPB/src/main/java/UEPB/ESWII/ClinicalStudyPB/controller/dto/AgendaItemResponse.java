package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Item da agenda do estudo")
public record AgendaItemResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID visitaAgendadaId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID participacaoId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String participanteCodigo,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String participanteNome,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID visitaPlanejadaId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String visitaNome,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) OffsetDateTime dataHora,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) SituacaoVisita situacao) {

    public static AgendaItemResponse de(VisitaAgendada agendada, Clock clock) {
        var participacao = agendada.getParticipacao();
        var visita = agendada.getVisitaPlanejada();
        return new AgendaItemResponse(agendada.getId(), participacao.getId(),
                participacao.getCandidato().getCodigo(), participacao.getCandidato().getNomeFicticio(),
                visita.getId(), visita.getNome(),
                agendada.getDataHoraAgendada().atZone(clock.getZone()).toOffsetDateTime(),
                agendada.getSituacao());
    }
}
