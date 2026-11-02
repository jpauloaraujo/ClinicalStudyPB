package UEPB.ESWII.ClinicalStudyPB.domain.enums;

/**
 * Etapa do candidato no funil de triagem. Decide o que ele pode fazer em seguida
 * (ser avaliado, consentir, ser incluído).
 */
public enum SituacaoCandidato {
    /** Recém-cadastrado, ainda sem avaliação de elegibilidade. */
    CADASTRADO,
    /** Reservado para uma avaliação em andamento; a API atual avalia todos os critérios de uma vez. */
    EM_AVALIACAO,
    /** Avaliação vigente aprovada, ainda sem consentimento. */
    ELEGIVEL,
    /** Avaliação vigente reprovada. */
    NAO_ELEGIVEL,
    /** Avaliação vigente aprovada e consentimento registrado: pronto para a inclusão. */
    CONSENTIDO,
    /** Incluído como participante oficial do estudo. */
    INCLUIDO
}
