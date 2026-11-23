package UEPB.ESWII.ClinicalStudyPB.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SolicitanteRetirada;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Registro formal da retirada de um participante do estudo (RF16, regra 6 da Sprint 3).
 *
 * <p>Há no máximo uma retirada por {@link Participacao} ({@code uk_retiradas_participacao}), pois a retirada é
 * <strong>irreversível</strong>. A entidade só guarda os dados; as validações de pré-condições, a mudança da
 * participação para {@code RETIRADO} e o cancelamento em cascata das visitas futuras ficam no serviço de domínio
 * (T102).</p>
 */
@Entity
@Table(
        name = "retiradas_participacao",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_retiradas_participacao",
                columnNames = "participacao_id"))
@Getter
@Setter
@NoArgsConstructor
public class RetiradaParticipacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participacao_id", nullable = false, unique = true, updatable = false)
    private Participacao participacao;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SolicitanteRetirada solicitante;

    @Column(name = "data_retirada", nullable = false)
    private LocalDate dataRetirada;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrado_por_id", nullable = false, updatable = false)
    private Usuario registradoPor;

    @Column(name = "registrado_em", nullable = false, updatable = false)
    private LocalDateTime registradoEm;

    /** Preenche a data e hora do registro se o serviço não as informou (o serviço usa o {@code Clock} da aplicação). */
    @PrePersist
    private void registrarCriacao() {
        if (registradoEm == null) {
            registradoEm = LocalDateTime.now();
        }
    }
}
