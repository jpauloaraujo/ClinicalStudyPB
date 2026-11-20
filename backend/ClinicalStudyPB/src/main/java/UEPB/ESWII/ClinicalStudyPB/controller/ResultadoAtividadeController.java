package UEPB.ESWII.ClinicalStudyPB.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.ResultadoAtividadeResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.ResultadoRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.ResultadosDaVisitaResponse;
import UEPB.ESWII.ClinicalStudyPB.security.Permissoes;
import UEPB.ESWII.ClinicalStudyPB.service.ResultadoAtividadeService;
import UEPB.ESWII.ClinicalStudyPB.service.ResultadoAtividadeService.ComandoResultado;
import UEPB.ESWII.ClinicalStudyPB.service.ResultadoAtividadeService.RespostaDoRegistro;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Resultados fictícios das avaliações de uma visita agendada (RF14, regra 4; T90). Escrita só do pesquisador; leitura
 * do pesquisador e do coordenador; o administrador e o participante recebem 403. Não há exclusão de resultado.
 */
@RestController
@RequestMapping("/api/visitas-agendadas/{visitaAgendadaId}/resultados")
@Tag(name = "Resultados das avaliações",
        description = "RF14 — Resultados fictícios das atividades de avaliação clínica de uma visita realizada; "
                + "escrita só do pesquisador, leitura de pesquisador e coordenador")
public class ResultadoAtividadeController {

    private final ResultadoAtividadeService resultadoService;

    public ResultadoAtividadeController(ResultadoAtividadeService resultadoService) {
        this.resultadoService = resultadoService;
    }

    @GetMapping
    @PreAuthorize(Permissoes.LEITURA)
    @Operation(summary = "Resultados das avaliações de uma visita",
            description = "Lista cada atividade da categoria AVALIACAO_CLINICA da visita prevista desta visita agendada, "
                    + "ordenada pelo nome, com o resultado registrado ou resultado nulo (pendente), e as contagens total, "
                    + "registrados e pendentes (pendentes = total - registrados). Os resultados são fictícios. Responde 200 "
                    + "em qualquer situação da visita; quando ela não aceita registro nem edição, o campo bloqueio diz por "
                    + "quê (VISITA_NAO_REALIZADA, ESTUDO_ENCERRADO ou, a partir da T102, PARTICIPANTE_RETIRADO). Perfis: "
                    + "PESQUISADOR e COORDENADOR leem; ADMINISTRADOR e PARTICIPANTE recebem 403; visita agendada "
                    + "inexistente devolve 404 (RECURSO_NAO_ENCONTRADO).")
    public ResultadosDaVisitaResponse consultar(@PathVariable UUID visitaAgendadaId) {
        return resultadoService.consultarResposta(visitaAgendadaId);
    }

    @PutMapping("/{acaoId}")
    @PreAuthorize(Permissoes.ESCRITA_RESULTADOS)
    @Operation(summary = "Registrar ou editar o resultado de uma atividade",
            description = "Regra 4: só o PESQUISADOR registra resultados, um por atividade da categoria AVALIACAO_CLINICA "
                    + "de uma visita REALIZADA, editável enquanto o estudo não estiver encerrado. Responde 201 no primeiro "
                    + "registro e 200 na edição. Verificações, na ordem: visita e atividade existem (404); visita REALIZADA "
                    + "(409 VISITA_NAO_REALIZADA); atividade da visita prevista (409 ATIVIDADE_NAO_PERTENCE_A_VISITA); "
                    + "categoria avaliação (409 ATIVIDADE_FORA_DA_CATEGORIA_AVALIACAO); estudo não encerrado (409 "
                    + "ESTUDO_ENCERRADO); valor não vazio (400 VALOR_OBRIGATORIO). Tamanhos acima do limite: 400 "
                    + "VALIDACAO_CAMPOS; corpo malformado: 400 CORPO_INVALIDO. O COORDENADOR recebe 403, assim como "
                    + "ADMINISTRADOR e PARTICIPANTE.")
    @ApiResponse(responseCode = "200", description = "Resultado editado.",
            content = @Content(schema = @Schema(implementation = ResultadoAtividadeResponse.class)))
    @ApiResponse(responseCode = "201", description = "Resultado registrado pela primeira vez.",
            content = @Content(schema = @Schema(implementation = ResultadoAtividadeResponse.class)))
    public ResponseEntity<ResultadoAtividadeResponse> registrar(@PathVariable UUID visitaAgendadaId,
            @PathVariable UUID acaoId, @Valid @RequestBody ResultadoRequest dados, @AuthenticationPrincipal Jwt jwt) {
        RespostaDoRegistro resposta = resultadoService.registrarResposta(visitaAgendadaId, acaoId,
                new ComandoResultado(dados.valor(), dados.unidade(), dados.observacao()), Autenticacao.idDe(jwt));
        return ResponseEntity.status(resposta.criado() ? HttpStatus.CREATED : HttpStatus.OK).body(resposta.resultado());
    }
}
