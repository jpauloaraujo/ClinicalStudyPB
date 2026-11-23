package UEPB.ESWII.ClinicalStudyPB.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.HistoricoParticipanteResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.HistoricoParticipanteResponse.ParticipanteDoHistorico;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.HistoricoParticipanteResponse.ResultadoDoHistorico;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.HistoricoParticipanteResponse.ResumoDoHistorico;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.HistoricoParticipanteResponse.VisitaDoHistorico;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoNoHistorico;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Candidato;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.ResultadoAtividade;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaPlanejada;
import UEPB.ESWII.ClinicalStudyPB.repository.ParticipacaoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.ResultadoAtividadeRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaAgendadaRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaPlanejadaRepository;

/**
 * Consulta agregada do histórico de um participante (RF15, regra 5; T96).
 *
 * <p>Junta as <b>visitas previstas</b> do estudo, os <b>agendamentos</b> da participação e os <b>resultados</b> das
 * atividades e devolve uma linha para cada visita prevista, inclusive as ainda não agendadas
 * ({@link SituacaoNoHistorico#NAO_AGENDADA}), ordenadas pelo dia previsto. O resumo é calculado aqui, no servidor.</p>
 *
 * <p><b>Sem N+1:</b> além de carregar a participação (com o candidato), são apenas 3 consultas por requisição,
 * qualquer que seja o número de visitas: previstas do estudo, agendamentos da participação e resultados da
 * participação. A junção é em memória, por id da visita prevista e da visita agendada.</p>
 *
 * <p><b>Somente leitura:</b> este serviço não grava nada e funciona com o estudo em qualquer estado. A permissão por
 * perfil (pesquisador e coordenador) é do controller.</p>
 */
@Service
@Transactional(readOnly = true)
public class HistoricoParticipanteService {

    private static final Comparator<ResultadoDoHistorico> POR_NOME_DA_ATIVIDADE = Comparator
            .comparing(ResultadoDoHistorico::nome, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(ResultadoDoHistorico::nome);

    private final ParticipacaoRepository participacoes;
    private final VisitaPlanejadaRepository visitasPlanejadas;
    private final VisitaAgendadaRepository visitasAgendadas;
    private final ResultadoAtividadeRepository resultados;
    private final Clock clock;

    public HistoricoParticipanteService(ParticipacaoRepository participacoes,
            VisitaPlanejadaRepository visitasPlanejadas, VisitaAgendadaRepository visitasAgendadas,
            ResultadoAtividadeRepository resultados, Clock clock) {
        this.participacoes = participacoes;
        this.visitasPlanejadas = visitasPlanejadas;
        this.visitasAgendadas = visitasAgendadas;
        this.resultados = resultados;
        this.clock = clock;
    }

    /**
     * Histórico completo da participação.
     *
     * @throws RecursoNaoEncontradoException se a participação não existe (404)
     */
    public HistoricoParticipanteResponse historico(UUID participacaoId) {
        Participacao participacao = participacoes.buscarComCandidato(participacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Participação não encontrada."));
        UUID estudoId = participacao.getEstudo().getId();

        // As 3 consultas do histórico (RQ-T96-01); a junção é em memória, sem consulta por visita.
        List<VisitaPlanejada> previstas = visitasPlanejadas.findByEstudoIdOrderByDiaPrevistoAscOrdemAsc(estudoId);
        Map<UUID, VisitaAgendada> agendamentoPorVisita = new HashMap<>();
        for (VisitaAgendada agendada : visitasAgendadas.listarComVisitaPlanejada(participacaoId)) {
            agendamentoPorVisita.put(agendada.getVisitaPlanejada().getId(), agendada);
        }
        Map<UUID, List<ResultadoDoHistorico>> resultadoPorAgendamento = new HashMap<>();
        for (ResultadoAtividade resultado : resultados.listarDaParticipacaoComAtividade(participacaoId)) {
            resultadoPorAgendamento.computeIfAbsent(resultado.getVisitaAgendada().getId(), id -> new ArrayList<>())
                    .add(paraResultado(resultado));
        }

        List<VisitaDoHistorico> visitas = previstas.stream()
                .map(prevista -> paraVisita(prevista, participacao, agendamentoPorVisita.get(prevista.getId()),
                        resultadoPorAgendamento))
                .toList();

        // Ponto de extensão da T104 (RQ-T96-10): a retirada acrescenta aqui a situação da participação e os dados da
        // retirada ao bloco "participante" (ver HistoricoParticipanteResponse.ParticipanteDoHistorico) e a marca
        // RETIRADO ao histórico. Esta task não os preenche.
        return new HistoricoParticipanteResponse(paraParticipante(participacao), ResumoDoHistorico.de(visitas), visitas);
    }

    private static ParticipanteDoHistorico paraParticipante(Participacao participacao) {
        Candidato candidato = participacao.getCandidato();
        return new ParticipanteDoHistorico(participacao.getId(), candidato.getId(), candidato.getCodigo(),
                candidato.getNomeFicticio(), participacao.getDataInclusao());
    }

    private VisitaDoHistorico paraVisita(VisitaPlanejada prevista, Participacao participacao,
            VisitaAgendada agendada, Map<UUID, List<ResultadoDoHistorico>> resultadoPorAgendamento) {
        var dataSugerida = participacao.getDataInclusao().plusDays(prevista.getDiaPrevisto());
        if (agendada == null) {
            return new VisitaDoHistorico(prevista.getId(), prevista.getNome(), prevista.getOrdem(),
                    prevista.getDiaPrevisto(), prevista.getTolerancia(), dataSugerida,
                    SituacaoNoHistorico.NAO_AGENDADA, null, null, null, null, null, List.of());
        }
        List<ResultadoDoHistorico> doAgendamento = new ArrayList<>(
                resultadoPorAgendamento.getOrDefault(agendada.getId(), List.of()));
        doAgendamento.sort(POR_NOME_DA_ATIVIDADE);
        return new VisitaDoHistorico(prevista.getId(), prevista.getNome(), prevista.getOrdem(),
                prevista.getDiaPrevisto(), prevista.getTolerancia(), dataSugerida,
                SituacaoNoHistorico.de(agendada.getSituacao()), agendada.getId(),
                comFuso(agendada.getDataHoraAgendada()), comFuso(agendada.getDataHoraRealizada()),
                agendada.getMotivo(), agendada.getObservacao(), List.copyOf(doAgendamento));
    }

    private static ResultadoDoHistorico paraResultado(ResultadoAtividade resultado) {
        return new ResultadoDoHistorico(resultado.getAcao().getId(), resultado.getAcao().getNome(),
                resultado.getValor(), resultado.getUnidade(), resultado.getObservacao());
    }

    /** Data e hora locais do banco viram data e hora com o fuso do centro de pesquisa (ISO 8601 na resposta). */
    private OffsetDateTime comFuso(LocalDateTime dataHora) {
        return dataHora == null ? null : dataHora.atZone(clock.getZone()).toOffsetDateTime();
    }
}
