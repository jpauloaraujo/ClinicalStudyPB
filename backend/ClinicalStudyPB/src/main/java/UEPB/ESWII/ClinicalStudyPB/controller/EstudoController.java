package UEPB.ESWII.ClinicalStudyPB.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.EstudoRequest;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.service.EstudoService;
import UEPB.ESWII.ClinicalStudyPB.service.EstudoTransicaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/estudos")
@Tag(name = "Estudos Clínicos", description = "RF04 — Cadastro, consulta, atualização e exclusão de protocolos de estudos clínicos")
public class EstudoController {

    /** RBAC (RF03): pesquisador gerencia estudos; coordenador apenas consulta. */
    private static final String LEITURA = "hasAnyRole('PESQUISADOR', 'COORDENADOR')";
    private static final String ESCRITA = "hasRole('PESQUISADOR')";

    private final EstudoService estudoService;
    private final EstudoTransicaoService transicaoService;

    public EstudoController(EstudoService estudoService, EstudoTransicaoService transicaoService) {
        this.estudoService = estudoService;
        this.transicaoService = transicaoService;
    }

    @GetMapping
    @PreAuthorize(LEITURA)
    @Operation(
            summary = "Listar todos os estudos clínicos",
            description = "Retorna todos os estudos clínicos cadastrados, incluindo o pesquisador responsável, datas, limite de participantes e status.")
    @ApiResponse(responseCode = "200", description = "Lista de estudos retornada com sucesso")
    public List<Estudo> listarTodos() {
        return estudoService.listarTodos();
    }

    @GetMapping("/{id}")
    @PreAuthorize(LEITURA)
    @Operation(
            summary = "Buscar estudo clínico por ID",
            description = "Consulta os detalhes de um protocolo de estudo clínico específico pelo seu UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estudo clínico encontrado"),
            @ApiResponse(responseCode = "404", description = "Estudo clínico não encontrado")
    })
    public Estudo buscarPorId(
            @Parameter(description = "UUID do estudo clínico", example = "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa")
            @PathVariable UUID id) {
        return estudoService.buscarPorId(id);
    }

    @PostMapping
    @PreAuthorize(ESCRITA)
    @Operation(
            summary = "Cadastrar novo estudo clínico",
            description = "Registra um novo estudo clínico em PLANEJAMENTO vinculado a um pesquisador responsável, validando datas e limite mínimo de participantes.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Estudo clínico cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Violação de validação ou data final anterior à data inicial"),
            @ApiResponse(responseCode = "404", description = "Usuário responsável não encontrado")
    })
    public ResponseEntity<Estudo> criar(@Valid @RequestBody EstudoRequest dados) {
        Estudo criado = estudoService.criar(dados);
        URI localizacao = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.getId()).toUri();
        return ResponseEntity.created(localizacao).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize(ESCRITA)
    @Operation(
            summary = "Atualizar estudo clínico",
            description = "Atualiza os dados cadastrais. O estado deve ser mantido; use o endpoint de transições para avançá-lo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estudo clínico atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou violação de regra de negócio"),
            @ApiResponse(responseCode = "404", description = "Estudo ou responsável não encontrado"),
            @ApiResponse(responseCode = "409", description = "O estado só pode mudar pelo endpoint de transições",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "object")))
    })
    public Estudo atualizar(
            @Parameter(description = "UUID do estudo clínico a ser atualizado", example = "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa")
            @PathVariable UUID id,
            @Valid @RequestBody EstudoRequest dados) {
        return estudoService.atualizar(id, dados);
    }

    public record TransicaoRequest(@NotNull(message = "O novo estado é obrigatório") StatusEstudo novoEstado) {}

    @PostMapping("/{id}/transicoes")
    @PreAuthorize(ESCRITA)
    @Operation(summary = "Avançar o estado do estudo",
            description = "Avança um estado por vez, valida critérios e visitas e registra usuário e data no histórico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estudo atualizado"),
            @ApiResponse(responseCode = "400", description = "Novo estado ausente ou inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "object"))),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária", content = @Content),
            @ApiResponse(responseCode = "403", description = "Somente o pesquisador pode avançar",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "object"))),
            @ApiResponse(responseCode = "404", description = "Estudo inexistente",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "object"))),
            @ApiResponse(responseCode = "409", description = "Transição inválida ou pré-condição não atendida",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "object")))
    })
    public Estudo transicionar(@PathVariable UUID id, @Valid @RequestBody TransicaoRequest dados,
            @AuthenticationPrincipal Jwt jwt) {
        return transicaoService.avancar(id, dados.novoEstado(), Autenticacao.idDe(jwt));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(ESCRITA)
    @Operation(
            summary = "Excluir estudo clínico",
            description = "Remove um estudo clínico cadastrado a partir de seu UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Estudo clínico excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Estudo clínico não encontrado")
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "UUID do estudo clínico a ser excluído", example = "bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb")
            @PathVariable UUID id) {
        estudoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
