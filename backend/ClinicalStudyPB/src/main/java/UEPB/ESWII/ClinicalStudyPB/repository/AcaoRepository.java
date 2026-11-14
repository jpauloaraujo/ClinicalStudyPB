package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.CategoriaAcao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Acao;

public interface AcaoRepository extends JpaRepository<Acao, UUID> {

    /** Atividades de uma categoria de uma visita prevista, ordenadas pelo nome (T89: atividades de avaliação). */
    List<Acao> findByVisitaIdAndCategoriaOrderByNomeAsc(UUID visitaId, CategoriaAcao categoria);
}
