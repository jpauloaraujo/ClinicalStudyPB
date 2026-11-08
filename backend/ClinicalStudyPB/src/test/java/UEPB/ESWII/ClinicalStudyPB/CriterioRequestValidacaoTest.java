package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.CriterioRequest;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.TipoCriterio;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/** T47 (RF05): limites de descrição, título, tipo e peso do {@link CriterioRequest}, sem Spring. */
class CriterioRequestValidacaoTest {

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

    private static Set<String> camposInvalidos(CriterioRequest pedido) {
        return validador.validate(pedido).stream().map(ConstraintViolation::getPropertyPath)
                .map(Object::toString).collect(java.util.stream.Collectors.toSet());
    }

    private static CriterioRequest comDescricao(int tamanho) {
        return new CriterioRequest(TipoCriterio.INCLUSAO, "Idade", "d".repeat(tamanho), null, null);
    }

    @ParameterizedTest(name = "descrição com {0} caracteres: inválida = {1}")
    @CsvSource({ "1,true", "4,true", "5,false", "6,false", "499,false", "500,false", "501,true", "1000,true" })
    @DisplayName("A descrição aceita de 5 a 500 caracteres (limites exatos incluídos)")
    void limitesDaDescricao(int tamanho, boolean invalida) {
        assertThat(camposInvalidos(comDescricao(tamanho)).contains("descricao")).isEqualTo(invalida);
    }

    @Test
    @DisplayName("Descrição ausente ou só com espaços é inválida")
    void descricaoAusenteOuEmBranco() {
        assertThat(camposInvalidos(new CriterioRequest(TipoCriterio.INCLUSAO, "Idade", null, null, null))).contains("descricao");
        assertThat(camposInvalidos(new CriterioRequest(TipoCriterio.INCLUSAO, "Idade", "      ", null, null))).contains("descricao");
    }

    @Test
    @DisplayName("Tipo ausente é inválido")
    void tipoAusente() {
        assertThat(camposInvalidos(new CriterioRequest(null, "Idade", "Maior de 18 anos", null, null))).containsExactly("tipo");
    }

    @ParameterizedTest(name = "título com {0} caracteres: inválido = {1}")
    @CsvSource({ "1,false", "120,false", "121,true" })
    @DisplayName("O título aceita até 120 caracteres")
    void limitesDoTitulo(int tamanho, boolean invalido) {
        CriterioRequest pedido = new CriterioRequest(TipoCriterio.INCLUSAO, "t".repeat(tamanho), "Maior de 18 anos", null, null);
        assertThat(camposInvalidos(pedido).contains("titulo")).isEqualTo(invalido);
    }

    @Test
    @DisplayName("Título opcional pode ser derivado da descrição pelo serviço")
    void tituloAusente() {
        assertThat(camposInvalidos(new CriterioRequest(TipoCriterio.INCLUSAO, null, "Maior de 18 anos", null, null))).isEmpty();
        assertThat(camposInvalidos(new CriterioRequest(TipoCriterio.INCLUSAO, "   ", "Maior de 18 anos", null, null))).isEmpty();
    }

    @ParameterizedTest(name = "peso {0}: inválido = {1}")
    @CsvSource({ "0.0,true", "-1.0,true", "0.1,false", "2.5,false" })
    @DisplayName("O peso, quando informado, precisa ser maior que zero")
    void limitesDoPeso(float peso, boolean invalido) {
        CriterioRequest pedido = new CriterioRequest(TipoCriterio.INCLUSAO, "Idade", "Maior de 18 anos", peso, null);
        assertThat(camposInvalidos(pedido).contains("peso")).isEqualTo(invalido);
    }

    @Test
    @DisplayName("Um pedido completo e correto não tem violações")
    void pedidoValido() {
        assertThat(validador.validate(new CriterioRequest(TipoCriterio.EXCLUSAO, "Gestante", "Em gestação", null, null))).isEmpty();
    }
}
