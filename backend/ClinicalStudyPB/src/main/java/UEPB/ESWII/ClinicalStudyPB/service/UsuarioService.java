package UEPB.ESWII.ClinicalStudyPB.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.UsuarioAtualizacaoRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.UsuarioCriacaoRequest;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.repository.EstudoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.UsuarioRepository;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EstudoRepository estudoRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, EstudoRepository estudoRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.estudoRepository = estudoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: " + id));
    }

    public Usuario criar(UsuarioCriacaoRequest dados) {
        if (usuarioRepository.existsByEmail(dados.email())) {
            throw new RegraNegocioException("Já existe um usuário com esse e-mail");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dados.nome());
        usuario.setEmail(dados.email());
        usuario.setSenha(passwordEncoder.encode(dados.senha()));
        usuario.setPerfil(dados.perfil());
        usuario.setAtivo(true);

        return usuarioRepository.save(usuario);
    }

    public Usuario atualizar(UUID id, UsuarioAtualizacaoRequest dados) {
        Usuario existente = buscarPorId(id);
        usuarioRepository.findByEmail(dados.email()).ifPresent(outro -> {
            if (!outro.getId().equals(id)) {
                throw new RegraNegocioException("Já existe um usuário com esse e-mail");
            }
        });

        existente.setNome(dados.nome());
        existente.setEmail(dados.email());
        if (dados.senha() != null) {
            existente.setSenha(passwordEncoder.encode(dados.senha()));
        }
        existente.setPerfil(dados.perfil());
        existente.setAtivo(dados.ativo());
        return usuarioRepository.save(existente);
    }

    public void excluir(UUID id) {
        Usuario usuario = buscarPorId(id);
        // A FK estudos.responsavel_id é ON DELETE RESTRICT; sem esta checagem a API responderia 500
        if (estudoRepository.existsByResponsavelId(id)) {
            throw new RegraNegocioException("Não é possível excluir: o usuário é responsável por estudos. Desative a conta");
        }
        usuarioRepository.delete(usuario);
    }
}
