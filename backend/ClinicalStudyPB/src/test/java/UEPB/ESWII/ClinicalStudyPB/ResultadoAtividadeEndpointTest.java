package UEPB.ESWII.ClinicalStudyPB;

import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.acaoDeAvaliacao;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.agendarVisita;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.avancar;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.criarAcao;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.criarCriterio;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.criarEstudo;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.criarVisita;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.enviar;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.incluirParticipante;
import static UEPB.ESWII.ClinicalStudyPB.support.CenarioRecrutamento.visitaRealizada;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.PerfilUsuario;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.repository.UsuarioRepository;
import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;

/**
 * T90 (RF14): teste de fumaça dos endpoints dos resultados com o banco e tokens reais, sobre os dados de demonstração
 * do estudo "Demo - Acompanhamento de participantes" (Triagem do P1 REALIZADA; Retorno 30 dias do P1 AGENDADO).
 * Cobre a autorização (só o pesquisador escreve; administrador e participante não acessam), 201 no primeiro registro
 * e 200 na edição, e um caso de cada código de erro. Os scripts rodam dentro da transação do teste, que é desfeita no
 * fim. Somente dados fictícios.
 *
 * <p>T92: a segunda metade monta um estudo próprio só pela API ({@link #cenario()}), com três avaliações e atividades
 * de outras categorias na Triagem, uma visita REALIZADA hoje e outra AGENDADA. Prova as contagens a cada passo, que
 * outras categorias não aparecem, a edição por outro pesquisador sem trocar o autor do registro, os limites do corpo,
 * cada 409 (inclusive estudo encerrado pela transição e participante retirado) e o 403 do coordenador antes das
 * regras. As datas vêm do {@code Clock} da aplicação, nunca do dia fixo. No CI roda também no job PostgreSQL.</p>
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
    private static final String OUTRO_PESQUISADOR = "Outro Pesquisador T92 (Ficticio)";
    private static final String SENHA_OUTRO_PESQUISADOR = "senha-outro-pesquisador-t92";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private UsuarioRepository usuarios;
    @Autowired
    private Clock relogio;

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

    // =====================================================================================================
    // T92: cenário montado só pela API, com três avaliações na Triagem e atividades de outras categorias
    // =====================================================================================================

    /**
     * Ids (texto) do cenário da T92: a Triagem (dia 0) tem três avaliações e três atividades de outras categorias; o
     * Retorno 30 dias tem uma avaliação. O participante foi incluído hoje, a Triagem está REALIZADA e o Retorno,
     * AGENDADO para daqui a 30 dias.
     */
    private record Cenario(String estudo, String pressao, String dor, String exame, String coleta, String questionario,
            String medicacao, String exameDoRetorno, String participacao, String realizada, String agendada) {
    }

    private Cenario cenario() throws Exception {
        LocalDate hoje = LocalDate.now(relogio);
        String estudo = criarEstudo(mvc, pesquisador, 10);
        criarCriterio(mvc, pesquisador, estudo, "INCLUSAO", "Idade adulta", "Maior de 18 anos");
        criarCriterio(mvc, pesquisador, estudo, "EXCLUSAO", "Gestante", "Em gestação");
        String triagem = criarVisita(mvc, pesquisador, estudo, "Triagem", 0);
        String retorno = criarVisita(mvc, pesquisador, estudo, "Retorno 30 dias", 30);
        String pressao = acaoDeAvaliacao(mvc, pesquisador, triagem, "Aferir pressão arterial");
        String dor = acaoDeAvaliacao(mvc, pesquisador, triagem, "Escala de dor");
        String exame = acaoDeAvaliacao(mvc, pesquisador, triagem, "Exame físico");
        String coleta = criarAcao(mvc, pesquisador, triagem, "Coletar sangue", "EXAME_LABORATORIAL");
        String questionario = criarAcao(mvc, pesquisador, triagem, "Questionário de qualidade de vida", "QUESTIONARIO");
        String medicacao = criarAcao(mvc, pesquisador, triagem, "Dispensar medicação", "MEDICACAO");
        String exameDoRetorno = acaoDeAvaliacao(mvc, pesquisador, retorno, "Exame físico do retorno");
        avancar(mvc, pesquisador, estudo, "RECRUTAMENTO").andExpect(status().isOk());

        String participacao = incluirParticipante(mvc, coordenador, estudo, "Participante T92 (Ficticio)");
        String realizada = visitaRealizada(mvc, coordenador, participacao, triagem, hoje);
        String agendada = agendarVisita(mvc, coordenador, participacao, retorno, hoje.plusDays(30));
        return new Cenario(estudo, pressao, dor, exame, coleta, questionario, medicacao, exameDoRetorno, participacao,
                realizada, agendada);
    }

    /** Segundo pesquisador, criado no teste, para provar que a edição não troca o autor do primeiro registro. */
    private String outroPesquisador() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNome(OUTRO_PESQUISADOR);
        usuario.setEmail("outro.pesquisador.t92@clinicalstudy.test");
        usuario.setSenha(new BCryptPasswordEncoder().encode(SENHA_OUTRO_PESQUISADOR));
        usuario.setPerfil(PerfilUsuario.PESQUISADOR);
        usuarios.saveAndFlush(usuario);
        return TokenDeTeste.bearer(mvc, usuario.getEmail(), SENHA_OUTRO_PESQUISADOR);
    }

    private void contagens(String visita, int total, int registrados, int pendentes) throws Exception {
        enviar(mvc, "GET", resultados(visita), coordenador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(total))
                .andExpect(jsonPath("$.registrados").value(registrados))
                .andExpect(jsonPath("$.pendentes").value(pendentes));
    }

    @Test
    @DisplayName("RQ-T92-11/06/12: GET lista só as avaliações, e total, registrados e pendentes acompanham cada registro")
    void listaSoAvaliacoesEContaPassoAPasso() throws Exception {
        Cenario c = cenario();

        for (String token : new String[] {pesquisador, coordenador}) {
            enviar(mvc, "GET", resultados(c.realizada()), token, null)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.visitaAgendadaId").value(c.realizada()))
                    .andExpect(jsonPath("$.situacaoVisita").value("REALIZADA"))
                    .andExpect(jsonPath("$.bloqueio").value(nullValue()))
                    .andExpect(jsonPath("$.total").value(3))
                    .andExpect(jsonPath("$.registrados").value(0))
                    .andExpect(jsonPath("$.pendentes").value(3))
                    // Ordenadas pelo nome; coleta, questionário e medicação não aparecem
                    .andExpect(jsonPath("$.atividades[*].acaoId").value(contains(c.pressao(), c.dor(), c.exame())))
                    .andExpect(jsonPath("$.atividades[*].nome")
                            .value(contains("Aferir pressão arterial", "Escala de dor", "Exame físico")))
                    .andExpect(jsonPath("$.atividades[*].resultado").value(everyItem(nullValue())));
        }

        enviar(mvc, "PUT", resultado(c.realizada(), c.dor()), pesquisador, "{\"valor\":\"3\",\"unidade\":\"pontos\"}")
                .andExpect(status().isCreated());
        contagens(c.realizada(), 3, 1, 2);

        enviar(mvc, "PUT", resultado(c.realizada(), c.pressao()), pesquisador, CORPO).andExpect(status().isCreated());
        contagens(c.realizada(), 3, 2, 1);

        // Editar não altera as contagens
        enviar(mvc, "PUT", resultado(c.realizada(), c.pressao()), pesquisador, "{\"valor\":\"130/85\"}")
                .andExpect(status().isOk());
        contagens(c.realizada(), 3, 2, 1);

        enviar(mvc, "PUT", resultado(c.realizada(), c.exame()), pesquisador, "{\"valor\":\"Normal\"}")
                .andExpect(status().isCreated());
        enviar(mvc, "GET", resultados(c.realizada()), pesquisador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.registrados").value(3))
                .andExpect(jsonPath("$.pendentes").value(0))
                .andExpect(jsonPath("$.bloqueio").value(nullValue()))
                .andExpect(jsonPath("$.atividades[0].resultado.valor").value("130/85"))
                .andExpect(jsonPath("$.atividades[1].resultado.valor").value("3"))
                .andExpect(jsonPath("$.atividades[1].resultado.unidade").value("pontos"))
                .andExpect(jsonPath("$.atividades[2].resultado.valor").value("Normal"));
    }

    @Test
    @DisplayName("RQ-T92-10/07: 201 com o corpo completo; a edição por outro pesquisador responde 200 e preserva o autor do registro")
    void edicaoPreservaOAutorDoPrimeiroRegistro() throws Exception {
        Cenario c = cenario();
        String url = resultado(c.realizada(), c.pressao());

        String primeiro = enviar(mvc, "PUT", url, pesquisador, CORPO)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.acaoId").value(c.pressao()))
                .andExpect(jsonPath("$.valor").value("120/80"))
                .andExpect(jsonPath("$.unidade").value("mmHg"))
                .andExpect(jsonPath("$.observacao").value("Sentado"))
                .andExpect(jsonPath("$.registradoPor").isNotEmpty())
                .andExpect(jsonPath("$.registradoEm").value(matchesPattern(ISO_COM_FUSO)))
                .andExpect(jsonPath("$.alteradoEm").value(matchesPattern(ISO_COM_FUSO)))
                .andReturn().getResponse().getContentAsString();
        String autor = JsonPath.read(primeiro, "$.registradoPor");
        String registradoEm = JsonPath.read(primeiro, "$.registradoEm");
        // No primeiro registro os dois pares são iguais
        assertThat((String) JsonPath.read(primeiro, "$.alteradoPor")).isEqualTo(autor);
        assertThat((String) JsonPath.read(primeiro, "$.alteradoEm")).isEqualTo(registradoEm);
        assertThat(autor).isNotEqualTo(OUTRO_PESQUISADOR);

        enviar(mvc, "PUT", url, outroPesquisador(),
                "{\"valor\":\"13/8\",\"unidade\":\"cmHg\",\"observacao\":\"Deitado, após repouso\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acaoId").value(c.pressao()))
                .andExpect(jsonPath("$.valor").value("13/8"))
                .andExpect(jsonPath("$.unidade").value("cmHg"))
                .andExpect(jsonPath("$.observacao").value("Deitado, após repouso"))
                .andExpect(jsonPath("$.registradoPor").value(autor))
                .andExpect(jsonPath("$.registradoEm").value(registradoEm))
                .andExpect(jsonPath("$.alteradoPor").value(OUTRO_PESQUISADOR))
                .andExpect(jsonPath("$.alteradoEm").value(matchesPattern(ISO_COM_FUSO)));

        enviar(mvc, "GET", resultados(c.realizada()), coordenador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.atividades[0].resultado.valor").value("13/8"))
                .andExpect(jsonPath("$.atividades[0].resultado.registradoPor").value(autor))
                .andExpect(jsonPath("$.atividades[0].resultado.registradoEm").value(registradoEm))
                .andExpect(jsonPath("$.atividades[0].resultado.alteradoPor").value(OUTRO_PESQUISADOR));
    }

    @Test
    @DisplayName("RQ-T92-10/09: 400 com 201 caracteres no valor, 501 na observação, valor ausente e JSON malformado; 200 caracteres passam")
    void limitesDoCorpoPelaApi() throws Exception {
        Cenario c = cenario();
        String url = resultado(c.realizada(), c.pressao());

        enviar(mvc, "PUT", url, pesquisador, "{\"valor\":\"" + "x".repeat(201) + "\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO_CAMPOS"))
                .andExpect(jsonPath("$.errosCampo[0].campo").value("valor"));
        enviar(mvc, "PUT", url, pesquisador, "{\"valor\":\"1\",\"observacao\":\"" + "x".repeat(501) + "\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO_CAMPOS"))
                .andExpect(jsonPath("$.errosCampo[0].campo").value("observacao"));
        enviar(mvc, "PUT", url, pesquisador, "{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALOR_OBRIGATORIO"));
        enviar(mvc, "PUT", url, pesquisador, "{\"valor\":\"120/80\",")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("CORPO_INVALIDO"));
        contagens(c.realizada(), 3, 0, 3);

        String duzentos = "9".repeat(200);
        enviar(mvc, "PUT", url, pesquisador,
                "{\"valor\":\"" + duzentos + "\",\"unidade\":\"" + "u".repeat(30) + "\",\"observacao\":\""
                        + "o".repeat(500) + "\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor").value(duzentos));
    }

    @Test
    @DisplayName("RQ-T92-10: 409 de cada regra com o cenário próprio; o coordenador recebe 403, nunca 409")
    void conflitosDeCadaRegra() throws Exception {
        Cenario c = cenario();

        // Visita AGENDADA (Retorno 30 dias), com uma atividade de avaliação dela mesma
        enviar(mvc, "PUT", resultado(c.agendada(), c.exameDoRetorno()), pesquisador, CORPO)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("VISITA_NAO_REALIZADA"));
        // Atividades de outras categorias da própria Triagem
        for (String atividade : new String[] {c.coleta(), c.questionario(), c.medicacao()}) {
            enviar(mvc, "PUT", resultado(c.realizada(), atividade), pesquisador, CORPO)
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.codigo").value("ATIVIDADE_FORA_DA_CATEGORIA_AVALIACAO"));
        }
        // Avaliação de outra visita prevista do mesmo estudo
        enviar(mvc, "PUT", resultado(c.realizada(), c.exameDoRetorno()), pesquisador, CORPO)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ATIVIDADE_NAO_PERTENCE_A_VISITA"));
        // Atividade inexistente na visita do cenário
        enviar(mvc, "PUT", resultado(c.realizada(), INEXISTENTE), pesquisador, CORPO)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"));

        // A permissão vem antes das regras: o coordenador recebe 403 mesmo onde o pesquisador receberia 409
        enviar(mvc, "PUT", resultado(c.agendada(), c.exameDoRetorno()), coordenador, CORPO)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));
        enviar(mvc, "PUT", resultado(c.realizada(), c.coleta()), coordenador, CORPO)
                .andExpect(status().isForbidden());

        // Nenhuma tentativa recusada gravou resultado
        contagens(c.realizada(), 3, 0, 3);
    }

    @Test
    @DisplayName("RQ-T92-12: visita AGENDADA devolve as avaliações sem resultado e bloqueio VISITA_NAO_REALIZADA")
    void visitaAgendadaDevolveAtividadesSemResultado() throws Exception {
        Cenario c = cenario();

        enviar(mvc, "GET", resultados(c.agendada()), pesquisador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacaoVisita").value("AGENDADA"))
                .andExpect(jsonPath("$.bloqueio").value("VISITA_NAO_REALIZADA"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.registrados").value(0))
                .andExpect(jsonPath("$.pendentes").value(1))
                .andExpect(jsonPath("$.atividades", hasSize(1)))
                .andExpect(jsonPath("$.atividades[0].acaoId").value(c.exameDoRetorno()))
                .andExpect(jsonPath("$.atividades[0].nome").value("Exame físico do retorno"))
                .andExpect(jsonPath("$.atividades[0].resultado").value(nullValue()));
    }

    @Test
    @DisplayName("RQ-T92-03: recrutamento e execução aceitam; com o estudo encerrado pela transição, 409 no primeiro registro e na edição")
    void estudoEncerradoPelaTransicao() throws Exception {
        Cenario c = cenario();

        // Recrutamento aceita
        enviar(mvc, "PUT", resultado(c.realizada(), c.pressao()), pesquisador, CORPO).andExpect(status().isCreated());
        // Execução aceita
        avancar(mvc, pesquisador, c.estudo(), "EXECUCAO").andExpect(status().isOk());
        enviar(mvc, "PUT", resultado(c.realizada(), c.dor()), pesquisador, "{\"valor\":\"2\"}")
                .andExpect(status().isCreated());

        avancar(mvc, pesquisador, c.estudo(), "ENCERRADO").andExpect(status().isOk());
        // Edição de um resultado existente
        enviar(mvc, "PUT", resultado(c.realizada(), c.pressao()), pesquisador, "{\"valor\":\"130/85\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESTUDO_ENCERRADO"));
        // Primeiro registro de uma avaliação ainda pendente
        enviar(mvc, "PUT", resultado(c.realizada(), c.exame()), pesquisador, "{\"valor\":\"Normal\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESTUDO_ENCERRADO"));

        // A leitura continua: os resultados ficam visíveis, sem a edição recusada
        enviar(mvc, "GET", resultados(c.realizada()), coordenador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bloqueio").value("ESTUDO_ENCERRADO"))
                .andExpect(jsonPath("$.registrados").value(2))
                .andExpect(jsonPath("$.pendentes").value(1))
                .andExpect(jsonPath("$.atividades[0].resultado.valor").value("120/80"));
    }

    @Test
    @DisplayName("RQ-T92-08 (T102): participante retirado pela API: 409 PARTICIPANTE_RETIRADO e consulta com o bloqueio")
    void participanteRetiradoPelaApi() throws Exception {
        Cenario c = cenario();
        enviar(mvc, "PUT", resultado(c.realizada(), c.pressao()), pesquisador, CORPO).andExpect(status().isCreated());

        enviar(mvc, "POST", "/api/participacoes/" + c.participacao() + "/retirada", coordenador,
                "{\"motivo\":\"Mudança de cidade\",\"solicitante\":\"PARTICIPANTE\",\"dataRetirada\":\""
                        + LocalDate.now(relogio) + "\"}")
                .andExpect(status().isCreated());

        enviar(mvc, "PUT", resultado(c.realizada(), c.pressao()), pesquisador, "{\"valor\":\"130/85\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("PARTICIPANTE_RETIRADO"));
        enviar(mvc, "PUT", resultado(c.realizada(), c.dor()), pesquisador, "{\"valor\":\"3\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("PARTICIPANTE_RETIRADO"));
        enviar(mvc, "GET", resultados(c.realizada()), coordenador, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bloqueio").value("PARTICIPANTE_RETIRADO"))
                .andExpect(jsonPath("$.registrados").value(1))
                .andExpect(jsonPath("$.atividades[0].resultado.valor").value("120/80"));
    }
}
