package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import UEPB.ESWII.ClinicalStudyPB.domain.model.Consentimento;

public interface ConsentimentoRepository extends JpaRepository<Consentimento, UUID> {

    Optional<Consentimento> findByCandidatoId(UUID candidatoId);

    boolean existsByCandidatoId(UUID candidatoId);
}
