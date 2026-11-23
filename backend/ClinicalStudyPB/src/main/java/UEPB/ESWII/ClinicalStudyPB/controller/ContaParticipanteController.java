package UEPB.ESWII.ClinicalStudyPB.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.ContaParticipanteResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.ContaVinculadaResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.VincularContaRequest;
import UEPB.ESWII.ClinicalStudyPB.security.Permissoes;
import UEPB.ESWII.ClinicalStudyPB.service.VinculoContaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Vínculo da conta do participante (RF17, T111). Não há rota para desvincular nem para trocar a conta. As rotas têm
 * bases diferentes, por isso não há {@code @RequestMapping} de classe.
 */
@RestController
@Tag(name = "Conta do participante",
        description = "RF17 — Vínculo da conta de perfil PARTICIPANTE ao participante do estudo; a escrita é do coordenador")
public class ContaParticipanteController {

    private final VinculoContaService vinculoService;

    public ContaParticipanteController(VinculoContaService vinculoService) {
        this.vinculoService = vinculoService;
    }

    @PutMapping("/api/participacoes/{participacaoId}/conta")
    @PreAuthorize(Permissoes.ESCRITA_RECRUTAMENTO)
    @Operation(summary = "Vincula uma conta de participante a um participante do estudo",
            description = "Só o COORDENADOR. Relação um para um, sem desvincular. Verifica, nesta ordem: participação e conta "
                    + "existem (404 RECURSO_NAO_ENCONTRADO); participação ativa (409 PARTICIPANTE_RETIRADO); conta de perfil "
                    + "PARTICIPANTE (409 CONTA_NAO_E_PARTICIPANTE); conta ativa (409 CONTA_INATIVA); participação ainda sem "
                    + "conta (409 PARTICIPACAO_JA_TEM_CONTA); conta ainda sem participante (409 CONTA_JA_VINCULADA). Perfis: "
                    + "PESQUISADOR, ADMINISTRADOR e PARTICIPANTE recebem 403; sem token, 401.")
    public ContaVinculadaResponse vincular(@PathVariable UUID participacaoId,
            @Valid @RequestBody VincularContaRequest corpo) {
        return vinculoService.vincular(participacaoId, corpo.usuarioId());
    }

    @GetMapping("/api/participacoes/{participacaoId}/conta")
    @PreAuthorize(Permissoes.LEITURA)
    @Operation(summary = "Conta vinculada a um participante",
            description = "PESQUISADOR e COORDENADOR leem. Devolve 404 (RECURSO_NAO_ENCONTRADO) se a participação não existe "
                    + "ou não tem conta vinculada. Nunca devolve senha nem hash. ADMINISTRADOR e PARTICIPANTE recebem 403.")
    public ContaVinculadaResponse consultar(@PathVariable UUID participacaoId) {
        return vinculoService.consultar(participacaoId);
    }

    @GetMapping("/api/contas-participante/disponiveis")
    @PreAuthorize(Permissoes.ESCRITA_RECRUTAMENTO)
    @Operation(summary = "Contas de participante disponíveis para vínculo",
            description = "Só o COORDENADOR. Contas ativas, de perfil PARTICIPANTE e ainda não vinculadas a nenhum "
                    + "participante, por nome. Devolve só id, nome e e-mail. Os demais perfis recebem 403.")
    public List<ContaParticipanteResponse> disponiveis() {
        return vinculoService.contasDisponiveis();
    }
}
