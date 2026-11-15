package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;

public interface ParticipacaoRepository extends JpaRepository<Participacao, UUID> {

    List<Participacao> findByEstudoIdOrderByDataInclusaoAscCandidatoCodigoAsc(UUID estudoId);

    /** Participação com o candidato, para o bloco "participante" do histórico (T96) sem consulta extra. */
    @Query("select p from Participacao p join fetch p.candidato where p.id = :id")
    Optional<Participacao> buscarComCandidato(@Param("id") UUID id);

    long countByEstudoId(UUID estudoId);

    boolean existsByCandidatoId(UUID candidatoId);
}
