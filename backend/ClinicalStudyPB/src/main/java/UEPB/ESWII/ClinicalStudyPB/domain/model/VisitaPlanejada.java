package UEPB.ESWII.ClinicalStudyPB.domain.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Molde (roteiro oficial) de um encontro médico do protocolo de um estudo. É a regra geral criada
 * pelo pesquisador para todos os participantes, e não o agendamento real de uma pessoa: o protocolo
 * usa uma régua de tempo relativa, contada em dias a partir da inclusão do participante (dia 0).
 *
 * <p>Exemplo: "Retorno Mensal" com {@code diaPrevisto = 30} e {@code tolerancia = 5}. Para quem é
 * incluído hoje, a visita real é daqui a 30 dias e pode ocorrer do 25º ao 35º dia.</p>
 */
@Entity
@Table(name = "visitas_planejadas")
@Getter
@Setter
@NoArgsConstructor
public class VisitaPlanejada {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudo_id", nullable = false)
    private Estudo estudo;

    @Column(nullable = false, length = 100)
    private String nome;

    /** Dia alvo da visita, contado a partir da inclusão do participante (dia 0). */
    @Column(nullable = false)
    private int diaPrevisto;

    /** Margem aceitável, em dias, antes e depois do dia previsto. */
    @Column(nullable = false)
    private int tolerancia;

    /** Posição cronológica da visita no estudo, derivada do dia previsto (1 = primeira). */
    @Column(nullable = false)
    private int ordem;

    @JsonIgnore
    @OneToMany(mappedBy = "visita", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("nome ASC")
    private List<Acao> acoes = new ArrayList<>();

    /** Data real em que a visita deve ocorrer para quem foi incluído em {@code dataInclusao}. */
    public LocalDate dataPrevista(LocalDate dataInclusao) {
        return dataInclusao.plusDays(diaPrevisto);
    }

    /** Primeiro dia em que a visita ainda é cientificamente válida. */
    public LocalDate inicioDaJanela(LocalDate dataInclusao) {
        return dataPrevista(dataInclusao).minusDays(tolerancia);
    }

    /** Último dia em que a visita ainda é cientificamente válida. */
    public LocalDate fimDaJanela(LocalDate dataInclusao) {
        return dataPrevista(dataInclusao).plusDays(tolerancia);
    }

    public boolean estaNaJanela(LocalDate dataInclusao, LocalDate data) {
        return !data.isBefore(inicioDaJanela(dataInclusao)) && !data.isAfter(fimDaJanela(dataInclusao));
    }
}
