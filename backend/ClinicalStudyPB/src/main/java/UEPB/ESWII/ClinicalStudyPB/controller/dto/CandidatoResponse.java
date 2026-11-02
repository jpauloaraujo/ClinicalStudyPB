package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.ResultadoAvaliacao;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoCandidato;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Candidato;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Candidato com um resumo da última avaliação e do consentimento")
public record CandidatoResponse(
        UUID id,
        UUID estudoId,
        String codigo,
        String nomeFicticio,
        LocalDate dataNascimento,
        String contato,
        @Schema(description = "Conta de usuário do participante, quando existir") UUID usuarioId,
        SituacaoCandidato situacao,
        @Schema(description = "Resultado da avaliação vigente; nulo se nunca foi avaliado") ResultadoAvaliacao resultadoAvaliacao,
        @Schema(description = "Data da avaliação vigente") LocalDateTime dataAvaliacao,
        @Schema(description = "Se há consentimento registrado") boolean consentimentoRegistrado,
        @Schema(description = "Data e hora do consentimento, quando registrado") LocalDateTime dataConsentimento) {

    public static CandidatoResponse de(Candidato candidato, ResultadoAvaliacao resultado,
            LocalDateTime dataAvaliacao, LocalDateTime dataConsentimento) {
        return new CandidatoResponse(candidato.getId(), candidato.getEstudo().getId(), candidato.getCodigo(),
                candidato.getNomeFicticio(), candidato.getDataNascimento(), candidato.getContato(),
                candidato.getUsuario() == null ? null : candidato.getUsuario().getId(),
                candidato.getSituacao(), resultado, dataAvaliacao, dataConsentimento != null, dataConsentimento);
    }
}
