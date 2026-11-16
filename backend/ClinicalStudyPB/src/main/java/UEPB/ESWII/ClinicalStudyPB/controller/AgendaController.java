package UEPB.ESWII.ClinicalStudyPB.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.AgendaItemResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.ErroResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.PaginaResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.ParticipanteAgendaResponse;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;
import UEPB.ESWII.ClinicalStudyPB.security.Permissoes;
import UEPB.ESWII.ClinicalStudyPB.service.AgendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/estudos/{estudoId}/agenda")
@Tag(name = "Agenda")
public class AgendaController {

    private final AgendaService agendaService;

    public AgendaController(AgendaService agendaService) {
        this.agendaService = agendaService;
    }

    @GetMapping
    @PreAuthorize(Permissoes.LEITURA)
    @Operation(summary = "Agenda do estudo por período e situação",
            description = "Lista visitas de todos os participantes, ordenadas por data e hora, com filtros opcionais "
                    + "de/ate inclusivos e situação. Paginação base zero: pagina (0) e tamanho (20, máximo 100). "
                    + "Leitura de coordenador e pesquisador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página da agenda do estudo"),
            @ApiResponse(responseCode = "400", description = "Parâmetro inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Estudo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public PaginaResponse<AgendaItemResponse> listar(@PathVariable UUID estudoId,
            @Parameter(description = "Data inicial inclusiva (yyyy-MM-dd)", example = "2026-11-01")
            @RequestParam(required = false) LocalDate de,
            @Parameter(description = "Data final inclusiva (yyyy-MM-dd)", example = "2026-11-30")
            @RequestParam(required = false) LocalDate ate,
            @Parameter(description = "Situação da visita agendada")
            @RequestParam(required = false) SituacaoVisita situacao,
            @Parameter(description = "Página, base 0", example = "0")
            @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Itens por página (máximo 100)", example = "20")
            @RequestParam(defaultValue = "20") int tamanho) {
        return agendaService.listar(estudoId, de, ate, situacao, pagina, tamanho);
    }

    @GetMapping("/participantes")
    @PreAuthorize(Permissoes.LEITURA)
    @Operation(summary = "Participantes do estudo com a contagem de visitas agendadas",
            description = "Lista os participantes por código, com o total de visitas previstas e quantas já têm "
                    + "agendamento em qualquer situação. Leitura de coordenador e pesquisador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Participantes e andamento do agendamento"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "404", description = "Estudo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))),
            @ApiResponse(responseCode = "409", description = "Regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class)))
    })
    public List<ParticipanteAgendaResponse> participantes(@PathVariable UUID estudoId) {
        return agendaService.participantes(estudoId);
    }
}
