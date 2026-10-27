package UEPB.ESWII.ClinicalStudyPB.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.UsuarioAtualizacaoRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.UsuarioCriacaoRequest;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.PerfilUsuario;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.repository.EstudoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.UsuarioRepository;

@Service
@Transactional
public class UsuarioService {

    private static final String MSG_EMAIL_DUPLICADO = "Já existe um usuário com esse e-mail";

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

    /** E-mails são tratados sem diferenciar maiúsculas de minúsculas: guardados e buscados em minúsculas. */
    public static String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public Usuario criar(UsuarioCriacaoRequest dados) {
        String email = normalizarEmail(dados.email());
        if (usuarioRepository.existsByEmail(email)) {
            throw new RegraNegocioException(MSG_EMAIL_DUPLICADO);
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dados.nome());
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(dados.senha()));
        usuario.setPerfil(dados.perfil());
        usuario.setAtivo(true);

        return salvar(usuario);
    }

    public Usuario atualizar(UUID id, UsuarioAtualizacaoRequest dados) {
        return atualizar(id, dados, null);
    }

    /**
     * @param solicitanteId usuário autenticado que faz a alteração (null quando não há token de usuário)
     */
    public Usuario atualizar(UUID id, UsuarioAtualizacaoRequest dados, UUID solicitanteId) {
        Usuario existente = buscarPorId(id);
        String email = normalizarEmail(dados.email());
        usuarioRepository.findByEmail(email).ifPresent(outro -> {
            if (!outro.getId().equals(id)) {
                throw new RegraNegocioException(MSG_EMAIL_DUPLICADO);
            }
        });

        boolean continuaAdminAtivo = dados.perfil() == PerfilUsuario.ADMINISTRADOR && Boolean.TRUE.equals(dados.ativo());
        if (id.equals(solicitanteId)
                && (dados.perfil() != existente.getPerfil() || !Boolean.TRUE.equals(dados.ativo()))) {
            throw new RegraNegocioException("Você não pode desativar nem alterar o perfil da sua própria conta");
        }
        if (ehAdministradorAtivo(existente) && !continuaAdminAtivo) {
            garantirOutroAdministradorAtivo();
        }

        existente.setNome(dados.nome());
        existente.setEmail(email);
        if (dados.senha() != null) {
            existente.setSenha(passwordEncoder.encode(dados.senha()));
        }
        existente.setPerfil(dados.perfil());
        existente.setAtivo(dados.ativo());
        return salvar(existente);
    }

    public void excluir(UUID id) {
        excluir(id, null);
    }

    public void excluir(UUID id, UUID solicitanteId) {
        Usuario usuario = buscarPorId(id);
        if (id.equals(solicitanteId)) {
            throw new RegraNegocioException("Você não pode excluir a sua própria conta");
        }
        if (ehAdministradorAtivo(usuario)) {
            garantirOutroAdministradorAtivo();
        }
        // A FK estudos.responsavel_id é ON DELETE RESTRICT; sem esta checagem a API responderia 500
        if (estudoRepository.existsByResponsavelId(id)) {
            throw new RegraNegocioException("Não é possível excluir: o usuário é responsável por estudos. Desative a conta");
        }
        usuarioRepository.delete(usuario);
    }

    private boolean ehAdministradorAtivo(Usuario usuario) {
        return usuario.getPerfil() == PerfilUsuario.ADMINISTRADOR && usuario.isAtivo();
    }

    // Chamado quando um administrador ativo deixa de sê-lo; o sistema não pode ficar sem nenhum
    private void garantirOutroAdministradorAtivo() {
        if (usuarioRepository.countByPerfilAndAtivoTrue(PerfilUsuario.ADMINISTRADOR) <= 1) {
            throw new RegraNegocioException("Deve existir ao menos um administrador ativo no sistema");
        }
    }

    // Grava na hora para que um cadastro simultâneo com o mesmo e-mail vire erro de regra, não 500
    private Usuario salvar(Usuario usuario) {
        try {
            return usuarioRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException ex) {
            throw new RegraNegocioException(MSG_EMAIL_DUPLICADO);
        }
    }
}
