package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.time.ZoneId;
import java.util.UUID;

import UEPB.ESWII.ClinicalStudyPB.service.ResultadoAtividadeService.AtividadeComResultado;
import io.swagger.v3.oas.annotations.media.Schema;

/** Uma atividade de avaliação clínica da visita prevista e o seu resultado ({@code null} = pendente). */
@Schema(name = "AtividadeComResultado",
        description = "Uma atividade de avaliação clínica da visita prevista e o seu resultado (nulo = pendente).")
public record AtividadeComResultadoResponse(
        UUID acaoId,
        @Schema(example = "Aferir pressão arterial") String nome,
        @Schema(example = "Medida simulada", nullable = true) String descricao,
        @Schema(description = "Nulo enquanto o resultado estiver pendente.", nullable = true)
        ResultadoAtividadeResponse resultado) {

    public static AtividadeComResultadoResponse de(AtividadeComResultado atividade, ZoneId fuso) {
        return new AtividadeComResultadoResponse(
                atividade.acao().getId(),
                atividade.acao().getNome(),
                atividade.acao().getDescricao(),
                atividade.registrado() ? ResultadoAtividadeResponse.de(atividade.resultado(), fuso) : null);
    }
}
