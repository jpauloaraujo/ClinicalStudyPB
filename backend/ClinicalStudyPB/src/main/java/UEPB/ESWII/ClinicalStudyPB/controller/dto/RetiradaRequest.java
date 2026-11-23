package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.time.LocalDate;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SolicitanteRetirada;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Pedido de registro de retirada de um participante do estudo (RF16, T103).
 *
 * <p>O motivo em branco ou com menos de 5 caracteres após {@code trim} não é barrado por {@code @NotBlank} nem
 * {@code @Size(min = 5)} aqui para que o {@code RetiradaService} valide a precedência (estudo encerrado e
 * participante já retirado vêm antes) e devolva o código estável {@code MOTIVO_OBRIGATORIO} (400), no mesmo padrão
 * de {@link DesfechoRequest} e {@link ResultadoRequest}. O limite máximo (500 caracteres) e a obrigatoriedade de
 * {@code solicitante} e {@code dataRetirada} são validados pelo Bean Validation ({@code VALIDACAO_CAMPOS}).</p>
 */
@Schema(description = "Pedido de registro de retirada de um participante do estudo.")
public record RetiradaRequest(
        @Schema(description = "Motivo da retirada; obrigatório, de 5 a 500 caracteres depois do trim (MOTIVO_OBRIGATORIO).",
                example = "Mudança de cidade", requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 5, maxLength = 500)
        @Size(max = 500, message = "O motivo deve ter no máximo 500 caracteres")
        String motivo,

        @Schema(description = "Quem solicitou a retirada do participante (PARTICIPANTE, PESQUISADOR ou COORDENACAO).",
                example = "PARTICIPANTE", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Informe quem solicitou a retirada")
        SolicitanteRetirada solicitante,

        @Schema(description = "Data da retirada (yyyy-MM-dd); não pode ser futura (DATA_RETIRADA_FUTURA).",
                example = "2026-11-18", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "A data da retirada é obrigatória")
        LocalDate dataRetirada) {
}
