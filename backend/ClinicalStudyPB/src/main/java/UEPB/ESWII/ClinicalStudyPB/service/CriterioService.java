package UEPB.ESWII.ClinicalStudyPB.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.CriterioRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.CriterioResponse;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.TipoCriterio;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Criterio;
import UEPB.ESWII.ClinicalStudyPB.domain.model.CriterioExclusao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.CriterioInclusao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.repository.CriterioRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.RespostaAvaliacaoRepository;

/** RF05: critérios de inclusão e exclusão de um estudo, editáveis só em planejamento. */
@Service
@Transactional
public class CriterioService {

    private final CriterioRepository criterioRepository;
    private final RespostaAvaliacaoRepository respostaRepository;
    private final EstudoAcesso estudoAcesso;

    public CriterioService(CriterioRepository criterioRepository, RespostaAvaliacaoRepository respostaRepository,
            EstudoAcesso estudoAcesso) {
        this.criterioRepository = criterioRepository;
        this.respostaRepository = respostaRepository;
        this.estudoAcesso = estudoAcesso;
    }

    @Transactional(readOnly = true)
    public List<CriterioResponse> listar(UUID estudoId) {
        estudoAcesso.buscar(estudoId);
        return criterioRepository.findByEstudoIdOrderByTituloAsc(estudoId).stream()
                .map(CriterioResponse::de)
                .toList();
    }

    public CriterioResponse criar(UUID estudoId, CriterioRequest dados) {
        Estudo estudo = estudoAcesso.buscarComBloqueio(estudoId);
        estudoAcesso.exigirPlanejamento(estudo);
        garantirDescricaoUnica(estudoId, dados.tipo(), dados.descricao(), null);
        garantirTituloUnico(estudoId, dados.tipo(), dados.titulo(), null);

        Criterio criterio = novo(dados.tipo());
        criterio.setEstudo(estudo);
        preencher(criterio, dados);
        return CriterioResponse.de(criterioRepository.save(criterio));
    }

    public CriterioResponse atualizar(UUID estudoId, UUID id, CriterioRequest dados) {
        Estudo estudo = estudoAcesso.buscarComBloqueio(estudoId);
        Criterio criterio = buscarDoEstudo(estudoId, id);
        estudoAcesso.exigirPlanejamento(estudo);
        if (criterio.getTipo() != dados.tipo()) {
            throw new RegraNegocioException("O tipo de um critério não pode ser alterado; exclua e crie outro");
        }
        garantirDescricaoUnica(estudoId, dados.tipo(), dados.descricao(), id);
        garantirTituloUnico(estudoId, dados.tipo(), dados.titulo(), id);

        preencher(criterio, dados);
        return CriterioResponse.de(criterioRepository.save(criterio));
    }

    public void excluir(UUID estudoId, UUID id) {
        Estudo estudo = estudoAcesso.buscarComBloqueio(estudoId);
        Criterio criterio = buscarDoEstudo(estudoId, id);
        estudoAcesso.exigirPlanejamento(estudo);
        if (respostaRepository.existsByCriterioId(id)) {
            throw new ConflitoRegraException("CRITERIO_EM_USO",
                    "Não é possível excluir: o critério já foi usado em avaliações de candidatos");
        }
        criterioRepository.delete(criterio);
    }

    private Criterio buscarDoEstudo(UUID estudoId, UUID id) {
        Criterio criterio = criterioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Critério não encontrado: " + id));
        if (!criterio.getEstudo().getId().equals(estudoId)) {
            throw new RecursoNaoEncontradoException("Critério não encontrado neste estudo: " + id);
        }
        return criterio;
    }

    private static Criterio novo(TipoCriterio tipo) {
        return tipo == TipoCriterio.INCLUSAO ? new CriterioInclusao() : new CriterioExclusao();
    }

    private static void preencher(Criterio criterio, CriterioRequest dados) {
        String titulo = dados.titulo();
        criterio.setTitulo(titulo == null || titulo.isBlank()
                ? dados.descricao().substring(0, Math.min(120, dados.descricao().length())) : titulo);
        criterio.setDescricao(dados.descricao().trim());
        if (criterio instanceof CriterioInclusao inclusao) {
            inclusao.setPeso(dados.peso() == null ? 1f : dados.peso());
            inclusao.setDeterminante(dados.determinante() == null || dados.determinante());
        }
    }

    private void garantirTituloUnico(UUID estudoId, TipoCriterio tipo, String titulo, UUID ignorarId) {
        if (titulo == null || titulo.isBlank()) return;
        boolean repetido = criterioRepository.findByEstudoIdOrderByTituloAsc(estudoId).stream()
                .filter(outro -> !outro.getId().equals(ignorarId))
                .anyMatch(outro -> outro.getTipo() == tipo && outro.getTitulo().equalsIgnoreCase(titulo.trim()));
        if (repetido) {
            throw new ConflitoRegraException("CRITERIO_DUPLICADO",
                    "Já existe um critério de " + (tipo == TipoCriterio.INCLUSAO ? "inclusão" : "exclusão")
                            + " com esse título neste estudo");
        }
    }

    // A descrição não pode repetir dentro do mesmo tipo e estudo (T45).
    private void garantirDescricaoUnica(UUID estudoId, TipoCriterio tipo, String descricao, UUID ignorarId) {
        boolean repetido = criterioRepository.findByEstudoIdOrderByTituloAsc(estudoId).stream()
                .filter(outro -> !outro.getId().equals(ignorarId))
                .anyMatch(outro -> outro.getTipo() == tipo && outro.getDescricao().trim().equalsIgnoreCase(descricao.trim()));
        if (repetido) {
            throw new ConflitoRegraException("CRITERIO_DUPLICADO",
                    "Já existe um critério de " + (tipo == TipoCriterio.INCLUSAO ? "inclusão" : "exclusão")
                            + " com essa descrição neste estudo");
        }
    }
}
