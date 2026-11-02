package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.time.LocalDate;
import java.util.UUID;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Dados recebidos pela API para criar ou atualizar um estudo. */
@Schema(description = "Payload de requisição para cadastro ou atualização de um estudo clínico (RF04)")
public record EstudoRequest(
        @Schema(description = "Título do estudo clínico (máx. 180 caracteres)", example = "Estudo Clínico Demonstrativo — Fase II")
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 180, message = "O título deve ter no máximo 180 caracteres")
        String titulo,

        @Schema(description = "Objetivo principal do protocolo de pesquisa", example = "Avaliar a adesão ao protocolo fictício de acompanhamento clínico.")
        @NotBlank(message = "O objetivo é obrigatório")
        String objetivo,

        @Schema(description = "UUID de um usuário com perfil PESQUISADOR, responsável pelo estudo", example = "22222222-2222-4222-8222-222222222222")
        @NotNull(message = "O responsável é obrigatório")
        UUID responsavelId,

        @Schema(description = "Data de início prevista do estudo (ISO-8601)", example = "2026-11-01")
        LocalDate dataInicio,

        @Schema(description = "Data de término prevista do estudo (não pode ser anterior à data de início)", example = "2027-02-01")
        LocalDate dataFim,

        @Schema(description = "Limite máximo de participantes incluídos no estudo (mínimo 1)", example = "50", minimum = "1")
        @NotNull(message = "O limite de participantes é obrigatório")
        @Min(value = 1, message = "O limite de participantes deve ser de no mínimo 1")
        Integer limiteParticipantes,

        @Schema(description = "Estado operacional do estudo clínico", example = "PLANEJAMENTO")
        @NotNull(message = "O status é obrigatório")
        StatusEstudo status) {
}
