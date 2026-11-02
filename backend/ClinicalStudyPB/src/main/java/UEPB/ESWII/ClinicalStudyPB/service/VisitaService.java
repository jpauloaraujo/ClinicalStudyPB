package UEPB.ESWII.ClinicalStudyPB.service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.AcaoRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.AcaoResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.VisitaRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.VisitaResponse;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Acao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaPlanejada;
import UEPB.ESWII.ClinicalStudyPB.repository.AcaoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaPlanejadaRepository;

/** RF06: visitas planejadas e suas ações, editáveis só em planejamento e sempre ordenadas pelo dia previsto. */
@Service
@Transactional
public class VisitaService {

    private final VisitaPlanejadaRepository visitaRepository;
    private final AcaoRepository acaoRepository;
    private final EstudoAcesso estudoAcesso;

    public VisitaService(VisitaPlanejadaRepository visitaRepository, AcaoRepository acaoRepository,
            EstudoAcesso estudoAcesso) {
        this.visitaRepository = visitaRepository;
        this.acaoRepository = acaoRepository;
        this.estudoAcesso = estudoAcesso;
    }

    @Transactional(readOnly = true)
    public List<VisitaResponse> listar(UUID estudoId) {
        estudoAcesso.buscar(estudoId);
        return visitaRepository.findByEstudoIdOrderByOrdemAsc(estudoId).stream()
                .map(VisitaResponse::de)
                .toList();
    }

    public VisitaResponse criar(UUID estudoId, VisitaRequest dados) {
        Estudo estudo = estudoAcesso.buscar(estudoId);
        estudoAcesso.exigirPlanejamento(estudo);
        if (visitaRepository.existsByEstudoIdAndDiaPrevisto(estudoId, dados.diaPrevisto())) {
            throw diaRepetido();
        }

        VisitaPlanejada visita = new VisitaPlanejada();
        visita.setEstudo(estudo);
        preencher(visita, dados);
        visita.setOrdem(Integer.MAX_VALUE);
        visitaRepository.saveAndFlush(visita);
        reordenar(estudoId);
        return VisitaResponse.de(visita);
    }

    public VisitaResponse atualizar(UUID estudoId, UUID id, VisitaRequest dados) {
        VisitaPlanejada visita = buscarDoEstudo(estudoId, id);
        estudoAcesso.exigirPlanejamento(visita.getEstudo());
        if (visitaRepository.existsByEstudoIdAndDiaPrevistoAndIdNot(estudoId, dados.diaPrevisto(), id)) {
            throw diaRepetido();
        }

        preencher(visita, dados);
        visitaRepository.saveAndFlush(visita);
        reordenar(estudoId);
        return VisitaResponse.de(visita);
    }

    /** Remove a visita e, em cascata, todas as suas ações. */
    public void excluir(UUID estudoId, UUID id) {
        VisitaPlanejada visita = buscarDoEstudo(estudoId, id);
        estudoAcesso.exigirPlanejamento(visita.getEstudo());
        visitaRepository.delete(visita);
        visitaRepository.flush();
        reordenar(estudoId);
    }

    public AcaoResponse criarAcao(UUID visitaId, AcaoRequest dados) {
        VisitaPlanejada visita = buscarVisita(visitaId);
        estudoAcesso.exigirPlanejamento(visita.getEstudo());

        Acao acao = new Acao();
        acao.setVisita(visita);
        preencher(acao, dados);
        visita.getAcoes().add(acao);
        return AcaoResponse.de(acaoRepository.saveAndFlush(acao));
    }

    public AcaoResponse atualizarAcao(UUID visitaId, UUID id, AcaoRequest dados) {
        Acao acao = buscarAcaoDaVisita(visitaId, id);
        estudoAcesso.exigirPlanejamento(acao.getVisita().getEstudo());
        preencher(acao, dados);
        return AcaoResponse.de(acaoRepository.saveAndFlush(acao));
    }

    public void excluirAcao(UUID visitaId, UUID id) {
        Acao acao = buscarAcaoDaVisita(visitaId, id);
        estudoAcesso.exigirPlanejamento(acao.getVisita().getEstudo());
        acao.getVisita().getAcoes().remove(acao);
        acaoRepository.delete(acao);
    }

    private VisitaPlanejada buscarVisita(UUID id) {
        return visitaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Visita não encontrada: " + id));
    }

    private VisitaPlanejada buscarDoEstudo(UUID estudoId, UUID id) {
        VisitaPlanejada visita = buscarVisita(id);
        if (!visita.getEstudo().getId().equals(estudoId)) {
            throw new RecursoNaoEncontradoException("Visita não encontrada neste estudo: " + id);
        }
        return visita;
    }

    private Acao buscarAcaoDaVisita(UUID visitaId, UUID id) {
        Acao acao = acaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ação não encontrada: " + id));
        if (!acao.getVisita().getId().equals(visitaId)) {
            throw new RecursoNaoEncontradoException("Ação não encontrada nesta visita: " + id);
        }
        return acao;
    }

    private static void preencher(VisitaPlanejada visita, VisitaRequest dados) {
        visita.setNome(dados.nome().trim());
        visita.setDiaPrevisto(dados.diaPrevisto());
        visita.setTolerancia(dados.tolerancia());
    }

    private static void preencher(Acao acao, AcaoRequest dados) {
        acao.setNome(dados.nome().trim());
        acao.setDescricao(dados.descricao() == null || dados.descricao().isBlank() ? null : dados.descricao().trim());
        acao.setCategoria(dados.categoria());
    }

    // A ordem é a posição da visita quando ordenadas pelo dia previsto (1 = primeira)
    private void reordenar(UUID estudoId) {
        List<VisitaPlanejada> visitas = visitaRepository.findByEstudoIdOrderByOrdemAsc(estudoId).stream()
                .sorted(Comparator.comparingInt(VisitaPlanejada::getDiaPrevisto))
                .toList();
        for (int i = 0; i < visitas.size(); i++) {
            visitas.get(i).setOrdem(i + 1);
        }
    }

    private static ConflitoRegraException diaRepetido() {
        return new ConflitoRegraException("VISITA_DIA_DUPLICADO",
                "Já existe uma visita prevista para esse dia neste estudo");
    }
}
