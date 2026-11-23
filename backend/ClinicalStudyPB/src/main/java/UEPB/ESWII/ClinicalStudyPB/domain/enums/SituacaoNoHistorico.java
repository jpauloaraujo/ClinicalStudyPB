package UEPB.ESWII.ClinicalStudyPB.domain.enums;

/**
 * Situação de uma visita prevista no histórico do participante (RF15). É o vocabulário de {@link SituacaoVisita}
 * mais {@link #NAO_AGENDADA}, para as visitas previstas que ainda não têm agendamento: o histórico mostra
 * <b>todas</b> as visitas previstas do estudo, e não só as agendadas.
 */
public enum SituacaoNoHistorico {
    NAO_AGENDADA,
    AGENDADA,
    REALIZADA,
    AUSENTE,
    CANCELADA;

    /** Converte a situação de uma visita agendada; {@code null} (sem agendamento) vira {@link #NAO_AGENDADA}. */
    public static SituacaoNoHistorico de(SituacaoVisita situacao) {
        if (situacao == null) {
            return NAO_AGENDADA;
        }
        return switch (situacao) {
            case AGENDADA -> AGENDADA;
            case REALIZADA -> REALIZADA;
            case AUSENTE -> AUSENTE;
            case CANCELADA -> CANCELADA;
        };
    }

    /** Visita ainda sem desfecho: não agendada ou agendada (as duas contam como pendentes no resumo). */
    public boolean estaPendente() {
        return this == NAO_AGENDADA || this == AGENDADA;
    }
}
