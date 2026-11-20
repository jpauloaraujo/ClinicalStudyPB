package UEPB.ESWII.ClinicalStudyPB.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.MeuEstudoDetalheResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.MeuEstudoResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.RetiradaDoMeuEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoParticipacao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;

/**
 * Lista e detalhe dos estudos do participante logado (T109, RF17). Somente leitura; o isolamento entre participantes
 * é do {@link AcessoParticipante}.
 */
@Service
@Transactional(readOnly = true)
public class MeusEstudosService {

    private final AcessoParticipante acesso;

    public MeusEstudosService(AcessoParticipante acesso) {
        this.acesso = acesso;
    }

    /** Um item por participação da conta; lista vazia se a conta não tem candidato vinculado. */
    public List<MeuEstudoResponse> listar(UUID usuarioId) {
        return acesso.participacoesDe(usuarioId).stream().map(MeusEstudosService::resumo).toList();
    }

    /** Detalhe do estudo; 404 se o estudo não é da conta (inclusive se pertence a outro participante). */
    public MeuEstudoDetalheResponse detalhar(UUID usuarioId, UUID estudoId) {
        Participacao participacao = acesso.participacaoDoEstudo(usuarioId, estudoId);
        Estudo estudo = participacao.getEstudo();
        return new MeuEstudoDetalheResponse(participacao.getId(), estudo.getId(), estudo.getTitulo(),
                MeuEstudoResponse.resumir(estudo.getObjetivo()), estudo.getStatus(), participacao.getDataInclusao(),
                situacaoDe(participacao), estudo.getObjetivo(), estudo.getDataInicio(), estudo.getDataFim(),
                estudo.getResponsavel().getNome(), retiradaDe(participacao));
    }

    private static MeuEstudoResponse resumo(Participacao participacao) {
        Estudo estudo = participacao.getEstudo();
        return new MeuEstudoResponse(participacao.getId(), estudo.getId(), estudo.getTitulo(),
                MeuEstudoResponse.resumir(estudo.getObjetivo()), estudo.getStatus(), participacao.getDataInclusao(),
                situacaoDe(participacao));
    }

    /**
     * Único ponto que decide a situação da participação. TODO(T101): a retirada ainda não existe, então toda
     * participação é ATIVO; trocar pela leitura da coluna {@code situacao} quando a T101 entrar.
     */
    private static SituacaoParticipacao situacaoDe(Participacao participacao) {
        return SituacaoParticipacao.ATIVO;
    }

    /** TODO(T101/T103): devolver só a data da retirada (nunca o motivo); sem retirada, nulo. */
    private static RetiradaDoMeuEstudo retiradaDe(Participacao participacao) {
        return null;
    }
}
