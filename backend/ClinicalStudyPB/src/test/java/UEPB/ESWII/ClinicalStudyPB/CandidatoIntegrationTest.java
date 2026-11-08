package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoCandidato;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.repository.CandidatoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.EstudoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.UsuarioRepository;
import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;

/** T53: contrato HTTP do RF08 com token real e persistência, isolado das telas e da elegibilidade. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CandidatoIntegrationTest {

    private static final String NASCIMENTO = "1990-04-12";
    @Autowired private MockMvc mockMvc;
    @Autowired private EstudoRepository estudoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private CandidatoRepository candidatoRepository;
    private Estudo estudo;
    private String coordenador;

    @BeforeEach
    void preparar() throws Exception {
        // Fixture persistida diretamente: o fluxo de transição é verificado pelos testes da T42.
        estudo = novoEstudo(StatusEstudo.RECRUTAMENTO);
        coordenador = TokenDeTeste.bearer(mockMvc, TokenDeTeste.COORDENADOR);
    }

    @Test
    void cadastraConsultaEEditaCandidatoComCodigoGerado() throws Exception {
        var criado = mockMvc.perform(post(url(estudo)).header("Authorization", coordenador)
                        .contentType(MediaType.APPLICATION_JSON).content(dados("Fictício A", NASCIMENTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.codigo").value("CAND-001"))
                .andExpect(jsonPath("$.situacao").value("CADASTRADO"))
                .andExpect(jsonPath("$.consentimentoRegistrado").value(false))
                .andReturn().getResponse();
        String id = JsonPath.read(criado.getContentAsString(), "$.id");
        assertThat(criado.getHeader("Location")).endsWith("/api/candidatos/" + id);

        mockMvc.perform(get("/api/candidatos/" + id).header("Authorization", coordenador))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nomeFicticio").value("Fictício A"));
        mockMvc.perform(put("/api/candidatos/" + id).header("Authorization", coordenador)
                        .contentType(MediaType.APPLICATION_JSON).content(dados("Fictício B", NASCIMENTO)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nomeFicticio").value("Fictício B"))
                .andExpect(jsonPath("$.codigo").value("CAND-001"));
    }

    @Test
    void codigosSaoSequenciaisNoEstudoERecomecamEmOutro() throws Exception {
        String primeiro = cadastrar(estudo, "Fictício A");
        String segundo = cadastrar(estudo, "Fictício B");
        Estudo outro = novoEstudo(StatusEstudo.RECRUTAMENTO);
        String emOutro = cadastrar(outro, "Fictício A");

        assertThat(candidatoRepository.findById(UUID.fromString(primeiro)).orElseThrow().getCodigo()).isEqualTo("CAND-001");
        assertThat(candidatoRepository.findById(UUID.fromString(segundo)).orElseThrow().getCodigo()).isEqualTo("CAND-002");
        assertThat(candidatoRepository.findById(UUID.fromString(emOutro)).orElseThrow().getCodigo()).isEqualTo("CAND-001");
        mockMvc.perform(get(url(estudo)).header("Authorization", coordenador))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElementos").value(2));
        mockMvc.perform(get(url(outro)).header("Authorization", coordenador))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElementos").value(1));
    }

    @Test
    void naoPermiteDuplicidadeNoCadastroOuNaEdicao() throws Exception {
        cadastrar(estudo, "Fictício A");
        String segundo = cadastrar(estudo, "Fictício B");
        mockMvc.perform(post(url(estudo)).header("Authorization", coordenador)
                        .contentType(MediaType.APPLICATION_JSON).content(dados("FICTÍCIO A", NASCIMENTO)))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CANDIDATO_DUPLICADO"));
        mockMvc.perform(put("/api/candidatos/" + segundo).header("Authorization", coordenador)
                        .contentType(MediaType.APPLICATION_JSON).content(dados("Fictício A", NASCIMENTO)))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CANDIDATO_DUPLICADO"));
        assertThat(candidatoRepository.countByEstudoId(estudo.getId())).isEqualTo(2);
        assertThat(candidatoRepository.findById(UUID.fromString(segundo)).orElseThrow().getNomeFicticio()).isEqualTo("Fictício B");
    }

    @Test
    void nascimentoHojeOuFuturoENomeAusenteRetornam400() throws Exception {
        for (String data : new String[] {LocalDate.now().toString(), LocalDate.now().plusDays(1).toString()}) {
            mockMvc.perform(post(url(estudo)).header("Authorization", coordenador)
                            .contentType(MediaType.APPLICATION_JSON).content(dados("Fictício A", data)))
                    .andExpect(status().isBadRequest());
        }
        mockMvc.perform(post(url(estudo)).header("Authorization", coordenador)
                        .contentType(MediaType.APPLICATION_JSON).content(dados("", NASCIMENTO)))
                .andExpect(status().isBadRequest());
        assertThat(candidatoRepository.countByEstudoId(estudo.getId())).isZero();
    }

    @ParameterizedTest
    @EnumSource(value = StatusEstudo.class, names = {"PLANEJAMENTO", "EXECUCAO", "ENCERRADO"})
    void estudoForaDeRecrutamentoRetorna409(StatusEstudo estado) throws Exception {
        estudo.setStatus(estado);
        estudoRepository.saveAndFlush(estudo);
        mockMvc.perform(post(url(estudo)).header("Authorization", coordenador)
                        .contentType(MediaType.APPLICATION_JSON).content(dados("Fictício A", NASCIMENTO)))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("ESTUDO_FORA_DE_RECRUTAMENTO"));
        assertThat(candidatoRepository.countByEstudoId(estudo.getId())).isZero();
    }

    @Test
    void edicaoAposInclusaoRetorna409EPreservaDados() throws Exception {
        String id = cadastrar(estudo, "Fictício A");
        var candidato = candidatoRepository.findById(UUID.fromString(id)).orElseThrow();
        candidato.setSituacao(SituacaoCandidato.INCLUIDO);
        candidatoRepository.saveAndFlush(candidato);

        mockMvc.perform(put("/api/candidatos/" + id).header("Authorization", coordenador)
                        .contentType(MediaType.APPLICATION_JSON).content(dados("Outro pseudônimo", NASCIMENTO)))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("CANDIDATO_JA_INCLUIDO"));
        assertThat(candidato.getNomeFicticio()).isEqualTo("Fictício A");
    }

    @ParameterizedTest
    @ValueSource(strings = {TokenDeTeste.PESQUISADOR, TokenDeTeste.PARTICIPANTE, TokenDeTeste.ADMIN})
    void perfilSemPermissaoNaoCadastraNemEdita(String email) throws Exception {
        String id = cadastrar(estudo, "Fictício A");
        String token = TokenDeTeste.bearer(mockMvc, email);
        mockMvc.perform(post(url(estudo)).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(dados("Fictício B", NASCIMENTO)))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/candidatos/" + id).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(dados("Fictício B", NASCIMENTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void pesquisadorPodeLerMasSemTokenRecebe401() throws Exception {
        String id = cadastrar(estudo, "Fictício A");
        String pesquisador = TokenDeTeste.bearer(mockMvc, TokenDeTeste.PESQUISADOR);
        mockMvc.perform(get(url(estudo)).header("Authorization", pesquisador)).andExpect(status().isOk());
        mockMvc.perform(get("/api/candidatos/" + id).header("Authorization", pesquisador)).andExpect(status().isOk());
        mockMvc.perform(get(url(estudo))).andExpect(status().isUnauthorized());
        mockMvc.perform(post(url(estudo)).contentType(MediaType.APPLICATION_JSON).content(dados("Fictício B", NASCIMENTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void recursosInexistentesRetornam404() throws Exception {
        String desconhecido = UUID.randomUUID().toString();
        mockMvc.perform(post("/api/estudos/" + desconhecido + "/candidatos").header("Authorization", coordenador)
                        .contentType(MediaType.APPLICATION_JSON).content(dados("Fictício A", NASCIMENTO)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/estudos/" + desconhecido + "/candidatos").header("Authorization", coordenador))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/candidatos/" + desconhecido).header("Authorization", coordenador))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/candidatos/" + desconhecido).header("Authorization", coordenador)
                        .contentType(MediaType.APPLICATION_JSON).content(dados("Fictício A", NASCIMENTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void paginaEFiltraSemMisturarCandidatosDeOutroEstudo() throws Exception {
        cadastrar(estudo, "Fictício A");
        String segundo = cadastrar(estudo, "Fictício B");
        cadastrar(estudo, "Fictício C");
        cadastrar(novoEstudo(StatusEstudo.RECRUTAMENTO), "Fictício externo");
        var candidato = candidatoRepository.findById(UUID.fromString(segundo)).orElseThrow();
        candidato.setSituacao(SituacaoCandidato.NAO_ELEGIVEL);
        candidatoRepository.saveAndFlush(candidato);

        mockMvc.perform(get(url(estudo) + "?pagina=1&tamanho=1").header("Authorization", coordenador))
                .andExpect(status().isOk()).andExpect(jsonPath("$.pagina").value(1))
                .andExpect(jsonPath("$.tamanho").value(1)).andExpect(jsonPath("$.totalElementos").value(3))
                .andExpect(jsonPath("$.totalPaginas").value(3)).andExpect(jsonPath("$.conteudo.length()").value(1))
                .andExpect(jsonPath("$.conteudo[0].codigo").value("CAND-002"));
        mockMvc.perform(get(url(estudo) + "?situacao=NAO_ELEGIVEL").header("Authorization", coordenador))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.conteudo[0].id").value(segundo));
        mockMvc.perform(get(url(estudo) + "?situacao=ELEGIVEL").header("Authorization", coordenador))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElementos").value(0))
                .andExpect(jsonPath("$.conteudo.length()").value(0));
        mockMvc.perform(get(url(estudo) + "?situacao=INVALIDA").header("Authorization", coordenador))
                .andExpect(status().isBadRequest());
    }

    private Estudo novoEstudo(StatusEstudo status) {
        Estudo novo = new Estudo();
        novo.setTitulo("Estudo fictício T53");
        novo.setObjetivo("Verificação do cadastro de candidatos");
        novo.setResponsavel(usuarioRepository.findByEmail(TokenDeTeste.PESQUISADOR).orElseThrow());
        novo.setLimiteParticipantes(10);
        novo.setStatus(status);
        return estudoRepository.saveAndFlush(novo);
    }

    private String cadastrar(Estudo estudoAlvo, String nome) throws Exception {
        String json = mockMvc.perform(post(url(estudoAlvo)).header("Authorization", coordenador)
                        .contentType(MediaType.APPLICATION_JSON).content(dados(nome, NASCIMENTO)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.id");
    }

    private static String url(Estudo estudoAlvo) {
        return "/api/estudos/" + estudoAlvo.getId() + "/candidatos";
    }

    private static String dados(String nome, String nascimento) {
        return "{\"nomeFicticio\":\"" + nome + "\",\"dataNascimento\":\"" + nascimento + "\"}";
    }
}
