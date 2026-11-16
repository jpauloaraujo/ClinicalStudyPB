package UEPB.ESWII.ClinicalStudyPB.service;

/**
 * Dado inválido segundo uma regra de negócio que o cliente pode explicar ao usuário (HTTP 400). Diferente da
 * {@link RegraNegocioException} (que responde {@code DADOS_INVALIDOS}), leva um {@code codigo} próprio da regra, como
 * {@code VALOR_OBRIGATORIO} ou {@code DATA_NO_PASSADO}.
 */
public class ValidacaoRegraException extends RuntimeException {

    private final String codigo;

    public ValidacaoRegraException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
