package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.util.UUID;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.PerfilUsuario;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

/** Representação pública de um usuário. Nunca inclui a senha ou seu hash. */
@Schema(description = "Dados de usuário devolvidos pela API, sem informações de senha")
public record UsuarioResponse(
        @Schema(description = "Identificador único do usuário", example = "22222222-2222-4222-8222-222222222222")
        UUID id,
        @Schema(description = "Nome completo do usuário", example = "Helena Costa")
        String nome,
        @Schema(description = "E-mail do usuário", example = "helena.costa@example.com")
        String email,
        @Schema(description = "Perfil de acesso", example = "PESQUISADOR")
        PerfilUsuario perfil,
        @Schema(description = "Indica se a conta está ativa", example = "true")
        boolean ativo) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.isAtivo());
    }
}
