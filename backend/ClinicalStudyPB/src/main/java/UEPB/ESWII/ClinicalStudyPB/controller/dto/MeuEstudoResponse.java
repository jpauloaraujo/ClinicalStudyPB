package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.time.LocalDate;
import java.util.UUID;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoParticipacao;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import io.swagger.v3.oas.annotations.media.Schema;

/** Estudo em que o participante logado está incluído (T109). Não traz nenhum dado de candidato. */
@Schema(description = "Estudo em que o participante logado está incluído (um item por participação)")
public record MeuEstudoResponse(
        UUID participacaoId,
        UUID estudoId,
        String titulo,
        @Schema(description = "Objetivo do estudo cortado em 160 caracteres (reticências quando cortado)") String resumo,
        StatusEstudo statusEstudo,
        @Schema(description = "Dia 0 da participação") LocalDate dataInclusao,
        SituacaoParticipacao situacaoParticipacao) {

    /** Tamanho máximo do resumo, reticências incluídas. */
    public static final int TAMANHO_RESUMO = 160;

    /** Corta o objetivo em {@value #TAMANHO_RESUMO} caracteres no máximo; ao cortar, termina com reticências. */
    public static String resumir(String objetivo) {
        if (objetivo == null) {
            return "";
        }
        String texto = objetivo.strip();
        if (texto.length() <= TAMANHO_RESUMO) {
            return texto;
        }
        return texto.substring(0, TAMANHO_RESUMO - 1).stripTrailing() + "…";
    }
}
