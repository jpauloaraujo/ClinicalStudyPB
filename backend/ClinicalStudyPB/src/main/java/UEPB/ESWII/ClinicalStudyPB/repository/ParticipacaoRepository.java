package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;
import jakarta.persistence.LockModeType;

public interface ParticipacaoRepository extends JpaRepository<Participacao, UUID> {

    List<Participacao> findByEstudoIdOrderByDataInclusaoAscCandidatoCodigoAsc(UUID estudoId);

    @EntityGraph(attributePaths = "candidato")
    List<Participacao> findByEstudoIdOrderByCandidatoCodigoAsc(UUID estudoId);

    /** Participação com o candidato, para o bloco "participante" do histórico (T96) sem consulta extra. */
    @Query("select p from Participacao p join fetch p.candidato where p.id = :id")
    Optional<Participacao> buscarComCandidato(@Param("id") UUID id);

    /**
     * Carrega a participação com bloqueio de escrita da linha até o fim da transação (T102): serializa retiradas
     * simultâneas para o mesmo participante, garantindo que a segunda já encontre a participação retirada. Sem
     * {@code join fetch}: o PostgreSQL não aceita {@code FOR UPDATE} com junção externa.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Participacao p where p.id = :id")
    Optional<Participacao> buscarComBloqueio(@Param("id") UUID id);

    long countByEstudoId(UUID estudoId);

    boolean existsByCandidatoId(UUID candidatoId);
}
