package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Resultado fictício de uma atividade de avaliação (RF14, T90). O valor é texto curto livre, sem faixa de referência.
 *
 * <p>O valor em branco <b>não</b> é barrado aqui com {@code @NotBlank}: o contrato (T88) pede o código próprio
 * {@code VALOR_OBRIGATORIO}, que o serviço (T89) devolve. Com {@code @NotBlank} a resposta seria o genérico
 * {@code VALIDACAO_CAMPOS}. Os tamanhos máximos, esses sim, são do Bean Validation ({@code VALIDACAO_CAMPOS}).</p>
 */
@Schema(description = "Resultado fictício de uma atividade de avaliação. O valor é texto curto livre (sem faixa de referência).")
public record ResultadoRequest(
        @Schema(description = "Obrigatório; não pode ser só espaços (VALOR_OBRIGATORIO).", example = "120/80",
                requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 200)
        @Size(max = 200, message = "O valor deve ter no máximo 200 caracteres") String valor,
        @Schema(example = "mmHg", nullable = true, maxLength = 30)
        @Size(max = 30, message = "A unidade deve ter no máximo 30 caracteres") String unidade,
        @Schema(example = "Sentado, após 5 minutos", nullable = true, maxLength = 500)
        @Size(max = 500, message = "A observação deve ter no máximo 500 caracteres") String observacao) {
}
