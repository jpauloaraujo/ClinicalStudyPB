package UEPB.ESWII.ClinicalStudyPB.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.VisitaParticipanteDTO;
import UEPB.ESWII.ClinicalStudyPB.service.UsuarioAutenticado;
import UEPB.ESWII.ClinicalStudyPB.service.VisitaParticipanteService;

@RestController
@RequestMapping("/me")
public class ParticipanteVisitaController {

    private final VisitaParticipanteService visitaParticipanteService;
    private final UsuarioAutenticado usuarioAutenticado;

    public ParticipanteVisitaController(
            VisitaParticipanteService visitaParticipanteService,
            UsuarioAutenticado usuarioAutenticado) {
        this.visitaParticipanteService = visitaParticipanteService;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    // Ajuste o caminho (ex.: /visits/upcoming) e a forma de extrair o ID do participante/candidato
    @GetMapping("/visits/upcoming")
    public List<VisitaParticipanteDTO> listarProximasVisitas() {
        UUID participanteId = resolverParticipanteId();
        return visitaParticipanteService.buscarProximasVisitas(participanteId);
    }

    // Ajuste o caminho (ex.: /visits/history) conforme o contrato da API
    @GetMapping("/visits/history")
    public Page<VisitaParticipanteDTO> listarHistorico(
            @PageableDefault(size = 10) Pageable pageable) {
        UUID participanteId = resolverParticipanteId();
        return visitaParticipanteService.buscarHistorico(participanteId, pageable);
    }

    private UUID resolverParticipanteId() {
        // Aqui você obtém o UUID do usuário autenticado (ex.: do SecurityContext / token)
        // e resolve o UUID do candidato/participante vinculado usando usuarioAutenticado / repositório/serviço.
        throw new UnsupportedOperationException("Resolver o ID do participante a partir do usuário autenticado");
    }
}