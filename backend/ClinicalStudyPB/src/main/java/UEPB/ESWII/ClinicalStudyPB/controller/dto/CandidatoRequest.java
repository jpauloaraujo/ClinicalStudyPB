package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

/** Dados de um candidato (RF08). Só dados fictícios: o código é gerado pelo sistema. */
@Schema(description = "Payload de cadastro ou edição de um candidato; use apenas dados fictícios")
public record CandidatoRequest(
        @Schema(description = "Pseudônimo ou iniciais, nunca o nome civil", example = "J. S.", maxLength = 120)
        @NotBlank(message = "O nome fictício é obrigatório")
        @Size(max = 120, message = "O nome fictício deve ter no máximo 120 caracteres")
        String nomeFicticio,

        @Schema(description = "Data de nascimento (no passado)", example = "1990-04-12")
        @NotNull(message = "A data de nascimento é obrigatória")
        @Past(message = "A data de nascimento deve estar no passado")
        LocalDate dataNascimento,

        @Schema(description = "Telefone ou e-mail para contato (opcional)", maxLength = 180)
        @Size(max = 180, message = "O contato deve ter no máximo 180 caracteres")
        String contato) {
}
