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

import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuários", description = "RF01 — Cadastro, consulta, atualização e exclusão de usuários por perfil")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(
            summary = "Listar todos os usuários",
            description = "Retorna a lista de usuários cadastrados no sistema com seus respectivos perfis de acesso.")
    @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso")
    public List<Usuario> listarTodos() {
        return usuarioService.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar usuário por ID",
            description = "Consulta os dados de um usuário específico a partir de seu identificador UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado para o UUID informado")
    })
    public Usuario buscarPorId(
            @Parameter(description = "UUID do usuário", example = "11111111-1111-4111-8111-111111111111")
            @PathVariable UUID id) {
        return usuarioService.buscarPorId(id);
    }

    @PostMapping
    @Operation(
            summary = "Cadastrar novo usuário",
            description = "Cria um novo usuário associado a um perfil operacional (ADMINISTRADOR, PESQUISADOR, COORDENADOR ou PARTICIPANTE) com senha criptografada em BCrypt.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou e-mail já cadastrado")
    })
    public ResponseEntity<Usuario> criar(@Valid @RequestBody Usuario usuario) {
        Usuario criado = usuarioService.criar(usuario);
        URI localizacao = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.getId()).toUri();
        return ResponseEntity.created(localizacao).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar usuário existente",
            description = "Atualiza dados cadastrais, perfil e status ativo de um usuário existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou conflito de e-mail"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public Usuario atualizar(
            @Parameter(description = "UUID do usuário a ser atualizado", example = "22222222-2222-4222-8222-222222222222")
            @PathVariable UUID id,
            @Valid @RequestBody Usuario usuario) {
        return usuarioService.atualizar(id, usuario);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Excluir usuário",
            description = "Remove o registro de um usuário a partir de seu identificador UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "UUID do usuário a ser excluído", example = "44444444-4444-4444-8444-444444444444")
            @PathVariable UUID id) {
        usuarioService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
