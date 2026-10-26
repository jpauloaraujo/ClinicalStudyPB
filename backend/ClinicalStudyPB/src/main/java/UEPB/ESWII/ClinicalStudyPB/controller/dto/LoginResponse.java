package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta de autenticação com Bearer Token JWT")
public record LoginResponse(
        @Schema(description = "Token de autenticação JWT assinado", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,

        @Schema(description = "Tipo do token", example = "Bearer")
        String tipo
) {
    public LoginResponse(String token) {
        this(token, "Bearer");
    }
}
