package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;

public interface ParticipacaoRepository extends JpaRepository<Participacao, UUID> {

    List<Participacao> findByEstudoIdOrderByDataInclusaoAscCandidatoCodigoAsc(UUID estudoId);

    @EntityGraph(attributePaths = "candidato")
    List<Participacao> findByEstudoIdOrderByCandidatoCodigoAsc(UUID estudoId);

    /** Participação com o candidato, para o bloco "participante" do histórico (T96) sem consulta extra. */
    @Query("select p from Participacao p join fetch p.candidato where p.id = :id")
    Optional<Participacao> buscarComCandidato(@Param("id") UUID id);

    /** Participações da conta (candidato.usuario), da inclusão mais recente para a mais antiga; sem N+1 (T109). */
    @EntityGraph(attributePaths = {"estudo", "estudo.responsavel"})
    List<Participacao> findByCandidatoUsuarioIdOrderByDataInclusaoDesc(UUID usuarioId);

    @EntityGraph(attributePaths = {"estudo", "estudo.responsavel"})
    Optional<Participacao> findByCandidatoUsuarioIdAndEstudoId(UUID usuarioId, UUID estudoId);

    @EntityGraph(attributePaths = {"estudo", "estudo.responsavel"})
    Optional<Participacao> findByIdAndCandidatoUsuarioId(UUID participacaoId, UUID usuarioId);

    long countByEstudoId(UUID estudoId);

    boolean existsByCandidatoId(UUID candidatoId);
}
