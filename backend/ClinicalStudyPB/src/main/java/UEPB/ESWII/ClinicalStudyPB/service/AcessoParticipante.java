package UEPB.ESWII.ClinicalStudyPB.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;
import UEPB.ESWII.ClinicalStudyPB.repository.ParticipacaoRepository;

/**
 * Resolve, a partir do usuário logado, as participações vinculadas à conta (regra 7: caminho
 * {@code Usuario -> Candidato -> Participacao}). Base de todos os endpoints {@code /api/me/**} (T109) e das consultas
 * de visitas do participante (T116).
 *
 * <p>Recebe só o {@link UUID} do usuário (vindo do token); não conhece {@code Jwt} nem a camada web. Participação que
 * não é da conta devolve <b>404</b>, nunca 403, para não revelar que o dado existe.</p>
 */
@Component
@Transactional(readOnly = true)
public class AcessoParticipante {

    private final ParticipacaoRepository participacoes;

    public AcessoParticipante(ParticipacaoRepository participacoes) {
        this.participacoes = participacoes;
    }

    /** Todas as participações da conta (inclusive as retiradas), da inclusão mais recente para a mais antiga. */
    public List<Participacao> participacoesDe(UUID usuarioId) {
        if (usuarioId == null) {
            return List.of();
        }
        return participacoes.findByCandidatoUsuarioIdOrderByDataInclusaoDesc(usuarioId);
    }

    /** Participação da conta no estudo; 404 se o estudo não existe ou a conta não participa dele. */
    public Participacao participacaoDoEstudo(UUID usuarioId, UUID estudoId) {
        if (usuarioId == null || estudoId == null) {
            throw naoEncontrada();
        }
        return participacoes.findByCandidatoUsuarioIdAndEstudoId(usuarioId, estudoId)
                .orElseThrow(AcessoParticipante::naoEncontrada);
    }

    /** Participação pelo id, desde que seja da conta; 404 se for de outro participante ou não existir. */
    public Participacao participacaoPorId(UUID usuarioId, UUID participacaoId) {
        if (usuarioId == null || participacaoId == null) {
            throw naoEncontrada();
        }
        return participacoes.findByIdAndCandidatoUsuarioId(participacaoId, usuarioId)
                .orElseThrow(AcessoParticipante::naoEncontrada);
    }

    private static RecursoNaoEncontradoException naoEncontrada() {
        return new RecursoNaoEncontradoException("Recurso não encontrado");
    }
}
