package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.TipoDesfecho;
import UEPB.ESWII.ClinicalStudyPB.domain.regra.MaquinaEstadosVisita;
import UEPB.ESWII.ClinicalStudyPB.domain.regra.MaquinaEstadosVisita.DadosDesfecho;
import UEPB.ESWII.ClinicalStudyPB.service.ConflitoRegraException;
import UEPB.ESWII.ClinicalStudyPB.service.ValidacaoRegraException;

/**
 * T80 (RF13): regras da máquina de estados da visita agendada, isoladas de banco e de Spring (roda em
 * milissegundos). A matriz completa de regras do serviço é a T85.
 */
class MaquinaEstadosVisitaTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 16, 10, 0);

    /** As 16 combinações de origem e destino. */
    static Stream<Arguments> combinacoes() {
        return Stream.of(SituacaoVisita.values()).flatMap(origem -> Stream.of(SituacaoVisita.values())
                .map(destino -> Arguments.of(origem, destino, origem == SituacaoVisita.AGENDADA
                        && destino != SituacaoVisita.AGENDADA)));
    }

    @ParameterizedTest(name = "{0} -> {1}: permitida = {2}")
    @MethodSource("combinacoes")
    @DisplayName("Das 16 combinações de origem e destino, só as 3 que saem de AGENDADA são permitidas")
    void soAgendadaTemSaida(SituacaoVisita origem, SituacaoVisita destino, boolean permitida) {
        assertThat(MaquinaEstadosVisita.transicaoPermitida(origem, destino)).isEqualTo(permitida);
    }

    @Test
    @DisplayName("Existem exatamente 3 transições válidas")
    void existemTresTransicoesValidas() {
        assertThat(combinacoes().filter(argumentos -> (boolean) argumentos.get()[2]).count()).isEqualTo(3);
    }

    @Test
    @DisplayName("Origem ou destino nulo nunca é transição permitida")
    void nuloNaoEhPermitido() {
        assertThat(MaquinaEstadosVisita.transicaoPermitida(null, SituacaoVisita.REALIZADA)).isFalse();
        assertThat(MaquinaEstadosVisita.transicaoPermitida(SituacaoVisita.AGENDADA, null)).isFalse();
        assertThat(MaquinaEstadosVisita.transicaoPermitida(null, null)).isFalse();
    }

    @Test
    @DisplayName("proximas: os três destinos para AGENDADA e nenhum para os estados finais")
    void proximasDeCadaEstado() {
        assertThat(MaquinaEstadosVisita.proximas(SituacaoVisita.AGENDADA))
                .containsExactlyInAnyOrder(SituacaoVisita.REALIZADA, SituacaoVisita.AUSENTE, SituacaoVisita.CANCELADA);
        assertThat(MaquinaEstadosVisita.proximas(SituacaoVisita.REALIZADA)).isEmpty();
        assertThat(MaquinaEstadosVisita.proximas(SituacaoVisita.AUSENTE)).isEmpty();
        assertThat(MaquinaEstadosVisita.proximas(SituacaoVisita.CANCELADA)).isEmpty();
        assertThat(MaquinaEstadosVisita.proximas(null)).isEmpty();
    }

    @Test
    @DisplayName("estadoFinal: REALIZADA, AUSENTE e CANCELADA são finais; AGENDADA não")
    void estadosFinais() {
        assertThat(MaquinaEstadosVisita.estadoFinal(SituacaoVisita.REALIZADA)).isTrue();
        assertThat(MaquinaEstadosVisita.estadoFinal(SituacaoVisita.AUSENTE)).isTrue();
        assertThat(MaquinaEstadosVisita.estadoFinal(SituacaoVisita.CANCELADA)).isTrue();
        assertThat(MaquinaEstadosVisita.estadoFinal(SituacaoVisita.AGENDADA)).isFalse();
        assertThat(MaquinaEstadosVisita.estadoFinal(null)).isFalse();
    }

    @Test
    @DisplayName("destinoDe converte cada tipo de desfecho na situação de mesmo nome")
    void destinoDeCadaTipo() {
        assertThat(MaquinaEstadosVisita.destinoDe(TipoDesfecho.REALIZADA)).isEqualTo(SituacaoVisita.REALIZADA);
        assertThat(MaquinaEstadosVisita.destinoDe(TipoDesfecho.AUSENTE)).isEqualTo(SituacaoVisita.AUSENTE);
        assertThat(MaquinaEstadosVisita.destinoDe(TipoDesfecho.CANCELADA)).isEqualTo(SituacaoVisita.CANCELADA);
    }

    @ParameterizedTest(name = "AGENDADA aceita o desfecho {0}")
    @ValueSource(strings = {"REALIZADA", "AUSENTE", "CANCELADA"})
    @DisplayName("validarTransicao aceita os três desfechos a partir de AGENDADA")
    void agendadaAceitaOsTresDesfechos(TipoDesfecho tipo) {
        assertThatCode(() -> MaquinaEstadosVisita.validarTransicao(SituacaoVisita.AGENDADA, tipo))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validarTransicao recusa qualquer desfecho em visita já finalizada, com 409 e mensagem clara")
    void estadoFinalRecusaDesfecho() {
        for (SituacaoVisita situacaoFinal : new SituacaoVisita[] {SituacaoVisita.REALIZADA, SituacaoVisita.AUSENTE,
                SituacaoVisita.CANCELADA}) {
            for (TipoDesfecho tipo : TipoDesfecho.values()) {
                assertThatThrownBy(() -> MaquinaEstadosVisita.validarTransicao(situacaoFinal, tipo))
                        .isInstanceOfSatisfying(ConflitoRegraException.class, erro -> {
                            assertThat(erro.getCodigo()).isEqualTo("TRANSICAO_VISITA_INVALIDA");
                            assertThat(erro.getMessage()).contains("já foi registrada como",
                                    MaquinaEstadosVisita.rotulo(situacaoFinal), "não pode mais ser alterada");
                        });
            }
        }
    }

    @Test
    @DisplayName("A mensagem da realizada diz que ela não pode mais ser alterada")
    void mensagemDaRealizada() {
        assertThatThrownBy(() -> MaquinaEstadosVisita.validarTransicao(SituacaoVisita.REALIZADA, TipoDesfecho.CANCELADA))
                .hasMessage("A visita já foi registrada como realizada e não pode mais ser alterada");
    }

    @Test
    @DisplayName("validarTransicao com situação atual nula é recusada")
    void situacaoAtualNulaEhRecusada() {
        assertThatThrownBy(() -> MaquinaEstadosVisita.validarTransicao(null, TipoDesfecho.REALIZADA))
                .isInstanceOfSatisfying(ConflitoRegraException.class,
                        erro -> assertThat(erro.getCodigo()).isEqualTo(MaquinaEstadosVisita.TRANSICAO_INVALIDA));
    }

    @Test
    @DisplayName("Realizada com data e hora reais no futuro é recusada com 400 DATA_REALIZACAO_FUTURA")
    void realizadaComDataFuturaEhRecusada() {
        assertThatThrownBy(() -> MaquinaEstadosVisita.validarDados(TipoDesfecho.REALIZADA, AGORA.plusMinutes(1), null,
                null, AGORA))
                .isInstanceOfSatisfying(ValidacaoRegraException.class,
                        erro -> assertThat(erro.getCodigo()).isEqualTo("DATA_REALIZACAO_FUTURA"));
    }

    @Test
    @DisplayName("Realizada aceita data e hora de agora, do passado e a omissão da data")
    void realizadaAceitaAgoraPassadoEOmissao() {
        assertThatCode(() -> MaquinaEstadosVisita.validarDados(TipoDesfecho.REALIZADA, AGORA, null, null, AGORA))
                .doesNotThrowAnyException();
        assertThatCode(() -> MaquinaEstadosVisita.validarDados(TipoDesfecho.REALIZADA, AGORA.minusDays(3), null, null,
                AGORA)).doesNotThrowAnyException();
        assertThatCode(() -> MaquinaEstadosVisita.validarDados(TipoDesfecho.REALIZADA, null, null, null, AGORA))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Ausente não tem exigência de dados: a data futura enviada é ignorada")
    void ausenteNaoTemExigencia() {
        assertThatCode(() -> MaquinaEstadosVisita.validarDados(TipoDesfecho.AUSENTE, AGORA.plusDays(5), null, null,
                AGORA)).doesNotThrowAnyException();
        assertThatCode(() -> MaquinaEstadosVisita.validarDados(TipoDesfecho.AUSENTE, null, null, "Não atendeu",
                AGORA)).doesNotThrowAnyException();
    }

    @ParameterizedTest(name = "Cancelada com motivo [{0}] é recusada")
    @ValueSource(strings = {"", "   ", "abcd", "  abcd  ", "ab c"})
    @DisplayName("Cancelada sem motivo, em branco ou com menos de 5 caracteres depois do trim é recusada")
    void canceladaSemMotivoValidoEhRecusada(String motivo) {
        assertThatThrownBy(() -> MaquinaEstadosVisita.validarDados(TipoDesfecho.CANCELADA, null, motivo, null, AGORA))
                .isInstanceOfSatisfying(ValidacaoRegraException.class,
                        erro -> assertThat(erro.getCodigo()).isEqualTo("MOTIVO_OBRIGATORIO"));
    }

    @Test
    @DisplayName("Cancelada com motivo nulo é recusada")
    void canceladaComMotivoNuloEhRecusada() {
        assertThatThrownBy(() -> MaquinaEstadosVisita.validarDados(TipoDesfecho.CANCELADA, null, null, null, AGORA))
                .isInstanceOfSatisfying(ValidacaoRegraException.class,
                        erro -> assertThat(erro.getCodigo()).isEqualTo("MOTIVO_OBRIGATORIO"));
    }

    @Test
    @DisplayName("Cancelada com motivo de 5 caracteres é aceita, mesmo com espaços em volta")
    void canceladaComCincoCaracteresEhAceita() {
        assertThatCode(() -> MaquinaEstadosVisita.validarDados(TipoDesfecho.CANCELADA, null, "abcde", null, AGORA))
                .doesNotThrowAnyException();
        assertThatCode(() -> MaquinaEstadosVisita.validarDados(TipoDesfecho.CANCELADA, null, "  abcde  ", null, AGORA))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Realizada e ausente não exigem motivo")
    void realizadaEAusenteNaoExigemMotivo() {
        assertThatCode(() -> MaquinaEstadosVisita.validarDados(TipoDesfecho.REALIZADA, AGORA, "ab", null, AGORA))
                .doesNotThrowAnyException();
        assertThatCode(() -> MaquinaEstadosVisita.validarDados(TipoDesfecho.AUSENTE, null, "", null, AGORA))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Realizada grava a data informada ou, na omissão, o agora; ignora o motivo")
    void dadosDaRealizada() {
        DadosDesfecho informada = MaquinaEstadosVisita.dadosParaGravar(TipoDesfecho.REALIZADA, AGORA.minusHours(2),
                "motivo que não vale", "  Tudo certo  ", AGORA);
        assertThat(informada).isEqualTo(new DadosDesfecho(AGORA.minusHours(2), null, "Tudo certo"));

        DadosDesfecho omitida = MaquinaEstadosVisita.dadosParaGravar(TipoDesfecho.REALIZADA, null, null, null, AGORA);
        assertThat(omitida).isEqualTo(new DadosDesfecho(AGORA, null, null));
    }

    @Test
    @DisplayName("Ausente não grava data real nem motivo; guarda só a observação")
    void dadosDaAusente() {
        DadosDesfecho dados = MaquinaEstadosVisita.dadosParaGravar(TipoDesfecho.AUSENTE, AGORA, "ignorado",
                "Não compareceu", AGORA);

        assertThat(dados).isEqualTo(new DadosDesfecho(null, null, "Não compareceu"));
    }

    @Test
    @DisplayName("Cancelada grava o motivo aparado e a observação; não grava data real")
    void dadosDaCancelada() {
        DadosDesfecho dados = MaquinaEstadosVisita.dadosParaGravar(TipoDesfecho.CANCELADA, AGORA, "  Viagem  ", "   ",
                AGORA);

        assertThat(dados).isEqualTo(new DadosDesfecho(null, "Viagem", null));
    }
}
