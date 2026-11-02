package UEPB.ESWII.ClinicalStudyPB.service;

import java.util.UUID;

import org.springframework.stereotype.Component;

import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.repository.UsuarioRepository;

/** Resolve o usuário que fez a requisição (claim {@code sub} do token) para registrar quem agiu. */
@Component
public class UsuarioAutenticado {

    private final UsuarioRepository usuarioRepository;

    public UsuarioAutenticado(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario buscar(UUID id) {
        if (id == null) {
            throw new RegraNegocioException("Não foi possível identificar o usuário autenticado");
        }
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: " + id));
    }
}
