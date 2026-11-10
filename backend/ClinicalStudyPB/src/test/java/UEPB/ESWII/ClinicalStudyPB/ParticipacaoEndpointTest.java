package UEPB.ESWII.ClinicalStudyPB;

import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.avaliarAprovado;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.avancar;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.consentir;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.criarCandidato;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.criarEstudo;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.enviar;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.estudoEmRecrutamento;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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

import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;

/**
 * T62 (RF11): endpoints de inclusão de participantes com tokens reais. 201 com os dados da participação, 400,
 * 401, 403 por perfil, 404 e um 409 para cada regra, conferindo o {@code codigoRegra}; o contador de vagas
 * acompanha cada inclusão. A concorrência está em {@code ParticipacaoConcorrenciaTest}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ParticipacaoEndpointTest {

    @Autowired
    private MockMvc mvc;

    private String pesquisador;
    private String coordenador;
    private String estudo;

    @BeforeEach
    void preparar() throws Exception {
        pesquisador = TokenDeTeste.bearer(mvc, TokenDeTeste.PESQUISADOR);
        coordenador = TokenDeTeste.bearer(mvc, TokenDeTeste.COORDENADOR);
        estudo = estudoEmRecrutamento(mvc, pesquisador, 1);
    }

    private String url() {
        return "/api/estudos/" + estudo + "/participantes";
    }

    private String candidatoPronto(String nome) throws Exception {
        String candidato = criarCandidato(mvc, coordenador, estudo, nome);
        avaliarAprovado(mvc, coordenador, candidato, estudo);
        consentir(mvc, coordenador, candidato).andExpect(status().isCreated());
        return candidato;
    }

    private static String corpo(String candidato) {
        return "{\"candidatoId\":\"" + candidato + "\"}";
    }

    @Test
    @DisplayName("201 ao incluir: devolve código, nome, data de inclusão, quem incluiu e a situação do candidato vira INCLUIDO")
    void incluiEDevolveAParticipacao() throws Exception {
        String candidato = candidatoPronto("Candidata Inclusao");

        enviar(mvc, "POST", url(), coordenador, corpo(candidato))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.candidatoId").value(candidato))
                .andExpect(jsonPath("$.codigo").value("CAND-001"))
                .andExpect(jsonPath("$.nomeFicticio").value("Candidata Inclusao"))
                .andExpect(jsonPath("$.estudoId").value(estudo))
                .andExpect(jsonPath("$.dataInclusao").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.incluidoPor").isNotEmpty());

        enviar(mvc, "GET", "/api/candidatos/" + candidato, coordenador, null)
                .andExpect(jsonPath("$.situacao").value("INCLUIDO"));
        enviar(mvc, "GET", url(), pesquisador, null).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].candidatoId").value(candidato));
    }

    @Test
    @DisplayName("O contador de vagas acompanha a inclusão: restantes cai de 1 para 0")
    void contadorDeVagas() throws Exception {
        enviar(mvc, "GET", url() + "/vagas", coordenador, null).andExpect(status().isOk())
                .andExpect(jsonPath("$.incluidos").value(0))
                .andExpect(jsonPath("$.limite").value(1))
                .andExpect(jsonPath("$.restantes").value(1));

        enviar(mvc, "POST", url(), coordenador, corpo(candidatoPronto("Candidato Vaga"))).andExpect(status().isCreated());

        enviar(mvc, "GET", url() + "/vagas", pesquisador, null).andExpect(status().isOk())
                .andExpect(jsonPath("$.incluidos").value(1))
                .andExpect(jsonPath("$.restantes").value(0));
    }

    @Test
    @DisplayName("400 sem candidatoId, com id malformado e com JSON malformado")
    void dadosInvalidos() throws Exception {
        enviar(mvc, "POST", url(), coordenador, "{}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", startsWith("candidatoId: ")));
        enviar(mvc, "POST", url(), coordenador, "{\"candidatoId\":\"nao-e-um-uuid\"}").andExpect(status().isBadRequest());
        enviar(mvc, "POST", url(), coordenador, "{não é json").andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "{0} recebe 403 ao incluir")
    @ValueSource(strings = { TokenDeTeste.PESQUISADOR, TokenDeTeste.PARTICIPANTE, TokenDeTeste.ADMIN })
    @DisplayName("Só o coordenador inclui: os demais perfis recebem 403 e nenhum participante é criado")
    void soOCoordenadorInclui(String email) throws Exception {
        String candidato = candidatoPronto("Candidato Permissao");

        enviar(mvc, "POST", url(), TokenDeTeste.bearer(mvc, email), corpo(candidato)).andExpect(status().isForbidden());

        enviar(mvc, "GET", url(), coordenador, null).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Leitura (lista e vagas): coordenador e pesquisador leem; administrador e participante recebem 403")
    void leituraPorPerfil() throws Exception {
        for (String email : new String[] { TokenDeTeste.COORDENADOR, TokenDeTeste.PESQUISADOR }) {
            String token = TokenDeTeste.bearer(mvc, email);
            enviar(mvc, "GET", url(), token, null).andExpect(status().isOk());
            enviar(mvc, "GET", url() + "/vagas", token, null).andExpect(status().isOk());
        }
        for (String email : new String[] { TokenDeTeste.ADMIN, TokenDeTeste.PARTICIPANTE }) {
            String token = TokenDeTeste.bearer(mvc, email);
            enviar(mvc, "GET", url(), token, null).andExpect(status().isForbidden());
            enviar(mvc, "GET", url() + "/vagas", token, null).andExpect(status().isForbidden());
        }
    }

    @Test
    @DisplayName("Sem token as três operações respondem 401")
    void semToken() throws Exception {
        enviar(mvc, "GET", url(), null, null).andExpect(status().isUnauthorized());
        enviar(mvc, "GET", url() + "/vagas", null, null).andExpect(status().isUnauthorized());
        enviar(mvc, "POST", url(), null, corpo(UUID.randomUUID().toString())).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("404 para estudo inexistente, candidato inexistente e candidato de outro estudo")
    void naoEncontrado() throws Exception {
        String fantasma = "/api/estudos/" + UUID.randomUUID() + "/participantes";
        enviar(mvc, "POST", fantasma, coordenador, corpo(UUID.randomUUID().toString())).andExpect(status().isNotFound());
        enviar(mvc, "GET", fantasma, coordenador, null).andExpect(status().isNotFound());
        enviar(mvc, "GET", fantasma + "/vagas", coordenador, null).andExpect(status().isNotFound());

        enviar(mvc, "POST", url(), coordenador, corpo(UUID.randomUUID().toString())).andExpect(status().isNotFound());

        String outroEstudo = estudoEmRecrutamento(mvc, pesquisador, 3);
        String deOutroEstudo = criarCandidato(mvc, coordenador, outroEstudo, "Candidato Outro Estudo");
        enviar(mvc, "POST", url(), coordenador, corpo(deOutroEstudo)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("409 ESTUDO_FORA_DE_RECRUTAMENTO depois que o estudo avança para execução")
    void estudoForaDeRecrutamento() throws Exception {
        String candidato = candidatoPronto("Candidato Execucao");
        avancar(mvc, pesquisador, estudo, "EXECUCAO").andExpect(status().isOk());

        enviar(mvc, "POST", url(), coordenador, corpo(candidato)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESTUDO_FORA_DE_RECRUTAMENTO"));
    }

    @Test
    @DisplayName("409 CANDIDATO_JA_INCLUIDO ao incluir o mesmo candidato de novo")
    void jaIncluido() throws Exception {
        String candidato = candidatoPronto("Candidata Repetida");
        enviar(mvc, "POST", url(), coordenador, corpo(candidato)).andExpect(status().isCreated());

        enviar(mvc, "POST", url(), coordenador, corpo(candidato)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CANDIDATO_JA_INCLUIDO"));
    }

    @Test
    @DisplayName("409 CANDIDATO_NAO_ELEGIVEL sem avaliação e com avaliação reprovada")
    void naoElegivel() throws Exception {
        String semAvaliacao = criarCandidato(mvc, coordenador, estudo, "Sem Avaliacao");
        consentir(mvc, coordenador, semAvaliacao).andExpect(status().isCreated());
        enviar(mvc, "POST", url(), coordenador, corpo(semAvaliacao)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CANDIDATO_NAO_ELEGIVEL"));

        String reprovado = criarCandidato(mvc, coordenador, estudo, "Reprovado");
        enviar(mvc, "POST", "/api/candidatos/" + reprovado + "/avaliacoes", coordenador, respostasReprovadas())
                .andExpect(status().isCreated());
        consentir(mvc, coordenador, reprovado).andExpect(status().isCreated());
        enviar(mvc, "POST", url(), coordenador, corpo(reprovado)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CANDIDATO_NAO_ELEGIVEL"));
    }

    private String respostasReprovadas() throws Exception {
        String lista = enviar(mvc, "GET", "/api/estudos/" + estudo + "/criterios", coordenador, null)
                .andReturn().getResponse().getContentAsString();
        java.util.List<String> ids = com.jayway.jsonpath.JsonPath.read(lista, "$[?(@.tipo=='INCLUSAO')].id");
        return "{\"respostas\":[{\"criterioId\":\"" + ids.get(0) + "\",\"aplica\":false},{\"criterioId\":\""
                + ((java.util.List<String>) com.jayway.jsonpath.JsonPath.read(lista, "$[?(@.tipo=='EXCLUSAO')].id")).get(0)
                + "\",\"aplica\":false}]}";
    }

    @Test
    @DisplayName("409 CONSENTIMENTO_AUSENTE com o candidato aprovado e sem consentimento")
    void semConsentimento() throws Exception {
        String candidato = criarCandidato(mvc, coordenador, estudo, "Sem Consentimento");
        avaliarAprovado(mvc, coordenador, candidato, estudo);

        enviar(mvc, "POST", url(), coordenador, corpo(candidato)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONSENTIMENTO_AUSENTE"));
    }

    @Test
    @DisplayName("409 VAGAS_ESGOTADAS: a última vaga é aceita e o candidato seguinte é recusado com o limite na mensagem")
    void vagasEsgotadas() throws Exception {
        String primeiro = candidatoPronto("Primeiro Candidato");
        String segundo = candidatoPronto("Segundo Candidato");

        enviar(mvc, "POST", url(), coordenador, corpo(primeiro)).andExpect(status().isCreated());

        enviar(mvc, "POST", url(), coordenador, corpo(segundo)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("VAGAS_ESGOTADAS"))
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("limite de 1")));
        enviar(mvc, "GET", "/api/candidatos/" + segundo, coordenador, null)
                .andExpect(jsonPath("$.situacao").value("CONSENTIDO"));
    }

    @Test
    @DisplayName("Estudo em planejamento não tem participantes: a lista é vazia e as vagas mostram o limite inteiro")
    void estudoEmPlanejamento() throws Exception {
        String planejamento = criarEstudo(mvc, pesquisador, 4);

        enviar(mvc, "GET", "/api/estudos/" + planejamento + "/participantes", coordenador, null)
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        enviar(mvc, "GET", "/api/estudos/" + planejamento + "/participantes/vagas", coordenador, null)
                .andExpect(jsonPath("$.limite").value(4)).andExpect(jsonPath("$.restantes").value(4));
    }
}
