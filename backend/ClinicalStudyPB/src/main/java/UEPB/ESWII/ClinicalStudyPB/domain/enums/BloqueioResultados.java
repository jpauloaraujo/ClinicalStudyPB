package UEPB.ESWII.ClinicalStudyPB.domain.enums;

/**
 * Motivo pelo qual uma visita agendada não aceita registro nem edição de resultados (RF14). A consulta dos resultados
 * devolve o primeiro motivo, nesta ordem de prioridade; sem motivo, o campo é nulo.
 */
public enum BloqueioResultados {
    /** A visita não está REALIZADA (agendada, ausente ou cancelada). */
    VISITA_NAO_REALIZADA,
    /** O estudo foi encerrado. */
    ESTUDO_ENCERRADO,
    /** O participante foi retirado do estudo (preenchido a partir da T102). */
    PARTICIPANTE_RETIRADO
}
