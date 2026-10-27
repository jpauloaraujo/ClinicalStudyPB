package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.repository.UsuarioRepository;
import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;

/**
 * Regras de proteção das contas e de consistência dos dados, com tokens reais do seed:
 * o administrador não derruba a própria conta nem o último administrador ativo, o
 * responsável de um estudo é sempre pesquisador, e-mails não diferenciam maiúsculas
 * de minúsculas e corpos malformados recebem o mesmo formato de erro.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RegrasDeContaIntegrationTest {

    private static final String USUARIOS = "/api/usuarios";
    private static final String ESTUDOS = "/api/estudos";
    private static final String ADMIN_ID = "11111111-1111-4111-8111-111111111111";
    private static final String PESQUISADOR_ID = "22222222-2222-4222-8222-222222222222";
    private static final String COORDENADOR_ID = "33333333-3333-4333-8333-333333333333";
    private static final String PARTICIPANTE_ID = "44444444-4444-4444-8444-444444444444";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private String admin() throws Exception {
        return TokenDeTeste.bearer(mockMvc, TokenDeTeste.ADMIN);
    }

    private static String atualizacao(String email, String perfil, boolean ativo) {
        return "{\"nome\":\"Nome\",\"email\":\"" + email + "\",\"perfil\":\"" + perfil + "\",\"ativo\":" + ativo + "}";
    }

    private static String estudo(String responsavelId) {
        return "{\"titulo\":\"Estudo\",\"objetivo\":\"Objetivo\",\"responsavelId\":\"" + responsavelId
                + "\",\"limiteParticipantes\":5,\"status\":\"PLANEJAMENTO\"}";
    }

    @Test
    @DisplayName("Administrador não exclui a própria conta")
    void administradorNaoExcluiAPropriaConta() throws Exception {
        mockMvc.perform(delete(USUARIOS + "/" + ADMIN_ID).header("Authorization", admin()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Você não pode excluir a sua própria conta"));

        assertThat(usuarioRepository.existsById(java.util.UUID.fromString(ADMIN_ID))).isTrue();
    }

    @Test
    @DisplayName("Administrador não desativa nem rebaixa a própria conta")
    void administradorNaoDesativaNemRebaixaAPropriaConta() throws Exception {
        String token = admin();

        mockMvc.perform(put(USUARIOS + "/" + ADMIN_ID).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(atualizacao(TokenDeTeste.ADMIN, "ADMINISTRADOR", false)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put(USUARIOS + "/" + ADMIN_ID).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(atualizacao(TokenDeTeste.ADMIN, "PESQUISADOR", true)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Administrador pode editar nome e e-mail da própria conta")
    void administradorEditaOsProprioDados() throws Exception {
        mockMvc.perform(put(USUARIOS + "/" + ADMIN_ID).header("Authorization", admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(atualizacao(TokenDeTeste.ADMIN, "ADMINISTRADOR", true)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("O último administrador ativo não é desativado, rebaixado nem excluído")
    void ultimoAdministradorAtivoEProtegido() throws Exception {
        // Sem token de usuário (só o serviço sabe quem é o último), o seed tem um único administrador
        String token = admin();
        String criado = mockMvc.perform(post(USUARIOS).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Segundo Admin\",\"email\":\"segundo.admin@clinicalstudy.test\","
                                + "\"senha\":\"senha-123456\",\"perfil\":\"ADMINISTRADOR\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String segundoId = com.jayway.jsonpath.JsonPath.read(criado, "$.id");
        String tokenSegundo = TokenDeTeste.bearer(mockMvc, "segundo.admin@clinicalstudy.test", "senha-123456");

        // Com dois ativos, o segundo desativa o primeiro; agora só resta ele
        mockMvc.perform(put(USUARIOS + "/" + ADMIN_ID).header("Authorization", tokenSegundo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(atualizacao(TokenDeTeste.ADMIN, "ADMINISTRADOR", false)))
                .andExpect(status().isOk());

        // O único ativo (o segundo) não pode ser removido por outro administrador inativo/ausente:
        // simulamos com o token do primeiro, ainda válido até expirar
        mockMvc.perform(delete(USUARIOS + "/" + segundoId).header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Deve existir ao menos um administrador ativo no sistema"));
        mockMvc.perform(put(USUARIOS + "/" + segundoId).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(atualizacao("segundo.admin@clinicalstudy.test", "COORDENADOR", true)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Deve existir ao menos um administrador ativo no sistema"));
    }

    @Test
    @DisplayName("Estudo só aceita pesquisador como responsável")
    void estudoExigeResponsavelPesquisador() throws Exception {
        String token = TokenDeTeste.bearer(mockMvc, TokenDeTeste.PESQUISADOR);

        for (String naoPesquisador : new String[] {ADMIN_ID, COORDENADOR_ID, PARTICIPANTE_ID}) {
            mockMvc.perform(post(ESTUDOS).header("Authorization", token)
                            .contentType(MediaType.APPLICATION_JSON).content(estudo(naoPesquisador)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.erro").value("O responsável pelo estudo deve ter o perfil de pesquisador"));
        }
        mockMvc.perform(post(ESTUDOS).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(estudo(PESQUISADOR_ID)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("E-mail é único sem diferenciar maiúsculas e o login aceita qualquer caixa")
    void emailNaoDiferenciaMaiusculas() throws Exception {
        String token = admin();

        mockMvc.perform(post(USUARIOS).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Duplicado\",\"email\":\"PESQUISADOR@ClinicalStudy.test\","
                                + "\"senha\":\"senha-123456\",\"perfil\":\"PESQUISADOR\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Já existe um usuário com esse e-mail"));

        mockMvc.perform(post(USUARIOS).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Maiúsculo\",\"email\":\"Novo.Usuario@ClinicalStudy.test\","
                                + "\"senha\":\"senha-123456\",\"perfil\":\"PARTICIPANTE\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("novo.usuario@clinicalstudy.test"));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"NOVO.USUARIO@clinicalstudy.test\",\"senha\":\"senha-123456\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("JSON malformado, enum inválido e UUID inválido recebem 400 no formato da API")
    void corposInvalidosRecebemErroPadronizado() throws Exception {
        String token = admin();

        mockMvc.perform(post(USUARIOS).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{nome:"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Corpo da requisição inválido ou malformado"));
        mockMvc.perform(post(USUARIOS).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"X\",\"email\":\"x@clinicalstudy.test\",\"senha\":\"senha-123456\",\"perfil\":\"INEXISTENTE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Corpo da requisição inválido ou malformado"));
        mockMvc.perform(get(USUARIOS + "/nao-e-um-uuid").header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    @DisplayName("Validação de estudo devolve mensagens em português")
    void validacaoDeEstudoEmPortugues() throws Exception {
        mockMvc.perform(post(ESTUDOS).header("Authorization", TokenDeTeste.bearer(mockMvc, TokenDeTeste.PESQUISADOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"\",\"objetivo\":\"O\",\"responsavelId\":\"" + PESQUISADOR_ID
                                + "\",\"limiteParticipantes\":5,\"status\":\"PLANEJAMENTO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("titulo: O título é obrigatório"));
    }
}
