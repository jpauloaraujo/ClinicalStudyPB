package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import UEPB.ESWII.ClinicalStudyPB.domain.model.AvaliacaoElegibilidade;

public interface AvaliacaoElegibilidadeRepository extends JpaRepository<AvaliacaoElegibilidade, UUID> {

    List<AvaliacaoElegibilidade> findByCandidatoIdOrderByDataAvaliacaoDesc(UUID candidatoId);

    Optional<AvaliacaoElegibilidade> findByCandidatoIdAndVigenteTrue(UUID candidatoId);
}
