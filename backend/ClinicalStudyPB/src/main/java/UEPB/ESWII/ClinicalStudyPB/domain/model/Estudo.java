package UEPB.ESWII.ClinicalStudyPB.domain.model;

import java.time.LocalDate;
import java.util.UUID;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
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
public class Estudo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "O título é obrigatório")
    @Size(max = 180, message = "O título deve ter no máximo 180 caracteres")
    @Column(nullable = false, length = 180)
    private String titulo;

    @NotBlank(message = "O objetivo é obrigatório")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String objetivo;

    @NotNull(message = "O responsável é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsavel_id", nullable = false)
    private Usuario responsavel;

    private LocalDate dataInicio;

    private LocalDate dataFim;

    @NotNull(message = "O limite de participantes é obrigatório")
    @Min(value = 1, message = "O limite de participantes deve ser pelo menos 1")
    @Column(nullable = false)
    private Integer limiteParticipantes;

    @NotNull(message = "O status é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusEstudo status = StatusEstudo.PLANEJAMENTO;
}
