package UEPB.ESWII.ClinicalStudyPB.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.VisitaAgendadaResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.VisitaDoParticipanteResponse;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaPlanejada;
import UEPB.ESWII.ClinicalStudyPB.repository.ParticipacaoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaAgendadaRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaPlanejadaRepository;

@Service
public class AgendamentoService {

    private final AgendamentoVisitaService regras;
    private final ParticipacaoRepository participacoes;
    private final VisitaPlanejadaRepository visitasPlanejadas;
    private final VisitaAgendadaRepository visitasAgendadas;
    private final UsuarioAutenticado usuarioAutenticado;
    private final Clock clock;

    public AgendamentoService(AgendamentoVisitaService regras, ParticipacaoRepository participacoes,
            VisitaPlanejadaRepository visitasPlanejadas, VisitaAgendadaRepository visitasAgendadas,
            UsuarioAutenticado usuarioAutenticado, Clock clock) {
        this.regras = regras;
        this.participacoes = participacoes;
        this.visitasPlanejadas = visitasPlanejadas;
        this.visitasAgendadas = visitasAgendadas;
        this.usuarioAutenticado = usuarioAutenticado;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<VisitaDoParticipanteResponse> visitasDoParticipante(UUID participacaoId) {
        Participacao participacao = participacoes.buscarComCandidato(participacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Participação não encontrada: " + participacaoId));
        List<VisitaPlanejada> previstas = visitasPlanejadas
                .findByEstudoIdOrderByOrdemAsc(participacao.getEstudo().getId());
        Map<UUID, VisitaAgendada> agendadas = visitasAgendadas
                .findByParticipacaoIdOrderByVisitaPlanejadaOrdemAsc(participacaoId)
                .stream()
                .collect(Collectors.toMap(v -> v.getVisitaPlanejada().getId(), Function.identity()));

        return previstas.stream()
                .map(visita -> VisitaDoParticipanteResponse.de(visita, participacao,
                        agendadas.containsKey(visita.getId())
                                ? VisitaAgendadaResponse.de(agendadas.get(visita.getId()), clock)
                                : null))
                .toList();
    }

    @Transactional
    public VisitaAgendada agendar(UUID participacaoId, UUID visitaPlanejadaId,
            LocalDateTime dataHora, UUID usuarioId) {
        Participacao participacao = participacoes.buscarComCandidato(participacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Participação não encontrada: " + participacaoId));
        regras.validarEstadoDoEstudo(participacao);
        VisitaPlanejada visita = visitasPlanejadas.findById(visitaPlanejadaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Visita prevista não encontrada: " + visitaPlanejadaId));
        Usuario usuario = usuarioAutenticado.buscar(usuarioId);
        return regras.agendarVisita(participacao, visita, dataHora, usuario);
    }

    @Transactional
    public VisitaAgendada reagendar(UUID visitaAgendadaId, LocalDateTime dataHora, UUID usuarioId) {
        VisitaAgendada visita = visitasAgendadas.buscarParaReagendar(visitaAgendadaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Visita agendada não encontrada: " + visitaAgendadaId));
        Usuario usuario = usuarioAutenticado.buscar(usuarioId);
        return regras.reagendarVisita(visita, dataHora, usuario);
    }
}
