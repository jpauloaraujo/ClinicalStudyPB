package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import UEPB.ESWII.ClinicalStudyPB.domain.model.Criterio;

public interface CriterioRepository extends JpaRepository<Criterio, UUID> {

    List<Criterio> findByEstudoIdOrderByTituloAsc(UUID estudoId);
}
