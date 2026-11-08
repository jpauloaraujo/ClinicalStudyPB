package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaPlanejada;

public interface VisitaPlanejadaRepository extends JpaRepository<VisitaPlanejada, UUID> {

    @EntityGraph(attributePaths = "acoes")
    List<VisitaPlanejada> findByEstudoIdOrderByOrdemAsc(UUID estudoId);

    /** Descobre o estudo sem carregar seu estado antes de obter o bloqueio de escrita. */
    @Query("select v.estudo.id from VisitaPlanejada v where v.id = :visitaId")
    Optional<UUID> buscarEstudoId(@Param("visitaId") UUID visitaId);

    long countByEstudoId(UUID estudoId);

    boolean existsByEstudoIdAndDiaPrevisto(UUID estudoId, int diaPrevisto);

    boolean existsByEstudoIdAndDiaPrevistoAndIdNot(UUID estudoId, int diaPrevisto, UUID id);
}
