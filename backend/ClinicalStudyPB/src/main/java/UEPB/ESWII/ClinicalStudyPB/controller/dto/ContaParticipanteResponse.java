package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

/** Conta de participante disponível para vínculo. Também só identificação (RQ-T111-07). */
@Schema(description = "Conta de perfil PARTICIPANTE, ativa e ainda não vinculada a nenhum participante")
public record ContaParticipanteResponse(UUID usuarioId, String nome, String email) {
}
