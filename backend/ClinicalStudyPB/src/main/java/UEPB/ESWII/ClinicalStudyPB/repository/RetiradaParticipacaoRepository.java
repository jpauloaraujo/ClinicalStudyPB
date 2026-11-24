package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import UEPB.ESWII.ClinicalStudyPB.domain.model.RetiradaParticipacao;

/** Repositório dos registros de retirada de participante (RF16, T101). */
public interface RetiradaParticipacaoRepository extends JpaRepository<RetiradaParticipacao, UUID> {

    @EntityGraph(attributePaths = {"participacao", "registradoPor"})
    Optional<RetiradaParticipacao> findByParticipacaoId(UUID participacaoId);

    boolean existsByParticipacaoId(UUID participacaoId);
}
