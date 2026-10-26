package UEPB.ESWII.ClinicalStudyPB.domain.model;

import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "estudos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Schema(description = "Representa um protocolo de estudo clínico gerenciado no sistema")
public class Estudo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Identificador único do estudo clínico (UUID)", example = "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotBlank(message = "O título é obrigatório")
    @Size(max = 180, message = "O título deve ter no máximo 180 caracteres")
    @Column(nullable = false, length = 180)
    @Schema(description = "Título oficial do protocolo do estudo clínico", example = "Estudo Clínico de Monitoramento da Hipertensão Arterial (Fictício)")
    private String titulo;

    @NotBlank(message = "O objetivo é obrigatório")
    @Column(nullable = false, columnDefinition = "TEXT")
    @Schema(description = "Descrição detalhada do objetivo científico e operacional do estudo", example = "Avaliar a adesão ao protocolo de acompanhamento ambulatorial simplificado com dados simulados.")
    private String objetivo;

    @NotNull(message = "O responsável é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsavel_id", nullable = false)
    @Schema(description = "Usuário pesquisador responsável pelo estudo clínico")
    private Usuario responsavel;

    @Schema(description = "Data prevista ou efetiva de início do estudo", example = "2026-11-01")
    private LocalDate dataInicio;

    @Schema(description = "Data prevista ou efetiva de encerramento do estudo", example = "2027-05-01")
    private LocalDate dataFim;

    @NotNull(message = "O limite de participantes é obrigatório")
    @Min(value = 1, message = "O limite de participantes deve ser pelo menos 1")
    @Column(nullable = false)
    @Schema(description = "Número máximo de participantes que podem ser incluídos no estudo", example = "50", minimum = "1")
    private Integer limiteParticipantes;

    @NotNull(message = "O status é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Schema(description = "Estado atual do ciclo de vida do estudo clínico", example = "PLANEJAMENTO", defaultValue = "PLANEJAMENTO")
    private StatusEstudo status = StatusEstudo.PLANEJAMENTO;
}
