package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;
import jakarta.persistence.LockModeType;

public interface VisitaAgendadaRepository extends JpaRepository<VisitaAgendada, UUID> {

    boolean existsByParticipacaoIdAndVisitaPlanejadaId(UUID participacaoId, UUID visitaPlanejadaId);

    Optional<VisitaAgendada> findByParticipacaoIdAndVisitaPlanejadaId(UUID participacaoId, UUID visitaPlanejadaId);

    List<VisitaAgendada> findByParticipacaoIdOrderByVisitaPlanejadaOrdemAsc(UUID participacaoId);

    long countByParticipacaoId(UUID participacaoId);

    /** Agendamentos da participação com a visita prevista, numa consulta só (histórico do participante, T96). */
    @Query("select va from VisitaAgendada va join fetch va.visitaPlanejada where va.participacao.id = :participacaoId")
    List<VisitaAgendada> listarComVisitaPlanejada(@Param("participacaoId") UUID participacaoId);

    /**
     * Carrega a visita com bloqueio de escrita da linha até o fim da transação: dois desfechos simultâneos para a
     * mesma visita são atendidos um por vez, e o segundo já encontra a visita finalizada (T81). A consulta não faz
     * {@code join fetch}: o PostgreSQL não aceita {@code FOR UPDATE} com junção externa.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from VisitaAgendada v where v.id = :id")
    Optional<VisitaAgendada> buscarComBloqueio(@Param("id") UUID id);
}
