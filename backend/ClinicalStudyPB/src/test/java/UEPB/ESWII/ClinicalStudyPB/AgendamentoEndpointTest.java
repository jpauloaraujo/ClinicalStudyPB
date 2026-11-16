package UEPB.ESWII.ClinicalStudyPB;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AgendamentoEndpointTest {

    private static final String ESTUDO = "d6300000-0000-4000-8000-000000001901";
    private static final String PARTICIPACAO_P1 = "d6300000-0000-4000-8000-000000001921";
    private static final String PARTICIPACAO_P3 = "d6300000-0000-4000-8000-000000001923";
    private static final String TRIAGEM = "d6300000-0000-4000-8000-000000001931";
    private static final String RETORNO_30_P1 = "d6300000-0000-4000-8000-000000001982";
    private static final String INEXISTENTE = "d6300000-0000-4000-8000-000000009999";
    private static final String ISO_COM_FUSO =
            "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2}(\\.\\d+)?)?[+-]\\d{2}:\\d{2}";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private Clock clock;

    private String coordenador;
    private String pesquisador;
    private String administrador;

    @BeforeEach
    void carregarDadosDeDemonstracao() throws Exception {
        Connection conexao = DataSourceUtils.getConnection(dataSource);
        ScriptUtils.executeSqlScript(conexao, new ClassPathResource("db/demo/R__dados_demonstracao.sql"));
        ScriptUtils.executeSqlScript(conexao, new ClassPathResource("db/demo/R__demo_19_visitas_agendadas.sql"));
        coordenador = TokenDeTeste.bearer(mvc, TokenDeTeste.COORDENADOR);
        pesquisador = TokenDeTeste.bearer(mvc, TokenDeTeste.PESQUISADOR);
        administrador = TokenDeTeste.bearer(mvc, TokenDeTeste.ADMIN);
    }

    @Test
    @DisplayName("Coordenador e pesquisador consultam visitas do participante e progresso da agenda")
    void consultasDeVisitasEProgresso() throws Exception {
        for (String token : new String[] {coordenador, pesquisador}) {
            mvc.perform(get("/api/participacoes/{id}/visitas", PARTICIPACAO_P1).header("Authorization", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[0].nome").value("Triagem"))
                    .andExpect(jsonPath("$[0].agendamento.situacao").value("REALIZADA"))
                    .andExpect(jsonPath("$[2].agendamento").value(nullValue()));
        }

        mvc.perform(get("/api/estudos/{id}/agenda/participantes", ESTUDO)
                        .header("Authorization", coordenador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].codigo").value("CAND-001"))
                .andExpect(jsonPath("$[0].visitasPrevistas").value(3))
                .andExpect(jsonPath("$[0].visitasAgendadas").value(2))
                .andExpect(jsonPath("$[2].visitasAgendadas").value(0));
    }

    @Test
    @DisplayName("Coordenador agenda (201 com Location) e reagenda (200) uma visita")
    void criaEAtualizaAgendamento() throws Exception {
        String hoje = LocalDate.now(clock).toString();
        String resposta = mvc.perform(post("/api/participacoes/{id}/visitas-agendadas", PARTICIPACAO_P3)
                        .header("Authorization", coordenador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"visitaPlanejadaId\":\"" + TRIAGEM + "\",\"dataHora\":\"" + hoje + "T10:00\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/visitas-agendadas/")))
                .andExpect(jsonPath("$.situacao").value("AGENDADA"))
                .andExpect(jsonPath("$.participacaoId").value(PARTICIPACAO_P3))
                .andExpect(jsonPath("$.dataHora").value(matchesPattern(ISO_COM_FUSO)))
                .andReturn().getResponse().getContentAsString();
        String id = com.jayway.jsonpath.JsonPath.read(resposta, "$.id");

        mvc.perform(put("/api/visitas-agendadas/{id}", id)
                        .header("Authorization", coordenador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataHora\":\"" + hoje + "T11:00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.dataHora").value(matchesPattern(ISO_COM_FUSO)));

        mvc.perform(put("/api/visitas-agendadas/{id}", RETORNO_30_P1)
                        .header("Authorization", coordenador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataHora\":\"" + LocalDate.now(clock).plusDays(11) + "T10:00\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Agenda filtra datas e situação e respeita página e tamanho")
    void filtraEPaginaAgenda() throws Exception {
        // A visita de demonstração é criada com current_date do banco, que pode estar em outro fuso
        // (ex.: CI em UTC) e cair num dia diferente do relógio da aplicação; por isso lê a data do próprio dado.
        String hoje = new JdbcTemplate(dataSource).queryForObject(
                "select cast(cast(data_hora_agendada as date) as varchar) from visitas_agendadas "
                        + "where id = cast('d6300000-0000-4000-8000-000000001983' as uuid)", String.class);
        mvc.perform(get("/api/estudos/{id}/agenda", ESTUDO)
                        .header("Authorization", pesquisador)
                        .param("de", hoje)
                        .param("ate", hoje)
                        .param("situacao", "AGENDADA")
                        .param("pagina", "0")
                        .param("tamanho", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo", hasSize(1)))
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanho").value(1))
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.totalPaginas").value(1))
                .andExpect(jsonPath("$.conteudo[0].situacao").value("AGENDADA"));

        // Sem nenhum filtro (de, ate e situacao ausentes): todas as visitas do estudo, sem parâmetro nulo no SQL
        mvc.perform(get("/api/estudos/{id}/agenda", ESTUDO).header("Authorization", pesquisador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(3));
    }

    @Test
    @DisplayName("Permissões e recursos inexistentes retornam 401, 403 e 404")
    void autorizacaoERecursosAusentes() throws Exception {
        mvc.perform(get("/api/estudos/{id}/agenda", ESTUDO))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/estudos/{id}/agenda", ESTUDO).header("Authorization", administrador))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));
        mvc.perform(post("/api/participacoes/{id}/visitas-agendadas", PARTICIPACAO_P3)
                        .header("Authorization", pesquisador)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"visitaPlanejadaId\":\"" + TRIAGEM + "\",\"dataHora\":\""
                                + LocalDate.now(clock) + "T10:00\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/estudos/{id}/agenda", INEXISTENTE).header("Authorization", coordenador))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"));
        mvc.perform(get("/api/participacoes/{id}/visitas", INEXISTENTE).header("Authorization", coordenador))
                .andExpect(status().isNotFound());
    }
}
