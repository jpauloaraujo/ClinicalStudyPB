package UEPB.ESWII.ClinicalStudyPB.controller;

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

import UEPB.ESWII.ClinicalStudyPB.controller.dto.DesfechoRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.VisitaAgendadaResponse;
import UEPB.ESWII.ClinicalStudyPB.security.Permissoes;
import UEPB.ESWII.ClinicalStudyPB.service.VisitaAgendadaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * RF13: consulta de uma visita agendada e registro do seu desfecho. O controller só valida o DTO e chama o serviço;
 * as regras estão em {@code DesfechoVisitaService} e na {@code MaquinaEstadosVisita}. O reagendamento
 * ({@code PUT /api/visitas-agendadas/{id}}) é do {@code AgendamentoController}.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Visitas agendadas",
        description = "RF13 — Desfecho da visita (realizada, ausente ou cancelada); escrita do coordenador, leitura de coordenador e pesquisador")
public class VisitaAgendadaController {

    private final VisitaAgendadaService visitaService;

    public VisitaAgendadaController(VisitaAgendadaService visitaService) {
        this.visitaService = visitaService;
    }

    @GetMapping("/visitas-agendadas/{visitaAgendadaId}")
    @PreAuthorize(Permissoes.LEITURA)
    @Operation(summary = "Consultar uma visita agendada",
            description = "Devolve a visita com situação, dados do desfecho (se houver), janela de datas e auditoria. 404 se não existir.")
    public VisitaAgendadaResponse buscar(@PathVariable UUID visitaAgendadaId) {
        return visitaService.buscar(visitaAgendadaId);
    }

    @PostMapping("/visitas-agendadas/{visitaAgendadaId}/desfecho")
    @PreAuthorize(Permissoes.ESCRITA_RECRUTAMENTO)
    @Operation(summary = "Registrar o desfecho de uma visita agendada",
            description = "Um único endpoint para os três tipos: REALIZADA (data e hora reais, que não podem ser futuras), "
                    + "AUSENTE (observação opcional) e CANCELADA (motivo obrigatório, de 5 a 500 caracteres). "
                    + "REALIZADA, AUSENTE e CANCELADA são estados finais: não há como desfazer. O estudo precisa estar em "
                    + "recrutamento ou execução; realizada e ausente só valem quando o dia agendado não é futuro, "
                    + "enquanto cancelar vale a qualquer momento antes do desfecho. Duas requisições simultâneas: uma vence "
                    + "e a outra recebe 409. O autor é o usuário autenticado.")
    public VisitaAgendadaResponse registrarDesfecho(@PathVariable UUID visitaAgendadaId,
            @Valid @RequestBody DesfechoRequest dados, @AuthenticationPrincipal Jwt jwt) {
        return visitaService.registrarDesfecho(visitaAgendadaId, dados, Autenticacao.idDe(jwt));
    }
}
