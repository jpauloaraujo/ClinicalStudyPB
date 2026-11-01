package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import UEPB.ESWII.ClinicalStudyPB.domain.model.Acao;

public interface AcaoRepository extends JpaRepository<Acao, UUID> {
}
