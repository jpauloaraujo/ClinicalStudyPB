package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para autenticação e obtenção do Bearer Token JWT")
public record LoginRequest(
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Informe um e-mail válido")
        @Schema(description = "E-mail cadastrado do usuário", example = "admin@clinicalstudy.test")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Schema(description = "Senha de acesso do usuário", example = "senha-de-teste")
        String senha
) {}
