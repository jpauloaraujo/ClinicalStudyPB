package UEPB.ESWII.ClinicalStudyPB.domain.regra;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.TipoDesfecho;
import UEPB.ESWII.ClinicalStudyPB.service.ConflitoRegraException;
import UEPB.ESWII.ClinicalStudyPB.service.ValidacaoRegraException;

/**
 * Máquina de estados da visita agendada (RF13), como regra pura de domínio: sem banco, sem Spring Web, sem
 * {@code Clock} implícito (o "agora" chega por parâmetro).
 *
 * <p>A visita nasce {@code AGENDADA} e só pode virar {@code REALIZADA}, {@code AUSENTE} ou {@code CANCELADA};
 * esses três estados são finais e não há como desfazê-los. Das 16 combinações de origem e destino só 3 são
 * válidas. A retirada de participante (T102) usa esta mesma máquina para cancelar as visitas futuras.</p>
 *
 * <p>Cada violação de transição é uma {@link ConflitoRegraException} (HTTP 409) e cada dado inválido do desfecho
 * é uma {@link ValidacaoRegraException} (HTTP 400), ambas com um código estável para a tela explicar o motivo.
 * Quem consulta o banco, confere o estudo e grava o desfecho é o {@code DesfechoVisitaService}; esta classe só
 * decide.</p>
 */
public final class MaquinaEstadosVisita {

    public static final String TRANSICAO_INVALIDA = "TRANSICAO_VISITA_INVALIDA";
    public static final String DATA_REALIZACAO_FUTURA = "DATA_REALIZACAO_FUTURA";
    public static final String MOTIVO_OBRIGATORIO = "MOTIVO_OBRIGATORIO";

    /** Tamanho mínimo do motivo do cancelamento, depois do {@code trim}. */
    public static final int MOTIVO_TAMANHO_MINIMO = 5;

    /**
     * Dados que de fato são gravados para um desfecho, depois de descartados os campos que não pertencem ao
     * tipo escolhido (ex.: {@code motivo} enviado em uma visita realizada não é gravado).
     *
     * @param dataHoraRealizada data e hora reais; só preenchida na visita realizada
     * @param motivo            só preenchido na visita cancelada
     * @param observacao        opcional nos três tipos; em branco vira {@code null}
     */
    public record DadosDesfecho(LocalDateTime dataHoraRealizada, String motivo, String observacao) {
    }

    private MaquinaEstadosVisita() {
    }

    /**
     * Se a mudança de {@code origem} para {@code destino} é permitida: só {@code AGENDADA} para {@code REALIZADA},
     * {@code AUSENTE} ou {@code CANCELADA}. {@code null} em qualquer lado nunca é permitido.
     */
    public static boolean transicaoPermitida(SituacaoVisita origem, SituacaoVisita destino) {
        return origem == SituacaoVisita.AGENDADA && destino != null && destino != SituacaoVisita.AGENDADA;
    }

    /** Os destinos permitidos a partir de {@code origem}, para a tela saber o que oferecer (vazio nos finais). */
    public static Set<SituacaoVisita> proximas(SituacaoVisita origem) {
        if (origem != SituacaoVisita.AGENDADA) {
            return Set.of();
        }
        return EnumSet.of(SituacaoVisita.REALIZADA, SituacaoVisita.AUSENTE, SituacaoVisita.CANCELADA);
    }

    /** Verdadeiro para {@code REALIZADA}, {@code AUSENTE} e {@code CANCELADA}: depois deles não há volta. */
    public static boolean estadoFinal(SituacaoVisita situacao) {
        return situacao != null && situacao != SituacaoVisita.AGENDADA;
    }

    /** A situação em que a visita fica depois de registrado o desfecho do tipo informado. */
    public static SituacaoVisita destinoDe(TipoDesfecho tipo) {
        return switch (tipo) {
            case REALIZADA -> SituacaoVisita.REALIZADA;
            case AUSENTE -> SituacaoVisita.AUSENTE;
            case CANCELADA -> SituacaoVisita.CANCELADA;
        };
    }

    /**
     * Confere se uma visita na situação {@code atual} pode receber o desfecho {@code tipo}.
     *
     * @throws ConflitoRegraException (409 {@code TRANSICAO_VISITA_INVALIDA}) quando a visita não está agendada
     * @throws NullPointerException   se {@code tipo} for nulo (o controller já exige o tipo)
     */
    public static void validarTransicao(SituacaoVisita atual, TipoDesfecho tipo) {
        SituacaoVisita destino = destinoDe(Objects.requireNonNull(tipo, "tipo do desfecho"));
        if (!transicaoPermitida(atual, destino)) {
            throw new ConflitoRegraException(TRANSICAO_INVALIDA, mensagemTransicaoInvalida(atual));
        }
    }

    /**
     * Confere os dados exigidos por cada desfecho: a visita realizada não pode ter data e hora reais futuras e a
     * cancelada exige motivo com ao menos {@value #MOTIVO_TAMANHO_MINIMO} caracteres depois do {@code trim}. A
     * visita ausente e a observação não têm exigência. Os tamanhos máximos (500 caracteres) são do DTO.
     *
     * @param dataHoraRealizada data e hora reais, ou {@code null} (nesse caso a realizada assume {@code agora})
     * @param agora             o instante de referência, vindo de quem chama
     * @throws ValidacaoRegraException (400 {@code DATA_REALIZACAO_FUTURA} ou {@code MOTIVO_OBRIGATORIO})
     */
    public static void validarDados(TipoDesfecho tipo, LocalDateTime dataHoraRealizada, String motivo,
            String observacao, LocalDateTime agora) {
        if (tipo == TipoDesfecho.REALIZADA && dataHoraRealizada != null && dataHoraRealizada.isAfter(agora)) {
            throw new ValidacaoRegraException(DATA_REALIZACAO_FUTURA,
                    "A data e a hora em que a visita foi realizada não podem estar no futuro");
        }
        if (tipo == TipoDesfecho.CANCELADA && (motivo == null || motivo.trim().length() < MOTIVO_TAMANHO_MINIMO)) {
            throw new ValidacaoRegraException(MOTIVO_OBRIGATORIO,
                    "Informe o motivo do cancelamento, com ao menos " + MOTIVO_TAMANHO_MINIMO + " caracteres");
        }
    }

    /**
     * Os dados que devem ser gravados para o desfecho: a realizada guarda a data e hora reais (a informada ou
     * {@code agora}); a cancelada guarda o motivo; a observação vale nos três tipos. Os campos que não pertencem
     * ao tipo são ignorados, sem erro. Texto em branco vira {@code null} e o restante é aparado com {@code trim}.
     *
     * <p>Não valida: chame antes {@link #validarDados}.</p>
     */
    public static DadosDesfecho dadosParaGravar(TipoDesfecho tipo, LocalDateTime dataHoraRealizada, String motivo,
            String observacao, LocalDateTime agora) {
        LocalDateTime dataReal = tipo == TipoDesfecho.REALIZADA
                ? (dataHoraRealizada == null ? agora : dataHoraRealizada)
                : null;
        String motivoGravado = tipo == TipoDesfecho.CANCELADA ? textoOuNulo(motivo) : null;
        return new DadosDesfecho(dataReal, motivoGravado, textoOuNulo(observacao));
    }

    /** Nome da situação como aparece nas mensagens ao usuário. */
    public static String rotulo(SituacaoVisita situacao) {
        return switch (situacao) {
            case AGENDADA -> "agendada";
            case REALIZADA -> "realizada";
            case AUSENTE -> "ausente";
            case CANCELADA -> "cancelada";
        };
    }

    private static String mensagemTransicaoInvalida(SituacaoVisita atual) {
        if (atual == null) {
            return "Informe a situação atual da visita";
        }
        return "A visita já foi registrada como " + rotulo(atual) + " e não pode mais ser alterada";
    }

    private static String textoOuNulo(String texto) {
        if (texto == null) {
            return null;
        }
        String aparado = texto.trim();
        return aparado.isEmpty() ? null : aparado;
    }
}
