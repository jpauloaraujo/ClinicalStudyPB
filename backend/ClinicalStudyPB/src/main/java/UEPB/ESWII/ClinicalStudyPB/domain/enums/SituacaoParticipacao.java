package UEPB.ESWII.ClinicalStudyPB.domain.enums;

/**
 * Situação de uma participação no estudo (RF16, regra 6 da Sprint 3).
 *
 * <p>cada um dos participantes incluídos nascem {@link #ATIVO}. Quando a retirada é registrada, a situação passa para
 * {@link #RETIRADO} de forma <strong>irreversível</strong>: não existe retorno para {@link #ATIVO} e a vaga
 * continua ocupada.</p>
 */
public enum SituacaoParticipacao {
    ATIVO,
    RETIRADO
}
