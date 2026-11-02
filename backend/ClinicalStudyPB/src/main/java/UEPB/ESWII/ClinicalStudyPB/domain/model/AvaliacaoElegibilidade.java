package UEPB.ESWII.ClinicalStudyPB.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.ResultadoAvaliacao;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A "folha da prova" de um candidato: o checklist em que o coordenador verifica se ele tem o perfil
 * do estudo. Agrupa uma {@link RespostaAvaliacao} por critério do estudo, a data e o resultado.
 *
 * <p>Uma nova avaliação passa a valer ({@code vigente}) e a anterior fica no histórico.</p>
 */
@Entity
@Table(name = "avaliacoes_elegibilidade")
@Getter
@Setter
@NoArgsConstructor
public class AvaliacaoElegibilidade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidato_id", nullable = false)
    private Candidato candidato;

    /** Quando o checklist foi preenchido. */
    @Column(nullable = false)
    private LocalDateTime dataAvaliacao;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "avaliador_id", nullable = false)
    private Usuario avaliador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResultadoAvaliacao resultado;

    /** Soma das notas dos critérios de inclusão (usada para ordenar quem concorre à vaga). */
    @Column(nullable = false)
    private float pontuacao;

    /** Só a avaliação mais recente do candidato é vigente. */
    @Column(nullable = false)
    private boolean vigente = true;

    @JsonIgnore
    @OneToMany(mappedBy = "avaliacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RespostaAvaliacao> respostas = new ArrayList<>();
}
