package UEPB.ESWII.ClinicalStudyPB.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.LoginRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.LoginResponse;
import UEPB.ESWII.ClinicalStudyPB.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controller de autenticação e emissão de tokens de acesso (T20 — Task 3063).
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "RF02 — Autenticação de usuários e emissão de tokens JWT")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Autenticar usuário",
            description = "Valida as credenciais informadas (e-mail e senha) e retorna um Bearer Token JWT para consumo da API.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credenciais válidas e token gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Corpo da requisição inválido ou campos obrigatórios ausentes"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas ou conta inativa")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.autenticar(request);
        return ResponseEntity.ok(response);
    }
}
