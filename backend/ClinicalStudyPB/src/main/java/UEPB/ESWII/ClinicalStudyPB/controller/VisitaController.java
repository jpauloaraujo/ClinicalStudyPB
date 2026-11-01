package UEPB.ESWII.ClinicalStudyPB.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.AcaoRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.AcaoResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.VisitaRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.VisitaResponse;
import UEPB.ESWII.ClinicalStudyPB.security.Permissoes;
import UEPB.ESWII.ClinicalStudyPB.service.VisitaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@Tag(name = "Visitas planejadas", description = "RF06 — Visitas do protocolo (dia previsto, tolerância) e suas ações; escrita do pesquisador só com o estudo em planejamento")
public class VisitaController {

    private final VisitaService visitaService;

    public VisitaController(VisitaService visitaService) {
        this.visitaService = visitaService;
    }

    @GetMapping("/estudos/{estudoId}/visitas")
    @PreAuthorize(Permissoes.LEITURA)
    @Operation(summary = "Listar as visitas do estudo", description = "Visitas ordenadas pelo dia previsto, já com as suas ações.")
    public List<VisitaResponse> listar(@PathVariable UUID estudoId) {
        return visitaService.listar(estudoId);
    }

    @PostMapping("/estudos/{estudoId}/visitas")
    @PreAuthorize(Permissoes.ESCRITA_PLANEJAMENTO)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar visita planejada", description = "Dois dias previstos iguais no mesmo estudo são recusados (409).")
    public VisitaResponse criar(@PathVariable UUID estudoId, @Valid @RequestBody VisitaRequest dados) {
        return visitaService.criar(estudoId, dados);
    }

    @PutMapping("/estudos/{estudoId}/visitas/{id}")
    @PreAuthorize(Permissoes.ESCRITA_PLANEJAMENTO)
    @Operation(summary = "Atualizar visita planejada")
    public VisitaResponse atualizar(@PathVariable UUID estudoId, @PathVariable UUID id,
            @Valid @RequestBody VisitaRequest dados) {
        return visitaService.atualizar(estudoId, id, dados);
    }

    @DeleteMapping("/estudos/{estudoId}/visitas/{id}")
    @PreAuthorize(Permissoes.ESCRITA_PLANEJAMENTO)
    @Operation(summary = "Excluir visita planejada", description = "Remove também todas as ações da visita.")
    public ResponseEntity<Void> excluir(@PathVariable UUID estudoId, @PathVariable UUID id) {
        visitaService.excluir(estudoId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/visitas/{visitaId}/acoes")
    @PreAuthorize(Permissoes.ESCRITA_PLANEJAMENTO)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar ação (procedimento) em uma visita")
    public AcaoResponse criarAcao(@PathVariable UUID visitaId, @Valid @RequestBody AcaoRequest dados) {
        return visitaService.criarAcao(visitaId, dados);
    }

    @PutMapping("/visitas/{visitaId}/acoes/{id}")
    @PreAuthorize(Permissoes.ESCRITA_PLANEJAMENTO)
    @Operation(summary = "Atualizar ação de uma visita")
    public AcaoResponse atualizarAcao(@PathVariable UUID visitaId, @PathVariable UUID id,
            @Valid @RequestBody AcaoRequest dados) {
        return visitaService.atualizarAcao(visitaId, id, dados);
    }

    @DeleteMapping("/visitas/{visitaId}/acoes/{id}")
    @PreAuthorize(Permissoes.ESCRITA_PLANEJAMENTO)
    @Operation(summary = "Excluir ação de uma visita")
    public ResponseEntity<Void> excluirAcao(@PathVariable UUID visitaId, @PathVariable UUID id) {
        visitaService.excluirAcao(visitaId, id);
        return ResponseEntity.noContent().build();
    }
}
