package UEPB.ESWII.ClinicalStudyPB.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;
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
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Visita real de um participante: instância de uma {@link VisitaPlanejada}. Só guarda dados; a conta da janela de
 * datas fica em {@link VisitaPlanejada} e as regras de agendamento e de desfecho ficam em serviço.
 *
 * <p>{@code versao} é o controle de versão otimista: dois desfechos simultâneos para a mesma visita não podem
 * dar certo ao mesmo tempo.</p>
 */
@Entity
@Table(name = "visitas_agendadas")
@Getter
@Setter
@NoArgsConstructor
public class VisitaAgendada {

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
    private SituacaoVisita situacao = SituacaoVisita.AGENDADA;

    @Column(name = "data_hora_realizada")
    private LocalDateTime dataHoraRealizada;

    @Column(length = 500)
    private String motivo;

    @Column(length = 500)
    private String observacao;

    @Version
    @Column(nullable = false)
    private Long versao;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "criado_por_id", nullable = false)
    private Usuario criadoPor;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alterado_por_id", nullable = false)
    private Usuario alteradoPor;

    @Column(name = "alterado_em", nullable = false)
    private LocalDateTime alteradoEm;

    @PrePersist
    private void preencherAuditoriaInicial() {
        LocalDateTime agora = LocalDateTime.now();
        if (criadoEm == null) {
            criadoEm = agora;
        }
        if (alteradoEm == null) {
            alteradoEm = agora;
        }
        if (alteradoPor == null) {
            alteradoPor = criadoPor;
        }
    }

    public boolean estaAgendada() {
        return situacao == SituacaoVisita.AGENDADA;
    }

    public LocalDate dataAgendada() {
        return dataHoraAgendada.toLocalDate();
    }
}