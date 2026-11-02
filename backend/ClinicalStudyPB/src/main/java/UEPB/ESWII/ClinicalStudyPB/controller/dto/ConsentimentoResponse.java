package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import UEPB.ESWII.ClinicalStudyPB.domain.model.Consentimento;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Comprovante do consentimento simulado")
public record ConsentimentoResponse(
        UUID id,
        UUID candidatoId,
        LocalDateTime dataHora,
        @Schema(description = "Nome do membro da equipe que aplicou o termo") String responsavel,
        String versaoTermo) {

    public static ConsentimentoResponse de(Consentimento consentimento) {
        return new ConsentimentoResponse(consentimento.getId(), consentimento.getCandidato().getId(),
                consentimento.getDataHora(), consentimento.getResponsavel().getNome(), consentimento.getVersaoTermo());
    }
}
