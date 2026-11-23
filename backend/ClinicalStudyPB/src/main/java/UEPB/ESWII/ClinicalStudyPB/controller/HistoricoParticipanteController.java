package UEPB.ESWII.ClinicalStudyPB.controller;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.HistoricoParticipanteResponse;
import UEPB.ESWII.ClinicalStudyPB.security.Permissoes;
import UEPB.ESWII.ClinicalStudyPB.service.HistoricoParticipanteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Histórico de um participante (RF15). Somente leitura: não há {@code POST}, {@code PUT} nem {@code DELETE} neste
 * recurso.
 */
@RestController
@RequestMapping("/api/participacoes/{participacaoId}/historico")
@Tag(name = "Histórico do participante",
        description = "RF15 — Consulta do histórico de visitas e resultados de um participante; somente leitura, para pesquisador e coordenador")
public class HistoricoParticipanteController {

    private final HistoricoParticipanteService historicoService;

    public HistoricoParticipanteController(HistoricoParticipanteService historicoService) {
        this.historicoService = historicoService;
    }

    @GetMapping
    @PreAuthorize(Permissoes.LEITURA)
    @Operation(summary = "Histórico de um participante",
            description = "Somente leitura. Devolve os dados do participante, o resumo por situação e uma linha para cada "
                    + "visita prevista do estudo, ordenadas pelo dia previsto (e pela ordem), inclusive as ainda não "
                    + "agendadas (situacao = NAO_AGENDADA, com os campos do agendamento nulos). Cada visita traz a situação, "
                    + "as datas e os resultados das atividades. pendentes = agendadas + naoAgendadas. Funciona com o estudo "
                    + "em qualquer estado. Perfis: PESQUISADOR e COORDENADOR leem; ADMINISTRADOR e PARTICIPANTE recebem 403; "
                    + "participação inexistente devolve 404 (RECURSO_NAO_ENCONTRADO).")
    public HistoricoParticipanteResponse historico(@PathVariable UUID participacaoId) {
        return historicoService.historico(participacaoId);
    }
}
