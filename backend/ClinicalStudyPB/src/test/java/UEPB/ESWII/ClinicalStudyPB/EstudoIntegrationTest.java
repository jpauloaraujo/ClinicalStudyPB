package UEPB.ESWII.ClinicalStudyPB;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;

/**
 * T30 (PBI-07 / RF04): cadastro e consulta de estudos pela API, com persistência
 * real (H2 + migrations Flyway) e token do pesquisador do seed.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EstudoIntegrationTest {

    private static final String API = "/api/estudos";
    private static final String PESQUISADOR_ID = "22222222-2222-4222-8222-222222222222";

    @Autowired
    private MockMvc mockMvc;

    private String token;

    @BeforeEach
    void autenticar() throws Exception {
        token = TokenDeTeste.bearer(mockMvc, TokenDeTeste.PESQUISADOR);
    }

    private static String estudo(String titulo, String inicio, String fim, Object limite, String responsavelId) {
        return "{\"titulo\":" + (titulo == null ? "null" : "\"" + titulo + "\"")
                + ",\"objetivo\":\"Objetivo fictício\",\"responsavelId\":\"" + responsavelId + "\""
                + ",\"dataInicio\":" + (inicio == null ? "null" : "\"" + inicio + "\"")
                + ",\"dataFim\":" + (fim == null ? "null" : "\"" + fim + "\"")
                + ",\"limiteParticipantes\":" + limite + ",\"status\":\"PLANEJAMENTO\"}";
    }

    @Test
    @DisplayName("Cadastra estudo, devolve 201 com Location e persiste os dados")
    void cadastraEConsultaEstudo() throws Exception {
        String corpo = mockMvc.perform(post(API).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(estudo("Estudo Integração", "2026-11-01", "2027-02-01", 30, PESQUISADOR_ID)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.titulo").value("Estudo Integração"))
                .andExpect(jsonPath("$.limiteParticipantes").value(30))
                .andExpect(jsonPath("$.responsavel.id").value(PESQUISADOR_ID))
                .andExpect(jsonPath("$.responsavel.senha").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(corpo, "$.id");

        mockMvc.perform(get(API + "/" + id).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.objetivo").value("Objetivo fictício"))
                .andExpect(jsonPath("$.dataInicio").value("2026-11-01"))
                .andExpect(jsonPath("$.dataFim").value("2027-02-01"))
                .andExpect(jsonPath("$.status").value("PLANEJAMENTO"));

        mockMvc.perform(get(API).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].titulo", hasItem("Estudo Integração")));
    }

    @Test
    @DisplayName("Lista os estudos do seed com o responsável, sem expor a senha")
    void listaEstudosDoSeed() throws Exception {
        mockMvc.perform(get(API).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa")))
                .andExpect(jsonPath("$[0].responsavel.nome").isNotEmpty())
                .andExpect(jsonPath("$[0].responsavel.senha").doesNotExist());
    }

    @Test
    @DisplayName("Atualiza e exclui um estudo")
    void atualizaEExcluiEstudo() throws Exception {
        String corpo = mockMvc.perform(post(API).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(estudo("Original", null, null, 5, PESQUISADOR_ID)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(corpo, "$.id");

        mockMvc.perform(put(API + "/" + id).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(estudo("Atualizado", null, null, 8, PESQUISADOR_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Atualizado"))
                .andExpect(jsonPath("$.limiteParticipantes").value(8));

        mockMvc.perform(delete(API + "/" + id).header("Authorization", token))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(API + "/" + id).header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Rejeita título ausente com 400")
    void rejeitaTituloAusente() throws Exception {
        mockMvc.perform(post(API).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(estudo(null, null, null, 5, PESQUISADOR_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(org.hamcrest.Matchers.startsWith("titulo")));
    }

    @Test
    @DisplayName("Rejeita limite de participantes menor que 1 com 400")
    void rejeitaLimiteInvalido() throws Exception {
        mockMvc.perform(post(API).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(estudo("Estudo", null, null, 0, PESQUISADOR_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value(org.hamcrest.Matchers.startsWith("limiteParticipantes")));
    }

    @Test
    @DisplayName("Rejeita data final anterior à inicial com 400")
    void rejeitaDatasInvertidas() throws Exception {
        mockMvc.perform(post(API).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(estudo("Estudo", "2027-01-01", "2026-01-01", 5, PESQUISADOR_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("A data final não pode ser anterior à data inicial"));
    }

    @Test
    @DisplayName("Rejeita responsável inexistente com 404")
    void rejeitaResponsavelInexistente() throws Exception {
        mockMvc.perform(post(API).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(estudo("Estudo", null, null, 5, "99999999-9999-4999-8999-999999999999")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Consulta de estudo inexistente retorna 404")
    void estudoInexistente() throws Exception {
        mockMvc.perform(get(API + "/99999999-9999-4999-8999-999999999999").header("Authorization", token))
                .andExpect(status().isNotFound());
    }
}
