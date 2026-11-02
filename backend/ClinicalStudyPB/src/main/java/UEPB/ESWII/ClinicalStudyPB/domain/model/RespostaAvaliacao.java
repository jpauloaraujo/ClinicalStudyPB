package UEPB.ESWII.ClinicalStudyPB.domain.model;

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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Resposta a um critério dentro de uma {@link AvaliacaoElegibilidade}: a "questão" é o critério e
 * a resposta diz se a condição dele se aplica ao candidato.
 *
 * <p>Num critério de inclusão a resposta precisa ser verdadeira para o candidato passar; num de
 * exclusão (ex.: ser fumante) precisa ser falsa.</p>
 */
@Entity
@Table(name = "respostas_avaliacao")
@Getter
@Setter
@NoArgsConstructor
public class RespostaAvaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "avaliacao_id", nullable = false)
    private AvaliacaoElegibilidade avaliacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "criterio_id", nullable = false)
    private Criterio criterio;

    /** Se a condição descrita pelo critério se aplica ao candidato. */
    @Column(nullable = false)
    private boolean aplica;

    /** Texto livre para o avaliador justificar a resposta. */
    @Column(length = 500)
    private String observacao;
}
