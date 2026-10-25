package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;

public interface EstudoRepository extends JpaRepository<Estudo, UUID> {
}
