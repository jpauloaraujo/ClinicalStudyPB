package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaPlanejada;

public interface VisitaPlanejadaRepository extends JpaRepository<VisitaPlanejada, UUID> {

    @EntityGraph(attributePaths = "acoes")
    List<VisitaPlanejada> findByEstudoIdOrderByOrdemAsc(UUID estudoId);

    long countByEstudoId(UUID estudoId);

    boolean existsByEstudoIdAndDiaPrevisto(UUID estudoId, int diaPrevisto);

    boolean existsByEstudoIdAndDiaPrevistoAndIdNot(UUID estudoId, int diaPrevisto, UUID id);
}
