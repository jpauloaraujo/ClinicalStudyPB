package UEPB.ESWII.ClinicalStudyPB.domain.enums;

/**
 * Situação de uma participação vista pelo participante (T109). A retirada (T101 a T103) ainda não existe; enquanto
 * isso toda participação é {@link #ATIVO}. Quando a T101 criar a coluna {@code situacao}, esta enum deve ser
 * reaproveitada pela entidade (ver docs/design/sprint3/T109-pendencias-para-proximas-tasks.md).
 */
public enum SituacaoParticipacao {
    ATIVO,
    RETIRADO
}
