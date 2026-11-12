package UEPB.ESWII.ClinicalStudyPB.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisitaAgendada;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "visitas_agendadas",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_visitas_agendadas_participacao_visita",
                columnNames = {"participacao_id", "visita_planejada_id"}))
@Getter
@Setter
@NoArgsConstructor
public class ScheduledVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participacao_id", nullable = false)
    private Participacao participacao;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visita_planejada_id", nullable = false)
    private VisitaPlanejada visitaPlanejada;

    @Column(name = "data_hora_agendada", nullable = false)
    private LocalDateTime dataHoraAgendada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoVisitaAgendada situacao;

    @Column(name = "motivo_observacao", length = 500)
    private String motivoObservacao;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_ultima_alteracao", nullable = false)
    private LocalDateTime dataUltimaAlteracao;

    @PrePersist
    private void registrarCriacao() {
        LocalDateTime agora = LocalDateTime.now();
        dataCriacao = agora;
        dataUltimaAlteracao = agora;
    }

    @PreUpdate
    private void registrarAlteracao() {
        dataUltimaAlteracao = LocalDateTime.now();
    }
}
