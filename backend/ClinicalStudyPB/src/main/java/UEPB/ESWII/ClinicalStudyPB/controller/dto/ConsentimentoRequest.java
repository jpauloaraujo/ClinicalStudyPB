package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/** Registro do consentimento simulado (RF10). Ambos os campos são opcionais. */
@Schema(description = "Dados do consentimento; sem campos, vale agora e a versão padrão do termo")
public record ConsentimentoRequest(
        @Schema(description = "Data e hora da assinatura (não pode ser futura); padrão: agora")
        LocalDateTime dataHora,

        @Schema(description = "Versão do termo; padrão: a versão configurada (TCLE-SIMULADO v1)", maxLength = 60)
        @Size(max = 60, message = "A versão do termo deve ter no máximo 60 caracteres")
        String versaoTermo) {
}
