package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Erro", description = "Formato único de erro da API")
public record ErroResponse(
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED) Integer status,
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED) String codigoRegra,
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED) String mensagem,
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED) List<ErroCampoResponse> errosCampo,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String erro,
        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED) String codigo) {

    @Schema(name = "ErroCampo")
    public record ErroCampoResponse(String campo, String erro) {
    }
}
