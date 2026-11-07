package UEPB.ESWII.ClinicalStudyPB.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.CriterioRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.CriterioResponse;
import UEPB.ESWII.ClinicalStudyPB.security.Permissoes;
import UEPB.ESWII.ClinicalStudyPB.service.CriterioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/estudos/{estudoId}/criterios")
@Tag(name = "Critérios", description = "RF05 — Critérios de inclusão e exclusão do estudo; escrita do pesquisador só com o estudo em planejamento")
public class CriterioController {

    private final CriterioService criterioService;

    public CriterioController(CriterioService criterioService) {
        this.criterioService = criterioService;
    }

    @GetMapping
    @PreAuthorize(Permissoes.LEITURA)
    @Operation(summary = "Listar os critérios do estudo", description = "Critérios de inclusão e de exclusão, ordenados por título.")
    public List<CriterioResponse> listar(@PathVariable UUID estudoId) {
        return criterioService.listar(estudoId);
    }

    @PostMapping
    @PreAuthorize(Permissoes.ESCRITA_PLANEJAMENTO)
    @Operation(summary = "Cadastrar critério", description = "Cria um critério de inclusão ou exclusão em planejamento. A descrição de 5 a 500 caracteres é única por tipo e estudo, ignorando maiúsculas; título opcional.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Critério criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "object"))),
            @ApiResponse(responseCode = "409", description = "Estudo fora de planejamento ou descrição repetida",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "object")))
    })
    public ResponseEntity<CriterioResponse> criar(@PathVariable UUID estudoId, @Valid @RequestBody CriterioRequest dados) {
        CriterioResponse criado = criterioService.criar(estudoId, dados);
        URI localizacao = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(localizacao).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize(Permissoes.ESCRITA_PLANEJAMENTO)
    @Operation(summary = "Atualizar critério", description = "O tipo do critério não pode ser alterado.")
    public CriterioResponse atualizar(@PathVariable UUID estudoId, @PathVariable UUID id,
            @Valid @RequestBody CriterioRequest dados) {
        return criterioService.atualizar(estudoId, id, dados);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(Permissoes.ESCRITA_PLANEJAMENTO)
    @Operation(summary = "Excluir critério", description = "Não é possível excluir um critério já usado em avaliações.")
    public ResponseEntity<Void> excluir(@PathVariable UUID estudoId, @PathVariable UUID id) {
        criterioService.excluir(estudoId, id);
        return ResponseEntity.noContent().build();
    }
}
