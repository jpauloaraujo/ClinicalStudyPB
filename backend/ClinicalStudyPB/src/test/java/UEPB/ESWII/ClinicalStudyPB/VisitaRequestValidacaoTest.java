package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.AcaoRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.VisitaRequest;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.CategoriaAcao;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/** T50 (RF06): limites do {@link VisitaRequest} e do {@link AcaoRequest}, sem Spring. */
class VisitaRequestValidacaoTest {

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

    private static <T> Set<String> campos(T pedido) {
        return validador.validate(pedido).stream().map(ConstraintViolation::getPropertyPath)
                .map(Object::toString).collect(Collectors.toSet());
    }

    @ParameterizedTest(name = "nome com {0} caracteres: inválido = {1}")
    @CsvSource({ "1,false", "100,false", "101,true" })
    @DisplayName("O nome da visita aceita até 100 caracteres")
    void limitesDoNome(int tamanho, boolean invalido) {
        assertThat(campos(new VisitaRequest("n".repeat(tamanho), 0, 0)).contains("nome")).isEqualTo(invalido);
    }

    @Test
    @DisplayName("Nome ausente ou em branco é inválido")
    void nomeAusente() {
        assertThat(campos(new VisitaRequest(null, 0, 0))).contains("nome");
        assertThat(campos(new VisitaRequest("   ", 0, 0))).contains("nome");
    }

    @ParameterizedTest(name = "dia previsto {0}: inválido = {1}")
    @CsvSource({ "-1,true", "0,false", "1,false", "365,false" })
    @DisplayName("O dia previsto precisa ser maior ou igual a zero")
    void limitesDoDia(int dia, boolean invalido) {
        assertThat(campos(new VisitaRequest("Triagem", dia, 0)).contains("diaPrevisto")).isEqualTo(invalido);
    }

    @Test
    @DisplayName("Dia previsto e tolerância ausentes são inválidos")
    void diaEToleranciaAusentes() {
        assertThat(campos(new VisitaRequest("Triagem", null, 0))).contains("diaPrevisto");
        assertThat(campos(new VisitaRequest("Triagem", 0, null))).contains("tolerancia");
    }

    @ParameterizedTest(name = "tolerância {0}: inválida = {1}")
    @CsvSource({ "-1,true", "0,false", "30,false", "31,true" })
    @DisplayName("A janela de tolerância vai de 0 a 30 dias")
    void limitesDaTolerancia(int tolerancia, boolean invalida) {
        assertThat(campos(new VisitaRequest("Triagem", 0, tolerancia)).contains("tolerancia")).isEqualTo(invalida);
    }

    @Test
    @DisplayName("Uma visita correta não tem violações")
    void visitaValida() {
        assertThat(validador.validate(new VisitaRequest("Triagem", 0, 3))).isEmpty();
    }

    @ParameterizedTest(name = "descrição da ação com {0} caracteres: inválida = {1}")
    @CsvSource({ "0,false", "500,false", "501,true" })
    @DisplayName("A descrição da ação é opcional e aceita até 500 caracteres")
    void limitesDaDescricaoDaAcao(int tamanho, boolean invalida) {
        AcaoRequest pedido = new AcaoRequest("Hemograma", "d".repeat(tamanho), CategoriaAcao.EXAME_LABORATORIAL);
        assertThat(campos(pedido).contains("descricao")).isEqualTo(invalida);
    }

    @Test
    @DisplayName("Ação sem nome, com nome longo ou sem categoria é inválida")
    void acaoInvalida() {
        assertThat(campos(new AcaoRequest(null, null, CategoriaAcao.OUTROS))).contains("nome");
        assertThat(campos(new AcaoRequest("n".repeat(101), null, CategoriaAcao.OUTROS))).contains("nome");
        assertThat(campos(new AcaoRequest("Exame", null, null))).containsExactly("categoria");
    }

    @Test
    @DisplayName("Todas as categorias de ação são aceitas")
    void categoriasAceitas() {
        for (CategoriaAcao categoria : CategoriaAcao.values()) {
            assertThat(validador.validate(new AcaoRequest("Atividade", null, categoria))).isEmpty();
        }
    }
}
