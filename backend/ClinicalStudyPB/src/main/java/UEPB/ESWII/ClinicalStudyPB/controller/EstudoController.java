package UEPB.ESWII.ClinicalStudyPB.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
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
import UEPB.ESWII.ClinicalStudyPB.service.EstudoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/estudos")
@Tag(name = "Estudos Clínicos", description = "RF04 — Cadastro, consulta, atualização e exclusão de protocolos de estudos clínicos")
public class EstudoController {

    private final EstudoService estudoService;

    public EstudoController(EstudoService estudoService) {
        this.estudoService = estudoService;
    }

    @GetMapping
    @Operation(
            summary = "Listar todos os estudos clínicos",
            description = "Retorna todos os estudos clínicos cadastrados, incluindo o pesquisador responsável, datas, limite de participantes e status.")
    @ApiResponse(responseCode = "200", description = "Lista de estudos retornada com sucesso")
    public List<Estudo> listarTodos() {
        return estudoService.listarTodos();
    }

    @GetMapping("/{id}")
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
    @Operation(
            summary = "Cadastrar novo estudo clínico",
            description = "Registra um novo estudo clínico vinculado a um pesquisador responsável, validando datas e limite mínimo de participantes.")
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
    @Operation(
            summary = "Atualizar estudo clínico",
            description = "Atualiza as informações principais, datas, limite de vagas, responsável ou estado de um estudo clínico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estudo clínico atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou violação de regra de negócio"),
            @ApiResponse(responseCode = "404", description = "Estudo ou responsável não encontrado")
    })
    public Estudo atualizar(
            @Parameter(description = "UUID do estudo clínico a ser atualizado", example = "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa")
            @PathVariable UUID id,
            @Valid @RequestBody EstudoRequest dados) {
        return estudoService.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
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
