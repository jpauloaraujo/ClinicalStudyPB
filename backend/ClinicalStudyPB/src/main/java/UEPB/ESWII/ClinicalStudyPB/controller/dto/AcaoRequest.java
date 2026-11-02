package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.CategoriaAcao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Dados de uma ação (procedimento) de uma visita planejada. */
@Schema(description = "Payload de cadastro ou edição de uma ação de uma visita planejada")
public record AcaoRequest(
        @Schema(description = "Nome do procedimento", example = "Coleta de sangue", maxLength = 100)
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @Schema(description = "Detalhes do procedimento (opcional)", maxLength = 500)
        @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
        String descricao,

        @Schema(description = "Categoria do procedimento", example = "EXAME_LABORATORIAL")
        @NotNull(message = "A categoria é obrigatória")
        CategoriaAcao categoria) {
}
