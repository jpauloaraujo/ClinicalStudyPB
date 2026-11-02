package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoCandidato;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Candidato;

public interface CandidatoRepository extends JpaRepository<Candidato, UUID> {

    Page<Candidato> findByEstudoId(UUID estudoId, Pageable pagina);

    Page<Candidato> findByEstudoIdAndSituacao(UUID estudoId, SituacaoCandidato situacao, Pageable pagina);

    long countByEstudoId(UUID estudoId);

    boolean existsByEstudoId(UUID estudoId);

    boolean existsByEstudoIdAndNomeFicticioIgnoreCaseAndDataNascimento(
            UUID estudoId, String nomeFicticio, java.time.LocalDate dataNascimento);

    boolean existsByEstudoIdAndNomeFicticioIgnoreCaseAndDataNascimentoAndIdNot(
            UUID estudoId, String nomeFicticio, java.time.LocalDate dataNascimento, UUID id);
}
