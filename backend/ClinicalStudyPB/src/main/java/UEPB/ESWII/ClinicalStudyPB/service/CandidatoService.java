package UEPB.ESWII.ClinicalStudyPB.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.CandidatoRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.CandidatoResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.PaginaResponse;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.ResultadoAvaliacao;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoCandidato;
import UEPB.ESWII.ClinicalStudyPB.domain.model.AvaliacaoElegibilidade;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Candidato;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Consentimento;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.repository.AvaliacaoElegibilidadeRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.CandidatoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.ConsentimentoRepository;

/** RF08: cadastro de candidatos de um estudo em recrutamento, com código gerado e sem duplicidade. */
@Service
@Transactional
public class CandidatoService {

    private static final int TAMANHO_MAXIMO_PAGINA = 100;

    private final CandidatoRepository candidatoRepository;
    private final AvaliacaoElegibilidadeRepository avaliacaoRepository;
    private final ConsentimentoRepository consentimentoRepository;
    private final EstudoAcesso estudoAcesso;

    public CandidatoService(CandidatoRepository candidatoRepository,
            AvaliacaoElegibilidadeRepository avaliacaoRepository, ConsentimentoRepository consentimentoRepository,
            EstudoAcesso estudoAcesso) {
        this.candidatoRepository = candidatoRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.consentimentoRepository = consentimentoRepository;
        this.estudoAcesso = estudoAcesso;
    }

    public CandidatoResponse criar(UUID estudoId, CandidatoRequest dados) {
        // Com o estudo bloqueado, dois cadastros simultâneos nunca recebem o mesmo código
        Estudo estudo = estudoAcesso.buscarComBloqueio(estudoId);
        estudoAcesso.exigirRecrutamento(estudo);
        String nome = dados.nomeFicticio().trim();
        if (candidatoRepository.existsByEstudoIdAndNomeFicticioIgnoreCaseAndDataNascimento(
                estudoId, nome, dados.dataNascimento())) {
            throw duplicado();
        }

        Candidato candidato = new Candidato();
        candidato.setEstudo(estudo);
        candidato.setCodigo(String.format("CAND-%03d", candidatoRepository.countByEstudoId(estudoId) + 1));
        preencher(candidato, dados);
        candidato.setSituacao(SituacaoCandidato.CADASTRADO);
        return montar(candidatoRepository.save(candidato));
    }

    /** Edição permitida até a inclusão. */
    public CandidatoResponse atualizar(UUID id, CandidatoRequest dados) {
        Candidato candidato = buscarEntidade(id);
        if (candidato.estaIncluido()) {
            throw new ConflitoRegraException("CANDIDATO_JA_INCLUIDO",
                    "O candidato já foi incluído no estudo e não pode mais ser editado");
        }
        if (candidatoRepository.existsByEstudoIdAndNomeFicticioIgnoreCaseAndDataNascimentoAndIdNot(
                candidato.getEstudo().getId(), dados.nomeFicticio().trim(), dados.dataNascimento(), id)) {
            throw duplicado();
        }

        preencher(candidato, dados);
        return montar(candidatoRepository.save(candidato));
    }

    @Transactional(readOnly = true)
    public CandidatoResponse buscar(UUID id) {
        return montar(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<CandidatoResponse> listar(UUID estudoId, SituacaoCandidato situacao, int pagina, int tamanho) {
        estudoAcesso.buscar(estudoId);
        PageRequest pedido = PageRequest.of(Math.max(0, pagina), Math.min(Math.max(1, tamanho), TAMANHO_MAXIMO_PAGINA),
                Sort.by("codigo"));
        Page<Candidato> resultado = situacao == null
                ? candidatoRepository.findByEstudoId(estudoId, pedido)
                : candidatoRepository.findByEstudoIdAndSituacao(estudoId, situacao, pedido);
        return PaginaResponse.de(resultado, this::montar);
    }

    /** Candidato existente ou 404; usado também pelos serviços de avaliação, consentimento e inclusão. */
    public Candidato buscarEntidade(UUID id) {
        return candidatoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Candidato não encontrado: " + id));
    }

    private CandidatoResponse montar(Candidato candidato) {
        AvaliacaoElegibilidade avaliacao = avaliacaoRepository.findByCandidatoIdAndVigenteTrue(candidato.getId())
                .orElse(null);
        ResultadoAvaliacao resultado = avaliacao == null ? null : avaliacao.getResultado();
        LocalDateTime dataAvaliacao = avaliacao == null ? null : avaliacao.getDataAvaliacao();
        LocalDateTime dataConsentimento = consentimentoRepository.findByCandidatoId(candidato.getId())
                .map(Consentimento::getDataHora).orElse(null);
        return CandidatoResponse.de(candidato, resultado, dataAvaliacao, dataConsentimento);
    }

    private static void preencher(Candidato candidato, CandidatoRequest dados) {
        candidato.setNomeFicticio(dados.nomeFicticio().trim());
        candidato.setDataNascimento(dados.dataNascimento());
        candidato.setContato(dados.contato() == null || dados.contato().isBlank() ? null : dados.contato().trim());
    }

    private static ConflitoRegraException duplicado() {
        return new ConflitoRegraException("CANDIDATO_DUPLICADO",
                "Já existe um candidato com esse nome fictício e data de nascimento neste estudo");
    }
}
