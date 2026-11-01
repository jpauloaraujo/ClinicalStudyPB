package UEPB.ESWII.ClinicalStudyPB.domain.model;

import java.util.function.Supplier;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.NivelAtendimento;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.TipoCriterio;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Critério que diz se o candidato será aceito no estudo. Tem um {@code peso} e pode ser
 * {@code determinante}: um critério determinante DEVE ser atendido por completo; os demais
 * podem ser atendidos, parcialmente atendidos ou não atendidos e o candidato ainda concorre à vaga.
 */
@Entity
@DiscriminatorValue("INCLUSAO")
@Getter
@Setter
@NoArgsConstructor
public class CriterioInclusao extends Criterio {

    /** Pontuação máxima do critério; a nota obtida varia de 0 até o peso. */
    private float peso;

    private boolean determinante;

    @Override
    public TipoCriterio getTipo() {
        return TipoCriterio.INCLUSAO;
    }

    /**
     * Valida o critério e devolve a nota obtida, de 0 até o {@link #getPeso() peso}:
     * 0 = não atendido, entre 0 e o peso = parcialmente atendido, igual ao peso = atendido.
     *
     * <p>O {@code validador} define o que de fato é feito para validar o critério (é uma função
     * qualquer, passada por quem chama). Valores acima do peso são limitados ao peso e valores
     * negativos ou nulos valem 0.</p>
     */
    public float validarCriterio(Supplier<Float> validador) {
        Float obtido = validador.get();
        if (obtido == null || obtido <= 0f) {
            return 0f;
        }
        return Math.min(obtido, peso);
    }

    /** Classifica uma nota obtida em relação ao peso deste critério. */
    public NivelAtendimento classificar(float obtido) {
        if (obtido <= 0f) {
            return NivelAtendimento.NAO_ATENDIDO;
        }
        return obtido >= peso ? NivelAtendimento.ATENDIDO : NivelAtendimento.PARCIALMENTE_ATENDIDO;
    }
}
