package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;

public interface ParticipacaoRepository extends JpaRepository<Participacao, UUID> {

    List<Participacao> findByEstudoIdOrderByDataInclusaoAscCandidatoCodigoAsc(UUID estudoId);

    long countByEstudoId(UUID estudoId);

    boolean existsByCandidatoId(UUID candidatoId);
}
