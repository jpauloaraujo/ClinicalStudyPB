package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.regra.MaquinaEstadosEstudo;
import UEPB.ESWII.ClinicalStudyPB.service.ConflitoRegraException;

/** T41 (RF07): regras da máquina de estados do estudo, isoladas de banco e Spring. */
class MaquinaEstadosEstudoTest {

    private static final StatusEstudo PLANEJAMENTO = StatusEstudo.PLANEJAMENTO;
    private static final StatusEstudo RECRUTAMENTO = StatusEstudo.RECRUTAMENTO;
    private static final StatusEstudo EXECUCAO = StatusEstudo.EXECUCAO;
    private static final StatusEstudo ENCERRADO = StatusEstudo.ENCERRADO;

    /** As 16 combinações de origem e destino, com um critério de inclusão e uma visita (pré-condições atendidas). */
    static Stream<Arguments> combinacoes() {
        return Stream.of(StatusEstudo.values()).flatMap(origem -> Stream.of(StatusEstudo.values())
                .map(destino -> Arguments.of(origem, destino, ehAvancoDeUm(origem, destino))));
    }

    private static boolean ehAvancoDeUm(StatusEstudo origem, StatusEstudo destino) {
        return destino.ordinal() == origem.ordinal() + 1;
    }

    @ParameterizedTest(name = "{0} -> {1}: permitida = {2}")
    @MethodSource("combinacoes")
    @DisplayName("Das 16 combinações de origem e destino, só as 3 que avançam um estado são permitidas")
    void soAvancaUmEstadoPorVez(StatusEstudo origem, StatusEstudo destino, boolean permitida) {
        assertThat(MaquinaEstadosEstudo.transicaoPermitida(origem, destino)).isEqualTo(permitida);

        if (permitida) {
            assertThatCode(() -> MaquinaEstadosEstudo.validar(origem, destino, 1, 1)).doesNotThrowAnyException();
        } else {
            assertThatThrownBy(() -> MaquinaEstadosEstudo.validar(origem, destino, 1, 1))
                    .isInstanceOfSatisfying(ConflitoRegraException.class,
                            erro -> assertThat(erro.getCodigo()).isEqualTo(MaquinaEstadosEstudo.TRANSICAO_INVALIDA));
        }
    }

    @Test
    @DisplayName("Existem exatamente 3 transições válidas")
    void existemTresTransicoesValidas() {
        long validas = combinacoes().filter(argumentos -> (boolean) argumentos.get()[2]).count();

        assertThat(validas).isEqualTo(3);
    }

    @Test
    @DisplayName("O próximo estado de cada um segue a sequência; o encerrado não tem próximo")
    void proximoEstadoSegueASequencia() {
        assertThat(MaquinaEstadosEstudo.proximoEstado(PLANEJAMENTO)).contains(RECRUTAMENTO);
        assertThat(MaquinaEstadosEstudo.proximoEstado(RECRUTAMENTO)).contains(EXECUCAO);
        assertThat(MaquinaEstadosEstudo.proximoEstado(EXECUCAO)).contains(ENCERRADO);
        assertThat(MaquinaEstadosEstudo.proximoEstado(ENCERRADO)).isEmpty();
        assertThat(MaquinaEstadosEstudo.proximoEstado(null)).isEmpty();
    }

    @Test
    @DisplayName("Pular um estado é recusado com mensagem que diz para onde se pode ir")
    void pularEstadoEhRecusado() {
        assertThatThrownBy(() -> MaquinaEstadosEstudo.validar(PLANEJAMENTO, EXECUCAO, 1, 1))
                .isInstanceOfSatisfying(ConflitoRegraException.class, erro -> {
                    assertThat(erro.getCodigo()).isEqualTo("TRANSICAO_ESTADO_INVALIDA");
                    assertThat(erro.getMessage()).contains("planejamento", "execução", "recrutamento");
                });
    }

    @Test
    @DisplayName("Voltar para um estado anterior é recusado")
    void voltarEhRecusado() {
        assertThatThrownBy(() -> MaquinaEstadosEstudo.validar(RECRUTAMENTO, PLANEJAMENTO, 1, 1))
                .isInstanceOfSatisfying(ConflitoRegraException.class,
                        erro -> assertThat(erro.getCodigo()).isEqualTo(MaquinaEstadosEstudo.TRANSICAO_INVALIDA));
    }

    @Test
    @DisplayName("Mudar para o mesmo estado e sair de encerrado são recusados")
    void mesmoEstadoEEncerradoSaoRecusados() {
        assertThatThrownBy(() -> MaquinaEstadosEstudo.validar(RECRUTAMENTO, RECRUTAMENTO, 1, 1))
                .hasMessage("O estudo já está em recrutamento");
        assertThatThrownBy(() -> MaquinaEstadosEstudo.validar(ENCERRADO, PLANEJAMENTO, 1, 1))
                .hasMessage("O estudo encerrado não pode mudar de estado");
    }

    @Test
    @DisplayName("Origem ou destino ausente é transição inválida, não erro de programa")
    void estadoAusenteEhTransicaoInvalida() {
        assertThatThrownBy(() -> MaquinaEstadosEstudo.validar(null, RECRUTAMENTO, 1, 1))
                .isInstanceOf(ConflitoRegraException.class);
        assertThatThrownBy(() -> MaquinaEstadosEstudo.validar(PLANEJAMENTO, null, 1, 1))
                .isInstanceOf(ConflitoRegraException.class);
    }

    @Test
    @DisplayName("Sair de planejamento sem critério de inclusão é recusado")
    void saidaDePlanejamentoExigeCriterioDeInclusao() {
        assertThatThrownBy(() -> MaquinaEstadosEstudo.validar(PLANEJAMENTO, RECRUTAMENTO, 0, 3))
                .isInstanceOfSatisfying(ConflitoRegraException.class, erro -> {
                    assertThat(erro.getCodigo()).isEqualTo(MaquinaEstadosEstudo.SEM_CRITERIO_INCLUSAO);
                    assertThat(erro.getMessage()).contains("critério de inclusão");
                });
    }

    @Test
    @DisplayName("Sair de planejamento sem visita prevista é recusado")
    void saidaDePlanejamentoExigeVisitaPrevista() {
        assertThatThrownBy(() -> MaquinaEstadosEstudo.validar(PLANEJAMENTO, RECRUTAMENTO, 2, 0))
                .isInstanceOfSatisfying(ConflitoRegraException.class, erro -> {
                    assertThat(erro.getCodigo()).isEqualTo(MaquinaEstadosEstudo.SEM_VISITA_PREVISTA);
                    assertThat(erro.getMessage()).contains("visita prevista");
                });
    }

    @Test
    @DisplayName("Sem critério e sem visita, o primeiro motivo informado é o critério de inclusão")
    void semCriterioESemVisitaInformaOCriterioPrimeiro() {
        assertThatThrownBy(() -> MaquinaEstadosEstudo.validar(PLANEJAMENTO, RECRUTAMENTO, 0, 0))
                .isInstanceOfSatisfying(ConflitoRegraException.class,
                        erro -> assertThat(erro.getCodigo()).isEqualTo(MaquinaEstadosEstudo.SEM_CRITERIO_INCLUSAO));
    }

    @Test
    @DisplayName("As pré-condições só valem na saída de planejamento")
    void preCondicoesSoValemNaSaidaDePlanejamento() {
        assertThatCode(() -> MaquinaEstadosEstudo.validar(RECRUTAMENTO, EXECUCAO, 0, 0)).doesNotThrowAnyException();
        assertThatCode(() -> MaquinaEstadosEstudo.validar(EXECUCAO, ENCERRADO, 0, 0)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Uma transição fora da sequência é recusada antes de olhar as pré-condições")
    void sequenciaVemAntesDasPreCondicoes() {
        assertThatThrownBy(() -> MaquinaEstadosEstudo.validar(PLANEJAMENTO, ENCERRADO, 0, 0))
                .isInstanceOfSatisfying(ConflitoRegraException.class,
                        erro -> assertThat(erro.getCodigo()).isEqualTo(MaquinaEstadosEstudo.TRANSICAO_INVALIDA));
    }
}
