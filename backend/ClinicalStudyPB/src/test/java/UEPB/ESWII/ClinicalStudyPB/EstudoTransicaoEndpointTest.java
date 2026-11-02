package UEPB.ESWII.ClinicalStudyPB;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
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

/**
 * T42: endpoint {@code POST /api/estudos/{id}/transicoes} e consulta do histórico, com tokens reais.
 * As regras da máquina de estados em si são testadas em {@code MaquinaEstadosEstudoTest}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EstudoTransicaoEndpointTest {

    private static final String PESQUISADOR_ID = "22222222-2222-4222-8222-222222222222";

    @Autowired
    private MockMvc mockMvc;

    private String pesquisador;
    private String coordenador;
    private String administrador;

    @BeforeEach
    void entrar() throws Exception {
        pesquisador = TokenDeTeste.bearer(mockMvc, TokenDeTeste.PESQUISADOR);
        coordenador = TokenDeTeste.bearer(mockMvc, TokenDeTeste.COORDENADOR);
        administrador = TokenDeTeste.bearer(mockMvc, TokenDeTeste.ADMIN);
    }

    private ResultActions enviar(String url, String token, String corpo) throws Exception {
        return mockMvc.perform(post(url).header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private String criarEstudo() throws Exception {
        String resposta = enviar("/api/estudos", pesquisador, "{\"titulo\":\"Estudo de Transição\","
                + "\"objetivo\":\"Objetivo fictício\",\"responsavelId\":\"" + PESQUISADOR_ID
                + "\",\"limiteParticipantes\":5,\"status\":\"PLANEJAMENTO\"}")
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

    private void prepararSaidaDePlanejamento(String estudo) throws Exception {
        enviar("/api/estudos/" + estudo + "/criterios", pesquisador,
                "{\"tipo\":\"INCLUSAO\",\"titulo\":\"Idade adulta\",\"descricao\":\"Maior de 18 anos\"}")
                .andExpect(status().isCreated());
        enviar("/api/estudos/" + estudo + "/visitas", pesquisador,
                "{\"nome\":\"Triagem\",\"diaPrevisto\":0,\"tolerancia\":0}").andExpect(status().isCreated());
    }

    private ResultActions avancar(String estudo, String token, String destino) throws Exception {
        return enviar("/api/estudos/" + estudo + "/transicoes", token, "{\"destino\":\"" + destino + "\"}");
    }

    @Test
    @DisplayName("O pesquisador avança o estudo um estado por vez e recebe o estudo atualizado")
    void pesquisadorAvancaOEstudo() throws Exception {
        String estudo = criarEstudo();
        prepararSaidaDePlanejamento(estudo);

        avancar(estudo, pesquisador, "RECRUTAMENTO").andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(estudo))
                .andExpect(jsonPath("$.status").value("RECRUTAMENTO"))
                .andExpect(jsonPath("$.responsavel.id").value(PESQUISADOR_ID));
        avancar(estudo, pesquisador, "EXECUCAO").andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EXECUCAO"));
        avancar(estudo, pesquisador, "ENCERRADO").andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENCERRADO"));

        mockMvc.perform(get("/api/estudos/" + estudo + "/historico-status").header("Authorization", coordenador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].estadoAnterior").value("PLANEJAMENTO"))
                .andExpect(jsonPath("$[0].estadoNovo").value("RECRUTAMENTO"))
                .andExpect(jsonPath("$[2].estadoNovo").value("ENCERRADO"))
                .andExpect(jsonPath("$[0].usuario").isNotEmpty())
                .andExpect(jsonPath("$[0].dataHora").isNotEmpty());
    }

    @Test
    @DisplayName("Cada regra violada responde 409 com o seu código e não altera o estado")
    void regrasVioladasRespondem409() throws Exception {
        String estudo = criarEstudo();

        avancar(estudo, pesquisador, "RECRUTAMENTO").andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESTUDO_SEM_CRITERIO_INCLUSAO"));

        enviar("/api/estudos/" + estudo + "/criterios", pesquisador,
                "{\"tipo\":\"INCLUSAO\",\"titulo\":\"Idade adulta\",\"descricao\":\"Maior de 18 anos\"}")
                .andExpect(status().isCreated());
        avancar(estudo, pesquisador, "RECRUTAMENTO").andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESTUDO_SEM_VISITA_PREVISTA"));

        avancar(estudo, pesquisador, "EXECUCAO").andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("TRANSICAO_ESTADO_INVALIDA"));
        avancar(estudo, pesquisador, "PLANEJAMENTO").andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("TRANSICAO_ESTADO_INVALIDA"));

        mockMvc.perform(get("/api/estudos/" + estudo).header("Authorization", pesquisador))
                .andExpect(jsonPath("$.status").value("PLANEJAMENTO"));
        mockMvc.perform(get("/api/estudos/" + estudo + "/historico-status").header("Authorization", pesquisador))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Só o pesquisador avança: coordenador e administrador recebem 403, sem token 401")
    void soOPesquisadorAvanca() throws Exception {
        String estudo = criarEstudo();
        prepararSaidaDePlanejamento(estudo);

        avancar(estudo, coordenador, "RECRUTAMENTO").andExpect(status().isForbidden());
        avancar(estudo, administrador, "RECRUTAMENTO").andExpect(status().isForbidden());
        mockMvc.perform(post("/api/estudos/" + estudo + "/transicoes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"destino\":\"RECRUTAMENTO\"}")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/estudos/" + estudo + "/historico-status").header("Authorization", administrador))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/estudos/" + estudo).header("Authorization", pesquisador))
                .andExpect(jsonPath("$.status").value("PLANEJAMENTO"));
    }

    @Test
    @DisplayName("Estudo inexistente responde 404 e destino ausente ou inválido responde 400")
    void estudoInexistenteEDestinoInvalido() throws Exception {
        avancar(UUID.randomUUID().toString(), pesquisador, "RECRUTAMENTO").andExpect(status().isNotFound());
        mockMvc.perform(get("/api/estudos/" + UUID.randomUUID() + "/historico-status")
                .header("Authorization", pesquisador)).andExpect(status().isNotFound());

        String estudo = criarEstudo();
        enviar("/api/estudos/" + estudo + "/transicoes", pesquisador, "{}").andExpect(status().isBadRequest());
        avancar(estudo, pesquisador, "INEXISTENTE").andExpect(status().isBadRequest());
    }
}
