package UEPB.ESWII.ClinicalStudyPB.domain.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.TipoCriterio;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Critério de aceitação de um estudo: o que um candidato precisa atender para ingressar nele.
 * Classe abstrata; os critérios concretos são {@link CriterioInclusao} e {@link CriterioExclusao},
 * gravados na mesma tabela e distinguidos pela coluna {@code tipo}.
 */
@Entity
@Table(name = "criterios")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", length = 20)
@Getter
@Setter
@NoArgsConstructor
public abstract class Criterio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudo_id", nullable = false)
    private Estudo estudo;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    /** Tipo do critério, igual ao valor da coluna discriminadora. */
    public abstract TipoCriterio getTipo();
}
