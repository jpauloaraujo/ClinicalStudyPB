package UEPB.ESWII.ClinicalStudyPB;

import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.avaliarAprovado;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.consentir;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.criarCandidato;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.enviar;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.estudoEmRecrutamento;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.id;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Connection;
import java.time.Clock;
import java.time.LocalDate;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;

/**
 * T103 (RF16): testes de fumaça dos endpoints de retirada ({@code POST} e {@code GET}) com tokens reais,
 * autorização por perfil, formato único de erro e cancelamento em cascata das visitas futuras. A bateria completa
 * (incluindo concorrência e rollback forçado) é a T106.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RetiradaEndpointTest {

    private static final String ESTUDO_DEMO = "d6300000-0000-4000-8000-000000001901";
    private static final String PARTICIPACAO_P1 = "d6300000-0000-4000-8000-000000001921";
    private static final String PARTICIPACAO_P3 = "d6300000-0000-4000-8000-000000001923";
    private static final String INEXISTENTE = "d6300000-0000-4000-8000-000000009999";
    private static final String ISO_COM_FUSO = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2}(\\.\\d+)?)?[+-]\\d{2}:\\d{2}";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private Clock clock;

    private String coordenador;
    private String pesquisador;
    private String hoje;

    @BeforeEach
    void preparar() throws Exception {
        Connection conexao = DataSourceUtils.getConnection(dataSource);
        ScriptUtils.executeSqlScript(conexao, new ClassPathResource("db/demo/R__dados_demonstracao.sql"));
        ScriptUtils.executeSqlScript(conexao, new ClassPathResource("db/demo/R__demo_19_visitas_agendadas.sql"));
        coordenador = TokenDeTeste.bearer(mvc, TokenDeTeste.COORDENADOR);
        pesquisador = TokenDeTeste.bearer(mvc, TokenDeTeste.PESQUISADOR);
        hoje = LocalDate.now(clock).toString();
    }

    private static String url(String participacaoId) {
        return "/api/participacoes/" + participacaoId + "/retirada";
    }

    private String corpoValido() {
        return "{\"motivo\":\"Mudança de cidade\",\"solicitante\":\"PARTICIPANTE\",\"dataRetirada\":\"" + hoje + "\"}";
    }

    @Test
    @DisplayName("Coordenador registra retirada (201 com Location), cancela visita futura e coordenador/pesquisador consultam (200)")
    void registraEConsultaRetirada() throws Exception {
        // Antes da retirada, GET devolve 404
        enviar(mvc, "GET", url(PARTICIPACAO_P1), coordenador, null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"));

        // P1 tem Triagem REALIZADA e Retorno 30 AGENDADA em D+10 (1 visita futura)
        enviar(mvc, "POST", url(PARTICIPACAO_P1), coordenador, corpoValido())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(url(PARTICIPACAO_P1))))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.participacaoId").value(PARTICIPACAO_P1))
                .andExpect(jsonPath("$.motivo").value("Mudança de cidade"))
                .andExpect(jsonPath("$.solicitante").value("PARTICIPANTE"))
                .andExpect(jsonPath("$.dataRetirada").value(hoje))
                .andExpect(jsonPath("$.registradoPor").isNotEmpty())
                .andExpect(jsonPath("$.registradoEm").value(matchesPattern(ISO_COM_FUSO)))
                .andExpect(jsonPath("$.visitasCanceladas").value(1));

        for (String token : new String[] {coordenador, pesquisador}) {
            enviar(mvc, "GET", url(PARTICIPACAO_P1), token, null)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.participacaoId").value(PARTICIPACAO_P1))
                    .andExpect(jsonPath("$.motivo").value("Mudança de cidade"))
                    .andExpect(jsonPath("$.solicitante").value("PARTICIPANTE"))
                    .andExpect(jsonPath("$.visitasCanceladas").value(1));
        }
    }

    @Test
    @DisplayName("401 sem token; 403 para pesquisador na escrita e para administrador e participante em tudo")
    void autorizacaoPorPerfil() throws Exception {
        enviar(mvc, "POST", url(PARTICIPACAO_P3), null, corpoValido()).andExpect(status().isUnauthorized());
        enviar(mvc, "GET", url(PARTICIPACAO_P3), null, null).andExpect(status().isUnauthorized());

        enviar(mvc, "POST", url(PARTICIPACAO_P3), pesquisador, corpoValido())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));

        for (String email : new String[] {TokenDeTeste.ADMIN, TokenDeTeste.PARTICIPANTE}) {
            String token = TokenDeTeste.bearer(mvc, email);
            enviar(mvc, "POST", url(PARTICIPACAO_P3), token, corpoValido()).andExpect(status().isForbidden());
            enviar(mvc, "GET", url(PARTICIPACAO_P3), token, null).andExpect(status().isForbidden());
        }
    }

    @Test
    @DisplayName("404 para participação inexistente no POST e no GET")
    void participacaoInexistente() throws Exception {
        enviar(mvc, "POST", url(INEXISTENTE), coordenador, corpoValido())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"));
        enviar(mvc, "GET", url(INEXISTENTE), pesquisador, null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"));
    }

    @Test
    @DisplayName("400 para MOTIVO_OBRIGATORIO, DATA_RETIRADA_FUTURA, VALIDACAO_CAMPOS e CORPO_INVALIDO")
    void errosDeValidacao() throws Exception {
        enviar(mvc, "POST", url(PARTICIPACAO_P3), coordenador,
                "{\"motivo\":\"   \",\"solicitante\":\"PARTICIPANTE\",\"dataRetirada\":\"" + hoje + "\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("MOTIVO_OBRIGATORIO"));

        String amanha = LocalDate.now(clock).plusDays(1).toString();
        enviar(mvc, "POST", url(PARTICIPACAO_P3), coordenador,
                "{\"motivo\":\"Mudança de cidade\",\"solicitante\":\"PARTICIPANTE\",\"dataRetirada\":\"" + amanha + "\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATA_RETIRADA_FUTURA"));

        enviar(mvc, "POST", url(PARTICIPACAO_P3), coordenador,
                "{\"motivo\":\"Mudança de cidade\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO_CAMPOS"));

        enviar(mvc, "POST", url(PARTICIPACAO_P3), coordenador,
                "{\"motivo\":\"Mudança de cidade\",\"solicitante\":\"INVALIDO\",\"dataRetirada\":\"" + hoje + "\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("CORPO_INVALIDO"));

        // T106 (RQ-T106-03 e RQ-T106-12): motivo com 4 caracteres, >500 caracteres e JSON malformado
        enviar(mvc, "POST", url(PARTICIPACAO_P3), coordenador,
                "{\"motivo\":\"abcd\",\"solicitante\":\"PARTICIPANTE\",\"dataRetirada\":\"" + hoje + "\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("MOTIVO_OBRIGATORIO"));

        enviar(mvc, "POST", url(PARTICIPACAO_P3), coordenador,
                "{\"motivo\":\"" + "m".repeat(501) + "\",\"solicitante\":\"PARTICIPANTE\",\"dataRetirada\":\"" + hoje + "\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO_CAMPOS"));

        enviar(mvc, "POST", url(PARTICIPACAO_P3), coordenador, "{\"motivo\":")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("CORPO_INVALIDO"));
    }

    @Test
    @DisplayName("T106 (RQ-T106-14): lista de participantes traz situacao RETIRADO após a retirada")
    void listaDeParticipantesMostraSituacaoRetirado() throws Exception {
        enviar(mvc, "POST", url(PARTICIPACAO_P1), coordenador, corpoValido())
                .andExpect(status().isCreated());

        enviar(mvc, "GET", "/api/estudos/" + ESTUDO_DEMO + "/participantes", pesquisador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='" + PARTICIPACAO_P1 + "')].situacao", hasItem("RETIRADO")))
                .andExpect(jsonPath("$[?(@.id=='" + PARTICIPACAO_P3 + "')].situacao", hasItem("ATIVO")));
    }

    @Test
    @DisplayName("T106 (RQ-T106-11): retirada não libera vaga e nova inclusão em estudo cheio devolve 409 VAGAS_ESGOTADAS")
    void retiradaNaoLiberaVagaECandidatoNovoRecebeVagasEsgotadas() throws Exception {
        String estudoId = estudoEmRecrutamento(mvc, pesquisador, 1);

        String cand1 = criarCandidato(mvc, coordenador, estudoId, "Pessoa Um (Ficticia)");
        avaliarAprovado(mvc, coordenador, cand1, estudoId);
        consentir(mvc, coordenador, cand1).andExpect(status().isCreated());
        String participacao1 = id(enviar(mvc, "POST", "/api/estudos/" + estudoId + "/participantes", coordenador,
                "{\"candidatoId\":\"" + cand1 + "\"}").andExpect(status().isCreated()));

        enviar(mvc, "GET", "/api/estudos/" + estudoId + "/participantes/vagas", coordenador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incluidos").value(1))
                .andExpect(jsonPath("$.limite").value(1))
                .andExpect(jsonPath("$.restantes").value(0));

        // Retira o único participante (com motivo exatamente no limite mínimo de 5 caracteres)
        enviar(mvc, "POST", url(participacao1), coordenador,
                "{\"motivo\":\"12345\",\"solicitante\":\"COORDENACAO\",\"dataRetirada\":\"" + hoje + "\"}")
                .andExpect(status().isCreated());

        // Contagem de vagas não muda
        enviar(mvc, "GET", "/api/estudos/" + estudoId + "/participantes/vagas", coordenador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incluidos").value(1))
                .andExpect(jsonPath("$.limite").value(1))
                .andExpect(jsonPath("$.restantes").value(0));

        // Segundo candidato pronto tenta entrar: continua VAGAS_ESGOTADAS
        String cand2 = criarCandidato(mvc, coordenador, estudoId, "Pessoa Dois (Ficticia)");
        avaliarAprovado(mvc, coordenador, cand2, estudoId);
        consentir(mvc, coordenador, cand2).andExpect(status().isCreated());

        enviar(mvc, "POST", "/api/estudos/" + estudoId + "/participantes", coordenador,
                "{\"candidatoId\":\"" + cand2 + "\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("VAGAS_ESGOTADAS"));
    }

    @Test
    @DisplayName("409 para segunda retirada do mesmo participante (PARTICIPANTE_JA_RETIRADO)")
    void segundaRetiradaRecusada() throws Exception {
        enviar(mvc, "POST", url(PARTICIPACAO_P3), coordenador, corpoValido())
                .andExpect(status().isCreated());
        enviar(mvc, "POST", url(PARTICIPACAO_P3), coordenador, corpoValido())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("PARTICIPANTE_JA_RETIRADO"));
    }

    @Test
    @DisplayName("409 para retirada em estudo encerrado (ESTUDO_ENCERRADO)")
    void estudoEncerradoRecusado() throws Exception {
        // Encerrado antes de qualquer requisição, para o estudo não estar ainda no contexto de persistência do teste
        jdbc.update("UPDATE estudos SET status = 'ENCERRADO' WHERE id = CAST(? AS UUID)", ESTUDO_DEMO);
        enviar(mvc, "POST", url(PARTICIPACAO_P1), coordenador, corpoValido())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESTUDO_ENCERRADO"));
    }
}
