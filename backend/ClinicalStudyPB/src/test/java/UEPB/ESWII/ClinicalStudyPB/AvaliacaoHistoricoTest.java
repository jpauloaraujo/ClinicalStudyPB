package UEPB.ESWII.ClinicalStudyPB;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;
import jakarta.persistence.EntityManager;

/**
 * Regressão: o histórico de avaliações lido em uma sessão nova do Hibernate (os critérios chegam como
 * proxies preguiçosos, não como CriterioInclusao/CriterioExclusao) precisa continuar informando os
 * critérios que reprovaram o candidato e as notas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AvaliacaoHistoricoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    private ResultActions enviar(boolean atualizar, String url, String token, String corpo) throws Exception {
        var requisicao = atualizar ? put(url) : post(url);
        return mockMvc.perform(requisicao.header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private static String id(ResultActions resposta) throws Exception {
        return JsonPath.read(resposta.andReturn().getResponse().getContentAsString(), "$.id");
    }

    @Test
    @DisplayName("O histórico lido depois de limpar a sessão mantém os critérios reprovadores")
    void historicoComCriteriosCarregadosDoBanco() throws Exception {
        String pesquisador = TokenDeTeste.bearer(mockMvc, TokenDeTeste.PESQUISADOR);
        String coordenador = TokenDeTeste.bearer(mockMvc, TokenDeTeste.COORDENADOR);

        String estudo = id(enviar(false, "/api/estudos", pesquisador, "{\"titulo\":\"Estudo\",\"objetivo\":\"Obj\","
                + "\"responsavelId\":\"22222222-2222-4222-8222-222222222222\",\"limiteParticipantes\":3,"
                + "\"status\":\"PLANEJAMENTO\"}").andExpect(status().isCreated()));
        String inclusao = id(enviar(false, "/api/estudos/" + estudo + "/criterios", pesquisador,
                "{\"tipo\":\"INCLUSAO\",\"titulo\":\"Idade adulta\",\"descricao\":\"Maior de 18 anos\",\"peso\":2}")
                .andExpect(status().isCreated()));
        String exclusao = id(enviar(false, "/api/estudos/" + estudo + "/criterios", pesquisador,
                "{\"tipo\":\"EXCLUSAO\",\"titulo\":\"Gestante\",\"descricao\":\"Em gestação\"}")
                .andExpect(status().isCreated()));
        enviar(false, "/api/estudos/" + estudo + "/visitas", pesquisador,
                "{\"nome\":\"Triagem\",\"diaPrevisto\":0,\"tolerancia\":0}").andExpect(status().isCreated());
        enviar(false, "/api/estudos/" + estudo + "/transicoes", pesquisador, "{\"destino\":\"RECRUTAMENTO\"}")
                .andExpect(status().isOk());
        String candidato = id(enviar(false, "/api/estudos/" + estudo + "/candidatos", coordenador,
                "{\"nomeFicticio\":\"J. S.\",\"dataNascimento\":\"1990-04-12\"}").andExpect(status().isCreated()));

        enviar(false, "/api/candidatos/" + candidato + "/avaliacoes", coordenador,
                "{\"respostas\":[{\"criterioId\":\"" + inclusao + "\",\"aplica\":true},{\"criterioId\":\""
                        + exclusao + "\",\"aplica\":true}]}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.criteriosReprovadores", hasSize(1)));

        // Sessão nova: os critérios das respostas agora vêm do banco como proxies
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/api/candidatos/" + candidato + "/avaliacoes").header("Authorization", coordenador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resultado").value("REPROVADO"))
                .andExpect(jsonPath("$[0].criteriosReprovadores", hasSize(1)))
                .andExpect(jsonPath("$[0].criteriosReprovadores[0].titulo").value("Gestante"))
                .andExpect(jsonPath("$[0].criteriosReprovadores[0].tipo").value("EXCLUSAO"))
                .andExpect(jsonPath("$[0].pontuacao").value(2.0));
    }
}
