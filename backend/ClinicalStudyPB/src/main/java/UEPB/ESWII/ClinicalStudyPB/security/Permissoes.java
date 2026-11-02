package UEPB.ESWII.ClinicalStudyPB.security;

/**
 * Expressões de autorização (@PreAuthorize) do módulo de recrutamento, conforme a matriz RBAC:
 * o pesquisador escreve critérios e visitas (RF05, RF06); o coordenador escreve candidatos,
 * avaliação, consentimento e inclusão (RF08 a RF11); ambos leem; o administrador não acessa.
 */
public final class Permissoes {

    public static final String LEITURA = "hasAnyRole('PESQUISADOR', 'COORDENADOR')";
    public static final String ESCRITA_PLANEJAMENTO = "hasRole('PESQUISADOR')";
    public static final String ESCRITA_RECRUTAMENTO = "hasRole('COORDENADOR')";

    private Permissoes() {
    }
}
