package UEPB.ESWII.ClinicalStudyPB.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.VisitaParticipanteDTO;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Acao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaPlanejada;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaAgendadaRepository;

@Service
public class VisitaParticipanteService {

    @Autowired
    private VisitaAgendadaRepository visitaRepository;

    // 1. Próximas Visitas: AGENDADA, de hoje em diante, ordenadas da mais próxima para a mais distante
    public List<VisitaParticipanteDTO> buscarProximasVisitas(UUID participanteId) {
        LocalDateTime agora = LocalDateTime.now();

        List<VisitaAgendada> visitas = visitaRepository
                .findByParticipacao_Candidato_IdAndSituacaoAndDataHoraAgendadaGreaterThanEqualOrderByDataHoraAgendadaAsc(
                        participanteId,
                        SituacaoVisita.AGENDADA,
                        agora
                );

        return visitas.stream()
                .map(this::converterParaDTO)
                .toList();
    }

    // 2. Histórico: REALIZADA, AUSENTE, CANCELADA, ordenadas da mais recente para a mais antiga, com paginação
    public Page<VisitaParticipanteDTO> buscarHistorico(UUID participanteId, Pageable pageable) {
        List<SituacaoVisita> situacoesFinalizadas = List.of(
                SituacaoVisita.REALIZADA,
                SituacaoVisita.AUSENTE,
                SituacaoVisita.CANCELADA
        );

        Page<VisitaAgendada> paginaVisitas = visitaRepository
                .findByParticipacao_Candidato_IdAndSituacaoInOrderByDataHoraAgendadaDesc(
                        participanteId,
                        situacoesFinalizadas,
                        pageable
                );

        return paginaVisitas.map(this::converterParaDTO);
    }

    private VisitaParticipanteDTO converterParaDTO(VisitaAgendada visita) {
        VisitaPlanejada planejada = visita.getVisitaPlanejada();

        String nomeEstudo = planejada.getEstudo().getTitulo();
        List<String> atividadesPrevistas = planejada.getAcoes().stream()
                .map(Acao::getNome)
                .toList();

        String motivoCancelamento = visita.getSituacao() == SituacaoVisita.CANCELADA
                ? visita.getMotivo()
                : null;

        return new VisitaParticipanteDTO(
                visita.getId().toString(),
                nomeEstudo,
                planejada.getNome(),
                visita.getDataHoraAgendada().toString(),
                visita.getSituacao().name(),
                atividadesPrevistas,
                motivoCancelamento
        );
    }
}