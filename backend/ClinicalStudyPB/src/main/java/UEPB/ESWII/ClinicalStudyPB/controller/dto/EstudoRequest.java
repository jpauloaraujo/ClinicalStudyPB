package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.time.LocalDate;
import java.util.UUID;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Dados recebidos pela API para criar ou atualizar um estudo. */
public record EstudoRequest(
        @NotBlank @Size(max = 180) String titulo,
        @NotBlank String objetivo,
        @NotNull UUID responsavelId,
        LocalDate dataInicio,
        LocalDate dataFim,
        @NotNull @Min(1) Integer limiteParticipantes,
        @NotNull StatusEstudo status) {
}
