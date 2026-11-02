package UEPB.ESWII.ClinicalStudyPB.domain.model;

import java.time.LocalDate;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Marco clínico da entrada do candidato na pesquisa (a inclusão). A {@code dataInclusao} é o "dia 0":
 * âncora do cronograma, pois somada ao dia previsto de cada {@link VisitaPlanejada} dá a data real
 * de cada visita do participante.
 */
@Entity
@Table(name = "participacoes")
@Getter
@Setter
@NoArgsConstructor
public class Participacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidato_id", nullable = false, unique = true)
    private Candidato candidato;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudo_id", nullable = false)
    private Estudo estudo;

    /** Dia 0 do participante. */
    @Column(nullable = false)
    private LocalDate dataInclusao;

    /** Usuário (coordenador) que fez a inclusão. */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incluido_por_id", nullable = false)
    private Usuario incluidoPor;
}
