package UEPB.ESWII.ClinicalStudyPB.security;

/**
 * Expressões de autorização (@PreAuthorize) do módulo de recrutamento, conforme a matriz RBAC:
 * o pesquisador escreve critérios e visitas (RF05, RF06); o coordenador escreve candidatos,
 * avaliação, consentimento e inclusão (RF08 a RF11); ambos leem; o administrador não acessa.
 *
 * <p>Sprint 3: só o pesquisador registra e edita os resultados fictícios das avaliações (RF14, regra 4); o
 * coordenador apenas consulta.</p>
 */
public final class Permissoes {

    public static final String LEITURA = "hasAnyRole('PESQUISADOR', 'COORDENADOR')";
    public static final String ESCRITA_PLANEJAMENTO = "hasRole('PESQUISADOR')";
    public static final String ESCRITA_RECRUTAMENTO = "hasRole('COORDENADOR')";
    /** Mesma expressão de {@link #ESCRITA_PLANEJAMENTO}; o nome diz que é a escrita dos resultados (T90). */
    public static final String ESCRITA_RESULTADOS = "hasRole('PESQUISADOR')";

    private Permissoes() {
    }
}
