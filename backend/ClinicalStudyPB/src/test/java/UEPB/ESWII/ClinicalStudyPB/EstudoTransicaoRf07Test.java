package UEPB.ESWII.ClinicalStudyPB;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;

/**
 * T44 (RF07): testes de integração do endpoint de transição, com tokens reais, que completam
 * {@code EstudoTransicaoEndpointTest} (T42): as 16 combinações de origem e destino pelo HTTP e o 403 para
 * cada perfil que não é o pesquisador (inclusive o participante). Roda sobre o banco do perfil de teste
 * (H2) e, no job {@code backend-postgres} do CI, sobre PostgreSQL.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EstudoTransicaoRf07Test {

    private static final String PESQUISADOR_ID = "22222222-2222-4222-8222-222222222222";

    @Autowired
    private MockMvc mockMvc;

    private String pesquisador;

    @BeforeEach
    void entrar() throws Exception {
        pesquisador = TokenDeTeste.bearer(mockMvc, TokenDeTeste.PESQUISADOR);
    }

    private ResultActions enviar(String url, String token, String corpo) throws Exception {
        return mockMvc.perform(post(url).header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private ResultActions avancar(String estudo, String token, StatusEstudo destino) throws Exception {
        return enviar("/api/estudos/" + estudo + "/transicoes", token, "{\"destino\":\"" + destino + "\"}");
    }

    /** Cria um estudo com critério e visita (pré-condições atendidas) e o leva até o estado pedido. */
    private String estudoEm(StatusEstudo estado) throws Exception {
        String resposta = enviar("/api/estudos", pesquisador, "{\"titulo\":\"Estudo RF07\","
                + "\"objetivo\":\"Objetivo fictício\",\"responsavelId\":\"" + PESQUISADOR_ID
                + "\",\"limiteParticipantes\":5,\"status\":\"PLANEJAMENTO\"}")
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String estudo = JsonPath.read(resposta, "$.id");
        enviar("/api/estudos/" + estudo + "/criterios", pesquisador,
                "{\"tipo\":\"INCLUSAO\",\"titulo\":\"Idade adulta\",\"descricao\":\"Maior de 18 anos\"}")
                .andExpect(status().isCreated());
        enviar("/api/estudos/" + estudo + "/visitas", pesquisador,
                "{\"nome\":\"Triagem\",\"diaPrevisto\":0,\"tolerancia\":0}").andExpect(status().isCreated());
        for (StatusEstudo proximo : StatusEstudo.values()) {
            if (proximo.ordinal() > 0 && proximo.ordinal() <= estado.ordinal()) {
                avancar(estudo, pesquisador, proximo).andExpect(status().isOk());
            }
        }
        return estudo;
    }

    static Stream<Arguments> combinacoes() {
        return Stream.of(StatusEstudo.values()).flatMap(origem -> Stream.of(StatusEstudo.values())
                .map(destino -> Arguments.of(origem, destino, destino.ordinal() == origem.ordinal() + 1)));
    }

    @ParameterizedTest(name = "{0} -> {1}: HTTP {2}")
    @MethodSource("combinacoes")
    @DisplayName("Pelo endpoint, as 16 combinações respondem 200 nas 3 válidas e 409 nas outras 13")
    void dezesseisCombinacoesPeloEndpoint(StatusEstudo origem, StatusEstudo destino, boolean permitida) throws Exception {
        String estudo = estudoEm(origem);

        if (permitida) {
            avancar(estudo, pesquisador, destino).andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(destino.name()));
        } else {
            avancar(estudo, pesquisador, destino).andExpect(status().isConflict())
                    .andExpect(jsonPath("$.codigo").value("TRANSICAO_ESTADO_INVALIDA"));
            mockMvc.perform(get("/api/estudos/" + estudo).header("Authorization", pesquisador))
                    .andExpect(jsonPath("$.status").value(origem.name()));
        }
    }

    @ParameterizedTest(name = "{0} recebe 403")
    @ValueSource(strings = { TokenDeTeste.COORDENADOR, TokenDeTeste.PARTICIPANTE, TokenDeTeste.ADMIN })
    @DisplayName("Coordenador, participante e administrador não avançam o estudo (403) e nada é gravado")
    void outrosPerfisRecebem403(String email) throws Exception {
        String estudo = estudoEm(StatusEstudo.PLANEJAMENTO);
        String token = TokenDeTeste.bearer(mockMvc, email);

        avancar(estudo, token, StatusEstudo.RECRUTAMENTO).andExpect(status().isForbidden());

        mockMvc.perform(get("/api/estudos/" + estudo).header("Authorization", pesquisador))
                .andExpect(jsonPath("$.status").value("PLANEJAMENTO"));
        mockMvc.perform(get("/api/estudos/" + estudo + "/historico-status").header("Authorization", pesquisador))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("O participante também não consulta o histórico de estados (403)")
    void participanteNaoLeOHistorico() throws Exception {
        String estudo = estudoEm(StatusEstudo.PLANEJAMENTO);
        String participante = TokenDeTeste.bearer(mockMvc, TokenDeTeste.PARTICIPANTE);

        mockMvc.perform(get("/api/estudos/" + estudo + "/historico-status").header("Authorization", participante))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("O PUT não muda o estado e o POST só cria em planejamento: ambos respondem 409")
    void estadoNaoMudaForaDaTransicao() throws Exception {
        String estudo = estudoEm(StatusEstudo.PLANEJAMENTO);
        String corpo = "{\"titulo\":\"Estudo RF07\",\"objetivo\":\"Objetivo fictício\",\"responsavelId\":\""
                + PESQUISADOR_ID + "\",\"limiteParticipantes\":5,\"status\":\"%s\"}";

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/estudos/" + estudo)
                .header("Authorization", pesquisador).contentType(MediaType.APPLICATION_JSON)
                .content(corpo.formatted("ENCERRADO")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESTADO_ALTERADO_FORA_DA_TRANSICAO"));
        enviar("/api/estudos", pesquisador, corpo.formatted("RECRUTAMENTO"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESTUDO_DEVE_COMECAR_EM_PLANEJAMENTO"));

        // Reenviar o mesmo estado (edição comum de outros campos) continua funcionando
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/estudos/" + estudo)
                .header("Authorization", pesquisador).contentType(MediaType.APPLICATION_JSON)
                .content(corpo.formatted("PLANEJAMENTO")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PLANEJAMENTO"));
        mockMvc.perform(get("/api/estudos/" + estudo).header("Authorization", pesquisador))
                .andExpect(jsonPath("$.status").value("PLANEJAMENTO"));
    }
}
