package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.ContaParticipanteResponse;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.PerfilUsuario;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    boolean existsByEmail(String email);
    Optional<Usuario> findByEmail(String email);
    long countByPerfilAndAtivoTrue(PerfilUsuario perfil);

    /** Contas de participante ativas que ainda não pertencem a nenhum candidato, por nome (T111, RQ-T111-07). */
    @Query("select new UEPB.ESWII.ClinicalStudyPB.controller.dto.ContaParticipanteResponse(u.id, u.nome, u.email) "
            + "from Usuario u where u.perfil = UEPB.ESWII.ClinicalStudyPB.domain.enums.PerfilUsuario.PARTICIPANTE "
            + "and u.ativo = true and not exists (select 1 from Candidato c where c.usuario = u) "
            + "order by u.nome, u.email")
    List<ContaParticipanteResponse> listarContasDisponiveis();
}
