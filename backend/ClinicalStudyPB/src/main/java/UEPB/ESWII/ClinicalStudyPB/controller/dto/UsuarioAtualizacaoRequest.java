package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.PerfilUsuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Dados aceitos pela API para atualizar um usuário existente. */
@Schema(description = "Dados para atualizar um usuário existente; a senha é opcional")
public record UsuarioAtualizacaoRequest(
        @Schema(description = "Nome completo do usuário", example = "Helena Costa", maxLength = 120)
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
        String nome,

        @Schema(description = "E-mail válido e único para acesso ao sistema", example = "helena.costa@example.com", maxLength = 180)
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Informe um e-mail válido")
        @Size(max = 180, message = "O e-mail deve ter no máximo 180 caracteres")
        String email,

        @Schema(description = "Nova senha (opcional; se informada, deve ter entre 8 e 100 caracteres)", example = "nova-senha-123", minLength = 8, maxLength = 100)
        @Size(min = 8, max = 100, message = "A senha deve ter entre 8 e 100 caracteres")
        String senha,

        @Schema(description = "Perfil de acesso do usuário", example = "PESQUISADOR")
        @NotNull(message = "O perfil é obrigatório")
        PerfilUsuario perfil,

        @Schema(description = "Indica se a conta está ativa", example = "true")
        @NotNull(message = "O estado ativo é obrigatório")
        Boolean ativo) {
}
