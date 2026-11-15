package UEPB.ESWII.ClinicalStudyPB.domain.enums;

/**
 * Desfecho que o coordenador pode registrar para uma visita agendada (RF13). Cada tipo leva a uma situação final
 * de {@link SituacaoVisita}; {@code AGENDADA} não é um desfecho.
 */
public enum TipoDesfecho {
    REALIZADA,
    AUSENTE,
    CANCELADA
}
