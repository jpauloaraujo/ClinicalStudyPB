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

import UEPB.ESWII.ClinicalStudyPB.controller.dto.InclusaoRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.ParticipacaoResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.VagasResponse;
import UEPB.ESWII.ClinicalStudyPB.security.Permissoes;
import UEPB.ESWII.ClinicalStudyPB.service.ParticipacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/estudos/{estudoId}/participantes")
@Tag(name = "Participantes", description = "RF11 — Inclusão de participantes respeitando o limite de vagas; escrita do coordenador, leitura de coordenador e pesquisador")
public class ParticipacaoController {

    private final ParticipacaoService participacaoService;

    public ParticipacaoController(ParticipacaoService participacaoService) {
        this.participacaoService = participacaoService;
    }

    @GetMapping
    @PreAuthorize(Permissoes.LEITURA)
    @Operation(summary = "Listar participantes incluídos")
    public List<ParticipacaoResponse> listar(@PathVariable UUID estudoId) {
        return participacaoService.listar(estudoId);
    }

    @GetMapping("/vagas")
    @PreAuthorize(Permissoes.LEITURA)
    @Operation(summary = "Resumo de vagas", description = "Incluídos, limite e vagas restantes.")
    public VagasResponse vagas(@PathVariable UUID estudoId) {
        return participacaoService.vagas(estudoId);
    }

    @PostMapping
    @PreAuthorize(Permissoes.ESCRITA_RECRUTAMENTO)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Incluir candidato como participante",
            description = "Exige estudo em recrutamento, candidato do estudo ainda não incluído, avaliação vigente aprovada, consentimento registrado e vaga disponível. Cada falha devolve 409 com um código próprio (CANDIDATO_NAO_ELEGIVEL, CONSENTIMENTO_AUSENTE, VAGAS_ESGOTADAS...).")
    public ParticipacaoResponse incluir(@PathVariable UUID estudoId, @Valid @RequestBody InclusaoRequest dados,
            @AuthenticationPrincipal Jwt jwt) {
        return participacaoService.incluir(estudoId, dados.candidatoId(), Autenticacao.idDe(jwt));
    }
}
