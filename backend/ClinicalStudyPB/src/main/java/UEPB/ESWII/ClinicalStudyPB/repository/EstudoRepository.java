package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;

public interface EstudoRepository extends JpaRepository<Estudo, UUID> {

    @Override
    @EntityGraph(attributePaths = "responsavel")
    List<Estudo> findAll();

    @Override
    @EntityGraph(attributePaths = "responsavel")
    Optional<Estudo> findById(UUID id);

    boolean existsByResponsavelId(UUID responsavelId);
}
