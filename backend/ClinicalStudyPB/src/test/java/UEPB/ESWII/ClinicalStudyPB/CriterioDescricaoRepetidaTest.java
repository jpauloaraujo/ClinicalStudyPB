package UEPB.ESWII.ClinicalStudyPB;

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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;

/** T45: a descrição de um critério não pode se repetir (ignorando maiúsculas) no mesmo tipo e estudo. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CriterioDescricaoRepetidaTest {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions enviar(boolean atualizar, String url, String token, String corpo) throws Exception {
        var requisicao = atualizar ? put(url) : post(url);
        return mockMvc.perform(requisicao.header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private static String criterio(String tipo, String titulo, String descricao) {
        return "{\"tipo\":\"" + tipo + "\",\"titulo\":\"" + titulo + "\",\"descricao\":\"" + descricao + "\"}";
    }

    @Test
    @DisplayName("Descrição repetida no mesmo tipo é recusada (409), em outro tipo é aceita, e a edição respeita a regra")
    void descricaoRepetida() throws Exception {
        String pesquisador = TokenDeTeste.bearer(mockMvc, TokenDeTeste.PESQUISADOR);
        String resposta = enviar(false, "/api/estudos", pesquisador, "{\"titulo\":\"Estudo\",\"objetivo\":\"Obj\","
                + "\"responsavelId\":\"22222222-2222-4222-8222-222222222222\",\"limiteParticipantes\":3,"
                + "\"status\":\"PLANEJAMENTO\"}").andReturn().getResponse().getContentAsString();
        String url = "/api/estudos/" + JsonPath.read(resposta, "$.id") + "/criterios";

        enviar(false, url, pesquisador, criterio("INCLUSAO", "Idade", "Maior de 18 anos")).andExpect(status().isCreated());
        enviar(false, url, pesquisador, criterio("INCLUSAO", "Outra idade", "MAIOR DE 18 ANOS"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CRITERIO_DUPLICADO"));
        enviar(false, url, pesquisador, criterio("EXCLUSAO", "Idade", "Maior de 18 anos")).andExpect(status().isCreated());

        String outro = JsonPath.read(enviar(false, url, pesquisador, criterio("INCLUSAO", "Peso", "Acima de 50 kg"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.id");
        enviar(true, url + "/" + outro, pesquisador, criterio("INCLUSAO", "Peso", "maior de 18 anos"))
                .andExpect(status().isConflict());
        enviar(true, url + "/" + outro, pesquisador, criterio("INCLUSAO", "Peso", "Acima de 50 kg"))
                .andExpect(status().isOk());
    }
}
