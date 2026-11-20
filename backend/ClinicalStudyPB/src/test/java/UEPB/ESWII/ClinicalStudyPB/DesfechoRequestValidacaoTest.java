package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.DesfechoRequest;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.TipoDesfecho;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/** T85 (RF13): limites do {@link DesfechoRequest}, sem Spring. As regras de motivo e de data são da máquina (T80). */
class DesfechoRequestValidacaoTest {

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

    private static Set<String> campos(DesfechoRequest pedido) {
        return validador.validate(pedido).stream().map(ConstraintViolation::getPropertyPath)
                .map(Object::toString).collect(Collectors.toSet());
    }

    @Test
    @DisplayName("O tipo é obrigatório")
    void tipoObrigatorio() {
        assertThat(campos(new DesfechoRequest(null, null, null, null))).containsExactly("tipo");
    }

    @Test
    @DisplayName("Só o tipo basta: o pedido de cada tipo sem os demais campos é válido para o Bean Validation")
    void soOTipoBasta() {
        for (TipoDesfecho tipo : TipoDesfecho.values()) {
            assertThat(campos(new DesfechoRequest(tipo, null, null, null))).isEmpty();
        }
    }

    @ParameterizedTest(name = "observação com {0} caracteres: inválida = {1}")
    @CsvSource({ "1,false", "500,false", "501,true" })
    @DisplayName("A observação aceita até 500 caracteres")
    void limitesDaObservacao(int tamanho, boolean invalido) {
        DesfechoRequest pedido = new DesfechoRequest(TipoDesfecho.AUSENTE, null, null, "o".repeat(tamanho));

        assertThat(campos(pedido).contains("observacao")).isEqualTo(invalido);
    }

    @ParameterizedTest(name = "motivo com {0} caracteres: inválido = {1}")
    @CsvSource({ "1,false", "500,false", "501,true" })
    @DisplayName("O motivo aceita até 500 caracteres (o mínimo de 5 é da máquina de estados)")
    void limitesDoMotivo(int tamanho, boolean invalido) {
        DesfechoRequest pedido = new DesfechoRequest(TipoDesfecho.CANCELADA, null, "m".repeat(tamanho), null);

        assertThat(campos(pedido).contains("motivo")).isEqualTo(invalido);
    }

    @Test
    @DisplayName("A data e hora reais não têm limite no DTO: a regra da data futura é da máquina de estados")
    void dataNaoTemLimiteNoDto() {
        assertThat(campos(new DesfechoRequest(TipoDesfecho.REALIZADA, LocalDateTime.now().plusYears(1), null, null)))
                .isEmpty();
    }
}
