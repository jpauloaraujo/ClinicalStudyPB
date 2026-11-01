package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;

public interface EstudoRepository extends JpaRepository<Estudo, UUID> {

    @Override
    @EntityGraph(attributePaths = "responsavel")
    List<Estudo> findAll();

    @Override
    @EntityGraph(attributePaths = "responsavel")
    Optional<Estudo> findById(UUID id);

    boolean existsByResponsavelId(UUID responsavelId);

    /**
     * Carrega o estudo com bloqueio de escrita até o fim da transação: operações concorrentes sobre
     * o mesmo estudo (geração do código do candidato, inclusão de participantes) são atendidas uma
     * por vez, para nunca passar do limite de vagas.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Estudo e where e.id = :id")
    Optional<Estudo> buscarComBloqueio(@Param("id") UUID id);
}
