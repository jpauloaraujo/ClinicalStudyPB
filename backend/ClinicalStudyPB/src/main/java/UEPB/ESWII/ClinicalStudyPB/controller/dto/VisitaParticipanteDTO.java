package UEPB.ESWII.ClinicalStudyPB.controller.dto;
import java.util.List;
public record VisitaParticipanteDTO(
    String id,
    String estudo,
    String nomeVisita,
    String dataHora, // Formato ISO 8601
    String situacao, // 'AGENDADA' | 'REALIZADA' | 'AUSENTE' | 'CANCELADA'
    List<String> atividadesPrevistas,
    String motivoCancelamento 
) {}