package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.CategoriaAcao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Acao;

public interface AcaoRepository extends JpaRepository<Acao, UUID> {

    /** Nome de uma atividade prevista e a visita prevista a que pertence (projeção, sem carregar a entidade). */
    interface NomeDaAtividade {
        UUID getVisitaId();
        String getNome();
    }

    /** Atividades de uma categoria de uma visita prevista, ordenadas pelo nome (T89: atividades de avaliação). */
    List<Acao> findByVisitaIdAndCategoriaOrderByNomeAsc(UUID visitaId, CategoriaAcao categoria);

    /**
     * Nomes das atividades previstas de várias visitas previstas, numa consulta só (consultas de visitas do
     * participante, T116). Só os nomes: o app não recebe descrição, categoria nem resultados.
     */
    @Query("select a.visita.id as visitaId, a.nome as nome from Acao a where a.visita.id in :visitas")
    List<NomeDaAtividade> nomesDasAtividades(@Param("visitas") Collection<UUID> visitas);
}
