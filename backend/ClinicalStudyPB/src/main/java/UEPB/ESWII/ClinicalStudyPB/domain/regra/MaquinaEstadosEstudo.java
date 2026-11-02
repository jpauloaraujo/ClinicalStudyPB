package UEPB.ESWII.ClinicalStudyPB.domain.regra;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.service.ConflitoRegraException;

/**
 * Máquina de estados do estudo (RF07), como regra pura de domínio: sem banco, sem Spring Web.
 *
 * <p>Os estados só avançam, um de cada vez: {@code PLANEJAMENTO -> RECRUTAMENTO -> EXECUCAO ->
 * ENCERRADO}. Não se pula, não se volta e não se "muda" para o mesmo estado; das 16 combinações de
 * origem e destino só 3 são válidas. Para sair de planejamento o estudo precisa ter ao menos um
 * critério de inclusão e ao menos uma visita prevista.</p>
 *
 * <p>Cada violação é uma {@link ConflitoRegraException} (HTTP 409) com um código próprio, para a
 * tela explicar o motivo. Quem consulta o banco (contagens) e grava o histórico é o
 * {@code EstudoTransicaoService}; esta classe só decide.</p>
 */
public final class MaquinaEstadosEstudo {

    public static final String TRANSICAO_INVALIDA = "TRANSICAO_ESTADO_INVALIDA";
    public static final String SEM_CRITERIO_INCLUSAO = "ESTUDO_SEM_CRITERIO_INCLUSAO";
    public static final String SEM_VISITA_PREVISTA = "ESTUDO_SEM_VISITA_PREVISTA";

    /** Mapa de transições permitidas: cada estado só vai para o seguinte. */
    private static final Map<StatusEstudo, StatusEstudo> PROXIMO = new EnumMap<>(Map.of(
            StatusEstudo.PLANEJAMENTO, StatusEstudo.RECRUTAMENTO,
            StatusEstudo.RECRUTAMENTO, StatusEstudo.EXECUCAO,
            StatusEstudo.EXECUCAO, StatusEstudo.ENCERRADO));

    private MaquinaEstadosEstudo() {
    }

    /** O único estado para o qual o estudo pode ir; vazio quando já está encerrado. */
    public static Optional<StatusEstudo> proximoEstado(StatusEstudo atual) {
        return atual == null ? Optional.empty() : Optional.ofNullable(PROXIMO.get(atual));
    }

    /** Se a mudança de {@code origem} para {@code destino} segue a sequência (não olha as pré-condições). */
    public static boolean transicaoPermitida(StatusEstudo origem, StatusEstudo destino) {
        return origem != null && destino != null && PROXIMO.get(origem) == destino;
    }

    /**
     * Verifica se o estudo pode mudar de {@code origem} para {@code destino}.
     *
     * @param criteriosInclusao quantidade de critérios de inclusão do estudo
     * @param visitasPrevistas  quantidade de visitas previstas do estudo
     * @throws ConflitoRegraException transição fora da sequência, ou pré-condição de saída de
     *                                planejamento não atendida
     */
    public static void validar(StatusEstudo origem, StatusEstudo destino, long criteriosInclusao,
            long visitasPrevistas) {
        if (!transicaoPermitida(origem, destino)) {
            throw new ConflitoRegraException(TRANSICAO_INVALIDA, mensagemTransicaoInvalida(origem, destino));
        }
        if (origem == StatusEstudo.PLANEJAMENTO) {
            if (criteriosInclusao < 1) {
                throw new ConflitoRegraException(SEM_CRITERIO_INCLUSAO,
                        "Adicione ao menos um critério de inclusão para sair de planejamento");
            }
            if (visitasPrevistas < 1) {
                throw new ConflitoRegraException(SEM_VISITA_PREVISTA,
                        "Defina ao menos uma visita prevista para sair de planejamento");
            }
        }
    }

    private static String mensagemTransicaoInvalida(StatusEstudo origem, StatusEstudo destino) {
        if (origem == null || destino == null) {
            return "Informe o estado de origem e o de destino da mudança";
        }
        if (origem == destino) {
            return "O estudo já está em " + rotulo(origem);
        }
        return proximoEstado(origem)
                .map(proximo -> "Não é possível mudar o estudo de " + rotulo(origem) + " para " + rotulo(destino)
                        + ": os estados só avançam, um de cada vez; de " + rotulo(origem) + " só se vai para "
                        + rotulo(proximo))
                .orElse("O estudo encerrado não pode mudar de estado");
    }

    /** Nome do estado como aparece nas mensagens ao usuário. */
    public static String rotulo(StatusEstudo estado) {
        return switch (estado) {
            case PLANEJAMENTO -> "planejamento";
            case RECRUTAMENTO -> "recrutamento";
            case EXECUCAO -> "execução";
            case ENCERRADO -> "encerrado";
        };
    }
}
