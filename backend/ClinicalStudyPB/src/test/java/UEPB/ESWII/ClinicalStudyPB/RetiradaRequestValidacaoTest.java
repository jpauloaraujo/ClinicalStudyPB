package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.RetiradaRequest;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SolicitanteRetirada;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/** T106 (RF16, RQ-T106-03): limites e obrigatoriedades do {@link RetiradaRequest}, sem Spring. */
class RetiradaRequestValidacaoTest {

    private static final LocalDate DATA = LocalDate.of(2026, 11, 18);

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

    private static Set<String> campos(RetiradaRequest pedido) {
        return validador.validate(pedido).stream().map(ConstraintViolation::getPropertyPath)
                .map(Object::toString).collect(Collectors.toSet());
    }

    @ParameterizedTest(name = "motivo com {0} caracteres: viola limite máximo = {1}")
    @CsvSource({ "5,false", "500,false", "501,true" })
    @DisplayName("O motivo aceita até 500 caracteres e recusa 501 no Bean Validation")
    void limitesDoMotivo(int tamanho, boolean invalido) {
        RetiradaRequest pedido = new RetiradaRequest("m".repeat(tamanho), SolicitanteRetirada.PARTICIPANTE, DATA);

        assertThat(campos(pedido).contains("motivo")).isEqualTo(invalido);
    }

    @Test
    @DisplayName("Motivo nulo, em branco ou curto passa pelo DTO para o serviço devolver MOTIVO_OBRIGATORIO na ordem certa")
    void motivoCurtoFicaParaOServico() {
        assertThat(campos(new RetiradaRequest(null, SolicitanteRetirada.PARTICIPANTE, DATA))).isEmpty();
        assertThat(campos(new RetiradaRequest("   ", SolicitanteRetirada.PARTICIPANTE, DATA))).isEmpty();
        assertThat(campos(new RetiradaRequest("abcd", SolicitanteRetirada.PARTICIPANTE, DATA))).isEmpty();
    }

    @Test
    @DisplayName("Solicitante e data da retirada ausentes são inválidos no DTO")
    void solicitanteEDataObrigatorios() {
        assertThat(campos(new RetiradaRequest("Mudança de cidade", null, DATA))).containsExactly("solicitante");
        assertThat(campos(new RetiradaRequest("Mudança de cidade", SolicitanteRetirada.COORDENACAO, null)))
                .containsExactly("dataRetirada");
    }

    @ParameterizedTest
    @EnumSource(SolicitanteRetirada.class)
    @DisplayName("Um pedido completo é aceito para qualquer solicitante válido")
    void pedidoValido(SolicitanteRetirada solicitante) {
        assertThat(validador.validate(new RetiradaRequest("Mudança de cidade", solicitante, DATA))).isEmpty();
    }
}
