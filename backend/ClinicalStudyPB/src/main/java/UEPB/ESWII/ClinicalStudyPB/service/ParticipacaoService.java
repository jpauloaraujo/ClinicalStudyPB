package UEPB.ESWII.ClinicalStudyPB.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.ParticipacaoResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.VagasResponse;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.ResultadoAvaliacao;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoCandidato;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Candidato;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;
import UEPB.ESWII.ClinicalStudyPB.repository.AvaliacaoElegibilidadeRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.ConsentimentoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.ParticipacaoRepository;

/**
 * RF11: inclusão de participantes. Tudo ocorre em uma transação com o estudo bloqueado para escrita,
 * então inclusões simultâneas são atendidas uma por vez e nunca passam do limite de vagas.
 */
@Service
@Transactional
public class ParticipacaoService {

    private final ParticipacaoRepository participacaoRepository;
    private final AvaliacaoElegibilidadeRepository avaliacaoRepository;
    private final ConsentimentoRepository consentimentoRepository;
    private final CandidatoService candidatoService;
    private final EstudoAcesso estudoAcesso;
    private final UsuarioAutenticado usuarioAutenticado;

    public ParticipacaoService(ParticipacaoRepository participacaoRepository,
            AvaliacaoElegibilidadeRepository avaliacaoRepository, ConsentimentoRepository consentimentoRepository,
            CandidatoService candidatoService, EstudoAcesso estudoAcesso, UsuarioAutenticado usuarioAutenticado) {
        this.participacaoRepository = participacaoRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.consentimentoRepository = consentimentoRepository;
        this.candidatoService = candidatoService;
        this.estudoAcesso = estudoAcesso;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    /**
     * Verifica, nesta ordem: estudo em recrutamento, candidato do estudo e ainda não incluído, avaliação
     * vigente aprovada, consentimento registrado e vaga disponível. Cada falha tem um código próprio.
     */
    public ParticipacaoResponse incluir(UUID estudoId, UUID candidatoId, UUID usuarioId) {
        Estudo estudo = estudoAcesso.buscarComBloqueio(estudoId);
        estudoAcesso.exigirRecrutamento(estudo);

        Candidato candidato = candidatoService.buscarEntidade(candidatoId);
        if (!candidato.getEstudo().getId().equals(estudoId)) {
            throw new RecursoNaoEncontradoException("Candidato não encontrado neste estudo: " + candidatoId);
        }
        if (candidato.estaIncluido() || participacaoRepository.existsByCandidatoId(candidatoId)) {
            throw new ConflitoRegraException("CANDIDATO_JA_INCLUIDO", "O candidato já foi incluído no estudo");
        }

        boolean elegivel = avaliacaoRepository.findByCandidatoIdAndVigenteTrue(candidatoId)
                .map(avaliacao -> avaliacao.getResultado() == ResultadoAvaliacao.APROVADO)
                .orElse(false);
        if (!elegivel) {
            throw new ConflitoRegraException("CANDIDATO_NAO_ELEGIVEL",
                    "O candidato precisa ter uma avaliação de elegibilidade aprovada para ser incluído");
        }
        if (!consentimentoRepository.existsByCandidatoId(candidatoId)) {
            throw new ConflitoRegraException("CONSENTIMENTO_AUSENTE",
                    "O candidato precisa ter o consentimento registrado para ser incluído");
        }
        if (participacaoRepository.countByEstudoId(estudoId) >= estudo.getLimiteParticipantes()) {
            throw new ConflitoRegraException("VAGAS_ESGOTADAS",
                    "Não há vagas disponíveis: o limite de " + estudo.getLimiteParticipantes()
                            + " participantes do estudo foi atingido");
        }

        Participacao participacao = new Participacao();
        participacao.setCandidato(candidato);
        participacao.setEstudo(estudo);
        participacao.setDataInclusao(LocalDate.now());
        participacao.setIncluidoPor(usuarioAutenticado.buscar(usuarioId));
        participacaoRepository.save(participacao);

        candidato.setSituacao(SituacaoCandidato.INCLUIDO);
        return ParticipacaoResponse.de(participacao);
    }

    @Transactional(readOnly = true)
    public List<ParticipacaoResponse> listar(UUID estudoId) {
        estudoAcesso.buscar(estudoId);
        return participacaoRepository.findByEstudoIdOrderByDataInclusaoAscCandidatoCodigoAsc(estudoId).stream()
                .map(ParticipacaoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public VagasResponse vagas(UUID estudoId) {
        Estudo estudo = estudoAcesso.buscar(estudoId);
        return VagasResponse.de(participacaoRepository.countByEstudoId(estudoId), estudo.getLimiteParticipantes());
    }
}
