package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.util.UUID;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.CategoriaAcao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Acao;

public record AcaoResponse(UUID id, UUID visitaId, String nome, String descricao, CategoriaAcao categoria) {

    public static AcaoResponse de(Acao acao) {
        return new AcaoResponse(acao.getId(), acao.getVisita().getId(), acao.getNome(), acao.getDescricao(),
                acao.getCategoria());
    }
}
