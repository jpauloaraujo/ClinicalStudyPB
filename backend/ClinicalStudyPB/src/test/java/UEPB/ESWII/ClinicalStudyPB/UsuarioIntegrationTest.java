package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.PerfilUsuario;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(roles = "ADMINISTRADOR")
class UsuarioIntegrationTest {

    private static final String API = "/api/usuarios";
    private static final String SENHA = "senha-task-3045";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void deveCadastrarUsuarioComSenhaProtegidaSemExporSenha() throws Exception {
        String email = "cadastro.task3045@clinicalstudy.test";

        String localizacao = mockMvc.perform(post(API)
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosUsuario(email, SENHA, true)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andReturn().getResponse().getHeader("Location");

        Usuario salvo = usuarioRepository.findByEmail(email).orElseThrow();
        assertThat(localizacao).endsWith(API + "/" + salvo.getId());
        assertThat(new BCryptPasswordEncoder().matches(SENHA, salvo.getSenha())).isTrue();
    }

    @Test
    void deveListarEConsultarUsuarioSemExporSenha() throws Exception {
        Usuario usuario = usuarioExistente("consulta.task3045@clinicalstudy.test");

        mockMvc.perform(get(API))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(usuario.getId().toString())))
                .andExpect(jsonPath("$[*].senha").isEmpty());

        mockMvc.perform(get(API + "/" + usuario.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(usuario.getEmail()))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void deveAtualizarCadastroPerfilEAtivoSemExigirNovaSenha() throws Exception {
        Usuario usuario = usuarioExistente("atualizacao.task3045@clinicalstudy.test");
        String senhaOriginal = usuario.getSenha();
        String novoEmail = "atualizado.task3045@clinicalstudy.test";

        mockMvc.perform(put(API + "/" + usuario.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosUsuario(novoEmail, null, false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Usuário atualizado"))
                .andExpect(jsonPath("$.email").value(novoEmail))
                .andExpect(jsonPath("$.perfil").value("COORDENADOR"))
                .andExpect(jsonPath("$.ativo").value(false))
                .andExpect(jsonPath("$.senha").doesNotExist());

        assertThat(usuarioRepository.findById(usuario.getId()).orElseThrow().getSenha())
                .isEqualTo(senhaOriginal);

        mockMvc.perform(put(API + "/" + usuario.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosUsuario(novoEmail, null, true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void deveTrocarSenhaQuandoUmaNovaSenhaForInformada() throws Exception {
        Usuario usuario = usuarioExistente("senha.task3045@clinicalstudy.test");
        String novaSenha = "nova-senha-task-3045";

        mockMvc.perform(put(API + "/" + usuario.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosUsuario(usuario.getEmail(), novaSenha, true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senha").doesNotExist());

        Usuario salvo = usuarioRepository.findById(usuario.getId()).orElseThrow();
        assertThat(new BCryptPasswordEncoder().matches(novaSenha, salvo.getSenha())).isTrue();
    }

    @Test
    void deveExcluirUsuarioESinalizarQueNaoExisteMais() throws Exception {
        Usuario usuario = usuarioExistente("exclusao.task3045@clinicalstudy.test");

        mockMvc.perform(delete(API + "/" + usuario.getId()))
                .andExpect(status().isNoContent());

        assertThat(usuarioRepository.existsById(usuario.getId())).isFalse();
        mockMvc.perform(get(API + "/" + usuario.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRejeitarEmailDuplicadoNoCadastroENaAtualizacao() throws Exception {
        Usuario primeiro = usuarioExistente("primeiro.task3045@clinicalstudy.test");
        Usuario segundo = usuarioExistente("segundo.task3045@clinicalstudy.test");

        mockMvc.perform(post(API)
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosUsuario(primeiro.getEmail(), SENHA, true)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put(API + "/" + segundo.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosUsuario(primeiro.getEmail(), null, true)))
                .andExpect(status().isBadRequest());

        assertThat(usuarioRepository.findById(segundo.getId()).orElseThrow().getEmail())
                .isEqualTo("segundo.task3045@clinicalstudy.test");
    }

    @Test
    void deveContinuarExigindoSenhaNoCadastro() throws Exception {
        String email = "sem-senha.task3045@clinicalstudy.test";

        mockMvc.perform(post(API)
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosUsuario(email, null, true)))
                .andExpect(status().isBadRequest());

        assertThat(usuarioRepository.existsByEmail(email)).isFalse();
    }

    @Test
    void deveRejeitarNovaSenhaCurtaNaAtualizacao() throws Exception {
        Usuario usuario = usuarioExistente("senha-curta.task3045@clinicalstudy.test");
        String senhaOriginal = usuario.getSenha();

        mockMvc.perform(put(API + "/" + usuario.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosUsuario(usuario.getEmail(), "curta", true)))
                .andExpect(status().isBadRequest());

        assertThat(usuarioRepository.findById(usuario.getId()).orElseThrow().getSenha())
                .isEqualTo(senhaOriginal);
    }

    @Test
    void deveRetornar404ParaUsuarioInexistente() throws Exception {
        String endereco = API + "/" + UUID.randomUUID();

        mockMvc.perform(get(endereco)).andExpect(status().isNotFound());
        mockMvc.perform(put(endereco)
                .contentType(MediaType.APPLICATION_JSON)
                .content(dadosUsuario("inexistente@clinicalstudy.test", null, true)))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete(endereco)).andExpect(status().isNotFound());
    }

    private Usuario usuarioExistente(String email) {
        Usuario usuario = new Usuario();
        usuario.setNome("Usuário original");
        usuario.setEmail(email);
        usuario.setSenha(new BCryptPasswordEncoder().encode(SENHA));
        usuario.setPerfil(PerfilUsuario.PESQUISADOR);
        return usuarioRepository.saveAndFlush(usuario);
    }

    private String dadosUsuario(String email, String senha, boolean ativo) {
        String campoSenha = senha == null ? "" : "\"senha\":\"" + senha + "\",";
        return """
                {"nome":"Usuário atualizado","email":"%s",%s"perfil":"COORDENADOR","ativo":%s}
                """.formatted(email, campoSenha, ativo);
    }
}
