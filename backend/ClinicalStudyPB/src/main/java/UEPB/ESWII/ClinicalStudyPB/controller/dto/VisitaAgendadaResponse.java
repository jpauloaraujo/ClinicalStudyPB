package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaPlanejada;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Visita agendada de um participante. O banco guarda data e hora locais do centro; a resposta traz o deslocamento
 * do fuso ({@code 2026-11-19T09:30:00-03:00}) para o app exibir no fuso do aparelho. Monte-a sempre dentro da
 * transação, pois lê relações carregadas sob demanda.
 */
@Schema(description = "Visita agendada; datas e horas com o deslocamento do fuso do centro")
public record VisitaAgendadaResponse(
        UUID id,
        UUID participacaoId,
        UUID estudoId,
        UUID visitaPlanejadaId,
        String visitaNome,
        String participanteCodigo,
        String participanteNome,
        @Schema(description = "Data e hora agendadas", example = "2026-11-19T09:30:00-03:00") OffsetDateTime dataHora,
        SituacaoVisita situacao,
        @Schema(description = "Preenchida só nas visitas REALIZADA", nullable = true) OffsetDateTime dataHoraRealizada,
        @Schema(description = "Motivo do cancelamento", nullable = true) String motivo,
        @Schema(nullable = true) String observacao,
        @Schema(description = "Primeira data aceita pela visita prevista", example = "2026-11-14") LocalDate janelaInicio,
        @Schema(description = "Última data aceita pela visita prevista", example = "2026-11-24") LocalDate janelaFim,
        String criadoPor,
        OffsetDateTime criadoEm,
        String alteradoPor,
        OffsetDateTime alteradoEm,
        @Schema(description = "Controle de versão otimista") Long versao) {

    public static VisitaAgendadaResponse de(VisitaAgendada visita, ZoneId fuso) {
        Participacao participacao = visita.getParticipacao();
        VisitaPlanejada prevista = visita.getVisitaPlanejada();
        return new VisitaAgendadaResponse(
                visita.getId(),
                participacao.getId(),
                participacao.getEstudo().getId(),
                prevista.getId(),
                prevista.getNome(),
                participacao.getCandidato().getCodigo(),
                participacao.getCandidato().getNomeFicticio(),
                comFuso(visita.getDataHoraAgendada(), fuso),
                visita.getSituacao(),
                comFuso(visita.getDataHoraRealizada(), fuso),
                visita.getMotivo(),
                visita.getObservacao(),
                prevista.inicioDaJanela(participacao.getDataInclusao()),
                prevista.fimDaJanela(participacao.getDataInclusao()),
                visita.getCriadoPor().getNome(),
                comFuso(visita.getCriadoEm(), fuso),
                visita.getAlteradoPor().getNome(),
                comFuso(visita.getAlteradoEm(), fuso),
                visita.getVersao());
    }

    private static OffsetDateTime comFuso(LocalDateTime local, ZoneId fuso) {
        return local == null ? null : local.atZone(fuso).toOffsetDateTime();
    }
}
