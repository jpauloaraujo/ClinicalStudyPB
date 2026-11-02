package UEPB.ESWII.ClinicalStudyPB.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.AvaliacaoRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.AvaliacaoResponse;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.ResultadoAvaliacao;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoCandidato;
import UEPB.ESWII.ClinicalStudyPB.domain.model.AvaliacaoElegibilidade;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Candidato;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Criterio;
import UEPB.ESWII.ClinicalStudyPB.domain.model.RespostaAvaliacao;
import UEPB.ESWII.ClinicalStudyPB.domain.regra.RegraElegibilidade;
import UEPB.ESWII.ClinicalStudyPB.repository.AvaliacaoElegibilidadeRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.ConsentimentoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.CriterioRepository;

/**
 * RF09: avaliação de elegibilidade. A avaliação responde a todos os critérios do estudo; a regra
 * ({@link RegraElegibilidade}) calcula o resultado e a nova avaliação passa a valer, deixando as
 * anteriores no histórico.
 */
@Service
@Transactional
public class AvaliacaoService {

    private final AvaliacaoElegibilidadeRepository avaliacaoRepository;
    private final CriterioRepository criterioRepository;
    private final ConsentimentoRepository consentimentoRepository;
    private final CandidatoService candidatoService;
    private final EstudoAcesso estudoAcesso;
    private final UsuarioAutenticado usuarioAutenticado;

    public AvaliacaoService(AvaliacaoElegibilidadeRepository avaliacaoRepository, CriterioRepository criterioRepository,
            ConsentimentoRepository consentimentoRepository, CandidatoService candidatoService,
            EstudoAcesso estudoAcesso, UsuarioAutenticado usuarioAutenticado) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.criterioRepository = criterioRepository;
        this.consentimentoRepository = consentimentoRepository;
        this.candidatoService = candidatoService;
        this.estudoAcesso = estudoAcesso;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    public AvaliacaoResponse avaliar(UUID candidatoId, AvaliacaoRequest dados, UUID avaliadorId) {
        Candidato candidato = candidatoService.buscarEntidade(candidatoId);
        estudoAcesso.exigirRecrutamento(candidato.getEstudo());
        if (candidato.estaIncluido()) {
            throw new ConflitoRegraException("CANDIDATO_JA_INCLUIDO",
                    "O candidato já foi incluído no estudo e não pode ser reavaliado");
        }

        List<Criterio> criterios = criterioRepository.findByEstudoIdOrderByTituloAsc(candidato.getEstudo().getId());
        Map<UUID, AvaliacaoRequest.Resposta> respostasPorCriterio = indexar(dados, criterios);
        Map<UUID, Boolean> aplica = new HashMap<>();
        respostasPorCriterio.forEach((id, resposta) -> aplica.put(id, resposta.aplica()));
        RegraElegibilidade.Resultado resultado = RegraElegibilidade.avaliar(criterios, aplica);

        avaliacaoRepository.findByCandidatoIdAndVigenteTrue(candidatoId).ifPresent(anterior -> {
            anterior.setVigente(false);
            avaliacaoRepository.saveAndFlush(anterior);
        });

        AvaliacaoElegibilidade avaliacao = new AvaliacaoElegibilidade();
        avaliacao.setCandidato(candidato);
        avaliacao.setAvaliador(usuarioAutenticado.buscar(avaliadorId));
        avaliacao.setDataAvaliacao(LocalDateTime.now());
        avaliacao.setResultado(resultado.elegivel() ? ResultadoAvaliacao.APROVADO : ResultadoAvaliacao.REPROVADO);
        avaliacao.setPontuacao(resultado.pontuacao());
        avaliacao.setVigente(true);
        for (Criterio criterio : criterios) {
            AvaliacaoRequest.Resposta resposta = respostasPorCriterio.get(criterio.getId());
            RespostaAvaliacao registro = new RespostaAvaliacao();
            registro.setAvaliacao(avaliacao);
            registro.setCriterio(criterio);
            registro.setAplica(resposta.aplica());
            registro.setObservacao(resposta.observacao() == null || resposta.observacao().isBlank()
                    ? null : resposta.observacao().trim());
            avaliacao.getRespostas().add(registro);
        }
        avaliacaoRepository.save(avaliacao);

        candidato.setSituacao(situacaoApos(candidato, resultado.elegivel()));
        return AvaliacaoResponse.de(avaliacao);
    }

    /** Histórico de avaliações do candidato, da mais recente para a mais antiga. */
    @Transactional(readOnly = true)
    public List<AvaliacaoResponse> historico(UUID candidatoId) {
        candidatoService.buscarEntidade(candidatoId);
        return avaliacaoRepository.findByCandidatoIdOrderByDataAvaliacaoDesc(candidatoId).stream()
                .map(AvaliacaoResponse::de)
                .toList();
    }

    // Aprovado com consentimento já registrado fica pronto para a inclusão (o consentimento não depende da ordem)
    private SituacaoCandidato situacaoApos(Candidato candidato, boolean elegivel) {
        if (!elegivel) {
            return SituacaoCandidato.NAO_ELEGIVEL;
        }
        return consentimentoRepository.existsByCandidatoId(candidato.getId())
                ? SituacaoCandidato.CONSENTIDO
                : SituacaoCandidato.ELEGIVEL;
    }

    // Exige exatamente uma resposta por critério do estudo: sem repetir, faltar nem sobrar
    private static Map<UUID, AvaliacaoRequest.Resposta> indexar(AvaliacaoRequest dados, List<Criterio> criterios) {
        Set<UUID> esperados = new HashSet<>();
        criterios.forEach(c -> esperados.add(c.getId()));

        Map<UUID, AvaliacaoRequest.Resposta> porCriterio = new HashMap<>();
        for (AvaliacaoRequest.Resposta resposta : dados.respostas()) {
            if (porCriterio.put(resposta.criterioId(), resposta) != null) {
                throw new RegraNegocioException("Há mais de uma resposta para o mesmo critério");
            }
            if (!esperados.contains(resposta.criterioId())) {
                throw new RegraNegocioException("O critério " + resposta.criterioId() + " não pertence a este estudo");
            }
        }
        if (porCriterio.size() != esperados.size()) {
            throw new RegraNegocioException("A avaliação deve responder a todos os critérios do estudo");
        }
        return porCriterio;
    }
}
