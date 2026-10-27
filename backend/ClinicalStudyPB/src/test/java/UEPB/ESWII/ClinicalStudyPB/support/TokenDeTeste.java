package UEPB.ESWII.ClinicalStudyPB.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

/** Obtém tokens reais pelo endpoint de login, usando os usuários do seed V2. */
public final class TokenDeTeste {

    public static final String SENHA_SEED = "senha-de-teste";
    public static final String ADMIN = "admin@clinicalstudy.test";
    public static final String PESQUISADOR = "pesquisador@clinicalstudy.test";
    public static final String COORDENADOR = "coordenador@clinicalstudy.test";
    public static final String PARTICIPANTE = "participante@clinicalstudy.test";

    private TokenDeTeste() {
    }

    /** Retorna o cabeçalho {@code Authorization} completo ("Bearer ..."). */
    public static String bearer(MockMvc mockMvc, String email) throws Exception {
        return bearer(mockMvc, email, SENHA_SEED);
    }

    /** Variante para usuários criados no próprio teste, com senha própria. */
    public static String bearer(MockMvc mockMvc, String email, String senha) throws Exception {
        String corpo = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(corpo, "$.token");
        return "Bearer " + token;
    }
}
