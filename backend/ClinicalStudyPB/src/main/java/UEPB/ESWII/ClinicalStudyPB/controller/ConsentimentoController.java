package UEPB.ESWII.ClinicalStudyPB.controller;

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

import UEPB.ESWII.ClinicalStudyPB.controller.dto.ConsentimentoRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.ConsentimentoResponse;
import UEPB.ESWII.ClinicalStudyPB.security.Permissoes;
import UEPB.ESWII.ClinicalStudyPB.service.ConsentimentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/candidatos/{candidatoId}/consentimento")
@Tag(name = "Consentimento", description = "RF10 — Consentimento simulado (TCLE); escrita do coordenador, leitura de coordenador e pesquisador")
public class ConsentimentoController {

    private final ConsentimentoService consentimentoService;

    public ConsentimentoController(ConsentimentoService consentimentoService) {
        this.consentimentoService = consentimentoService;
    }

    @GetMapping
    @PreAuthorize(Permissoes.LEITURA)
    @Operation(summary = "Comprovante do consentimento", description = "404 se ainda não foi registrado.")
    public ConsentimentoResponse buscar(@PathVariable UUID candidatoId) {
        return consentimentoService.buscar(candidatoId);
    }

    @PostMapping
    @PreAuthorize(Permissoes.ESCRITA_RECRUTAMENTO)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar consentimento",
            description = "Um por candidato, com o estudo em recrutamento e o candidato ainda não incluído. O responsável é o usuário autenticado.")
    public ConsentimentoResponse registrar(@PathVariable UUID candidatoId,
            @Valid @RequestBody(required = false) ConsentimentoRequest dados, @AuthenticationPrincipal Jwt jwt) {
        ConsentimentoRequest pedido = dados == null ? new ConsentimentoRequest(null, null) : dados;
        return consentimentoService.registrar(candidatoId, pedido, Autenticacao.idDe(jwt));
    }
}
