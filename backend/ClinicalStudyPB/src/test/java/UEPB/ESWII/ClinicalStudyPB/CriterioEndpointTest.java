package UEPB.ESWII.ClinicalStudyPB;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
 * T47 (RF05): endpoints de critérios com tokens reais. 201 ao criar, 400 com o campo que falhou, 403 por perfil,
 * 401, 404 e 409. O 409 por critério já usado em avaliações não é alcançável pelo HTTP (um candidato só existe
 * com o estudo em recrutamento, e aí a exclusão já é recusada por estado); ele é coberto em {@code CriterioServiceTest}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CriterioEndpointTest {

    private static final String PESQUISADOR_ID = "22222222-2222-4222-8222-222222222222";

    @Autowired
    private MockMvc mockMvc;

    private String pesquisador;
    private String estudo;

    @BeforeEach
    void preparar() throws Exception {
        pesquisador = TokenDeTeste.bearer(mockMvc, TokenDeTeste.PESQUISADOR);
        estudo = criarEstudo();
    }

    private ResultActions enviar(String metodo, String url, String token, String corpo) throws Exception {
        var requisicao = switch (metodo) {
            case "PUT" -> put(url);
            case "DELETE" -> delete(url);
            default -> post(url);
        };
        if (token != null) {
            requisicao.header("Authorization", token);
        }
        return mockMvc.perform(requisicao.contentType(MediaType.APPLICATION_JSON).content(corpo == null ? "" : corpo));
    }

    private String criarEstudo() throws Exception {
        String resposta = enviar("POST", "/api/estudos", pesquisador, "{\"titulo\":\"Estudo RF05\","
                + "\"objetivo\":\"Objetivo fictício\",\"responsavelId\":\"" + PESQUISADOR_ID
                + "\",\"limiteParticipantes\":5,\"status\":\"PLANEJAMENTO\"}")
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

    private String url() {
        return "/api/estudos/" + estudo + "/criterios";
    }

    private static String corpo(String tipo, String titulo, String descricao) {
        return "{\"tipo\":\"" + tipo + "\",\"titulo\":\"" + titulo + "\",\"descricao\":\"" + descricao + "\"}";
    }

    private String criarCriterio(String tipo, String titulo, String descricao) throws Exception {
        String resposta = enviar("POST", url(), pesquisador, corpo(tipo, titulo, descricao))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

    private void avancarParaRecrutamento() throws Exception {
        enviar("POST", "/api/estudos/" + estudo + "/visitas", pesquisador,
                "{\"nome\":\"Triagem\",\"diaPrevisto\":0,\"tolerancia\":0}").andExpect(status().isCreated());
        enviar("POST", "/api/estudos/" + estudo + "/transicoes", pesquisador, "{\"destino\":\"RECRUTAMENTO\"}")
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("201 ao criar: devolve o critério com Location, peso 1 e determinante por padrão")
    void criaCriterioDeInclusao() throws Exception {
        enviar("POST", url(), pesquisador, corpo("INCLUSAO", "Idade adulta", "Maior de 18 anos"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("http://localhost" + url() + "/")))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.estudoId").value(estudo))
                .andExpect(jsonPath("$.tipo").value("INCLUSAO"))
                .andExpect(jsonPath("$.peso").value(1.0))
                .andExpect(jsonPath("$.determinante").value(true));

        enviar("POST", url(), pesquisador, corpo("EXCLUSAO", "Gestante", "Em gestação"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.peso").doesNotExist());

        mockMvc.perform(get(url()).header("Authorization", pesquisador))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
            "descrição curta|{\"tipo\":\"INCLUSAO\",\"titulo\":\"Idade\",\"descricao\":\"abc\"}|descricao",
            "descrição ausente|{\"tipo\":\"INCLUSAO\",\"titulo\":\"Idade\"}|descricao",
            "título ausente|{\"tipo\":\"INCLUSAO\",\"descricao\":\"Maior de 18 anos\"}|titulo",
            "tipo ausente|{\"titulo\":\"Idade\",\"descricao\":\"Maior de 18 anos\"}|tipo",
            "peso zero|{\"tipo\":\"INCLUSAO\",\"titulo\":\"Idade\",\"descricao\":\"Maior de 18 anos\",\"peso\":0}|peso" })
    @DisplayName("400 com o nome do campo e a mensagem quando os dados são inválidos")
    void dadosInvalidos(String caso, String json, String campo) throws Exception {
        enviar("POST", url(), pesquisador, json).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", startsWith(campo + ": ")));
        mockMvc.perform(get(url()).header("Authorization", pesquisador)).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("400 para descrição com mais de 500 caracteres, tipo desconhecido e JSON malformado")
    void outrosCorposInvalidos() throws Exception {
        enviar("POST", url(), pesquisador, corpo("INCLUSAO", "Idade", "d".repeat(501)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro", containsString("descricao")));
        enviar("POST", url(), pesquisador, corpo("OUTRO", "Idade", "Maior de 18 anos")).andExpect(status().isBadRequest());
        enviar("POST", url(), pesquisador, "{não é json").andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "{0} em {1}")
    @CsvSource({ "coordenador@clinicalstudy.test,POST", "participante@clinicalstudy.test,POST",
            "admin@clinicalstudy.test,POST", "coordenador@clinicalstudy.test,PUT", "coordenador@clinicalstudy.test,DELETE",
            "participante@clinicalstudy.test,PUT", "participante@clinicalstudy.test,DELETE" })
    @DisplayName("Só o pesquisador escreve critérios: os outros perfis recebem 403 e nada muda")
    void escritaProibidaParaOutrosPerfis(String email, String metodo) throws Exception {
        String existente = criarCriterio("INCLUSAO", "Idade adulta", "Maior de 18 anos");
        String token = TokenDeTeste.bearer(mockMvc, email);
        String alvo = "POST".equals(metodo) ? url() : url() + "/" + existente;

        enviar(metodo, alvo, token, corpo("INCLUSAO", "Outro", "Outra descrição")).andExpect(status().isForbidden());

        mockMvc.perform(get(url()).header("Authorization", pesquisador))
                .andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].titulo").value("Idade adulta"));
    }

    @Test
    @DisplayName("Leitura: pesquisador e coordenador leem (200); participante e administrador recebem 403")
    void leituraPorPerfil() throws Exception {
        criarCriterio("INCLUSAO", "Idade adulta", "Maior de 18 anos");

        for (String email : new String[] { TokenDeTeste.PESQUISADOR, TokenDeTeste.COORDENADOR }) {
            mockMvc.perform(get(url()).header("Authorization", TokenDeTeste.bearer(mockMvc, email)))
                    .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        }
        for (String email : new String[] { TokenDeTeste.PARTICIPANTE, TokenDeTeste.ADMIN }) {
            mockMvc.perform(get(url()).header("Authorization", TokenDeTeste.bearer(mockMvc, email)))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    @DisplayName("Sem token todas as operações respondem 401")
    void semTokenRespondeNaoAutenticado() throws Exception {
        String existente = criarCriterio("INCLUSAO", "Idade adulta", "Maior de 18 anos");

        mockMvc.perform(get(url())).andExpect(status().isUnauthorized());
        enviar("POST", url(), null, corpo("INCLUSAO", "Outro", "Outra descrição")).andExpect(status().isUnauthorized());
        enviar("PUT", url() + "/" + existente, null, corpo("INCLUSAO", "Outro", "Outra descrição")).andExpect(status().isUnauthorized());
        enviar("DELETE", url() + "/" + existente, null, null).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("404 para estudo inexistente e para critério inexistente ou de outro estudo")
    void naoEncontrado() throws Exception {
        String fantasma = "/api/estudos/" + UUID.randomUUID() + "/criterios";
        mockMvc.perform(get(fantasma).header("Authorization", pesquisador)).andExpect(status().isNotFound());
        enviar("POST", fantasma, pesquisador, corpo("INCLUSAO", "Idade", "Maior de 18 anos")).andExpect(status().isNotFound());

        enviar("PUT", url() + "/" + UUID.randomUUID(), pesquisador, corpo("INCLUSAO", "Idade", "Maior de 18 anos"))
                .andExpect(status().isNotFound());
        enviar("DELETE", url() + "/" + UUID.randomUUID(), pesquisador, null).andExpect(status().isNotFound());

        String existente = criarCriterio("INCLUSAO", "Idade adulta", "Maior de 18 anos");
        String outroEstudo = criarEstudo();
        enviar("DELETE", "/api/estudos/" + outroEstudo + "/criterios/" + existente, pesquisador, null)
                .andExpect(status().isNotFound());
        enviar("DELETE", url() + "/" + existente, pesquisador, null).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("409 para descrição repetida, título repetido e para troca de tipo na edição (400)")
    void conflitosDeDuplicidade() throws Exception {
        String existente = criarCriterio("INCLUSAO", "Idade adulta", "Maior de 18 anos");

        enviar("POST", url(), pesquisador, corpo("INCLUSAO", "Outro título", "MAIOR DE 18 ANOS"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CRITERIO_DUPLICADO"))
                .andExpect(jsonPath("$.erro", containsString("descrição")));
        enviar("POST", url(), pesquisador, corpo("INCLUSAO", "IDADE ADULTA", "Outra descrição"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CRITERIO_DUPLICADO"))
                .andExpect(jsonPath("$.erro", containsString("título")));
        enviar("PUT", url() + "/" + existente, pesquisador, corpo("EXCLUSAO", "Idade adulta", "Maior de 18 anos"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("409 ESTUDO_FORA_DE_PLANEJAMENTO para criar, editar e excluir depois que o estudo avançou")
    void foraDePlanejamento() throws Exception {
        String existente = criarCriterio("INCLUSAO", "Idade adulta", "Maior de 18 anos");
        avancarParaRecrutamento();

        enviar("POST", url(), pesquisador, corpo("INCLUSAO", "Outro", "Outra descrição"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("ESTUDO_FORA_DE_PLANEJAMENTO"));
        enviar("PUT", url() + "/" + existente, pesquisador, corpo("INCLUSAO", "Idade adulta", "Nova descrição"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("ESTUDO_FORA_DE_PLANEJAMENTO"));
        enviar("DELETE", url() + "/" + existente, pesquisador, null)
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("ESTUDO_FORA_DE_PLANEJAMENTO"));

        mockMvc.perform(get(url()).header("Authorization", pesquisador))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].descricao").value("Maior de 18 anos"));
    }
}
