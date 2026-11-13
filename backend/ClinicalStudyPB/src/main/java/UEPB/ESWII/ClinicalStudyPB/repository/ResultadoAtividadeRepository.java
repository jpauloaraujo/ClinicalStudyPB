package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import UEPB.ESWII.ClinicalStudyPB.domain.model.ResultadoAtividade;

public interface ResultadoAtividadeRepository extends JpaRepository<ResultadoAtividade, UUID> {

    List<ResultadoAtividade> findByVisitaAgendadaId(UUID visitaAgendadaId);

    Optional<ResultadoAtividade> findByVisitaAgendadaIdAndAcaoId(UUID visitaAgendadaId, UUID acaoId);

    long countByVisitaAgendadaId(UUID visitaAgendadaId);
}
