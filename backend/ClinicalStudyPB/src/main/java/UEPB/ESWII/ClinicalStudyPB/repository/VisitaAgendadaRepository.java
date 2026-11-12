package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;

public interface VisitaAgendadaRepository extends JpaRepository<VisitaAgendada, UUID> {

    boolean existsByParticipacaoIdAndVisitaPlanejadaId(UUID participacaoId, UUID visitaPlanejadaId);

    Optional<VisitaAgendada> findByParticipacaoIdAndVisitaPlanejadaId(UUID participacaoId, UUID visitaPlanejadaId);

    List<VisitaAgendada> findByParticipacaoIdOrderByVisitaPlanejadaOrdemAsc(UUID participacaoId);

    long countByParticipacaoId(UUID participacaoId);
}
