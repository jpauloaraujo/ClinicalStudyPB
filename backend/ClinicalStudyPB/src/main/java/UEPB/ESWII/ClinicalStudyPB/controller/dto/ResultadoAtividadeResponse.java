package UEPB.ESWII.ClinicalStudyPB.controller.dto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

import UEPB.ESWII.ClinicalStudyPB.domain.model.ResultadoAtividade;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resultado fictício de uma atividade, com quem registrou e quem alterou por último (nome do usuário). Datas e horas
 * saem como {@link OffsetDateTime}, no fuso de {@code app.fuso-horario}.
 */
@Schema(description = "Resultado fictício de uma atividade, com quem registrou e quem alterou por último (nome do usuário).")
public record ResultadoAtividadeResponse(
        UUID acaoId,
        @Schema(example = "120/80") String valor,
        @Schema(example = "mmHg", nullable = true) String unidade,
        @Schema(example = "Sentado, após 5 minutos", nullable = true) String observacao,
        @Schema(example = "Pesquisador Demo (Ficticio)") String registradoPor,
        @Schema(example = "2026-11-14T10:00:00-03:00") OffsetDateTime registradoEm,
        @Schema(example = "Pesquisador Demo (Ficticio)") String alteradoPor,
        @Schema(example = "2026-11-14T10:00:00-03:00") OffsetDateTime alteradoEm) {

    /** Monta a resposta; precisa de sessão aberta, porque lê o nome dos usuários (relações LAZY). */
    public static ResultadoAtividadeResponse de(ResultadoAtividade resultado, ZoneId fuso) {
        return new ResultadoAtividadeResponse(
                resultado.getAcao().getId(),
                resultado.getValor(),
                resultado.getUnidade(),
                resultado.getObservacao(),
                resultado.getRegistradoPor().getNome(),
                comFuso(resultado.getRegistradoEm(), fuso),
                resultado.getAlteradoPor().getNome(),
                comFuso(resultado.getAlteradoEm(), fuso));
    }

    private static OffsetDateTime comFuso(LocalDateTime dataHora, ZoneId fuso) {
        return dataHora == null ? null : dataHora.atZone(fuso).toOffsetDateTime();
    }
}
