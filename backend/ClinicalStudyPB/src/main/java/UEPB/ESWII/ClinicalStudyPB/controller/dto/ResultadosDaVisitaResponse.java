package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.BloqueioResultados;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;
import UEPB.ESWII.ClinicalStudyPB.service.ResultadoAtividadeService.ResultadosDaVisita;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Atividades de avaliação clínica de uma visita agendada, com o resultado fictício de cada uma (nulo se pendente).
 * {@code bloqueio} é a decisão do servidor pelo estado da visita e do estudo; o modo somente leitura do coordenador é
 * decidido pela tela.
 */
@Schema(description = "Atividades de avaliação clínica de uma visita agendada, com o resultado fictício de cada uma (nulo se pendente).")
public record ResultadosDaVisitaResponse(
        UUID visitaAgendadaId,
        SituacaoVisita situacaoVisita,
        @Schema(description = "Nulo quando a visita aceita registro e edição; senão, o motivo do modo somente leitura.",
                nullable = true) BloqueioResultados bloqueio,
        @Schema(description = "Quantidade de atividades AVALIACAO_CLINICA da visita prevista.", example = "1") int total,
        @Schema(description = "Quantas dessas atividades já têm resultado.", example = "1") int registrados,
        @Schema(description = "total - registrados.", example = "0") int pendentes,
        List<AtividadeComResultadoResponse> atividades) {

    /** Monta a resposta; precisa de sessão aberta (lê relações LAZY da visita e dos resultados). */
    public static ResultadosDaVisitaResponse de(ResultadosDaVisita resultados, ZoneId fuso) {
        return new ResultadosDaVisitaResponse(
                resultados.visitaAgendada().getId(),
                resultados.visitaAgendada().getSituacao(),
                resultados.bloqueio(),
                resultados.total(),
                resultados.registrados(),
                resultados.pendentes(),
                resultados.atividades().stream().map(a -> AtividadeComResultadoResponse.de(a, fuso)).toList());
    }
}
