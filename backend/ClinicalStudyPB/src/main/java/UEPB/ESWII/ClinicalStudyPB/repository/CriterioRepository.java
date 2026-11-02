package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import UEPB.ESWII.ClinicalStudyPB.domain.model.Criterio;

public interface CriterioRepository extends JpaRepository<Criterio, UUID> {

    List<Criterio> findByEstudoIdOrderByTituloAsc(UUID estudoId);

    /** Quantidade de critérios de inclusão do estudo; ao menos um é exigido para sair de planejamento. */
    @Query("select count(c) from CriterioInclusao c where c.estudo.id = :estudoId")
    long contarInclusaoPorEstudo(@Param("estudoId") UUID estudoId);
}
