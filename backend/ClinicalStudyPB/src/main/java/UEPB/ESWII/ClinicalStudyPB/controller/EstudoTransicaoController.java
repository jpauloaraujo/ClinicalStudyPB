package UEPB.ESWII.ClinicalStudyPB.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.HistoricoStatusResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.TransicaoRequest;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.security.Permissoes;
import UEPB.ESWII.ClinicalStudyPB.service.EstudoTransicaoService;
import UEPB.ESWII.ClinicalStudyPB.service.HistoricoStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/estudos/{estudoId}")
@Tag(name = "Estado do estudo", description = "RF07 — Avanço do estado (planejamento, recrutamento, execução, encerrado), sem pular nem voltar; só o pesquisador avança")
public class EstudoTransicaoController {

    private final EstudoTransicaoService transicaoService;
    private final HistoricoStatusService historicoService;

    public EstudoTransicaoController(EstudoTransicaoService transicaoService, HistoricoStatusService historicoService) {
        this.transicaoService = transicaoService;
        this.historicoService = historicoService;
    }

    @PostMapping("/transicoes")
    @PreAuthorize(Permissoes.ESCRITA_PLANEJAMENTO)
    @Operation(summary = "Avançar o estado do estudo",
            description = "Aceita só o estado seguinte ao atual. Para sair de planejamento exige ao menos um critério de inclusão e uma visita prevista. Cada avanço grava usuário e data no histórico. Devolve o estudo já no novo estado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado alterado; devolve o estudo atualizado"),
            @ApiResponse(responseCode = "400", description = "Estado de destino ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão (só o pesquisador avança)"),
            @ApiResponse(responseCode = "404", description = "Estudo não encontrado"),
            @ApiResponse(responseCode = "409", description = "Transição inválida ou pré-condição não atendida (TRANSICAO_ESTADO_INVALIDA, ESTUDO_SEM_CRITERIO_INCLUSAO, ESTUDO_SEM_VISITA_PREVISTA)")
    })
    public Estudo avancar(@PathVariable UUID estudoId, @Valid @RequestBody TransicaoRequest dados,
            @AuthenticationPrincipal Jwt jwt) {
        return transicaoService.avancar(estudoId, dados.destino(), Autenticacao.idDe(jwt));
    }

    @GetMapping("/historico-status")
    @PreAuthorize(Permissoes.LEITURA)
    @Operation(summary = "Histórico de mudanças de estado do estudo",
            description = "Estado anterior, novo estado, usuário e data de cada avanço, do mais antigo ao mais recente.")
    public List<HistoricoStatusResponse> historico(@PathVariable UUID estudoId) {
        return historicoService.listar(estudoId);
    }
}
