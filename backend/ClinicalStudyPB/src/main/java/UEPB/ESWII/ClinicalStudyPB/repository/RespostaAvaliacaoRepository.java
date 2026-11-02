package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import UEPB.ESWII.ClinicalStudyPB.domain.model.RespostaAvaliacao;

public interface RespostaAvaliacaoRepository extends JpaRepository<RespostaAvaliacao, UUID> {

    boolean existsByCriterioId(UUID criterioId);
}
