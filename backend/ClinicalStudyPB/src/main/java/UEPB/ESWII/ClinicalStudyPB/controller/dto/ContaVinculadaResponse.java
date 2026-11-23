package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/** Conta vinculada a um participante. Só dados de identificação: nunca senha, hash, perfil nem situação da conta. */
@Schema(description = "Conta de participante vinculada a uma participação")
public record ContaVinculadaResponse(UUID participacaoId, UUID usuarioId, String nome, String email) {
}
