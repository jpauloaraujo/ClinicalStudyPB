package UEPB.ESWII.ClinicalStudyPB.service;

/**
 * Violação de uma regra de negócio do recrutamento que o cliente pode explicar ao usuário
 * (HTTP 409). Leva um {@code codigo} estável para a tela traduzir a regra em uma mensagem.
 */
public class ConflitoRegraException extends RuntimeException {

    private final String codigo;

    public ConflitoRegraException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
