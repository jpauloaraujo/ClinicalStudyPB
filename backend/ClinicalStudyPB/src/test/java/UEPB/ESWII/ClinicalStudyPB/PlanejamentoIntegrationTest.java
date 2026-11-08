package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;
import UEPB.ESWII.ClinicalStudyPB.repository.HistoricoStatusEstudoRepository;
import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;

/** Regressões de T42 e T45: transições reais, histórico e descrição única. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PlanejamentoIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private HistoricoStatusEstudoRepository historico;
    private String pesquisador;

    @BeforeEach
    void autenticar() throws Exception {
        pesquisador = TokenDeTeste.bearer(mvc, TokenDeTeste.PESQUISADOR);
    }

    private String dadosEstudo(String estado) {
        return "{\"titulo\":\"Planejamento T42\",\"objetivo\":\"Objetivo fictício\","
                + "\"responsavelId\":\"22222222-2222-4222-8222-222222222222\",\"limiteParticipantes\":5,\"status\":\"" + estado + "\"}";
    }

    private String criar() throws Exception {
        var resposta = mvc.perform(post("/api/estudos").header("Authorization", pesquisador)
                .contentType(MediaType.APPLICATION_JSON).content(dadosEstudo("PLANEJAMENTO")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PLANEJAMENTO"))
                .andReturn().getResponse();
        return JsonPath.read(resposta.getContentAsString(), "$.id");
    }

    private ResultActions transicao(String estudo, String estado, String token) throws Exception {
        return mvc.perform(post("/api/estudos/" + estudo + "/transicoes").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"novoEstado\":\"" + estado + "\"}"));
    }

    private ResultActions criterio(String estudo, String tipo, String descricao) throws Exception {
        return mvc.perform(post("/api/estudos/" + estudo + "/criterios").header("Authorization", pesquisador)
                .contentType(MediaType.APPLICATION_JSON).content("{\"tipo\":\"" + tipo + "\",\"descricao\":\"" + descricao + "\"}"));
    }

    @Test
    void percorreSequenciaPorHttpComHistoricoSemPermitirDesvios() throws Exception {
        String id = criar();
        transicao(id, "EXECUCAO", pesquisador).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigoRegra").value("TRANSICAO_ESTADO_INVALIDA"));
        transicao(id, "RECRUTAMENTO", pesquisador).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigoRegra").value("ESTUDO_SEM_CRITERIO_INCLUSAO"));
        criterio(id, "INCLUSAO", "Ter dezoito anos ou mais").andExpect(status().isCreated());
        transicao(id, "RECRUTAMENTO", pesquisador).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigoRegra").value("ESTUDO_SEM_VISITA_PREVISTA"));
        mvc.perform(post("/api/estudos/" + id + "/visitas").header("Authorization", pesquisador)
                .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Inicial\",\"diaPrevisto\":0,\"tolerancia\":0}"))
                .andExpect(status().isCreated());
        for (String estado : new String[]{"RECRUTAMENTO", "EXECUCAO", "ENCERRADO"}) {
            transicao(id, estado, pesquisador).andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(estado));
        }
        transicao(id, "PLANEJAMENTO", pesquisador).andExpect(status().isConflict());
        var registros = historico.findByEstudoIdOrderByDataHoraAsc(UUID.fromString(id));
        assertThat(registros).hasSize(3).allSatisfy(r -> {
            assertThat(r.getUsuario().getId().toString()).isEqualTo("22222222-2222-4222-8222-222222222222");
            assertThat(r.getDataHora()).isNotNull();
        });
        criterio(id, "EXCLUSAO", "Condição posterior ao planejamento").andExpect(status().isConflict());
    }

    @Test
    void putNaoAlteraEstadoNemCriaHistorico() throws Exception {
        String id = criar();
        mvc.perform(put("/api/estudos/" + id).header("Authorization", pesquisador)
                .contentType(MediaType.APPLICATION_JSON).content(dadosEstudo("ENCERRADO")))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigoRegra").value("ESTADO_ALTERADO_FORA_DA_TRANSICAO"));
        mvc.perform(get("/api/estudos/" + id).header("Authorization", pesquisador))
                .andExpect(jsonPath("$.status").value("PLANEJAMENTO"));
        assertThat(historico.findByEstudoIdOrderByDataHoraAsc(UUID.fromString(id))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {TokenDeTeste.COORDENADOR, TokenDeTeste.PARTICIPANTE, TokenDeTeste.ADMIN})
    void outrosPerfisNaoAvancam(String email) throws Exception {
        transicao(criar(), "RECRUTAMENTO", TokenDeTeste.bearer(mvc, email))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.codigoRegra").value("ACESSO_NEGADO"));
    }

    @Test
    void endpointValidaAutenticacaoRecursoECorpo() throws Exception {
        String id = criar();
        mvc.perform(post("/api/estudos/" + id + "/transicoes").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        transicao(UUID.randomUUID().toString(), "RECRUTAMENTO", pesquisador).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigoRegra").value("RECURSO_NAO_ENCONTRADO"));
        mvc.perform(post("/api/estudos/" + id + "/transicoes").header("Authorization", pesquisador)
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errosCampo[0].campo").value("destino"));
        transicao(id, "INVALIDO", pesquisador).andExpect(status().isBadRequest());
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/estudos/{estudoId}/transicoes'].post").exists())
                .andExpect(jsonPath("$.paths['/api/estudos/{estudoId}/transicoes'].post.responses['409'].content['application/json'].schema.type")
                        .value("object"))
                .andExpect(jsonPath("$.paths['/api/estudos/{estudoId}/transicoes'].post.responses['401'].content").doesNotExist());
    }

    @Test
    void descricaoUnicaPorTipoEEstudoInclusiveNaEdicao() throws Exception {
        String estudo = criar();
        String primeiro = JsonPath.read(criterio(estudo, "INCLUSAO", "Adulto com dezoito anos").andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id");
        criterio(estudo, "INCLUSAO", "  ADULTO COM DEZOITO ANOS  ").andExpect(status().isConflict());
        criterio(estudo, "EXCLUSAO", "Adulto com dezoito anos").andExpect(status().isCreated());
        criterio(criar(), "INCLUSAO", "Adulto com dezoito anos").andExpect(status().isCreated());
        String segundo = JsonPath.read(criterio(estudo, "INCLUSAO", "Outra condição válida").andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id");
        mvc.perform(put("/api/estudos/" + estudo + "/criterios/" + segundo).header("Authorization", pesquisador)
                .contentType(MediaType.APPLICATION_JSON).content("{\"tipo\":\"INCLUSAO\",\"titulo\":\"Novo título\",\"descricao\":\"adulto com dezoito anos\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigoRegra").value("CRITERIO_DUPLICADO"));
        mvc.perform(put("/api/estudos/" + estudo + "/criterios/" + primeiro).header("Authorization", pesquisador)
                .contentType(MediaType.APPLICATION_JSON).content("{\"tipo\":\"INCLUSAO\",\"descricao\":\"Adulto com dezoito anos\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void validacaoRetornaTodosOsCamposEContratoCompativel() throws Exception {
        String id = criar();
        mvc.perform(post("/api/estudos/" + id + "/criterios").header("Authorization", pesquisador)
                .contentType(MediaType.APPLICATION_JSON).content("{\"descricao\":\"  ab  \"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.codigoRegra").value("VALIDACAO_CAMPOS"))
                .andExpect(jsonPath("$.errosCampo", hasSize(2))).andExpect(jsonPath("$.erro").isNotEmpty())
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
        criterio(id, "INCLUSAO", "x".repeat(501)).andExpect(status().isBadRequest());
        criterio(id, "INCLUSAO", "x".repeat(500)).andExpect(status().isCreated());
    }
}
