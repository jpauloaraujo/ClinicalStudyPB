package UEPB.ESWII.ClinicalStudyPB.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.AvaliacaoRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.AvaliacaoResponse;
import UEPB.ESWII.ClinicalStudyPB.security.Permissoes;
import UEPB.ESWII.ClinicalStudyPB.service.AvaliacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/candidatos/{candidatoId}/avaliacoes")
@Tag(name = "Elegibilidade", description = "RF09 — Avaliação de elegibilidade do candidato; escrita do coordenador, leitura de coordenador e pesquisador")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @GetMapping
    @PreAuthorize(Permissoes.LEITURA)
    @Operation(summary = "Histórico de avaliações do candidato", description = "Da mais recente (vigente) para a mais antiga.")
    public List<AvaliacaoResponse> historico(@PathVariable UUID candidatoId) {
        return avaliacaoService.historico(candidatoId);
    }

    @PostMapping
    @PreAuthorize(Permissoes.ESCRITA_RECRUTAMENTO)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar avaliação de elegibilidade",
            description = "Exige uma resposta para cada critério do estudo. Elegível = todos os critérios de inclusão determinantes atendidos e nenhum de exclusão satisfeito. A avaliação anterior vai para o histórico.")
    public AvaliacaoResponse avaliar(@PathVariable UUID candidatoId, @Valid @RequestBody AvaliacaoRequest dados,
            @AuthenticationPrincipal Jwt jwt) {
        return avaliacaoService.avaliar(candidatoId, dados, Autenticacao.idDe(jwt));
    }
}
