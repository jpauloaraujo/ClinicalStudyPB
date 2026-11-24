package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/** Página do histórico de comparecimento do participante logado, com o resumo (T116). */
@Schema(description = "Página do histórico de comparecimento do participante logado, com o resumo")
public record HistoricoMinhasVisitasResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) ResumoComparecimentoResponse resumo,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<MinhaVisitaResponse> conteudo,
        @Schema(description = "Base 0", requiredMode = Schema.RequiredMode.REQUIRED) int pagina,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int tamanho,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long totalElementos,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int totalPaginas) {
}
