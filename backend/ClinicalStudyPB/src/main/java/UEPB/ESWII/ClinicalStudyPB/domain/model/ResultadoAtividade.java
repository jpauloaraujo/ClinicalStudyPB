package UEPB.ESWII.ClinicalStudyPB.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * Resultado de uma atividade ({@link Acao}) de avaliação em uma {@link VisitaAgendada} realizada (RF14).
 *
 * <p><strong>Os resultados são fictícios</strong> (LGPD): o sistema é acadêmico e nenhum dado real de pessoa é
 * armazenado aqui.</p>
 *
 * <p>Há um resultado por atividade e por visita agendada ({@code uk_resultados_atividade_visita_acao}). A entidade
 * só guarda dados: as regras de quem pode registrar e quando (visita {@code REALIZADA}, categoria
 * {@code AVALIACAO_CLINICA}, estudo não encerrado) ficam no serviço de resultados (T89).</p>
 */
@Entity
@Table(
        name = "resultados_atividade",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_resultados_atividade_visita_acao",
                columnNames = {"visita_agendada_id", "acao_id"}))
@Getter
@Setter
@NoArgsConstructor
public class ResultadoAtividade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visita_agendada_id", nullable = false, updatable = false)
    private VisitaAgendada visitaAgendada;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "acao_id", nullable = false, updatable = false)
    private Acao acao;

    /** Valor fictício em texto curto livre (ex.: "120/80"); obrigatório. */
    @Column(nullable = false, length = 200)
    private String valor;

    @Column(length = 30)
    private String unidade;

    @Column(length = 500)
    private String observacao;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrado_por_id", nullable = false, updatable = false)
    private Usuario registradoPor;

    @Column(name = "registrado_em", nullable = false, updatable = false)
    private LocalDateTime registradoEm;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alterado_por_id", nullable = false)
    private Usuario alteradoPor;

    @Column(name = "alterado_em", nullable = false)
    private LocalDateTime alteradoEm;

    @PrePersist
    private void registrarCriacao() {
        LocalDateTime agora = LocalDateTime.now();
        registradoEm = agora;
        alteradoEm = agora;
        if (alteradoPor == null) {
            alteradoPor = registradoPor;
        }
    }

    @PreUpdate
    private void registrarAlteracao() {
        alteradoEm = LocalDateTime.now();
    }
}
