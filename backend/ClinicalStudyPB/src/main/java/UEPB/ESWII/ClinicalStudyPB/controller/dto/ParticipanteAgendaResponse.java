package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.time.LocalDate;
import java.util.UUID;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoParticipacao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Participante do estudo com o andamento do agendamento")
public record ParticipanteAgendaResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID participacaoId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID candidatoId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String codigo,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String nomeFicticio,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate dataInclusao,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int visitasPrevistas,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int visitasAgendadas,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) SituacaoParticipacao situacaoParticipacao) {

    public static ParticipanteAgendaResponse de(Participacao participacao, int visitasPrevistas,
            long visitasAgendadas) {
        var candidato = participacao.getCandidato();
        return new ParticipanteAgendaResponse(participacao.getId(), candidato.getId(), candidato.getCodigo(),
                candidato.getNomeFicticio(), participacao.getDataInclusao(), visitasPrevistas,
                Math.toIntExact(visitasAgendadas), participacao.getSituacao());
    }
}
