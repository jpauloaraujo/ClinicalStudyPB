package UEPB.ESWII.ClinicalStudyPB.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.AgendaItemResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.PaginaResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.ParticipanteAgendaResponse;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;
import UEPB.ESWII.ClinicalStudyPB.repository.EstudoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.ParticipacaoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaAgendadaRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaPlanejadaRepository;

@Service
@Transactional(readOnly = true)
public class AgendaService {

    private final EstudoRepository estudos;
    private final ParticipacaoRepository participacoes;
    private final VisitaPlanejadaRepository visitasPlanejadas;
    private final VisitaAgendadaRepository visitasAgendadas;
    private final Clock clock;

    public AgendaService(EstudoRepository estudos, ParticipacaoRepository participacoes,
            VisitaPlanejadaRepository visitasPlanejadas, VisitaAgendadaRepository visitasAgendadas, Clock clock) {
        this.estudos = estudos;
        this.participacoes = participacoes;
        this.visitasPlanejadas = visitasPlanejadas;
        this.visitasAgendadas = visitasAgendadas;
        this.clock = clock;
    }

    public PaginaResponse<AgendaItemResponse> listar(UUID estudoId, LocalDate de, LocalDate ate,
            SituacaoVisita situacao, int pagina, int tamanho) {
        exigirEstudo(estudoId);
        if (de != null && ate != null && de.isAfter(ate)) {
            throw new ValidacaoRegraException("PARAMETRO_INVALIDO", "O parâmetro 'de' não pode ser posterior a 'ate'.");
        }
        if (pagina < 0 || tamanho < 1 || tamanho > 100) {
            throw new ValidacaoRegraException("PARAMETRO_INVALIDO",
                    "A página deve ser maior ou igual a 0 e o tamanho deve estar entre 1 e 100.");
        }

        PageRequest paginacao = PageRequest.of(pagina, tamanho,
                Sort.by(Sort.Order.asc("dataHoraAgendada"), Sort.Order.asc("id")));
        Page<VisitaAgendada> resultado = visitasAgendadas.buscarAgenda(
                estudoId,
                de == null ? null : de.atStartOfDay(),
                ate == null ? null : ate.atTime(LocalTime.MAX),
                situacao,
                paginacao);
        return PaginaResponse.de(resultado, visita -> AgendaItemResponse.de(visita, clock));
    }

    public List<ParticipanteAgendaResponse> participantes(UUID estudoId) {
        exigirEstudo(estudoId);
        int visitasPrevistas = Math.toIntExact(visitasPlanejadas.countByEstudoId(estudoId));
        Map<UUID, Long> agendamentos = visitasAgendadas.contarPorParticipacaoNoEstudo(estudoId).stream()
                .collect(Collectors.toMap(VisitaAgendadaRepository.ContagemPorParticipacao::getParticipacaoId,
                        VisitaAgendadaRepository.ContagemPorParticipacao::getTotal));
        return participacoes.findByEstudoIdOrderByCandidatoCodigoAsc(estudoId).stream()
                .map(participacao -> ParticipanteAgendaResponse.de(participacao, visitasPrevistas,
                        agendamentos.getOrDefault(participacao.getId(), 0L)))
                .toList();
    }

    private void exigirEstudo(UUID estudoId) {
        if (!estudos.existsById(estudoId)) {
            throw new RecursoNaoEncontradoException("Estudo não encontrado: " + estudoId);
        }
    }
}
