package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.TipoCriterio;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Dados para criar ou editar um critério de aceitação de um estudo (RF05). */
@Schema(description = "Payload de cadastro ou edição de um critério de inclusão ou exclusão")
public record CriterioRequest(
        @Schema(description = "Tipo do critério; não pode ser trocado depois de criado", example = "INCLUSAO")
        @NotNull(message = "O tipo é obrigatório")
        TipoCriterio tipo,

        @Schema(description = "Título opcional; quando ausente, é derivado da descrição", example = "Idade entre 18 e 65 anos", maxLength = 120)
        @Size(max = 120, message = "O título deve ter no máximo 120 caracteres")
        String titulo,

        @Schema(description = "Descrição da condição que o candidato precisa atender (ou não)", minLength = 5, maxLength = 500)
        @NotBlank(message = "A descrição é obrigatória")
        @Size(min = 5, max = 500, message = "A descrição deve ter entre 5 e 500 caracteres")
        String descricao,

        @Schema(description = "Só para INCLUSAO: pontuação máxima do critério (maior que 0; padrão 1). Ignorado em EXCLUSAO", example = "1.0")
        @DecimalMin(value = "0.0", inclusive = false, message = "O peso deve ser maior que zero")
        Float peso,

        @Schema(description = "Só para INCLUSAO: se o critério DEVE ser atendido para o candidato ser elegível (padrão true). Ignorado em EXCLUSAO", example = "true")
        Boolean determinante) {

    public CriterioRequest {
        titulo = titulo == null ? null : titulo.trim();
        descricao = descricao == null ? null : descricao.trim();
    }
}
