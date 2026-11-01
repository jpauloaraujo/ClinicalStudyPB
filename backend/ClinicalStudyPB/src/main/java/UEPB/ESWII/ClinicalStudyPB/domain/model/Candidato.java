package UEPB.ESWII.ClinicalStudyPB.domain.model;

import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoCandidato;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pessoa em triagem para um estudo antes de ser efetivada como participante. Guarda só dados
 * fictícios (LGPD): um código gerado pelo sistema e um nome fictício, nunca o nome civil.
 */
@Entity
@Table(name = "candidatos")
@Getter
@Setter
@NoArgsConstructor
public class Candidato {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Estudo ao qual o candidato está vinculado; ele só é avaliado contra os critérios deste estudo. */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudo_id", nullable = false)
    private Estudo estudo;

    /** Identificador único dentro do estudo, gerado pelo sistema (ex.: CAND-001). */
    @Column(nullable = false, length = 20)
    private String codigo;

    /** Pseudônimo ou iniciais (ex.: "J. S."). */
    @Column(nullable = false, length = 120)
    private String nomeFicticio;

    @Column(nullable = false)
    private LocalDate dataNascimento;

    /** Telefone ou e-mail para agendamento; opcional (o candidato pode estar internado no hospital). */
    @Column(length = 180)
    private String contato;

    /**
     * Conta de usuário do participante. Fica vazio enquanto o candidato é gerenciado só pelo
     * coordenador na web; é preenchido quando ele cria credenciais no aplicativo mobile.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SituacaoCandidato situacao = SituacaoCandidato.CADASTRADO;

    public boolean estaIncluido() {
        return situacao == SituacaoCandidato.INCLUIDO;
    }
}
