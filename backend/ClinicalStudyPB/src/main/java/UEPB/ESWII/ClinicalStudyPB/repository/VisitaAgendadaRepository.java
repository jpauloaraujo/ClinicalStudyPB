package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;

public interface VisitaAgendadaRepository extends JpaRepository<VisitaAgendada, UUID> {

    boolean existsByParticipacaoIdAndVisitaPlanejadaId(UUID participacaoId, UUID visitaPlanejadaId);

    Optional<VisitaAgendada> findByParticipacaoIdAndVisitaPlanejadaId(UUID participacaoId, UUID visitaPlanejadaId);

    List<VisitaAgendada> findByParticipacaoIdOrderByVisitaPlanejadaOrdemAsc(UUID participacaoId);

    long countByParticipacaoId(UUID participacaoId);

    /** Agendamentos da participação com a visita prevista, numa consulta só (histórico do participante, T96). */
    @Query("select va from VisitaAgendada va join fetch va.visitaPlanejada where va.participacao.id = :participacaoId")
    List<VisitaAgendada> listarComVisitaPlanejada(@Param("participacaoId") UUID participacaoId);
}
