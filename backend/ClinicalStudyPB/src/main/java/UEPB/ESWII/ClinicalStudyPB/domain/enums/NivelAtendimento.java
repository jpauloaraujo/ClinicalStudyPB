package UEPB.ESWII.ClinicalStudyPB.domain.enums;

/** Quanto um critério de inclusão foi atendido, comparando a nota obtida com o peso. */
public enum NivelAtendimento {
    /** Nota 0. */
    NAO_ATENDIDO,
    /** Nota maior que 0 e menor que o peso. */
    PARCIALMENTE_ATENDIDO,
    /** Nota igual ao peso. */
    ATENDIDO
}
