package UEPB.ESWII.ClinicalStudyPB.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.HistoricoMinhasVisitasResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.MinhaVisitaResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.ResumoComparecimentoResponse;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;
import UEPB.ESWII.ClinicalStudyPB.repository.AcaoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaAgendadaRepository;

/**
 * Consultas de visitas do participante logado (RF18, T116). Parte sempre das participações da conta (regra 7): um
 * {@code estudoId} que não é do participante devolve lista vazia e resumo zerado, nunca dado de outro.
 */
@Service
@Transactional(readOnly = true)
public class MinhasVisitasService {

    static final List<SituacaoVisita> SITUACOES_DO_HISTORICO =
            List.of(SituacaoVisita.REALIZADA, SituacaoVisita.AUSENTE, SituacaoVisita.CANCELADA);

    private final AcessoParticipante acesso;
    private final VisitaAgendadaRepository visitas;
    private final AcaoRepository acoes;
    private final Clock clock;

    public MinhasVisitasService(AcessoParticipante acesso, VisitaAgendadaRepository visitas, AcaoRepository acoes,
            Clock clock) {
        this.acesso = acesso;
        this.visitas = visitas;
        this.acoes = acoes;
        this.clock = clock;
    }

    /** Visitas AGENDADA de agora em diante, da mais próxima para a mais distante. */
    public List<MinhaVisitaResponse> proximas(UUID usuarioId, UUID estudoId) {
        List<UUID> participacoes = idsDasParticipacoes(usuarioId, estudoId);
        if (participacoes.isEmpty()) {
            return List.of();
        }
        List<VisitaAgendada> encontradas = visitas.proximasDasParticipacoes(participacoes,
                SituacaoVisita.AGENDADA, LocalDateTime.now(clock));
        return converter(encontradas);
    }

    /** Visitas REALIZADA, AUSENTE e CANCELADA, da mais recente para a mais antiga, com o resumo do participante. */
    public HistoricoMinhasVisitasResponse historico(UUID usuarioId, UUID estudoId, int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 100) {
            throw new ValidacaoRegraException("PARAMETRO_INVALIDO",
                    "A página deve ser maior ou igual a 0 e o tamanho deve estar entre 1 e 100.");
        }
        List<UUID> participacoes = idsDasParticipacoes(usuarioId, estudoId);
        if (participacoes.isEmpty()) {
            return new HistoricoMinhasVisitasResponse(ResumoComparecimentoResponse.zerado(), List.of(), pagina,
                    tamanho, 0, 0);
        }
        Page<VisitaAgendada> resultado = visitas.historicoDasParticipacoes(participacoes, SITUACOES_DO_HISTORICO,
                PageRequest.of(pagina, tamanho));
        return new HistoricoMinhasVisitasResponse(resumo(participacoes), converter(resultado.getContent()),
                resultado.getNumber(), resultado.getSize(), resultado.getTotalElements(),
                resultado.getTotalPages());
    }

    private List<UUID> idsDasParticipacoes(UUID usuarioId, UUID estudoId) {
        return acesso.participacoesDe(usuarioId).stream()
                .filter(p -> estudoId == null || p.getEstudo().getId().equals(estudoId))
                .map(Participacao::getId)
                .toList();
    }

    private ResumoComparecimentoResponse resumo(Collection<UUID> participacoes) {
        Map<SituacaoVisita, Long> totais = new EnumMap<>(SituacaoVisita.class);
        for (VisitaAgendadaRepository.ContagemPorSituacao c
                : visitas.contarPorSituacao(participacoes, SITUACOES_DO_HISTORICO)) {
            totais.put(c.getSituacao(), c.getTotal());
        }
        return new ResumoComparecimentoResponse(totais.getOrDefault(SituacaoVisita.REALIZADA, 0L),
                totais.getOrDefault(SituacaoVisita.AUSENTE, 0L), totais.getOrDefault(SituacaoVisita.CANCELADA, 0L));
    }

    /** Monta as respostas com os nomes das atividades de todas as visitas numa consulta só. */
    private List<MinhaVisitaResponse> converter(List<VisitaAgendada> encontradas) {
        if (encontradas.isEmpty()) {
            return List.of();
        }
        List<UUID> previstas = encontradas.stream().map(v -> v.getVisitaPlanejada().getId()).distinct().toList();
        Map<UUID, List<String>> nomes = new HashMap<>();
        for (AcaoRepository.NomeDaAtividade a : acoes.nomesDasAtividades(previstas)) {
            nomes.computeIfAbsent(a.getVisitaId(), k -> new ArrayList<>()).add(a.getNome());
        }
        nomes.values().forEach(lista -> lista.sort(String::compareToIgnoreCase));
        return encontradas.stream()
                .map(v -> MinhaVisitaResponse.de(v, nomes.getOrDefault(v.getVisitaPlanejada().getId(), List.of()),
                        clock))
                .toList();
    }
}
