package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import UEPB.ESWII.ClinicalStudyPB.domain.model.ResultadoAtividade;

public interface ResultadoAtividadeRepository extends JpaRepository<ResultadoAtividade, UUID> {

    /** Resultados da visita com quem registrou e quem alterou, numa consulta só (sem N+1 na tela de resultados). */
    @EntityGraph(attributePaths = {"registradoPor", "alteradoPor"})
    List<ResultadoAtividade> findByVisitaAgendadaId(UUID visitaAgendadaId);

    Optional<ResultadoAtividade> findByVisitaAgendadaIdAndAcaoId(UUID visitaAgendadaId, UUID acaoId);

    long countByVisitaAgendadaId(UUID visitaAgendadaId);

    /**
     * Todos os resultados da participação com a atividade e a visita agendada, numa consulta só (histórico do
     * participante, T96). A ordem por nome da atividade é feita na junção, em memória.
     */
    @Query("select r from ResultadoAtividade r join fetch r.acao join fetch r.visitaAgendada va "
            + "where va.participacao.id = :participacaoId")
    List<ResultadoAtividade> listarDaParticipacaoComAtividade(@Param("participacaoId") UUID participacaoId);
}
