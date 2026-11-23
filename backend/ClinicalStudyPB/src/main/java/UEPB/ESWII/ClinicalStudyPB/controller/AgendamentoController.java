package UEPB.ESWII.ClinicalStudyPB.controller;

import java.net.URI;
import java.time.Clock;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.ErroResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.VisitaAgendadaRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.VisitaAgendadaResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.VisitaDoParticipanteResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.VisitaReagendamentoRequest;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;
import UEPB.ESWII.ClinicalStudyPB.security.Permissoes;
import UEPB.ESWII.ClinicalStudyPB.service.AgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@Tag(name = "Agendamento")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;
    private final Clock clock;

    public AgendamentoController(AgendamentoService agendamentoService, Clock clock) {
        this.agendamentoService = agendamentoService;
        this.clock = clock;
    }

    @GetMapping("/participacoes/{participacaoId}/visitas")
    @PreAuthorize(Permissoes.LEITURA)
    @Operation(summary = "Visitas previstas do participante e seus agendamentos",
            description = "Devolve todas as visitas previstas do estudo, ordenadas por ordem, com a data sugerida, "
                    + "a janela válida e o agendamento existente, se houver. Leitura de pesquisador e coordenador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Visitas previstas com os respectivos agendamentos"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Participação não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public List<VisitaDoParticipanteResponse> visitasDoParticipante(@PathVariable UUID participacaoId) {
        return agendamentoService.visitasDoParticipante(participacaoId);
    }

    @PostMapping("/participacoes/{participacaoId}/visitas-agendadas")
    @PreAuthorize(Permissoes.ESCRITA_RECRUTAMENTO)
    @Operation(summary = "Agendar uma visita do participante",
            description = "Agenda uma visita prevista dentro da janela válida. A escrita é exclusiva do coordenador; "
                    + "a data e hora recebidas são locais, sem fuso.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Visita agendada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou data no passado",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Participação ou visita prevista não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Estudo fora de estado, visita já agendada ou data fora da janela",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public ResponseEntity<VisitaAgendadaResponse> agendar(@PathVariable UUID participacaoId,
            @Valid @RequestBody VisitaAgendadaRequest dados, @AuthenticationPrincipal Jwt jwt) {
        VisitaAgendada criada = agendamentoService.agendar(participacaoId, dados.visitaPlanejadaId(),
                dados.dataHora(), Autenticacao.idDe(jwt));
        URI localizacao = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/visitas-agendadas/{id}").buildAndExpand(criada.getId()).toUri();
        return ResponseEntity.created(localizacao).body(VisitaAgendadaResponse.de(criada, clock));
    }

    @PutMapping("/visitas-agendadas/{visitaAgendadaId}")
    @PreAuthorize(Permissoes.ESCRITA_RECRUTAMENTO)
    @Operation(summary = "Reagendar uma visita",
            description = "Altera a data e hora de uma visita ainda AGENDADA, respeitando a mesma janela. "
                    + "A escrita é exclusiva do coordenador; a data e hora recebidas são locais, sem fuso.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Visita reagendada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou data no passado",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Visita agendada não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Estudo fora de estado, visita não agendada ou data fora da janela",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public VisitaAgendadaResponse reagendar(@PathVariable UUID visitaAgendadaId,
            @Valid @RequestBody VisitaReagendamentoRequest dados, @AuthenticationPrincipal Jwt jwt) {
        return VisitaAgendadaResponse.de(
                agendamentoService.reagendar(visitaAgendadaId, dados.dataHora(), Autenticacao.idDe(jwt)), clock);
    }
}
