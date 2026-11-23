package UEPB.ESWII.ClinicalStudyPB.controller;

import java.net.URI;
import java.time.Clock;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.ErroResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.RetiradaRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.RetiradaResponse;
import UEPB.ESWII.ClinicalStudyPB.security.Permissoes;
import UEPB.ESWII.ClinicalStudyPB.service.RetiradaService;
import UEPB.ESWII.ClinicalStudyPB.service.RetiradaService.ComandoRetirada;
import UEPB.ESWII.ClinicalStudyPB.service.RetiradaService.ResultadoRetirada;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * RF16 (T103): registro e consulta da retirada de um participante do estudo. Escrita restrita ao coordenador;
 * leitura permitida a coordenador e pesquisador; administrador e participante recebem 403. Não há endpoint para
 * desfazer retirada (irreversível). O {@link RetiradaService} permanece independente deste controller para reuso
 * pelo pedido de retirada no app (RF20, Sprint 4).
 */
@RestController
@RequestMapping("/api/participacoes/{participacaoId}/retirada")
@Tag(name = "Retirada",
        description = "RF16 — Registro e consulta da retirada de participante; escrita só do coordenador, leitura de coordenador e pesquisador")
public class RetiradaController {

    private final RetiradaService retiradaService;
    private final Clock clock;

    public RetiradaController(RetiradaService retiradaService, Clock clock) {
        this.retiradaService = retiradaService;
        this.clock = clock;
    }

    @PostMapping
    @PreAuthorize(Permissoes.ESCRITA_RECRUTAMENTO)
    @Operation(summary = "Registrar a retirada de um participante do estudo",
            description = "Regra 6: registra a retirada de um participante ativo em uma única transação. A retirada é "
                    + "irreversível (o participante passa de ATIVO para RETIRADO), cancela automaticamente as visitas "
                    + "futuras ainda AGENDADA com o motivo \"retirada do estudo\", preserva o histórico e mantém a "
                    + "vaga ocupada (a contagem de incluídos do RF11 não diminui). Perfis: somente COORDENADOR; "
                    + "PESQUISADOR, ADMINISTRADOR e PARTICIPANTE recebem 403.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Retirada registrada",
                    content = @Content(schema = @Schema(implementation = RetiradaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, motivo obrigatório ou data futura",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Participação não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Participante já retirado ou estudo encerrado",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<RetiradaResponse> registrar(@PathVariable UUID participacaoId,
            @Valid @RequestBody RetiradaRequest dados, @AuthenticationPrincipal Jwt jwt) {
        ResultadoRetirada resultado = retiradaService.registrar(participacaoId,
                new ComandoRetirada(dados.motivo(), dados.solicitante(), dados.dataRetirada()),
                Autenticacao.idDe(jwt));
        URI localizacao = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/participacoes/{participacaoId}/retirada")
                .buildAndExpand(participacaoId)
                .toUri();
        return ResponseEntity.created(localizacao)
                .body(RetiradaResponse.de(resultado.retirada(), resultado.visitasCanceladas(), clock.getZone()));
    }

    @GetMapping
    @PreAuthorize(Permissoes.LEITURA)
    @Operation(summary = "Consultar a retirada de um participante",
            description = "Devolve o registro formal de retirada da participação com a quantidade de visitas canceladas "
                    + "pelo motivo \"retirada do estudo\". Responde 404 se a participação não existir ou se o "
                    + "participante ainda estiver ATIVO. Perfis: COORDENADOR e PESQUISADOR leem; ADMINISTRADOR e "
                    + "PARTICIPANTE recebem 403.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro de retirada encontrado",
                    content = @Content(schema = @Schema(implementation = RetiradaResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Participação inexistente ou participante ainda ativo",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public RetiradaResponse buscar(@PathVariable UUID participacaoId) {
        ResultadoRetirada resultado = retiradaService.buscar(participacaoId);
        return RetiradaResponse.de(resultado.retirada(), resultado.visitasCanceladas(), clock.getZone());
    }
}
