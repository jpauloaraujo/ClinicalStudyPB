package UEPB.ESWII.ClinicalStudyPB;

import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.avaliarAprovado;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.avancar;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.consentir;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.criarCandidato;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.enviar;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.estudoEmRecrutamento;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;

/**
 * T56 (RF09): endpoints de avaliação de elegibilidade com tokens reais. 201 aprovado e reprovado (com os critérios
 * reprovadores), 400 de validação e de respostas incompletas, 401, 403 por perfil, 404, 409 de cada regra e o
 * histórico preservado na reavaliação. O estudo de apoio tem um critério de inclusão determinante ("Idade
 * adulta") e um de exclusão ("Gestante").
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AvaliacaoEndpointTest {

    @Autowired
    private MockMvc mvc;

    private String pesquisador;
    private String coordenador;
    private String estudo;
    private String candidato;
    private String inclusaoId;
    private String exclusaoId;

    @BeforeEach
    void preparar() throws Exception {
        pesquisador = TokenDeTeste.bearer(mvc, TokenDeTeste.PESQUISADOR);
        coordenador = TokenDeTeste.bearer(mvc, TokenDeTeste.COORDENADOR);
        estudo = estudoEmRecrutamento(mvc, pesquisador, 5);
        candidato = criarCandidato(mvc, coordenador, estudo, "Candidata Avaliacao");

        String lista = enviar(mvc, "GET", "/api/estudos/" + estudo + "/criterios", coordenador, null)
                .andReturn().getResponse().getContentAsString();
        inclusaoId = primeiro(lista, "INCLUSAO");
        exclusaoId = primeiro(lista, "EXCLUSAO");
    }

    private static String primeiro(String lista, String tipo) {
        List<String> ids = JsonPath.read(lista, "$[?(@.tipo=='" + tipo + "')].id");
        return ids.get(0);
    }

    private String url() {
        return "/api/candidatos/" + candidato + "/avaliacoes";
    }

    private String respostas(boolean aplicaInclusao, boolean aplicaExclusao) {
        return "{\"respostas\":[{\"criterioId\":\"" + inclusaoId + "\",\"aplica\":" + aplicaInclusao
                + "},{\"criterioId\":\"" + exclusaoId + "\",\"aplica\":" + aplicaExclusao + "}]}";
    }

    @Test
    @DisplayName("201 aprovado: resultado, pontuação, avaliador, vigente, respostas e nenhum critério reprovador")
    void aprovado() throws Exception {
        enviar(mvc, "POST", url(), coordenador, respostas(true, false))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.candidatoId").value(candidato))
                .andExpect(jsonPath("$.resultado").value("APROVADO"))
                .andExpect(jsonPath("$.pontuacao").value(1.0))
                .andExpect(jsonPath("$.avaliador").isNotEmpty())
                .andExpect(jsonPath("$.vigente").value(true))
                .andExpect(jsonPath("$.respostas", hasSize(2)))
                .andExpect(jsonPath("$.criteriosReprovadores", hasSize(0)));

        enviar(mvc, "GET", "/api/candidatos/" + candidato, coordenador, null)
                .andExpect(jsonPath("$.situacao").value("ELEGIVEL"))
                .andExpect(jsonPath("$.resultadoAvaliacao").value("APROVADO"));
    }

    @Test
    @DisplayName("201 reprovado por inclusão determinante não atendida: aponta o critério e muda a situação")
    void reprovadoPorInclusao() throws Exception {
        enviar(mvc, "POST", url(), coordenador, respostas(false, false))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultado").value("REPROVADO"))
                .andExpect(jsonPath("$.pontuacao").value(0.0))
                .andExpect(jsonPath("$.criteriosReprovadores", hasSize(1)))
                .andExpect(jsonPath("$.criteriosReprovadores[0].criterioId").value(inclusaoId))
                .andExpect(jsonPath("$.criteriosReprovadores[0].tipo").value("INCLUSAO"));

        enviar(mvc, "GET", "/api/candidatos/" + candidato, coordenador, null)
                .andExpect(jsonPath("$.situacao").value("NAO_ELEGIVEL"));
    }

    @Test
    @DisplayName("201 reprovado por exclusão satisfeita, mesmo com a inclusão atendida")
    void reprovadoPorExclusao() throws Exception {
        enviar(mvc, "POST", url(), coordenador, respostas(true, true))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultado").value("REPROVADO"))
                .andExpect(jsonPath("$.criteriosReprovadores", hasSize(1)))
                .andExpect(jsonPath("$.criteriosReprovadores[0].criterioId").value(exclusaoId));
    }

    @Test
    @DisplayName("A observação da resposta é gravada sem espaços nas pontas; em branco vira vazio")
    void observacaoGravada() throws Exception {
        enviar(mvc, "POST", url(), coordenador, "{\"respostas\":[{\"criterioId\":\"" + inclusaoId
                + "\",\"aplica\":true,\"observacao\":\"  Documento conferido  \"},{\"criterioId\":\"" + exclusaoId
                + "\",\"aplica\":false,\"observacao\":\"   \"}]}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.respostas[?(@.criterioId=='" + inclusaoId + "')].observacao")
                        .value(contains("Documento conferido")))
                .andExpect(jsonPath("$.respostas[?(@.criterioId=='" + exclusaoId + "')].observacao")
                        .value(contains(nullValue())));
    }

    @Test
    @DisplayName("Reavaliar mantém o histórico: a nova é vigente, a anterior deixa de ser, da mais recente para a mais antiga")
    void reavaliacaoPreservaOHistorico() throws Exception {
        enviar(mvc, "POST", url(), coordenador, respostas(false, false)).andExpect(status().isCreated());
        enviar(mvc, "POST", url(), coordenador, respostas(true, false)).andExpect(status().isCreated());

        enviar(mvc, "GET", url(), pesquisador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].resultado").value("APROVADO"))
                .andExpect(jsonPath("$[0].vigente").value(true))
                .andExpect(jsonPath("$[1].resultado").value("REPROVADO"))
                .andExpect(jsonPath("$[1].vigente").value(false));
    }

    @Test
    @DisplayName("Histórico de candidato sem avaliação é uma lista vazia")
    void historicoVazio() throws Exception {
        enviar(mvc, "GET", url(), coordenador, null).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("400 com resposta faltando, repetida, de critério alheio e com lista vazia")
    void respostasIncompletas() throws Exception {
        enviar(mvc, "POST", url(), coordenador, "{\"respostas\":[{\"criterioId\":\"" + inclusaoId
                + "\",\"aplica\":true}]}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro", containsString("todos os critérios")));
        enviar(mvc, "POST", url(), coordenador, "{\"respostas\":[{\"criterioId\":\"" + inclusaoId
                + "\",\"aplica\":true},{\"criterioId\":\"" + inclusaoId + "\",\"aplica\":false}]}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro", containsString("mais de uma resposta")));
        enviar(mvc, "POST", url(), coordenador, "{\"respostas\":[{\"criterioId\":\"" + UUID.randomUUID()
                + "\",\"aplica\":true}]}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro", containsString("não pertence")));
        enviar(mvc, "POST", url(), coordenador, "{\"respostas\":[]}").andExpect(status().isBadRequest());

        enviar(mvc, "GET", url(), coordenador, null).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("400 com campo obrigatório ausente, observação longa demais e JSON malformado")
    void dadosInvalidos() throws Exception {
        enviar(mvc, "POST", url(), coordenador, "{}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", startsWith("respostas: ")));
        enviar(mvc, "POST", url(), coordenador, "{\"respostas\":[{\"criterioId\":\"" + inclusaoId + "\"}]}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro", containsString("aplica")));
        enviar(mvc, "POST", url(), coordenador, "{\"respostas\":[{\"criterioId\":\"" + inclusaoId
                + "\",\"aplica\":true,\"observacao\":\"" + "o".repeat(501) + "\"}]}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro", containsString("observacao")));
        enviar(mvc, "POST", url(), coordenador, "{não é json").andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "{0} recebe 403 ao avaliar")
    @ValueSource(strings = { TokenDeTeste.PESQUISADOR, TokenDeTeste.PARTICIPANTE, TokenDeTeste.ADMIN })
    @DisplayName("Só o coordenador avalia: os demais perfis recebem 403 e nada é gravado")
    void soOCoordenadorAvalia(String email) throws Exception {
        enviar(mvc, "POST", url(), TokenDeTeste.bearer(mvc, email), respostas(true, false))
                .andExpect(status().isForbidden());

        enviar(mvc, "GET", url(), coordenador, null).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Leitura do histórico: coordenador e pesquisador leem; participante e administrador recebem 403")
    void leituraPorPerfil() throws Exception {
        for (String email : new String[] { TokenDeTeste.COORDENADOR, TokenDeTeste.PESQUISADOR }) {
            enviar(mvc, "GET", url(), TokenDeTeste.bearer(mvc, email), null).andExpect(status().isOk());
        }
        for (String email : new String[] { TokenDeTeste.PARTICIPANTE, TokenDeTeste.ADMIN }) {
            enviar(mvc, "GET", url(), TokenDeTeste.bearer(mvc, email), null).andExpect(status().isForbidden());
        }
    }

    @Test
    @DisplayName("Sem token as duas operações respondem 401")
    void semToken() throws Exception {
        enviar(mvc, "GET", url(), null, null).andExpect(status().isUnauthorized());
        enviar(mvc, "POST", url(), null, respostas(true, false)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("404 para candidato inexistente, ao avaliar e ao consultar o histórico")
    void candidatoInexistente() throws Exception {
        String fantasma = "/api/candidatos/" + UUID.randomUUID() + "/avaliacoes";
        enviar(mvc, "POST", fantasma, coordenador, respostas(true, false)).andExpect(status().isNotFound());
        enviar(mvc, "GET", fantasma, coordenador, null).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("409 ESTUDO_FORA_DE_RECRUTAMENTO depois que o estudo avança para execução")
    void estudoForaDeRecrutamento() throws Exception {
        avancar(mvc, pesquisador, estudo, "EXECUCAO").andExpect(status().isOk());

        enviar(mvc, "POST", url(), coordenador, respostas(true, false)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESTUDO_FORA_DE_RECRUTAMENTO"));
    }

    @Test
    @DisplayName("409 CANDIDATO_JA_INCLUIDO: o participante incluído não pode ser reavaliado")
    void candidatoIncluido() throws Exception {
        avaliarAprovado(mvc, coordenador, candidato, estudo);
        consentir(mvc, coordenador, candidato).andExpect(status().isCreated());
        enviar(mvc, "POST", "/api/estudos/" + estudo + "/participantes", coordenador,
                "{\"candidatoId\":\"" + candidato + "\"}").andExpect(status().isCreated());

        enviar(mvc, "POST", url(), coordenador, respostas(false, true)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CANDIDATO_JA_INCLUIDO"));
    }

    @Test
    @DisplayName("Consentimento dado antes da aprovação: a aprovação leva direto a CONSENTIDO; a reprovação leva a NAO_ELEGIVEL")
    void situacaoDependeDoConsentimento() throws Exception {
        consentir(mvc, coordenador, candidato).andExpect(status().isCreated());

        enviar(mvc, "POST", url(), coordenador, respostas(false, false)).andExpect(status().isCreated());
        enviar(mvc, "GET", "/api/candidatos/" + candidato, coordenador, null)
                .andExpect(jsonPath("$.situacao").value("NAO_ELEGIVEL"));

        enviar(mvc, "POST", url(), coordenador, respostas(true, false)).andExpect(status().isCreated());
        enviar(mvc, "GET", "/api/candidatos/" + candidato, coordenador, null)
                .andExpect(jsonPath("$.situacao").value("CONSENTIDO"));
    }
}
