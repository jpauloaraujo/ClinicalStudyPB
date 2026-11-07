package UEPB.ESWII.ClinicalStudyPB;

import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.avancar;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.criarCriterio;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.criarEstudo;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.criarVisita;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.enviar;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.id;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;

/**
 * T50 (RF06): endpoints de visitas previstas e de ações com tokens reais. 201 ao criar, 400 com o campo que
 * falhou, 401, 403 por perfil, 404, 409 de cada regra, ordenação da resposta e remoção em cascata.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VisitaEndpointTest {

    @Autowired
    private MockMvc mvc;

    private String pesquisador;
    private String estudo;

    @BeforeEach
    void preparar() throws Exception {
        pesquisador = TokenDeTeste.bearer(mvc, TokenDeTeste.PESQUISADOR);
        estudo = criarEstudo(mvc, pesquisador, 5);
    }

    private String urlVisitas() {
        return "/api/estudos/" + estudo + "/visitas";
    }

    private static String corpoVisita(String nome, int dia, int tolerancia) {
        return "{\"nome\":\"" + nome + "\",\"diaPrevisto\":" + dia + ",\"tolerancia\":" + tolerancia + "}";
    }

    private static String corpoAcao(String nome, String categoria) {
        return "{\"nome\":\"" + nome + "\",\"descricao\":\"Descrição fictícia\",\"categoria\":\"" + categoria + "\"}";
    }

    @Test
    @DisplayName("201 ao criar a visita: devolve o dia, a tolerância, a ordem e a lista de ações vazia")
    void criaVisita() throws Exception {
        enviar(mvc, "POST", urlVisitas(), pesquisador, corpoVisita("Triagem", 0, 2))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.estudoId").value(estudo))
                .andExpect(jsonPath("$.nome").value("Triagem"))
                .andExpect(jsonPath("$.diaPrevisto").value(0))
                .andExpect(jsonPath("$.tolerancia").value(2))
                .andExpect(jsonPath("$.ordem").value(1))
                .andExpect(jsonPath("$.acoes", hasSize(0)));
    }

    @Test
    @DisplayName("A listagem vem ordenada pelo dia previsto, com as ações de cada visita")
    void listagemOrdenadaComAcoes() throws Exception {
        String retorno = criarVisita(mvc, pesquisador, estudo, "Retorno", 30);
        String triagem = criarVisita(mvc, pesquisador, estudo, "Triagem", 0);
        criarVisita(mvc, pesquisador, estudo, "Seguimento", 14);
        enviar(mvc, "POST", "/api/visitas/" + triagem + "/acoes", pesquisador, corpoAcao("Hemograma", "EXAME_LABORATORIAL"))
                .andExpect(status().isCreated());
        enviar(mvc, "POST", "/api/visitas/" + triagem + "/acoes", pesquisador, corpoAcao("Questionário inicial", "QUESTIONARIO"))
                .andExpect(status().isCreated());

        enviar(mvc, "GET", urlVisitas(), pesquisador, null).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].nome").value("Triagem")).andExpect(jsonPath("$[0].ordem").value(1))
                .andExpect(jsonPath("$[0].acoes", hasSize(2)))
                .andExpect(jsonPath("$[1].nome").value("Seguimento")).andExpect(jsonPath("$[1].ordem").value(2))
                .andExpect(jsonPath("$[2].id").value(retorno)).andExpect(jsonPath("$[2].ordem").value(3));
    }

    @Test
    @DisplayName("Uma visita com atividades é criada, consultada e removida; a remoção leva as ações junto")
    void criaConsultaERemoveEmCascata() throws Exception {
        String visita = criarVisita(mvc, pesquisador, estudo, "Triagem", 0);
        String acao = id(enviar(mvc, "POST", "/api/visitas/" + visita + "/acoes", pesquisador,
                corpoAcao("Hemograma", "EXAME_LABORATORIAL")).andExpect(status().isCreated())
                .andExpect(jsonPath("$.visitaId").value(visita)).andExpect(jsonPath("$.categoria").value("EXAME_LABORATORIAL")));

        enviar(mvc, "GET", urlVisitas(), pesquisador, null).andExpect(jsonPath("$[0].acoes[0].id").value(acao));

        enviar(mvc, "DELETE", urlVisitas() + "/" + visita, pesquisador, null).andExpect(status().isNoContent());

        enviar(mvc, "GET", urlVisitas(), pesquisador, null).andExpect(jsonPath("$", hasSize(0)));
        // A ação foi apagada junto com a visita
        enviar(mvc, "PUT", "/api/visitas/" + visita + "/acoes/" + acao, pesquisador, corpoAcao("Outra", "OUTROS"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Atualiza visita e ação; a edição do dia reordena a lista")
    void atualizaVisitaEAcao() throws Exception {
        String a = criarVisita(mvc, pesquisador, estudo, "A", 0);
        criarVisita(mvc, pesquisador, estudo, "B", 10);
        String acao = id(enviar(mvc, "POST", "/api/visitas/" + a + "/acoes", pesquisador, corpoAcao("Exame", "EXAME_IMAGEM"))
                .andExpect(status().isCreated()));

        enviar(mvc, "PUT", urlVisitas() + "/" + a, pesquisador, corpoVisita("A", 20, 5)).andExpect(status().isOk())
                .andExpect(jsonPath("$.diaPrevisto").value(20)).andExpect(jsonPath("$.tolerancia").value(5));
        enviar(mvc, "GET", urlVisitas(), pesquisador, null)
                .andExpect(jsonPath("$[0].nome").value("B")).andExpect(jsonPath("$[1].nome").value("A"));

        enviar(mvc, "PUT", "/api/visitas/" + a + "/acoes/" + acao, pesquisador, corpoAcao("Ressonância", "EXAME_IMAGEM"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Ressonância"));
        enviar(mvc, "DELETE", "/api/visitas/" + a + "/acoes/" + acao, pesquisador, null).andExpect(status().isNoContent());
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
            "nome ausente|{\"diaPrevisto\":0,\"tolerancia\":0}|nome",
            "nome em branco|{\"nome\":\"  \",\"diaPrevisto\":0,\"tolerancia\":0}|nome",
            "dia ausente|{\"nome\":\"Triagem\",\"tolerancia\":0}|diaPrevisto",
            "dia negativo|{\"nome\":\"Triagem\",\"diaPrevisto\":-1,\"tolerancia\":0}|diaPrevisto",
            "tolerância ausente|{\"nome\":\"Triagem\",\"diaPrevisto\":0}|tolerancia",
            "tolerância negativa|{\"nome\":\"Triagem\",\"diaPrevisto\":0,\"tolerancia\":-1}|tolerancia",
            "tolerância acima de 30|{\"nome\":\"Triagem\",\"diaPrevisto\":0,\"tolerancia\":31}|tolerancia" })
    @DisplayName("400 com o nome do campo e a mensagem quando a visita é inválida")
    void visitaInvalida(String caso, String json, String campo) throws Exception {
        enviar(mvc, "POST", urlVisitas(), pesquisador, json).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro", startsWith(campo + ": ")));
        enviar(mvc, "GET", urlVisitas(), pesquisador, null).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("400 para nome com mais de 100 caracteres, ação sem categoria ou com categoria desconhecida e JSON malformado")
    void outrosCorposInvalidos() throws Exception {
        enviar(mvc, "POST", urlVisitas(), pesquisador, corpoVisita("n".repeat(101), 0, 0))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro", containsString("nome")));
        enviar(mvc, "POST", urlVisitas(), pesquisador, "{não é json").andExpect(status().isBadRequest());

        String visita = criarVisita(mvc, pesquisador, estudo, "Triagem", 0);
        enviar(mvc, "POST", "/api/visitas/" + visita + "/acoes", pesquisador, "{\"nome\":\"Exame\"}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro", startsWith("categoria: ")));
        enviar(mvc, "POST", "/api/visitas/" + visita + "/acoes", pesquisador, corpoAcao("Exame", "INEXISTENTE"))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "{0} em {1}")
    @CsvSource({ "coordenador@clinicalstudy.test,POST", "participante@clinicalstudy.test,POST", "admin@clinicalstudy.test,POST",
            "coordenador@clinicalstudy.test,PUT", "participante@clinicalstudy.test,PUT",
            "coordenador@clinicalstudy.test,DELETE", "admin@clinicalstudy.test,DELETE" })
    @DisplayName("Só o pesquisador escreve visitas e ações: os outros perfis recebem 403 e nada muda")
    void escritaProibidaParaOutrosPerfis(String email, String metodo) throws Exception {
        String visita = criarVisita(mvc, pesquisador, estudo, "Triagem", 0);
        String token = TokenDeTeste.bearer(mvc, email);
        String alvoVisita = "POST".equals(metodo) ? urlVisitas() : urlVisitas() + "/" + visita;
        String alvoAcao = "POST".equals(metodo) ? "/api/visitas/" + visita + "/acoes" : "/api/visitas/" + visita + "/acoes/" + UUID.randomUUID();

        enviar(mvc, metodo, alvoVisita, token, corpoVisita("Outra", 5, 0)).andExpect(status().isForbidden());
        enviar(mvc, metodo, alvoAcao, token, corpoAcao("Exame", "OUTROS")).andExpect(status().isForbidden());

        enviar(mvc, "GET", urlVisitas(), pesquisador, null).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Triagem")).andExpect(jsonPath("$[0].acoes", hasSize(0)));
    }

    @Test
    @DisplayName("Leitura: pesquisador e coordenador leem; participante e administrador recebem 403")
    void leituraPorPerfil() throws Exception {
        criarVisita(mvc, pesquisador, estudo, "Triagem", 0);
        for (String email : new String[] { TokenDeTeste.PESQUISADOR, TokenDeTeste.COORDENADOR }) {
            enviar(mvc, "GET", urlVisitas(), TokenDeTeste.bearer(mvc, email), null).andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }
        for (String email : new String[] { TokenDeTeste.PARTICIPANTE, TokenDeTeste.ADMIN }) {
            enviar(mvc, "GET", urlVisitas(), TokenDeTeste.bearer(mvc, email), null).andExpect(status().isForbidden());
        }
    }

    @Test
    @DisplayName("Sem token todas as operações respondem 401")
    void semToken() throws Exception {
        String visita = criarVisita(mvc, pesquisador, estudo, "Triagem", 0);
        enviar(mvc, "GET", urlVisitas(), null, null).andExpect(status().isUnauthorized());
        enviar(mvc, "POST", urlVisitas(), null, corpoVisita("X", 1, 0)).andExpect(status().isUnauthorized());
        enviar(mvc, "PUT", urlVisitas() + "/" + visita, null, corpoVisita("X", 1, 0)).andExpect(status().isUnauthorized());
        enviar(mvc, "DELETE", urlVisitas() + "/" + visita, null, null).andExpect(status().isUnauthorized());
        enviar(mvc, "POST", "/api/visitas/" + visita + "/acoes", null, corpoAcao("X", "OUTROS")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("404 para estudo, visita e ação inexistentes ou de outro estudo")
    void naoEncontrado() throws Exception {
        String fantasma = "/api/estudos/" + UUID.randomUUID() + "/visitas";
        enviar(mvc, "GET", fantasma, pesquisador, null).andExpect(status().isNotFound());
        enviar(mvc, "POST", fantasma, pesquisador, corpoVisita("X", 1, 0)).andExpect(status().isNotFound());
        enviar(mvc, "PUT", urlVisitas() + "/" + UUID.randomUUID(), pesquisador, corpoVisita("X", 1, 0)).andExpect(status().isNotFound());
        enviar(mvc, "DELETE", urlVisitas() + "/" + UUID.randomUUID(), pesquisador, null).andExpect(status().isNotFound());
        enviar(mvc, "POST", "/api/visitas/" + UUID.randomUUID() + "/acoes", pesquisador, corpoAcao("X", "OUTROS")).andExpect(status().isNotFound());

        String visita = criarVisita(mvc, pesquisador, estudo, "Triagem", 0);
        String outroEstudo = criarEstudo(mvc, pesquisador, 3);
        enviar(mvc, "DELETE", "/api/estudos/" + outroEstudo + "/visitas/" + visita, pesquisador, null).andExpect(status().isNotFound());
        enviar(mvc, "PUT", "/api/visitas/" + visita + "/acoes/" + UUID.randomUUID(), pesquisador, corpoAcao("X", "OUTROS")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("409 VISITA_DIA_DUPLICADO para dia repetido ao criar e ao editar")
    void diaRepetido() throws Exception {
        criarVisita(mvc, pesquisador, estudo, "Triagem", 0);
        String outra = criarVisita(mvc, pesquisador, estudo, "Retorno", 10);

        enviar(mvc, "POST", urlVisitas(), pesquisador, corpoVisita("Duplicada", 0, 0)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("VISITA_DIA_DUPLICADO"));
        enviar(mvc, "PUT", urlVisitas() + "/" + outra, pesquisador, corpoVisita("Retorno", 0, 0)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("VISITA_DIA_DUPLICADO"));
        // O mesmo dia em outro estudo é permitido
        String outroEstudo = criarEstudo(mvc, pesquisador, 3);
        criarVisita(mvc, pesquisador, outroEstudo, "Triagem", 0);
    }

    @Test
    @DisplayName("409 ESTUDO_FORA_DE_PLANEJAMENTO para criar, editar e excluir visitas e ações depois que o estudo avançou")
    void foraDePlanejamento() throws Exception {
        criarCriterio(mvc, pesquisador, estudo, "INCLUSAO", "Idade", "Maior de 18 anos");
        String visita = criarVisita(mvc, pesquisador, estudo, "Triagem", 0);
        String acao = id(enviar(mvc, "POST", "/api/visitas/" + visita + "/acoes", pesquisador, corpoAcao("Exame", "OUTROS"))
                .andExpect(status().isCreated()));
        avancar(mvc, pesquisador, estudo, "RECRUTAMENTO").andExpect(status().isOk());

        String codigo = "ESTUDO_FORA_DE_PLANEJAMENTO";
        enviar(mvc, "POST", urlVisitas(), pesquisador, corpoVisita("Nova", 5, 0)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value(codigo));
        enviar(mvc, "PUT", urlVisitas() + "/" + visita, pesquisador, corpoVisita("Triagem", 0, 1)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value(codigo));
        enviar(mvc, "DELETE", urlVisitas() + "/" + visita, pesquisador, null).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value(codigo));
        enviar(mvc, "POST", "/api/visitas/" + visita + "/acoes", pesquisador, corpoAcao("Outra", "OUTROS")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value(codigo));
        enviar(mvc, "PUT", "/api/visitas/" + visita + "/acoes/" + acao, pesquisador, corpoAcao("Exame", "OUTROS")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value(codigo));
        enviar(mvc, "DELETE", "/api/visitas/" + visita + "/acoes/" + acao, pesquisador, null).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value(codigo));

        enviar(mvc, "GET", urlVisitas(), pesquisador, null).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].acoes", hasSize(1)));
    }
}
