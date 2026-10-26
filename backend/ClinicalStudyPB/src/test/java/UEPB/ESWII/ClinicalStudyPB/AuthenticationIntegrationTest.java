package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.UsuarioAtualizacaoRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.UsuarioCriacaoRequest;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.PerfilUsuario;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.repository.UsuarioRepository;
import UEPB.ESWII.ClinicalStudyPB.service.UsuarioService;

/**
 * Testes de integração de autenticação e segurança (Tasks 3060 e 3063).
 *
 * <p>Verifica o endpoint de login, validação de credenciais, emissão e validação
 * criptográfica real de tokens JWT pelo Spring Security Resource Server.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthenticationIntegrationTest {

    private static final String LOGIN_URL = "/api/auth/login";
    private static final String PROTECTED_URL = "/api/usuarios";

    // Usuário fictício existente no seed da migration V2
    private static final String VALID_EMAIL = "admin@clinicalstudy.test";
    private static final String VALID_PASSWORD = "senha-de-teste";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private UsuarioService usuarioService;

    @Value("${jwt.issuer:clinicalstudypb}")
    private String jwtIssuer;

    private static String credentials(String email, String password) {
        return "{\"email\":\"" + email + "\",\"senha\":\"" + password + "\"}";
    }

    @Test
    @DisplayName("Login com credenciais válidas retorna status 200 e Bearer token")
    void loginComCredenciaisValidasRetornaTokenComStatus200() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(credentials(VALID_EMAIL, VALID_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.perfil").doesNotExist())
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    @DisplayName("Login com senha incorreta retorna 401 com mensagem genérica sem expor detalhes")
    void loginComSenhaIncorretaRetorna401SemTokenNemDetalhesDoErro() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(credentials(VALID_EMAIL, "senha-errada")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.erro").value("Credenciais inválidas"));
    }

    @Test
    @DisplayName("Login com e-mail inexistente retorna 401 com a mesma mensagem genérica")
    void loginComEmailInexistenteRetorna401() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(credentials("nao-existe@clinicalstudy.test", VALID_PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.erro").value("Credenciais inválidas"));
    }

    @Test
    @DisplayName("Login com usuário inativo retorna 401 com a mesma mensagem genérica")
    void loginComUsuarioInativoRetorna401() throws Exception {
        String emailInativo = "inativo.teste@clinicalstudy.test";
        Usuario inativo = new Usuario();
        inativo.setNome("Usuário Inativo");
        inativo.setEmail(emailInativo);
        inativo.setSenha(passwordEncoder.encode(VALID_PASSWORD));
        inativo.setPerfil(PerfilUsuario.PARTICIPANTE);
        inativo.setAtivo(false);
        usuarioRepository.saveAndFlush(inativo);

        mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(credentials(emailInativo, VALID_PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.erro").value("Credenciais inválidas"));
    }

    @Test
    @DisplayName("Login com dados obrigatórios ausentes ou inválidos retorna 400")
    void loginComDadosObrigatoriosAusentesRetorna400() throws Exception {
        // Corpo vazio
        mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").isNotEmpty());

        // E-mail inválido e senha vazia
        mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"invalido\",\"senha\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").isNotEmpty());
    }

    @Test
    @DisplayName("Acesso a endpoint protegido sem token retorna 401")
    void acessoAEndpointProtegidoSemTokenRetorna401() throws Exception {
        mockMvc.perform(get(PROTECTED_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Acesso a endpoint protegido com token malformado retorna 401")
    void acessoAEndpointProtegidoComTokenMalformadoRetorna401() throws Exception {
        mockMvc.perform(get(PROTECTED_URL)
                .header("Authorization", "Bearer token.invalido.qualquer"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Acesso a endpoint protegido com token adulterado na assinatura retorna 401")
    void acessoAEndpointProtegidoComTokenAdulteradoRetorna401() throws Exception {
        String tokenValido = obterTokenValidoViaLogin();
        // Adultera um caractere do meio da assinatura (o último carrega bits de preenchimento Base64URL)
        int posicao = tokenValido.lastIndexOf('.') + 10;
        char original = tokenValido.charAt(posicao);
        char adulteradoChar = (original == 'a') ? 'b' : 'a';
        String tokenAdulterado = tokenValido.substring(0, posicao) + adulteradoChar + tokenValido.substring(posicao + 1);

        mockMvc.perform(get(PROTECTED_URL)
                .header("Authorization", "Bearer " + tokenAdulterado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Acesso a endpoint protegido com token expirado retorna 401")
    void acessoAEndpointProtegidoComTokenExpiradoRetorna401() throws Exception {
        Instant passado = Instant.now().minus(10, ChronoUnit.MINUTES);
        Instant expiracaoNoPassado = passado.plus(2, ChronoUnit.MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtIssuer)
                .subject(UUID.randomUUID().toString())
                .issuedAt(passado)
                .expiresAt(expiracaoNoPassado)
                .claim("role", "ADMINISTRADOR")
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String tokenExpirado = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        mockMvc.perform(get(PROTECTED_URL)
                .header("Authorization", "Bearer " + tokenExpirado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Acesso a endpoint protegido com emissor diferente do configurado retorna 401")
    void acessoAEndpointProtegidoComEmissorInvalidoRetorna401() throws Exception {
        Instant agora = Instant.now();
        Instant futuro = agora.plus(30, ChronoUnit.MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("emissor-estranho-invalido")
                .subject(UUID.randomUUID().toString())
                .issuedAt(agora)
                .expiresAt(futuro)
                .claim("role", "ADMINISTRADOR")
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String tokenEmissorInvalido = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        mockMvc.perform(get(PROTECTED_URL)
                .header("Authorization", "Bearer " + tokenEmissorInvalido))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Token válido permite acessar endpoint existente da API")
    void tokenValidoPermiteAcessarEndpointExistente() throws Exception {
        String token = obterTokenValidoViaLogin();

        mockMvc.perform(get(PROTECTED_URL)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Rotas do Swagger UI e OpenAPI continuam acessíveis publicamente sem token")
    void swaggerContinuaAcessivelSemAutenticacao() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.paths['/api/auth/login']").exists());

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Cadastro e atualização continuam usando BCrypt compatível através do PasswordEncoder centralizado")
    void cadastroEAtualizacaoContinuamUsandoBcryptCompativel() {
        String emailTeste = "bcrypt.compat@clinicalstudy.test";
        String senhaInicial = "senha-inicial-123";
        String senhaNova = "senha-nova-456";

        Usuario criado = usuarioService.criar(new UsuarioCriacaoRequest(
                "Teste BCrypt", emailTeste, senhaInicial, PerfilUsuario.COORDENADOR));

        // Valida que o hash foi salvo em BCrypt compatível
        assertThat(passwordEncoder.matches(senhaInicial, criado.getSenha())).isTrue();
        assertThat(new BCryptPasswordEncoder().matches(senhaInicial, criado.getSenha())).isTrue();

        Usuario atualizado = usuarioService.atualizar(criado.getId(), new UsuarioAtualizacaoRequest(
                "Teste BCrypt Atualizado", emailTeste, senhaNova, PerfilUsuario.COORDENADOR, true));

        assertThat(passwordEncoder.matches(senhaNova, atualizado.getSenha())).isTrue();
        assertThat(new BCryptPasswordEncoder().matches(senhaNova, atualizado.getSenha())).isTrue();
    }

    private String obterTokenValidoViaLogin() throws Exception {
        MvcResult result = mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(credentials(VALID_EMAIL, VALID_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }
}
