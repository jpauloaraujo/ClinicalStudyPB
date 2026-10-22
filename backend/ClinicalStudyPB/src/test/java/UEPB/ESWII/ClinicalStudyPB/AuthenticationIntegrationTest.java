package UEPB.ESWII.ClinicalStudyPB;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * T22 (PBI-05 / RF02): login válido, login inválido e acesso não autenticado.
 *
 * Os testes seguem o contrato combinado para a API de autenticação. Ajustar as
 * constantes abaixo se o contrato final (T19/T20) for diferente.
 *
 * Desabilitados até T19/T20 e a configuração de banco de teste estarem no main.
 */
@Disabled("Aguardando T19/T20 (Spring Security + endpoint de login) e banco de teste")
@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationIntegrationTest {

    private static final String LOGIN_URL = "/api/auth/login";
    private static final String PROTECTED_URL = "/api/usuarios";

    // Usuário fictício que deve existir via seed/fixture de teste.
    private static final String VALID_EMAIL = "admin@clinicalstudy.test";
    private static final String VALID_PASSWORD = "senha-de-teste";

    @Autowired
    private MockMvc mockMvc;

    private static String credentials(String email, String password) {
        return "{\"email\":\"" + email + "\",\"senha\":\"" + password + "\"}";
    }

    @Test
    void loginComCredenciaisValidasRetornaTokenComStatus200() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(credentials(VALID_EMAIL, VALID_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void loginComSenhaIncorretaRetorna401SemTokenNemDetalhesDoErro() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(credentials(VALID_EMAIL, "senha-errada")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void loginComEmailInexistenteRetorna401() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(credentials("nao-existe@clinicalstudy.test", VALID_PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void acessoAEndpointProtegidoSemTokenRetorna401() throws Exception {
        mockMvc.perform(get(PROTECTED_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void acessoAEndpointProtegidoComTokenInvalidoRetorna401() throws Exception {
        mockMvc.perform(get(PROTECTED_URL)
                .header("Authorization", "Bearer token.invalido.qualquer"))
                .andExpect(status().isUnauthorized());
    }

    /*
     * O que falta para fechar a T22
     * Combinar o contrato com o I4 (T19/T20): URL e corpo do login, códigos de
     * resposta e uma rota protegida para usar
     * nos testes. Depois, ajustar as constantes.
     * 
     * Esperar T19 e T20 entrarem no main: são o Spring Security e o endpoint de
     * login.
     * 
     * Resolver o banco dos testes: hoje o application.properties não tem datasource
     * (T07). Para os testes rodarem no CI sem o
     * PostgreSQL da equipe, é preciso H2 em memória ou Testcontainers. Isso mexe no
     * pom.xml, então combine com o I1 ou o I2.
     * 
     * Criar o usuário de teste: um usuário fictício com senha protegida (via seed
     * ou @BeforeEach), que depende da entidade
     * Usuario ter e-mail e senha e do repository (T06/T08/T15). Hoje ela só tem id
     * e nome.
     * 
     * Remover o @Disabled, rodar ./mvnw test e corrigir o que divergir.
     * 
     * Abrir o PR: com outro integrante revisando, como pede a Definition of Done, e
     * registrar o resultado dos testes. Se a equipe quiser testes de login na web
     * também, isso depende da T21 e das ferramentas de teste do React, ainda não
     * definidas.
     */
}
