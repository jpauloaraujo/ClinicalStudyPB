package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.time.LocalDate;
import java.util.UUID;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoParticipacao;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import io.swagger.v3.oas.annotations.media.Schema;

/** Detalhe de um estudo do participante logado: os campos de {@link MeuEstudoResponse} mais os do estudo (T109). */
@Schema(description = "Detalhe de um estudo do participante logado")
public record MeuEstudoDetalheResponse(
        UUID participacaoId,
        UUID estudoId,
        String titulo,
        String resumo,
        StatusEstudo statusEstudo,
        LocalDate dataInclusao,
        SituacaoParticipacao situacaoParticipacao,
        @Schema(description = "Objetivo completo do estudo") String objetivo,
        LocalDate dataInicio,
        LocalDate dataFim,
        @Schema(description = "Nome do responsável pelo estudo") String responsavelNome,
        @Schema(description = "Só a data da retirada; nulo se a participação não foi retirada", nullable = true)
        RetiradaDoMeuEstudo retirada) {
}
