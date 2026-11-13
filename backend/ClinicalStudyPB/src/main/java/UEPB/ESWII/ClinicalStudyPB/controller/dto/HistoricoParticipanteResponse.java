package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoNoHistorico;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Histórico de um participante (RF15, somente leitura): quem é, o resumo por situação e <b>uma linha para cada
 * visita prevista</b> do estudo, com a situação, as datas e os resultados. Datas e horas saem como
 * {@link OffsetDateTime} (fuso de {@code app.fuso-horario}); datas puras como {@link LocalDate}.
 *
 * <p>A T96 monta este objeto; a T104 acrescenta a situação da participação e a marca de retirada em
 * {@link ParticipanteDoHistorico}.</p>
 */
@Schema(description = "Histórico de um participante: dados, resumo e todas as visitas previstas do estudo")
public record HistoricoParticipanteResponse(
        ParticipanteDoHistorico participante,
        ResumoDoHistorico resumo,
        @Schema(description = "Uma linha para cada visita prevista do estudo, ordenadas por ordem e dia previsto, "
                + "inclusive as ainda não agendadas") List<VisitaDoHistorico> visitas) {

    @Schema(description = "Participante a que o histórico se refere")
    public record ParticipanteDoHistorico(
            UUID participacaoId,
            UUID candidatoId,
            @Schema(example = "CAND-001") String codigo,
            @Schema(example = "Pessoa Teta (Ficticia)") String nomeFicticio,
            @Schema(description = "Dia 0 do cronograma do participante", example = "2026-10-20") LocalDate dataInclusao) {
    }

    @Schema(description = "Contagem de visitas por situação. pendentes = agendadas + naoAgendadas")
    public record ResumoDoHistorico(
            @Schema(description = "Total de visitas previstas do estudo") int previstas,
            int realizadas,
            int ausentes,
            int canceladas,
            @Schema(description = "Visitas agendadas que ainda não tiveram desfecho") int agendadas,
            @Schema(description = "Visitas previstas sem agendamento") int naoAgendadas,
            @Schema(description = "Visitas sem desfecho: agendadas mais não agendadas") int pendentes) {

        /** Conta as visitas por situação; garante {@code pendentes = agendadas + naoAgendadas}. */
        public static ResumoDoHistorico de(List<VisitaDoHistorico> visitas) {
            int realizadas = contar(visitas, SituacaoNoHistorico.REALIZADA);
            int ausentes = contar(visitas, SituacaoNoHistorico.AUSENTE);
            int canceladas = contar(visitas, SituacaoNoHistorico.CANCELADA);
            int agendadas = contar(visitas, SituacaoNoHistorico.AGENDADA);
            int naoAgendadas = contar(visitas, SituacaoNoHistorico.NAO_AGENDADA);
            return new ResumoDoHistorico(visitas.size(), realizadas, ausentes, canceladas, agendadas, naoAgendadas,
                    agendadas + naoAgendadas);
        }

        private static int contar(List<VisitaDoHistorico> visitas, SituacaoNoHistorico situacao) {
            return (int) visitas.stream().filter(v -> v.situacao() == situacao).count();
        }
    }

    @Schema(description = "Uma visita prevista do estudo e o que aconteceu com ela para este participante. "
            + "Sem agendamento, situacao é NAO_AGENDADA e os campos do agendamento vêm nulos")
    public record VisitaDoHistorico(
            UUID visitaPlanejadaId,
            @Schema(example = "Triagem") String nome,
            int ordem,
            @Schema(description = "Dias a partir da inclusão") int diaPrevisto,
            @Schema(description = "Dias de tolerância para mais e para menos") int tolerancia,
            @Schema(description = "Data prevista: inclusão mais dia previsto", example = "2026-10-20")
            LocalDate dataSugerida,
            SituacaoNoHistorico situacao,
            @Schema(description = "Nulo quando a visita não foi agendada") UUID visitaAgendadaId,
            OffsetDateTime dataHoraAgendada,
            @Schema(description = "Preenchida só nas visitas REALIZADA") OffsetDateTime dataHoraRealizada,
            @Schema(description = "Motivo do cancelamento") String motivo,
            String observacao,
            @Schema(description = "Resultado de cada atividade da visita (vazio se não houver)")
            List<ResultadoDoHistorico> resultados) {
    }

    @Schema(description = "Resultado fictício registrado para uma atividade da visita")
    public record ResultadoDoHistorico(
            UUID acaoId,
            @Schema(example = "Aferir pressão arterial") String nome,
            @Schema(example = "120/80") String valor,
            @Schema(example = "mmHg") String unidade,
            String observacao) {
    }
}
