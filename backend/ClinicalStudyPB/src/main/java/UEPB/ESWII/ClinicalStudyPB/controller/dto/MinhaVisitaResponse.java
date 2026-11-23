package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Visita do participante logado, para o app (RF18, T116). Traz só os <b>nomes</b> das atividades previstas e
 * <b>nenhum resultado</b> das avaliações nem dado de candidato (decisão de escopo e de privacidade). Monte-a dentro
 * da transação: lê a visita prevista e o estudo, que a consulta já carrega.
 */
@Schema(description = "Visita do participante logado; datas com o deslocamento do fuso do centro, sem resultados")
public record MinhaVisitaResponse(
        @Schema(description = "Identificador da visita agendada", requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID participacaoId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID estudoId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String estudoTitulo,
        @Schema(description = "Nome da visita prevista", example = "Retorno 30 dias",
                requiredMode = Schema.RequiredMode.REQUIRED) String visitaNome,
        @Schema(description = "Data e hora agendadas", example = "2026-11-24T09:30:00-03:00",
                requiredMode = Schema.RequiredMode.REQUIRED) OffsetDateTime dataHora,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) SituacaoVisita situacao,
        @Schema(description = "Preenchida nas visitas REALIZADA", nullable = true) OffsetDateTime dataHoraRealizada,
        @Schema(description = "Preenchido nas visitas CANCELADA (inclusive \"retirada do estudo\")", nullable = true)
        String motivo,
        @Schema(description = "Nomes das atividades previstas da visita, em ordem alfabética",
                requiredMode = Schema.RequiredMode.REQUIRED) List<String> atividades) {

    public static MinhaVisitaResponse de(VisitaAgendada visita, List<String> atividades, Clock clock) {
        Participacao participacao = visita.getParticipacao();
        return new MinhaVisitaResponse(
                visita.getId(),
                participacao.getId(),
                participacao.getEstudo().getId(),
                participacao.getEstudo().getTitulo(),
                visita.getVisitaPlanejada().getNome(),
                comFuso(visita.getDataHoraAgendada(), clock),
                visita.getSituacao(),
                comFuso(visita.getDataHoraRealizada(), clock),
                visita.getSituacao() == SituacaoVisita.CANCELADA ? visita.getMotivo() : null,
                atividades);
    }

    private static OffsetDateTime comFuso(LocalDateTime dataHora, Clock clock) {
        return dataHora == null ? null : dataHora.atZone(clock.getZone()).toOffsetDateTime();
    }
}
