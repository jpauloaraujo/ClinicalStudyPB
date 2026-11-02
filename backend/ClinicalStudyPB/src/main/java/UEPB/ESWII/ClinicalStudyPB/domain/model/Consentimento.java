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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Registro (simulado) do Termo de Consentimento Livre e Esclarecido: prova de auditoria de que o
 * candidato concordou em participar, com data e hora, quem aplicou o termo e a versão assinada.
 * Pertence ao histórico do candidato (no máximo um por candidato).
 */
@Entity
@Table(name = "consentimentos")
@Getter
@Setter
@NoArgsConstructor
public class Consentimento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidato_id", nullable = false, unique = true)
    private Candidato candidato;

    /** Momento exato da assinatura; nenhum procedimento do estudo pode ocorrer antes dele. */
    @Column(nullable = false)
    private LocalDateTime dataHora;

    /** Pessoa da equipe de pesquisa que aplicou o termo. */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsavel_id", nullable = false)
    private Usuario responsavel;

    /** Versão do termo aprovada pelo comitê de ética que o candidato leu e assinou. */
    @Column(nullable = false, length = 60)
    private String versaoTermo;
}
