package UEPB.ESWII.ClinicalStudyPB.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.CandidatoRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.CandidatoResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.PaginaResponse;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoCandidato;
import UEPB.ESWII.ClinicalStudyPB.security.Permissoes;
import UEPB.ESWII.ClinicalStudyPB.service.CandidatoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@Tag(name = "Candidatos", description = "RF08 — Candidatos em triagem (dados fictícios); escrita do coordenador, leitura de coordenador e pesquisador")
public class CandidatoController {

    private final CandidatoService candidatoService;

    public CandidatoController(CandidatoService candidatoService) {
        this.candidatoService = candidatoService;
    }

    @GetMapping("/estudos/{estudoId}/candidatos")
    @PreAuthorize(Permissoes.LEITURA)
    @Operation(summary = "Listar candidatos do estudo",
            description = "Lista paginada (ordenada pelo código), com filtro opcional por situação.")
    public PaginaResponse<CandidatoResponse> listar(@PathVariable UUID estudoId,
            @RequestParam(required = false) SituacaoCandidato situacao,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return candidatoService.listar(estudoId, situacao, pagina, tamanho);
    }

    @PostMapping("/estudos/{estudoId}/candidatos")
    @PreAuthorize(Permissoes.ESCRITA_RECRUTAMENTO)
    @Operation(summary = "Cadastrar candidato",
            description = "Só com o estudo em recrutamento. O código (CAND-001...) é gerado pelo sistema; nome e data de nascimento não podem repetir no estudo (409).")
    public ResponseEntity<CandidatoResponse> criar(@PathVariable UUID estudoId,
            @Valid @RequestBody CandidatoRequest dados) {
        CandidatoResponse criado = candidatoService.criar(estudoId, dados);
        URI localizacao = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/candidatos/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(localizacao).body(criado);
    }

    @GetMapping("/candidatos/{id}")
    @PreAuthorize(Permissoes.LEITURA)
    @Operation(summary = "Detalhe do candidato",
            description = "Inclui o resumo da avaliação vigente e do consentimento.")
    public CandidatoResponse buscar(@PathVariable UUID id) {
        return candidatoService.buscar(id);
    }

    @PutMapping("/candidatos/{id}")
    @PreAuthorize(Permissoes.ESCRITA_RECRUTAMENTO)
    @Operation(summary = "Atualizar candidato", description = "Permitido até a inclusão.")
    public CandidatoResponse atualizar(@PathVariable UUID id, @Valid @RequestBody CandidatoRequest dados) {
        return candidatoService.atualizar(id, dados);
    }
}
