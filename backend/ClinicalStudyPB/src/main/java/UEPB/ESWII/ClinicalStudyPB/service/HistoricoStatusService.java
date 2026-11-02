package UEPB.ESWII.ClinicalStudyPB.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.HistoricoStatusResponse;
import UEPB.ESWII.ClinicalStudyPB.repository.HistoricoStatusEstudoRepository;

/** Consulta do histórico de mudanças de estado de um estudo (RF07). */
@Service
@Transactional(readOnly = true)
public class HistoricoStatusService {

    private final HistoricoStatusEstudoRepository historicoRepository;
    private final EstudoAcesso estudoAcesso;

    public HistoricoStatusService(HistoricoStatusEstudoRepository historicoRepository, EstudoAcesso estudoAcesso) {
        this.historicoRepository = historicoRepository;
        this.estudoAcesso = estudoAcesso;
    }

    /** Mudanças do estudo, da mais antiga para a mais recente. */
    public List<HistoricoStatusResponse> listar(UUID estudoId) {
        estudoAcesso.buscar(estudoId);
        return historicoRepository.findByEstudoIdOrderByDataHoraAsc(estudoId).stream()
                .map(HistoricoStatusResponse::de)
                .toList();
    }
}
