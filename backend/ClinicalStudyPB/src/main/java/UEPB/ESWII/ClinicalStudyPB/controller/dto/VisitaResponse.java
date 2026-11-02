package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.util.List;
import java.util.UUID;

import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaPlanejada;

public record VisitaResponse(
        UUID id,
        UUID estudoId,
        String nome,
        int diaPrevisto,
        int tolerancia,
        int ordem,
        List<AcaoResponse> acoes) {

    public static VisitaResponse de(VisitaPlanejada visita) {
        return new VisitaResponse(visita.getId(), visita.getEstudo().getId(), visita.getNome(),
                visita.getDiaPrevisto(), visita.getTolerancia(), visita.getOrdem(),
                visita.getAcoes().stream().map(AcaoResponse::de).toList());
    }
}
