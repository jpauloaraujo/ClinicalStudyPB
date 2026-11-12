package UEPB.ESWII.ClinicalStudyPB.domain.enums;

/**
 * Situação de uma visita agendada. A visita nasce {@link #AGENDADA}; {@link #REALIZADA}, {@link #AUSENTE} e
 * {@link #CANCELADA} são finais: depois delas não há volta (regra 3 do planejamento da Sprint 3).
 */
public enum SituacaoVisita {
    AGENDADA,
    REALIZADA,
    AUSENTE,
    CANCELADA
}
