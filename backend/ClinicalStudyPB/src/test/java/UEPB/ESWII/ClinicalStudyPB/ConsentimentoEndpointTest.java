package UEPB.ESWII.ClinicalStudyPB;

import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.avaliarAprovado;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.avancar;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.consentir;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.criarCandidato;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.criarCriterio;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.criarEstudo;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.criarVisita;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.enviar;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.estudoEmRecrutamento;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
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
 * T59 (RF10): endpoints de consentimento com tokens reais. 201 no registro, 400 com data futura, 401, 403 por
 * perfil, 404 e 409 de cada regra, mais o resumo do consentimento no detalhe do candidato.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ConsentimentoEndpointTest {

    @Autowired
    private MockMvc mvc;

    private String pesquisador;
    private String coordenador;
    private String estudo;
    private String candidato;

    @BeforeEach
    void preparar() throws Exception {
        pesquisador = TokenDeTeste.bearer(mvc, TokenDeTeste.PESQUISADOR);
        coordenador = TokenDeTeste.bearer(mvc, TokenDeTeste.COORDENADOR);
        estudo = estudoEmRecrutamento(mvc, pesquisador, 5);
        candidato = criarCandidato(mvc, coordenador, estudo, "Candidata Consentimento");
    }

    private String url() {
        return "/api/candidatos/" + candidato + "/consentimento";
    }

    @Test
    @DisplayName("201 ao registrar: devolve o comprovante com data, responsável e versão padrão do termo")
    void registraEDevolveComprovante() throws Exception {
        enviar(mvc, "POST", url(), coordenador, "{}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.candidatoId").value(candidato))
                .andExpect(jsonPath("$.dataHora").isNotEmpty())
                .andExpect(jsonPath("$.responsavel").isNotEmpty())
                .andExpect(jsonPath("$.versaoTermo").value("TCLE-SIMULADO v1"));

        enviar(mvc, "GET", url(), pesquisador, null).andExpect(status().isOk())
                .andExpect(jsonPath("$.candidatoId").value(candidato));
    }

    @Test
    @DisplayName("Aceita versão própria do termo e uma data e hora do passado")
    void versaoEDataInformadas() throws Exception {
        String ontem = LocalDateTime.now().minusDays(1).withNano(0).toString();
        enviar(mvc, "POST", url(), coordenador, "{\"dataHora\":\"" + ontem + "\",\"versaoTermo\":\"TCLE-SIMULADO v2\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.versaoTermo").value("TCLE-SIMULADO v2"))
                .andExpect(jsonPath("$.dataHora", startsWith(ontem.substring(0, 16))));
    }

    @Test
    @DisplayName("400 para data futura, versão com mais de 60 caracteres, data malformada e JSON malformado")
    void dadosInvalidos() throws Exception {
        String amanha = LocalDateTime.now().plusDays(1).withNano(0).toString();
        enviar(mvc, "POST", url(), coordenador, "{\"dataHora\":\"" + amanha + "\"}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro", containsString("futura")));
        enviar(mvc, "POST", url(), coordenador, "{\"versaoTermo\":\"" + "v".repeat(61) + "\"}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro", startsWith("versaoTermo: ")));
        enviar(mvc, "POST", url(), coordenador, "{\"dataHora\":\"ontem\"}").andExpect(status().isBadRequest());
        enviar(mvc, "POST", url(), coordenador, "{não é json").andExpect(status().isBadRequest());

        enviar(mvc, "GET", url(), coordenador, null).andExpect(status().isNotFound());
    }

    @ParameterizedTest(name = "{0} recebe 403 ao registrar")
    @ValueSource(strings = { TokenDeTeste.PESQUISADOR, TokenDeTeste.PARTICIPANTE, TokenDeTeste.ADMIN })
    @DisplayName("Só o coordenador registra: pesquisador, participante e administrador recebem 403 e nada é gravado")
    void soOCoordenadorRegistra(String email) throws Exception {
        String token = TokenDeTeste.bearer(mvc, email);

        enviar(mvc, "POST", url(), token, "{}").andExpect(status().isForbidden());

        enviar(mvc, "GET", url(), coordenador, null).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Leitura: coordenador e pesquisador leem; participante e administrador recebem 403")
    void leituraPorPerfil() throws Exception {
        consentir(mvc, coordenador, candidato).andExpect(status().isCreated());

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
        enviar(mvc, "POST", url(), null, "{}").andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("404 para candidato inexistente e para consentimento ainda não registrado")
    void naoEncontrado() throws Exception {
        String fantasma = "/api/candidatos/" + UUID.randomUUID() + "/consentimento";
        enviar(mvc, "POST", fantasma, coordenador, "{}").andExpect(status().isNotFound());
        enviar(mvc, "GET", fantasma, coordenador, null).andExpect(status().isNotFound());

        enviar(mvc, "GET", url(), coordenador, null).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro", containsString("não registrado")));
    }

    @Test
    @DisplayName("409 CONSENTIMENTO_DUPLICADO ao registrar de novo")
    void duplicado() throws Exception {
        consentir(mvc, coordenador, candidato).andExpect(status().isCreated());

        consentir(mvc, coordenador, candidato).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONSENTIMENTO_DUPLICADO"));
    }

    @Test
    @DisplayName("409 ESTUDO_FORA_DE_RECRUTAMENTO depois que o estudo avança para execução")
    void estudoForaDeRecrutamento() throws Exception {
        avancar(mvc, pesquisador, estudo, "EXECUCAO").andExpect(status().isOk());

        consentir(mvc, coordenador, candidato).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESTUDO_FORA_DE_RECRUTAMENTO"));
    }

    @Test
    @DisplayName("409 CANDIDATO_JA_INCLUIDO: o candidato incluído não recebe consentimento (inclusão com o consentimento já dado)")
    void candidatoIncluido() throws Exception {
        avaliarAprovado(mvc, coordenador, candidato, estudo);
        consentir(mvc, coordenador, candidato).andExpect(status().isCreated());
        enviar(mvc, "POST", "/api/estudos/" + estudo + "/participantes", coordenador,
                "{\"candidatoId\":\"" + candidato + "\"}").andExpect(status().isCreated());

        // O consentimento já existe, mas a regra de inclusão é verificada antes da de duplicidade
        consentir(mvc, coordenador, candidato).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CANDIDATO_JA_INCLUIDO"));
    }

    @Test
    @DisplayName("O detalhe do candidato traz o resumo do consentimento (registrado e data) e a situação consentido")
    void resumoNoDetalheDoCandidato() throws Exception {
        enviar(mvc, "GET", "/api/candidatos/" + candidato, coordenador, null).andExpect(status().isOk())
                .andExpect(jsonPath("$.consentimentoRegistrado").value(false))
                .andExpect(jsonPath("$.dataConsentimento").doesNotExist());

        avaliarAprovado(mvc, coordenador, candidato, estudo);
        consentir(mvc, coordenador, candidato).andExpect(status().isCreated());

        enviar(mvc, "GET", "/api/candidatos/" + candidato, pesquisador, null).andExpect(status().isOk())
                .andExpect(jsonPath("$.consentimentoRegistrado").value(true))
                .andExpect(jsonPath("$.dataConsentimento").isNotEmpty())
                .andExpect(jsonPath("$.situacao").value("CONSENTIDO"));
    }

    @Test
    @DisplayName("O consentimento pode vir antes da avaliação: a situação só muda quando o candidato é aprovado")
    void consentimentoAntesDaAvaliacao() throws Exception {
        consentir(mvc, coordenador, candidato).andExpect(status().isCreated());
        enviar(mvc, "GET", "/api/candidatos/" + candidato, coordenador, null)
                .andExpect(jsonPath("$.situacao").value("CADASTRADO"))
                .andExpect(jsonPath("$.consentimentoRegistrado").value(true));

        avaliarAprovado(mvc, coordenador, candidato, estudo);

        enviar(mvc, "GET", "/api/candidatos/" + candidato, coordenador, null)
                .andExpect(jsonPath("$.situacao").value("CONSENTIDO"));
    }

    @Test
    @DisplayName("Estudo sem recrutamento aberto não permite cadastrar candidato, então não há consentimento a registrar")
    void estudoEmPlanejamentoNaoTemCandidatos() throws Exception {
        String planejamento = criarEstudo(mvc, pesquisador, 3);
        criarCriterio(mvc, pesquisador, planejamento, "INCLUSAO", "Idade", "Maior de 18 anos");
        criarVisita(mvc, pesquisador, planejamento, "Triagem", 0);

        enviar(mvc, "POST", "/api/estudos/" + planejamento + "/candidatos", coordenador,
                "{\"nomeFicticio\":\"Fora\",\"dataNascimento\":\"1990-01-01\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESTUDO_FORA_DE_RECRUTAMENTO"));
    }
}
