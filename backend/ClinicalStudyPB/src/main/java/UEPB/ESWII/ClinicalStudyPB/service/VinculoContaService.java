package UEPB.ESWII.ClinicalStudyPB.service;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.ContaParticipanteResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.ContaVinculadaResponse;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.PerfilUsuario;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Candidato;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.repository.CandidatoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.ParticipacaoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.UsuarioRepository;

/**
 * RF17 (T111): vínculo da conta do participante. Liga uma conta de perfil PARTICIPANTE ao candidato da participação,
 * numa relação um para um, para que o app mostre ao participante os estudos dele (T109). Não há desvincular nesta
 * Sprint (RQ-T111-05).
 *
 * <p>Ordem das verificações do vínculo (RQ-T111-03): participação e conta existem (404), participação ativa, perfil
 * da conta, conta ativa, participação sem conta, conta sem outro participante. Cada violação tem um código próprio
 * (409). A corrida entre dois vínculos é decidida pelo banco: o {@code UPDATE} condicional e o índice único
 * {@code uk_candidatos_usuario} garantem 409 e nunca 500 (RQ-T111-04).</p>
 */
@Service
@Transactional
public class VinculoContaService {

    public static final String PARTICIPANTE_RETIRADO = "PARTICIPANTE_RETIRADO";
    public static final String CONTA_NAO_E_PARTICIPANTE = "CONTA_NAO_E_PARTICIPANTE";
    public static final String CONTA_INATIVA = "CONTA_INATIVA";
    public static final String PARTICIPACAO_JA_TEM_CONTA = "PARTICIPACAO_JA_TEM_CONTA";
    public static final String CONTA_JA_VINCULADA = "CONTA_JA_VINCULADA";

    private final ParticipacaoRepository participacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CandidatoRepository candidatoRepository;

    public VinculoContaService(ParticipacaoRepository participacaoRepository, UsuarioRepository usuarioRepository,
            CandidatoRepository candidatoRepository) {
        this.participacaoRepository = participacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.candidatoRepository = candidatoRepository;
    }

    public ContaVinculadaResponse vincular(UUID participacaoId, UUID usuarioId) {
        Participacao participacao = participacaoRepository.buscarComCandidato(participacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Participação não encontrada"));
        Usuario conta = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada"));

        if (!participacaoAtiva(participacao)) {
            throw new ConflitoRegraException(PARTICIPANTE_RETIRADO,
                    "O participante foi retirado do estudo e não pode receber uma conta");
        }
        if (conta.getPerfil() != PerfilUsuario.PARTICIPANTE) {
            throw new ConflitoRegraException(CONTA_NAO_E_PARTICIPANTE,
                    "A conta informada não é de um participante");
        }
        if (!conta.isAtivo()) {
            throw new ConflitoRegraException(CONTA_INATIVA, "A conta informada está desativada");
        }
        Candidato candidato = participacao.getCandidato();
        if (candidato.getUsuario() != null) {
            throw new ConflitoRegraException(PARTICIPACAO_JA_TEM_CONTA,
                    "Este participante já tem uma conta vinculada");
        }
        if (candidatoRepository.existsByUsuarioId(usuarioId)) {
            throw contaJaVinculada();
        }

        gravar(candidato.getId(), conta);
        return new ContaVinculadaResponse(participacaoId, conta.getId(), conta.getNome(), conta.getEmail());
    }

    @Transactional(readOnly = true)
    public ContaVinculadaResponse consultar(UUID participacaoId) {
        Participacao participacao = participacaoRepository.buscarComCandidato(participacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Participação não encontrada"));
        Usuario conta = participacao.getCandidato().getUsuario();
        if (conta == null) {
            throw new RecursoNaoEncontradoException("Este participante não tem conta vinculada");
        }
        return new ContaVinculadaResponse(participacaoId, conta.getId(), conta.getNome(), conta.getEmail());
    }

    @Transactional(readOnly = true)
    public List<ContaParticipanteResponse> contasDisponiveis() {
        return usuarioRepository.listarContasDisponiveis();
    }

    /**
     * Grava o vínculo com UPDATE condicional (só se o candidato ainda não tem conta). O UNIQUE estoura dentro desta
     * chamada, então o tratamento fica aqui; com {@code save()} o erro só apareceria no commit e viraria 500.
     */
    private void gravar(UUID candidatoId, Usuario conta) {
        int afetadas;
        try {
            afetadas = candidatoRepository.vincularConta(candidatoId, conta);
        } catch (DataIntegrityViolationException erro) {
            throw contaJaVinculada();
        }
        if (afetadas == 0) {
            throw new ConflitoRegraException(PARTICIPACAO_JA_TEM_CONTA,
                    "Este participante já tem uma conta vinculada");
        }
    }

    /**
     * Ponto único da regra "participação ativa" (RQ-T111-03, passo 2): participante retirado (RF16, T101) não recebe
     * conta e a recusa é 409 {@code PARTICIPANTE_RETIRADO}.
     */
    boolean participacaoAtiva(Participacao participacao) {
        return !participacao.estaRetirada();
    }

    private static ConflitoRegraException contaJaVinculada() {
        return new ConflitoRegraException(CONTA_JA_VINCULADA,
                "Esta conta já está vinculada a outro participante");
    }
}
