package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.VisitaAgendadaRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.VisitaReagendamentoRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/** T78 (RF12, RQ-T78-08): obrigatoriedade dos campos dos pedidos de agendamento e de reagendamento, sem Spring. */
class AgendamentoRequestValidacaoTest {

    private static final LocalDateTime DATA = LocalDateTime.of(2026, 11, 19, 9, 30);

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

    private static Set<String> campos(Object pedido) {
        return validador.validate(pedido).stream().map(ConstraintViolation::getPropertyPath)
                .map(Object::toString).collect(Collectors.toSet());
    }

    @Test
    @DisplayName("Agendar: pedido completo é válido")
    void pedidoCompletoEhValido() {
        assertThat(campos(new VisitaAgendadaRequest(UUID.randomUUID(), DATA))).isEmpty();
    }

    @Test
    @DisplayName("Agendar: a visita prevista e a data e hora são obrigatórias")
    void camposObrigatoriosDoAgendamento() {
        assertThat(campos(new VisitaAgendadaRequest(null, DATA))).containsExactly("visitaPlanejadaId");
        assertThat(campos(new VisitaAgendadaRequest(UUID.randomUUID(), null))).containsExactly("dataHora");
        assertThat(campos(new VisitaAgendadaRequest(null, null))).containsExactlyInAnyOrder("visitaPlanejadaId",
                "dataHora");
    }

    @Test
    @DisplayName("Reagendar: só a data e hora, e é obrigatória")
    void camposDoReagendamento() {
        assertThat(campos(new VisitaReagendamentoRequest(DATA))).isEmpty();
        assertThat(campos(new VisitaReagendamentoRequest(null))).containsExactly("dataHora");
    }
}
