package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import UEPB.ESWII.ClinicalStudyPB.domain.model.ResultadoAtividade;

public interface ResultadoAtividadeRepository extends JpaRepository<ResultadoAtividade, UUID> {

    /** Resultados da visita com quem registrou e quem alterou, numa consulta só (sem N+1 na tela de resultados). */
    @EntityGraph(attributePaths = {"registradoPor", "alteradoPor"})
    List<ResultadoAtividade> findByVisitaAgendadaId(UUID visitaAgendadaId);

    Optional<ResultadoAtividade> findByVisitaAgendadaIdAndAcaoId(UUID visitaAgendadaId, UUID acaoId);

    long countByVisitaAgendadaId(UUID visitaAgendadaId);
}
