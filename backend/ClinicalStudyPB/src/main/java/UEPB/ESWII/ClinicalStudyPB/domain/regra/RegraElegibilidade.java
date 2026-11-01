package UEPB.ESWII.ClinicalStudyPB.domain.regra;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import UEPB.ESWII.ClinicalStudyPB.domain.model.Criterio;
import UEPB.ESWII.ClinicalStudyPB.domain.model.CriterioExclusao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.CriterioInclusao;

/**
 * Regra de elegibilidade como função pura de domínio, sem banco nem Spring.
 *
 * <p>Recebe os critérios do estudo e, para cada um, se a condição dele se aplica ao candidato.
 * O candidato é elegível quando todos os critérios de inclusão <b>determinantes</b> são atendidos e
 * nenhum critério de exclusão é satisfeito. Critérios de inclusão não determinantes não reprovam:
 * só somam pontos à pontuação do candidato.</p>
 */
public final class RegraElegibilidade {

    private RegraElegibilidade() {
    }

    /**
     * @param criterios todos os critérios do estudo
     * @param aplica    para cada id de critério, se a condição dele se aplica ao candidato; deve ter
     *                  uma entrada para cada critério informado
     */
    public static Resultado avaliar(List<? extends Criterio> criterios, Map<UUID, Boolean> aplica) {
        List<Criterio> falhas = new ArrayList<>();
        float pontuacao = 0f;

        for (Criterio criterio : criterios) {
            boolean seAplica = aplica.getOrDefault(criterio.getId(), false);
            if (criterio instanceof CriterioInclusao inclusao) {
                float obtido = inclusao.validarCriterio(() -> seAplica ? inclusao.getPeso() : 0f);
                pontuacao += obtido;
                if (inclusao.isDeterminante() && obtido < inclusao.getPeso()) {
                    falhas.add(inclusao);
                }
            } else if (criterio instanceof CriterioExclusao exclusao && exclusao.validarCriterio(() -> seAplica)) {
                falhas.add(exclusao);
            }
        }
        return new Resultado(falhas.isEmpty(), pontuacao, List.copyOf(falhas));
    }

    /**
     * @param elegivel  se o candidato passou na triagem
     * @param pontuacao soma das notas dos critérios de inclusão
     * @param falhas    critérios que causaram a inelegibilidade (vazio quando elegível)
     */
    public record Resultado(boolean elegivel, float pontuacao, List<Criterio> falhas) {
    }
}
