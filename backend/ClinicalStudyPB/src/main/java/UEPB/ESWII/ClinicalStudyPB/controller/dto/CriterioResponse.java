package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.util.UUID;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.TipoCriterio;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Criterio;
import UEPB.ESWII.ClinicalStudyPB.domain.model.CriterioInclusao;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Critério de aceitação de um estudo")
public record CriterioResponse(
        UUID id,
        UUID estudoId,
        TipoCriterio tipo,
        String titulo,
        String descricao,
        @Schema(description = "Só em critérios de inclusão") Float peso,
        @Schema(description = "Só em critérios de inclusão; critérios de exclusão são sempre determinantes") Boolean determinante) {

    public static CriterioResponse de(Criterio criterio) {
        Float peso = null;
        Boolean determinante = null;
        if (criterio instanceof CriterioInclusao inclusao) {
            peso = inclusao.getPeso();
            determinante = inclusao.isDeterminante();
        }
        return new CriterioResponse(criterio.getId(), criterio.getEstudo().getId(), criterio.getTipo(),
                criterio.getTitulo(), criterio.getDescricao(), peso, determinante);
    }
}
