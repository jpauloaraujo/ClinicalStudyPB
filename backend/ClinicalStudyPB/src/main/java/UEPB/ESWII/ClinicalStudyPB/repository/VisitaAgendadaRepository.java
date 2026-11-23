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
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;
import jakarta.persistence.LockModeType;

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

    /**
     * Carrega a visita com bloqueio de escrita da linha até o fim da transação: dois desfechos simultâneos para a
     * mesma visita são atendidos um por vez, e o segundo já encontra a visita finalizada (T81). A consulta não faz
     * {@code join fetch}: o PostgreSQL não aceita {@code FOR UPDATE} com junção externa.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from VisitaAgendada v where v.id = :id")
    Optional<VisitaAgendada> buscarComBloqueio(@Param("id") UUID id);

    /**
     * Visitas futuras de uma participação em uma situação específica (usada na retirada de participante, T102,
     * para cancelar em cascata apenas as visitas {@code AGENDADA} com {@code dataHoraAgendada > agora}).
     */
    @Query("""
            select va from VisitaAgendada va
            where va.participacao.id = :participacaoId
              and va.situacao = :situacao
              and va.dataHoraAgendada > :agora
            order by va.dataHoraAgendada asc, va.id asc
            """)
    List<VisitaAgendada> listarFuturasPorSituacao(@Param("participacaoId") UUID participacaoId,
            @Param("situacao") SituacaoVisita situacao, @Param("agora") LocalDateTime agora);

    long countByParticipacaoIdAndSituacaoAndMotivo(UUID participacaoId, SituacaoVisita situacao, String motivo);

    /** Total de visitas de uma situação (resumo de comparecimento do app, T116). */
    interface ContagemPorSituacao {
        SituacaoVisita getSituacao();
        long getTotal();
    }

    /**
     * Próximas visitas do participante (T116): as de uma situação (AGENDADA) a partir de {@code agora}, da mais
     * próxima para a mais distante. Parte das <b>participações da conta logada</b> (nunca de um id do cliente) e traz a
     * visita prevista e o estudo na mesma consulta, para não haver consulta por linha. {@code participacoes} não pode
     * ser vazio (quem chama devolve a lista vazia antes).
     */
    @Query("""
            select va from VisitaAgendada va
            join fetch va.visitaPlanejada
            join fetch va.participacao p
            join fetch p.estudo
            where p.id in :participacoes
              and va.situacao = :situacao
              and va.dataHoraAgendada >= :agora
            order by va.dataHoraAgendada asc, va.id asc
            """)
    List<VisitaAgendada> proximasDasParticipacoes(@Param("participacoes") Collection<UUID> participacoes,
            @Param("situacao") SituacaoVisita situacao, @Param("agora") LocalDateTime agora);

    /**
     * Histórico de comparecimento do participante (T116): visitas nas situações pedidas (REALIZADA, AUSENTE e
     * CANCELADA), da mais recente para a mais antiga, paginado. O desempate pelo id mantém a ordem estável entre as
     * páginas. Traz a visita prevista e o estudo na mesma consulta.
     */
    @Query(value = """
            select va from VisitaAgendada va
            join fetch va.visitaPlanejada
            join fetch va.participacao p
            join fetch p.estudo
            where p.id in :participacoes
              and va.situacao in :situacoes
            order by va.dataHoraAgendada desc, va.id desc
            """,
            countQuery = """
                    select count(va) from VisitaAgendada va
                    where va.participacao.id in :participacoes
                      and va.situacao in :situacoes
                    """)
    Page<VisitaAgendada> historicoDasParticipacoes(@Param("participacoes") Collection<UUID> participacoes,
            @Param("situacoes") Collection<SituacaoVisita> situacoes, Pageable pageable);

    /** Totais por situação das visitas das participações, para o resumo do histórico (T116): uma consulta só. */
    @Query("""
            select va.situacao as situacao, count(va.id) as total
            from VisitaAgendada va
            where va.participacao.id in :participacoes
              and va.situacao in :situacoes
            group by va.situacao
            """)
    List<ContagemPorSituacao> contarPorSituacao(@Param("participacoes") Collection<UUID> participacoes,
            @Param("situacoes") Collection<SituacaoVisita> situacoes);
}
