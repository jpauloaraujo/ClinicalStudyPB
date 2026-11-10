package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.AvaliacaoRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/** T56 (RF09): validação do {@link AvaliacaoRequest} e das respostas, sem Spring. */
class AvaliacaoRequestValidacaoTest {

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

    private static Set<String> camposInvalidos(AvaliacaoRequest pedido) {
        return validador.validate(pedido).stream().map(ConstraintViolation::getPropertyPath)
                .map(Object::toString).collect(Collectors.toSet());
    }

    private static AvaliacaoRequest com(AvaliacaoRequest.Resposta... respostas) {
        return new AvaliacaoRequest(List.of(respostas));
    }

    @Test
    @DisplayName("Resposta completa, com e sem observação, é válida")
    void respostaValida() {
        assertThat(camposInvalidos(com(new AvaliacaoRequest.Resposta(UUID.randomUUID(), true, null),
                new AvaliacaoRequest.Resposta(UUID.randomUUID(), false, "Justificativa do avaliador")))).isEmpty();
    }

    @Test
    @DisplayName("A lista de respostas é obrigatória (null é inválido)")
    void respostasObrigatorias() {
        assertThat(camposInvalidos(new AvaliacaoRequest(null))).containsExactly("respostas");
    }

    @Test
    @DisplayName("Lista vazia passa na validação do corpo; quem recusa é o serviço, que exige uma resposta por critério")
    void listaVaziaPassaNoCorpo() {
        assertThat(camposInvalidos(new AvaliacaoRequest(List.of()))).isEmpty();
    }

    @Test
    @DisplayName("O critério da resposta é obrigatório")
    void criterioObrigatorio() {
        assertThat(camposInvalidos(com(new AvaliacaoRequest.Resposta(null, true, null))))
                .containsExactly("respostas[0].criterioId");
    }

    @Test
    @DisplayName("Informar se o critério se aplica é obrigatório")
    void aplicaObrigatorio() {
        assertThat(camposInvalidos(com(new AvaliacaoRequest.Resposta(UUID.randomUUID(), null, null))))
                .containsExactly("respostas[0].aplica");
    }

    @Test
    @DisplayName("Observação de até 500 caracteres é aceita; 501 é recusada")
    void limiteDaObservacao() {
        assertThat(camposInvalidos(com(new AvaliacaoRequest.Resposta(UUID.randomUUID(), true, "o".repeat(500)))))
                .isEmpty();
        assertThat(camposInvalidos(com(new AvaliacaoRequest.Resposta(UUID.randomUUID(), true, "o".repeat(501)))))
                .containsExactly("respostas[0].observacao");
    }

    @Test
    @DisplayName("Os erros apontam o índice da resposta que falhou")
    void indiceDaRespostaInvalida() {
        var pedido = com(new AvaliacaoRequest.Resposta(UUID.randomUUID(), true, null),
                new AvaliacaoRequest.Resposta(null, null, null));

        assertThat(camposInvalidos(pedido)).containsExactlyInAnyOrder("respostas[1].criterioId", "respostas[1].aplica");
    }
}
