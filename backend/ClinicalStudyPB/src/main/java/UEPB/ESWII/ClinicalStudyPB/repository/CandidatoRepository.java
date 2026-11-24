package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.UUID;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoCandidato;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Candidato;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;

public interface CandidatoRepository extends JpaRepository<Candidato, UUID> {

    @Query("select c.estudo.id from Candidato c where c.id = :candidatoId")
    Optional<UUID> buscarEstudoId(@Param("candidatoId") UUID candidatoId);

    Page<Candidato> findByEstudoId(UUID estudoId, Pageable pagina);

    Page<Candidato> findByEstudoIdAndSituacao(UUID estudoId, SituacaoCandidato situacao, Pageable pagina);

    long countByEstudoId(UUID estudoId);

    /** Há candidato ligado a esta conta (relação um para um, T111). */
    boolean existsByUsuarioId(UUID usuarioId);

    /**
     * Liga a conta ao candidato só se ele ainda não tem conta (T111, RQ-T111-04). Devolve as linhas afetadas: 0 quer
     * dizer que outra requisição vinculou antes. A violação do UNIQUE (mesma conta em outro candidato) estoura aqui,
     * dentro da chamada, e não no commit.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Candidato c set c.usuario = :usuario where c.id = :candidatoId and c.usuario is null")
    int vincularConta(@Param("candidatoId") UUID candidatoId, @Param("usuario") Usuario usuario);

    boolean existsByEstudoId(UUID estudoId);

    boolean existsByEstudoIdAndNomeFicticioIgnoreCaseAndDataNascimento(
            UUID estudoId, String nomeFicticio, java.time.LocalDate dataNascimento);

    boolean existsByEstudoIdAndNomeFicticioIgnoreCaseAndDataNascimentoAndIdNot(
            UUID estudoId, String nomeFicticio, java.time.LocalDate dataNascimento, UUID id);
}
