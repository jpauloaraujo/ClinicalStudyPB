package UEPB.ESWII.ClinicalStudyPB;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;

/**
 * T26 (PBI-06 / RF03): permissões por perfil e acessos negados.
 *
 * <p>Usa tokens reais emitidos pelo login para os quatro perfis do seed, aplicando
 * a matriz RBAC de {@code docs/arquitetura.md}: usuários só para o administrador;
 * estudos com leitura para pesquisador e coordenador e escrita só do pesquisador.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PermissaoAcessoIntegrationTest {

    private static final String USUARIOS = "/api/usuarios";
    private static final String ESTUDOS = "/api/estudos";
    private static final String ESTUDO_SEED = ESTUDOS + "/aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa";
    private static final String NOVO_ESTUDO = "{\"titulo\":\"Estudo de Permissão\",\"objetivo\":\"Objetivo fictício\","
            + "\"responsavelId\":\"22222222-2222-4222-8222-222222222222\",\"dataInicio\":\"2026-11-01\","
            + "\"dataFim\":\"2027-01-01\",\"limiteParticipantes\":10,\"status\":\"PLANEJAMENTO\"}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Administrador acessa a gestão de usuários")
    void administradorAcessaUsuarios() throws Exception {
        mockMvc.perform(get(USUARIOS).header("Authorization", TokenDeTeste.bearer(mockMvc, TokenDeTeste.ADMIN)))
                .andExpect(status().isOk());
    }

    @ParameterizedTest(name = "{0} recebe 403 em GET /api/usuarios")
    @ValueSource(strings = {TokenDeTeste.PESQUISADOR, TokenDeTeste.COORDENADOR, TokenDeTeste.PARTICIPANTE})
    @DisplayName("Perfis sem permissão não listam usuários")
    void perfisSemPermissaoNaoListamUsuarios(String email) throws Exception {
        mockMvc.perform(get(USUARIOS).header("Authorization", TokenDeTeste.bearer(mockMvc, email)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Acesso negado para o seu perfil"));
    }

    @Test
    @DisplayName("Pesquisador não cadastra usuários")
    void pesquisadorNaoCadastraUsuario() throws Exception {
        mockMvc.perform(post(USUARIOS)
                        .header("Authorization", TokenDeTeste.bearer(mockMvc, TokenDeTeste.PESQUISADOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"X\",\"email\":\"x@clinicalstudy.test\",\"senha\":\"senha-123456\",\"perfil\":\"ADMINISTRADOR\"}"))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest(name = "{0} consulta estudos")
    @ValueSource(strings = {TokenDeTeste.PESQUISADOR, TokenDeTeste.COORDENADOR})
    @DisplayName("Pesquisador e coordenador consultam estudos")
    void pesquisadorECoordenadorConsultamEstudos(String email) throws Exception {
        String token = TokenDeTeste.bearer(mockMvc, email);
        mockMvc.perform(get(ESTUDOS).header("Authorization", token)).andExpect(status().isOk());
        mockMvc.perform(get(ESTUDO_SEED).header("Authorization", token)).andExpect(status().isOk());
    }

    @ParameterizedTest(name = "{0} recebe 403 em GET /api/estudos")
    @ValueSource(strings = {TokenDeTeste.ADMIN, TokenDeTeste.PARTICIPANTE})
    @DisplayName("Administrador e participante não consultam a gestão de estudos")
    void administradorEParticipanteNaoConsultamEstudos(String email) throws Exception {
        mockMvc.perform(get(ESTUDOS).header("Authorization", TokenDeTeste.bearer(mockMvc, email)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Pesquisador cadastra estudo")
    void pesquisadorCadastraEstudo() throws Exception {
        mockMvc.perform(post(ESTUDOS)
                        .header("Authorization", TokenDeTeste.bearer(mockMvc, TokenDeTeste.PESQUISADOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NOVO_ESTUDO))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest(name = "{0} não cadastra, altera nem exclui estudos")
    @ValueSource(strings = {TokenDeTeste.COORDENADOR, TokenDeTeste.ADMIN, TokenDeTeste.PARTICIPANTE})
    @DisplayName("Apenas o pesquisador cadastra, altera e exclui estudos")
    void demaisPerfisNaoAlteramEstudos(String email) throws Exception {
        String token = TokenDeTeste.bearer(mockMvc, email);
        mockMvc.perform(post(ESTUDOS).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(NOVO_ESTUDO))
                .andExpect(status().isForbidden());
        mockMvc.perform(put(ESTUDO_SEED).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(NOVO_ESTUDO))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete(ESTUDO_SEED).header("Authorization", token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Qualquer perfil autenticado consulta os próprios dados em /api/auth/me")
    void usuarioAutenticadoConsultaOsPropriosDados() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", TokenDeTeste.bearer(mockMvc, TokenDeTeste.PARTICIPANTE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(TokenDeTeste.PARTICIPANTE))
                .andExpect(jsonPath("$.perfil").value("PARTICIPANTE"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    @DisplayName("/api/auth/me sem token retorna 401")
    void usuarioAutenticadoSemTokenRetorna401() throws Exception {
        mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }
}
