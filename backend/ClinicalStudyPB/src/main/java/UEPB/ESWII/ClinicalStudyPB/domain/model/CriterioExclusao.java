package UEPB.ESWII.ClinicalStudyPB.domain.model;

import java.util.function.BooleanSupplier;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.TipoCriterio;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

/**
 * Critério que impede o candidato de entrar no estudo. Todo critério de exclusão é determinante
 * (por isso não tem atributo para isso) e só pode estar satisfeito ou não: se está satisfeito,
 * o candidato é excluído.
 */
@Entity
@DiscriminatorValue("EXCLUSAO")
@NoArgsConstructor
public class CriterioExclusao extends Criterio {

    @Override
    public TipoCriterio getTipo() {
        return TipoCriterio.EXCLUSAO;
    }

    /**
     * Valida o critério e diz se ele foi satisfeito (isto é, se a condição de exclusão vale para o
     * candidato). O {@code validador} define o que de fato é verificado.
     */
    public boolean validarCriterio(BooleanSupplier validador) {
        return validador.getAsBoolean();
    }
}
