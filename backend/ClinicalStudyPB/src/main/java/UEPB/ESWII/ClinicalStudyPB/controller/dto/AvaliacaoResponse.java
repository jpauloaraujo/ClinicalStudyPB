package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.ResultadoAvaliacao;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.TipoCriterio;
import UEPB.ESWII.ClinicalStudyPB.domain.model.AvaliacaoElegibilidade;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Criterio;
import UEPB.ESWII.ClinicalStudyPB.domain.model.RespostaAvaliacao;
import UEPB.ESWII.ClinicalStudyPB.domain.regra.RegraElegibilidade;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Avaliação de elegibilidade de um candidato")
public record AvaliacaoResponse(
        UUID id,
        UUID candidatoId,
        LocalDateTime dataAvaliacao,
        String avaliador,
        ResultadoAvaliacao resultado,
        float pontuacao,
        @Schema(description = "Só a avaliação mais recente é vigente; as demais formam o histórico") boolean vigente,
        List<RespostaResponse> respostas,
        @Schema(description = "Critérios que causaram a reprovação (vazio quando aprovado)") List<CriterioFalho> criteriosReprovadores) {

    public record RespostaResponse(UUID criterioId, TipoCriterio tipo, String titulo, boolean aplica, String observacao) {
    }

    public record CriterioFalho(UUID criterioId, TipoCriterio tipo, String titulo) {
    }

    public static AvaliacaoResponse de(AvaliacaoElegibilidade avaliacao) {
        List<RespostaResponse> respostas = avaliacao.getRespostas().stream()
                .map(r -> new RespostaResponse(r.getCriterio().getId(), r.getCriterio().getTipo(),
                        r.getCriterio().getTitulo(), r.isAplica(), r.getObservacao()))
                .toList();

        List<Criterio> criterios = avaliacao.getRespostas().stream().map(RespostaAvaliacao::getCriterio).toList();
        Map<UUID, Boolean> aplica = new HashMap<>();
        avaliacao.getRespostas().forEach(r -> aplica.put(r.getCriterio().getId(), r.isAplica()));
        List<CriterioFalho> falhas = RegraElegibilidade.avaliar(criterios, aplica).falhas().stream()
                .map(c -> new CriterioFalho(c.getId(), c.getTipo(), c.getTitulo()))
                .toList();

        return new AvaliacaoResponse(avaliacao.getId(), avaliacao.getCandidato().getId(), avaliacao.getDataAvaliacao(),
                avaliacao.getAvaliador().getNome(), avaliacao.getResultado(), avaliacao.getPontuacao(),
                avaliacao.isVigente(), respostas, falhas);
    }
}
