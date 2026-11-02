package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.time.LocalDate;
import java.util.UUID;

import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Participante incluído em um estudo")
public record ParticipacaoResponse(
        UUID id,
        UUID candidatoId,
        String codigo,
        String nomeFicticio,
        UUID estudoId,
        @Schema(description = "Dia 0 do cronograma do participante") LocalDate dataInclusao,
        @Schema(description = "Nome de quem fez a inclusão") String incluidoPor) {

    public static ParticipacaoResponse de(Participacao participacao) {
        return new ParticipacaoResponse(participacao.getId(), participacao.getCandidato().getId(),
                participacao.getCandidato().getCodigo(), participacao.getCandidato().getNomeFicticio(),
                participacao.getEstudo().getId(), participacao.getDataInclusao(),
                participacao.getIncluidoPor().getNome());
    }
}
