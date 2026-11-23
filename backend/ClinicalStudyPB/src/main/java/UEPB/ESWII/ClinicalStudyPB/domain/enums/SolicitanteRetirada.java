package UEPB.ESWII.ClinicalStudyPB.domain.enums;

/**
 * Quem solicitou a retirada de um participante do estudo (RF16, regra 6 da Sprint 3).
 *
 * <p>A retirada é <strong>irreversível</strong> ({@link SituacaoParticipacao#RETIRADO}), independentemente de quem a
 * solicitou. O valor {@link #PARTICIPANTE} também será reutilizado pelo pedido de retirada no app (RF20, Sprint 4).</p>
 */
public enum SolicitanteRetirada {
    PARTICIPANTE,
    PESQUISADOR,
    COORDENACAO
}
