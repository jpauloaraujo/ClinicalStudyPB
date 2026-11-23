package UEPB.ESWII.ClinicalStudyPB.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.HistoricoMinhasVisitasResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.MeuEstudoDetalheResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.MeuEstudoResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.MinhaVisitaResponse;
import UEPB.ESWII.ClinicalStudyPB.security.Permissoes;
import UEPB.ESWII.ClinicalStudyPB.service.MeusEstudosService;
import UEPB.ESWII.ClinicalStudyPB.service.MinhasVisitasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Base dos endpoints do app do participante (RF17, T109). O participante é sempre o dono do token: nenhum endpoint
 * recebe id de participante. Restrito ao perfil PARTICIPANTE (403 para os demais).
 */
@RestController
@RequestMapping("/api/me")
@PreAuthorize(Permissoes.PARTICIPANTE)
@Tag(name = "Participante (me)",
        description = "RF17 — Estudos do participante logado; identificado pelo token, só o perfil PARTICIPANTE acessa")
public class MeController {

    private final MeusEstudosService meusEstudos;
    private final MinhasVisitasService minhasVisitas;

    public MeController(MeusEstudosService meusEstudos, MinhasVisitasService minhasVisitas) {
        this.meusEstudos = meusEstudos;
        this.minhasVisitas = minhasVisitas;
    }

    @GetMapping("/visitas/proximas")
    @Operation(summary = "Próximas visitas do participante logado",
            description = "Visitas AGENDADA de agora em diante, da mais próxima para a mais distante, com os nomes das "
                    + "atividades previstas e sem resultados. O filtro opcional estudoId que não é do participante "
                    + "devolve lista vazia (200). Perfis: só PARTICIPANTE; os demais recebem 403.")
    public List<MinhaVisitaResponse> proximasVisitas(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) UUID estudoId) {
        return minhasVisitas.proximas(Autenticacao.idDe(jwt), estudoId);
    }

    @GetMapping("/visitas/historico")
    @Operation(summary = "Histórico de comparecimento do participante logado",
            description = "Visitas REALIZADA, AUSENTE e CANCELADA (inclusive as canceladas pela retirada, com o "
                    + "motivo), da mais recente para a mais antiga, paginadas (pagina base 0, tamanho 1 a 100, padrão "
                    + "20), com o resumo de comparecimento independente da página. Perfis: só PARTICIPANTE.")
    public HistoricoMinhasVisitasResponse historicoVisitas(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) UUID estudoId,
            @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamanho) {
        return minhasVisitas.historico(Autenticacao.idDe(jwt), estudoId, pagina, tamanho);
    }

    @GetMapping("/estudos")
    @Operation(summary = "Estudos do participante logado",
            description = "Um item por participação, da inclusão mais recente para a mais antiga. Conta sem candidato "
                    + "vinculado devolve lista vazia (200). Perfis: só PARTICIPANTE; os demais recebem 403.")
    public List<MeuEstudoResponse> estudos(@AuthenticationPrincipal Jwt jwt) {
        return meusEstudos.listar(Autenticacao.idDe(jwt));
    }

    @GetMapping("/estudos/{estudoId}")
    @Operation(summary = "Detalhe de um estudo do participante logado",
            description = "Devolve 404 (RECURSO_NAO_ENCONTRADO) se o estudo não existe ou não é da conta logada. O "
                    + "motivo da retirada nunca é exposto. Perfis: só PARTICIPANTE; os demais recebem 403.")
    public MeuEstudoDetalheResponse estudo(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID estudoId) {
        return meusEstudos.detalhar(Autenticacao.idDe(jwt), estudoId);
    }
}
