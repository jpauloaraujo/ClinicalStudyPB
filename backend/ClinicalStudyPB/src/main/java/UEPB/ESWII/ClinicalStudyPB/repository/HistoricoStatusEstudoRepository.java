package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import UEPB.ESWII.ClinicalStudyPB.domain.model.HistoricoStatusEstudo;

public interface HistoricoStatusEstudoRepository extends JpaRepository<HistoricoStatusEstudo, UUID> {

    /** Mudanças de estado do estudo, da mais antiga para a mais recente. */
    List<HistoricoStatusEstudo> findByEstudoIdOrderByDataHoraAsc(UUID estudoId);
}
