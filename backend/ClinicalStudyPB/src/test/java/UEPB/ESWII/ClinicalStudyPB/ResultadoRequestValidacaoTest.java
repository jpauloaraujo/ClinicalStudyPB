package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.ResultadoRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * T92 (RF14, RQ-T92-09): limites do {@link ResultadoRequest}, sem Spring. Os tamanhos máximos são do Bean Validation
 * (400 {@code VALIDACAO_CAMPOS}); o valor em branco ou ausente passa aqui de propósito, porque quem o recusa é o
 * serviço, com o código próprio {@code VALOR_OBRIGATORIO} (contrato da T88).
 */
class ResultadoRequestValidacaoTest {

    private static ValidatorFactory fabrica;
    private static Validator validador;

    @BeforeAll
    static void criarValidador() {
        fabrica = Validation.buildDefaultValidatorFactory();
        validador = fabrica.getValidator();
    }

    @AfterAll
    static void fechar() {
        fabrica.close();
    }

    private static Set<String> camposInvalidos(ResultadoRequest pedido) {
        return validador.validate(pedido).stream().map(ConstraintViolation::getPropertyPath)
                .map(Object::toString).collect(Collectors.toSet());
    }

    private static String texto(int tamanho) {
        return "x".repeat(tamanho);
    }

    @Test
    @DisplayName("Resultado completo, ou só com o valor, é válido")
    void resultadoValido() {
        assertThat(camposInvalidos(new ResultadoRequest("120/80", "mmHg", "Sentado, após 5 minutos"))).isEmpty();
        assertThat(camposInvalidos(new ResultadoRequest("Normal", null, null))).isEmpty();
    }

    @Test
    @DisplayName("Valor: 1 e 200 caracteres são aceitos; 201 é recusado")
    void limitesDoValor() {
        assertThat(camposInvalidos(new ResultadoRequest(texto(1), null, null))).isEmpty();
        assertThat(camposInvalidos(new ResultadoRequest(texto(200), null, null))).isEmpty();
        assertThat(camposInvalidos(new ResultadoRequest(texto(201), null, null))).containsExactly("valor");
    }

    @Test
    @DisplayName("Unidade: 30 caracteres são aceitos; 31 é recusado")
    void limitesDaUnidade() {
        assertThat(camposInvalidos(new ResultadoRequest("1", texto(30), null))).isEmpty();
        assertThat(camposInvalidos(new ResultadoRequest("1", texto(31), null))).containsExactly("unidade");
    }

    @Test
    @DisplayName("Observação: 500 caracteres são aceitos; 501 é recusado")
    void limitesDaObservacao() {
        assertThat(camposInvalidos(new ResultadoRequest("1", null, texto(500)))).isEmpty();
        assertThat(camposInvalidos(new ResultadoRequest("1", null, texto(501)))).containsExactly("observacao");
    }

    @Test
    @DisplayName("Os três campos acima do limite são apontados juntos")
    void todosAcimaDoLimite() {
        assertThat(camposInvalidos(new ResultadoRequest(texto(201), texto(31), texto(501))))
                .containsExactlyInAnyOrder("valor", "unidade", "observacao");
    }

    @Test
    @DisplayName("Valor nulo, vazio ou só com espaços passa no corpo: o serviço responde VALOR_OBRIGATORIO")
    void valorEmBrancoFicaParaOServico() {
        assertThat(camposInvalidos(new ResultadoRequest(null, null, null))).isEmpty();
        assertThat(camposInvalidos(new ResultadoRequest("", null, null))).isEmpty();
        assertThat(camposInvalidos(new ResultadoRequest("   ", null, null))).isEmpty();
    }
}
