package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;

public interface VisitaAgendadaRepository extends JpaRepository<VisitaAgendada, UUID> {

    interface ContagemPorParticipacao {
        UUID getParticipacaoId();
        long getTotal();
    }

    boolean existsByParticipacaoIdAndVisitaPlanejadaId(UUID participacaoId, UUID visitaPlanejadaId);

    Optional<VisitaAgendada> findByParticipacaoIdAndVisitaPlanejadaId(UUID participacaoId, UUID visitaPlanejadaId);

    @EntityGraph(attributePaths = {"visitaPlanejada", "participacao.candidato", "participacao.estudo",
            "criadoPor", "alteradoPor"})
    List<VisitaAgendada> findByParticipacaoIdOrderByVisitaPlanejadaOrdemAsc(UUID participacaoId);

    @EntityGraph(attributePaths = {"visitaPlanejada", "participacao.candidato", "participacao.estudo",
            "criadoPor", "alteradoPor"})
    @Query("select va from VisitaAgendada va where va.id = :id")
    Optional<VisitaAgendada> buscarParaReagendar(@Param("id") UUID id);

    long countByParticipacaoId(UUID participacaoId);

    @Query(value = """
            select va from VisitaAgendada va
            join fetch va.visitaPlanejada
            join fetch va.participacao p
            join fetch p.candidato
            where p.estudo.id = :estudoId
              and va.dataHoraAgendada >= :de
              and va.dataHoraAgendada <= :ate
              and va.situacao in :situacoes
            """,
            countQuery = """
                    select count(va) from VisitaAgendada va
                    where va.participacao.estudo.id = :estudoId
                      and va.dataHoraAgendada >= :de
                      and va.dataHoraAgendada <= :ate
                      and va.situacao in :situacoes
                    """)
    /**
     * Nenhum parâmetro pode ser nulo: o PostgreSQL não infere o tipo de {@code ? is null}. Quem chama passa um
     * intervalo amplo no lugar de "sem filtro de data" e a lista de todas as situações no lugar de "sem filtro".
     */
    Page<VisitaAgendada> buscarAgenda(@Param("estudoId") UUID estudoId,
            @Param("de") LocalDateTime de, @Param("ate") LocalDateTime ate,
            @Param("situacoes") Collection<SituacaoVisita> situacoes, Pageable pageable);

    @Query("""
            select va.participacao.id as participacaoId, count(va.id) as total
            from VisitaAgendada va
            where va.participacao.estudo.id = :estudoId
            group by va.participacao.id
            """)
    List<ContagemPorParticipacao> contarPorParticipacaoNoEstudo(@Param("estudoId") UUID estudoId);

    /** Agendamentos da participação com a visita prevista, numa consulta só (histórico do participante, T96). */
    @Query("select va from VisitaAgendada va join fetch va.visitaPlanejada where va.participacao.id = :participacaoId")
    List<VisitaAgendada> listarComVisitaPlanejada(@Param("participacaoId") UUID participacaoId);
}
