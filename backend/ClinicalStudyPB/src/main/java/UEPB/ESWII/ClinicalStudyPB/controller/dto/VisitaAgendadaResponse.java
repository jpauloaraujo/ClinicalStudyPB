package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaPlanejada;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Visita agendada de um participante, com datas no fuso do centro")
public record VisitaAgendadaResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID participacaoId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID estudoId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID visitaPlanejadaId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String visitaNome,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String participanteCodigo,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String participanteNome,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) OffsetDateTime dataHora,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) SituacaoVisita situacao,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED) OffsetDateTime dataHoraRealizada,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED) String motivo,
        @Schema(nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED) String observacao,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate janelaInicio,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate janelaFim,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String criadoPor,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) OffsetDateTime criadoEm,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String alteradoPor,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) OffsetDateTime alteradoEm,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Long versao) {

    public static VisitaAgendadaResponse de(VisitaAgendada agendada, Clock clock) {
        Participacao participacao = agendada.getParticipacao();
        VisitaPlanejada visita = agendada.getVisitaPlanejada();
        LocalDate dataInclusao = participacao.getDataInclusao();
        return new VisitaAgendadaResponse(
                agendada.getId(),
                participacao.getId(),
                participacao.getEstudo().getId(),
                visita.getId(),
                visita.getNome(),
                participacao.getCandidato().getCodigo(),
                participacao.getCandidato().getNomeFicticio(),
                comFuso(agendada.getDataHoraAgendada(), clock),
                agendada.getSituacao(),
                comFuso(agendada.getDataHoraRealizada(), clock),
                agendada.getMotivo(),
                agendada.getObservacao(),
                visita.inicioDaJanela(dataInclusao),
                visita.fimDaJanela(dataInclusao),
                agendada.getCriadoPor().getNome(),
                comFuso(agendada.getCriadoEm(), clock),
                agendada.getAlteradoPor().getNome(),
                comFuso(agendada.getAlteradoEm(), clock),
                agendada.getVersao());
    }

    private static OffsetDateTime comFuso(LocalDateTime dataHora, Clock clock) {
        return dataHora == null ? null : dataHora.atZone(clock.getZone()).toOffsetDateTime();
    }
}
