package UEPB.ESWII.ClinicalStudyPB;

import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.enviar;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Connection;

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
 * T90 (RF14): teste de fumaça dos endpoints dos resultados com o banco e tokens reais, sobre os dados de demonstração
 * do estudo "Demo - Acompanhamento de participantes" (Triagem do P1 REALIZADA; Retorno 30 dias do P1 AGENDADO).
 * Cobre a autorização (só o pesquisador escreve; administrador e participante não acessam), 201 no primeiro registro
 * e 200 na edição, e um caso de cada código de erro. A bateria completa das regras é a T92. Os scripts rodam dentro
 * da transação do teste, que é desfeita no fim. Somente dados fictícios.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ResultadoAtividadeEndpointTest {

    private static final String TRIAGEM_P1 = "d6300000-0000-4000-8000-000000001981";
    private static final String RETORNO_30_P1 = "d6300000-0000-4000-8000-000000001982";
    private static final String AFERIR_PRESSAO = "d6300000-0000-4000-8000-000000001941";
    private static final String COLETAR_SANGUE = "d6300000-0000-4000-8000-000000001942";
    private static final String EXAME_FISICO_DO_RETORNO_30 = "d6300000-0000-4000-8000-000000001944";
    private static final String INEXISTENTE = "d6300000-0000-4000-8000-000000009999";
    private static final String ESTUDO_DEMO = "d6300000-0000-4000-8000-000000001901";
    private static final String CORPO = "{\"valor\":\"120/80\",\"unidade\":\"mmHg\",\"observacao\":\"Sentado\"}";
    private static final String ISO_COM_FUSO = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2}(\\.\\d+)?)?[+-]\\d{2}:\\d{2}";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private DataSource dataSource;

    private String pesquisador;
    private String coordenador;

    @BeforeEach
    void carregarDemonstracaoEEntrar() throws Exception {
        Connection conexao = DataSourceUtils.getConnection(dataSource);
        ScriptUtils.executeSqlScript(conexao, new ClassPathResource("db/demo/R__dados_demonstracao.sql"));
        ScriptUtils.executeSqlScript(conexao, new ClassPathResource("db/demo/R__demo_19_visitas_agendadas.sql"));
        pesquisador = TokenDeTeste.bearer(mvc, TokenDeTeste.PESQUISADOR);
        coordenador = TokenDeTeste.bearer(mvc, TokenDeTeste.COORDENADOR);
    }

    private static String resultados(String visita) {
        return "/api/visitas-agendadas/" + visita + "/resultados";
    }

    private static String resultado(String visita, String acao) {
        return resultados(visita) + "/" + acao;
    }

    @Test
    @DisplayName("Pesquisador e coordenador consultam: a Triagem do P1 tem 1 avaliação pendente e nenhum bloqueio")
    void pesquisadorECoordenadorConsultam() throws Exception {
        for (String token : new String[] {pesquisador, coordenador}) {
            enviar(mvc, "GET", resultados(TRIAGEM_P1), token, null)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.visitaAgendadaId").value(TRIAGEM_P1))
                    .andExpect(jsonPath("$.situacaoVisita").value("REALIZADA"))
                    .andExpect(jsonPath("$.bloqueio").value(nullValue()))
                    .andExpect(jsonPath("$.total").value(1))
                    .andExpect(jsonPath("$.registrados").value(0))
                    .andExpect(jsonPath("$.pendentes").value(1))
                    .andExpect(jsonPath("$.atividades", hasSize(1)))
                    .andExpect(jsonPath("$.atividades[0].acaoId").value(AFERIR_PRESSAO))
                    .andExpect(jsonPath("$.atividades[0].nome").value("Aferir pressão arterial"))
                    .andExpect(jsonPath("$.atividades[0].resultado").value(nullValue()));
        }
    }

    @Test
    @DisplayName("Visita não realizada: 200 com bloqueio VISITA_NAO_REALIZADA")
    void consultaDeVisitaNaoRealizadaInformaOBloqueio() throws Exception {
        enviar(mvc, "GET", resultados(RETORNO_30_P1), coordenador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacaoVisita").value("AGENDADA"))
                .andExpect(jsonPath("$.bloqueio").value("VISITA_NAO_REALIZADA"));
    }

    @Test
    @DisplayName("Pesquisador registra (201) e depois edita (200); a consulta passa a mostrar o resultado com autor e datas com fuso")
    void pesquisadorRegistraEEdita() throws Exception {
        enviar(mvc, "PUT", resultado(TRIAGEM_P1, AFERIR_PRESSAO), pesquisador, CORPO)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.acaoId").value(AFERIR_PRESSAO))
                .andExpect(jsonPath("$.valor").value("120/80"))
                .andExpect(jsonPath("$.unidade").value("mmHg"))
                .andExpect(jsonPath("$.observacao").value("Sentado"))
                .andExpect(jsonPath("$.registradoPor").isNotEmpty())
                .andExpect(jsonPath("$.registradoEm").value(matchesPattern(ISO_COM_FUSO)))
                .andExpect(jsonPath("$.alteradoEm").value(matchesPattern(ISO_COM_FUSO)));

        enviar(mvc, "PUT", resultado(TRIAGEM_P1, AFERIR_PRESSAO), pesquisador,
                "{\"valor\":\"  130/85  \",\"unidade\":\"mmHg\",\"observacao\":\"   \"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value("130/85"))
                .andExpect(jsonPath("$.observacao").value(nullValue()));

        enviar(mvc, "GET", resultados(TRIAGEM_P1), coordenador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrados").value(1))
                .andExpect(jsonPath("$.pendentes").value(0))
                .andExpect(jsonPath("$.atividades[0].resultado.valor").value("130/85"));
    }

    @Test
    @DisplayName("401 sem token; 403 para o coordenador na escrita e para administrador e participante em qualquer operação")
    void autorizacaoPorPerfil() throws Exception {
        enviar(mvc, "GET", resultados(TRIAGEM_P1), null, null).andExpect(status().isUnauthorized());
        enviar(mvc, "PUT", resultado(TRIAGEM_P1, AFERIR_PRESSAO), null, CORPO).andExpect(status().isUnauthorized());

        // O coordenador não escreve: 403 (não 409), antes de qualquer regra
        enviar(mvc, "PUT", resultado(TRIAGEM_P1, AFERIR_PRESSAO), coordenador, CORPO)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));

        for (String email : new String[] {TokenDeTeste.ADMIN, TokenDeTeste.PARTICIPANTE}) {
            String token = TokenDeTeste.bearer(mvc, email);
            enviar(mvc, "GET", resultados(TRIAGEM_P1), token, null).andExpect(status().isForbidden());
            enviar(mvc, "PUT", resultado(TRIAGEM_P1, AFERIR_PRESSAO), token, CORPO).andExpect(status().isForbidden());
        }
    }

    @Test
    @DisplayName("404 para visita agendada ou atividade inexistente")
    void naoEncontrado() throws Exception {
        enviar(mvc, "GET", resultados(INEXISTENTE), pesquisador, null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"));
        enviar(mvc, "PUT", resultado(INEXISTENTE, AFERIR_PRESSAO), pesquisador, CORPO)
                .andExpect(status().isNotFound());
        enviar(mvc, "PUT", resultado(TRIAGEM_P1, INEXISTENTE), pesquisador, CORPO)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"));
    }

    @Test
    @DisplayName("400: VALOR_OBRIGATORIO (em branco ou ausente), VALIDACAO_CAMPOS (tamanho) e CORPO_INVALIDO")
    void dadosInvalidos() throws Exception {
        enviar(mvc, "PUT", resultado(TRIAGEM_P1, AFERIR_PRESSAO), pesquisador, "{\"valor\":\"   \"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALOR_OBRIGATORIO"));
        enviar(mvc, "PUT", resultado(TRIAGEM_P1, AFERIR_PRESSAO), pesquisador, "{\"unidade\":\"mmHg\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALOR_OBRIGATORIO"));
        enviar(mvc, "PUT", resultado(TRIAGEM_P1, AFERIR_PRESSAO), pesquisador,
                "{\"valor\":\"120/80\",\"unidade\":\"" + "x".repeat(31) + "\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO_CAMPOS"))
                .andExpect(jsonPath("$.errosCampo[0].campo").value("unidade"));
        enviar(mvc, "PUT", resultado(TRIAGEM_P1, AFERIR_PRESSAO), pesquisador, "{\"valor\":")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("CORPO_INVALIDO"));
    }

    @Test
    @DisplayName("409: visita não realizada, atividade fora da categoria e atividade de outra visita")
    void regrasDeNegocio() throws Exception {
        enviar(mvc, "PUT", resultado(RETORNO_30_P1, EXAME_FISICO_DO_RETORNO_30), pesquisador, CORPO)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("VISITA_NAO_REALIZADA"));
        enviar(mvc, "PUT", resultado(TRIAGEM_P1, COLETAR_SANGUE), pesquisador, CORPO)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ATIVIDADE_FORA_DA_CATEGORIA_AVALIACAO"));
        enviar(mvc, "PUT", resultado(TRIAGEM_P1, EXAME_FISICO_DO_RETORNO_30), pesquisador, CORPO)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ATIVIDADE_NAO_PERTENCE_A_VISITA"));
    }

    @Test
    @DisplayName("409 ESTUDO_ENCERRADO na escrita; a consulta continua 200, com bloqueio ESTUDO_ENCERRADO")
    void estudoEncerrado() throws Exception {
        // Encerrado antes de qualquer requisição, para o estudo não estar ainda no contexto de persistência do teste
        jdbc.update("UPDATE estudos SET status = 'ENCERRADO' WHERE id = CAST(? AS UUID)", ESTUDO_DEMO);
        enviar(mvc, "PUT", resultado(TRIAGEM_P1, AFERIR_PRESSAO), pesquisador, CORPO)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESTUDO_ENCERRADO"));
        enviar(mvc, "GET", resultados(TRIAGEM_P1), coordenador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bloqueio").value("ESTUDO_ENCERRADO"));
    }
}
