package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.jayway.jsonpath.JsonPath;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoCandidato;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.TipoCriterio;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.CategoriaAcao;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.CriterioRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.AcaoRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.VisitaRequest;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Candidato;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.repository.AcaoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.CandidatoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.ConsentimentoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.EstudoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.UsuarioRepository;
import UEPB.ESWII.ClinicalStudyPB.support.TokenDeTeste;
import UEPB.ESWII.ClinicalStudyPB.service.CriterioService;
import UEPB.ESWII.ClinicalStudyPB.service.VisitaService;
import UEPB.ESWII.ClinicalStudyPB.service.EstudoTransicaoService;
import UEPB.ESWII.ClinicalStudyPB.service.ConflitoRegraException;

/** Verifica os fluxos de edição da T48 e o contrato funcional do consentimento da T57. */
@SpringBootTest(properties = "app.consentimento.versao-termo=TCLE-SIMULADO teste")
@AutoConfigureMockMvc
@Transactional
class VisitaConsentimentoIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private EstudoRepository estudoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private CandidatoRepository candidatoRepository;
    @Autowired private AcaoRepository acaoRepository;
    @Autowired private ConsentimentoRepository consentimentoRepository;
    @Autowired private CriterioService criterioService;
    @Autowired private VisitaService visitaService;
    @Autowired private EstudoTransicaoService transicaoService;
    @Autowired private PlatformTransactionManager transactionManager;

    private String pesquisador;
    private String coordenador;

    @BeforeEach
    void autenticar() throws Exception {
        pesquisador = TokenDeTeste.bearer(mockMvc, TokenDeTeste.PESQUISADOR);
        coordenador = TokenDeTeste.bearer(mockMvc, TokenDeTeste.COORDENADOR);
    }

    @Test
    void editarERemoverVisitasMantemOrdemDerivadaDoDia() throws Exception {
        Estudo estudo = estudo(StatusEstudo.PLANEJAMENTO);
        String url = visitas(estudo);
        UUID inicial = criarVisita(estudo, 0);
        criarVisita(estudo, 30);
        UUID ultima = criarVisita(estudo, 60);

        mockMvc.perform(put(url + "/" + ultima).header("Authorization", pesquisador)
                        .contentType(MediaType.APPLICATION_JSON).content(dadosVisita(10)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ordem").value(2));
        mockMvc.perform(get(url).header("Authorization", coordenador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].diaPrevisto").value(0))
                .andExpect(jsonPath("$[1].diaPrevisto").value(10))
                .andExpect(jsonPath("$[2].diaPrevisto").value(30));

        mockMvc.perform(put(url + "/" + ultima).header("Authorization", pesquisador)
                        .contentType(MediaType.APPLICATION_JSON).content(dadosVisita(30)))
                .andExpect(status().isConflict());
        mockMvc.perform(delete(url + "/" + inicial).header("Authorization", pesquisador))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(url).header("Authorization", coordenador))
                .andExpect(jsonPath("$[0].diaPrevisto").value(10))
                .andExpect(jsonPath("$[0].ordem").value(1))
                .andExpect(jsonPath("$[1].diaPrevisto").value(30))
                .andExpect(jsonPath("$[1].ordem").value(2));
    }

    @Test
    void atividadesPodemSerEditadasERemovidasSomenteDaPropriaVisita() throws Exception {
        Estudo estudo = estudo(StatusEstudo.PLANEJAMENTO);
        UUID visita = criarVisita(estudo, 0);
        UUID outra = criarVisita(estudo, 30);
        UUID acao = criarAcao(visita);

        mockMvc.perform(put(acoes(outra) + "/" + acao).header("Authorization", pesquisador)
                        .contentType(MediaType.APPLICATION_JSON).content(dadosAcao("Outro", "QUESTIONARIO")))
                .andExpect(status().isNotFound());
        mockMvc.perform(put(acoes(visita) + "/" + acao).header("Authorization", coordenador)
                        .contentType(MediaType.APPLICATION_JSON).content(dadosAcao("Outro", "QUESTIONARIO")))
                .andExpect(status().isForbidden());
        mockMvc.perform(put(acoes(visita) + "/" + acao).header("Authorization", pesquisador)
                        .contentType(MediaType.APPLICATION_JSON).content(dadosAcao("Questionário", "QUESTIONARIO")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Questionário"))
                .andExpect(jsonPath("$.categoria").value("QUESTIONARIO"));
        mockMvc.perform(get(visitas(estudo)).header("Authorization", coordenador))
                .andExpect(jsonPath("$[0].acoes[0].nome").value("Questionário"));

        mockMvc.perform(delete(acoes(visita) + "/" + acao).header("Authorization", pesquisador))
                .andExpect(status().isNoContent());
        assertThat(acaoRepository.existsById(acao)).isFalse();
        UUID outraAcao = criarAcao(visita);
        mockMvc.perform(delete(visitas(estudo) + "/" + visita).header("Authorization", pesquisador))
                .andExpect(status().isNoContent());
        assertThat(acaoRepository.existsById(outraAcao)).isFalse();
    }

    @Test
    void todasAsEdicoesDeVisitaEAtividadeSaoBloqueadasForaDePlanejamento() throws Exception {
        Estudo estudo = estudo(StatusEstudo.PLANEJAMENTO);
        UUID visita = criarVisita(estudo, 0);
        UUID acao = criarAcao(visita);
        estudo.setStatus(StatusEstudo.RECRUTAMENTO);
        estudoRepository.saveAndFlush(estudo);

        mockMvc.perform(put(visitas(estudo) + "/" + visita).header("Authorization", pesquisador)
                        .contentType(MediaType.APPLICATION_JSON).content(dadosVisita(1)))
                .andExpect(status().isConflict());
        mockMvc.perform(delete(visitas(estudo) + "/" + visita).header("Authorization", pesquisador))
                .andExpect(status().isConflict());
        mockMvc.perform(post(acoes(visita)).header("Authorization", pesquisador)
                        .contentType(MediaType.APPLICATION_JSON).content(dadosAcao("Nova", "OUTROS")))
                .andExpect(status().isConflict());
        mockMvc.perform(put(acoes(visita) + "/" + acao).header("Authorization", pesquisador)
                        .contentType(MediaType.APPLICATION_JSON).content(dadosAcao("Nova", "OUTROS")))
                .andExpect(status().isConflict());
        mockMvc.perform(delete(acoes(visita) + "/" + acao).header("Authorization", pesquisador))
                .andExpect(status().isConflict());
        assertThat(acaoRepository.existsById(acao)).isTrue();
    }

    @Test
    void consentimentoUsaVersaoConfiguradaEResponsavelDoTokenSemExigirAvaliacao() throws Exception {
        Candidato candidato = candidato(StatusEstudo.RECRUTAMENTO, SituacaoCandidato.CADASTRADO);
        String url = consentimento(candidato);
        registrar(candidato, "{}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.versaoTermo").value("TCLE-SIMULADO teste"))
                .andExpect(jsonPath("$.candidatoId").value(candidato.getId().toString()))
                .andExpect(jsonPath("$.dataHora").isNotEmpty());

        var salvo = consentimentoRepository.findByCandidatoId(candidato.getId()).orElseThrow();
        assertThat(salvo.getResponsavel().getId()).isEqualTo(
                usuarioRepository.findByEmail(TokenDeTeste.COORDENADOR).orElseThrow().getId());
        mockMvc.perform(get(url).header("Authorization", coordenador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(salvo.getId().toString()))
                .andExpect(jsonPath("$.versaoTermo").value("TCLE-SIMULADO teste"));
        mockMvc.perform(get("/api/candidatos/" + candidato.getId()).header("Authorization", coordenador))
                .andExpect(jsonPath("$.consentimentoRegistrado").value(true))
                .andExpect(jsonPath("$.dataConsentimento").isNotEmpty());
        registrar(candidato, "{}").andExpect(status().isConflict());
    }

    @Test
    void consentimentoRecusaDataFuturaEstadoInadequadoCandidatoIncluidoEPerfilIncorreto() throws Exception {
        Candidato candidato = candidato(StatusEstudo.RECRUTAMENTO, SituacaoCandidato.CADASTRADO);
        registrar(candidato, "{\"dataHora\":\"" + LocalDateTime.now().plusDays(1) + "\"}")
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(consentimento(candidato)).header("Authorization", pesquisador)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        registrar(candidato(StatusEstudo.PLANEJAMENTO, SituacaoCandidato.CADASTRADO), "{}")
                .andExpect(status().isConflict());
        registrar(candidato(StatusEstudo.RECRUTAMENTO, SituacaoCandidato.INCLUIDO), "{}")
                .andExpect(status().isConflict());
        assertThat(consentimentoRepository.findByCandidatoId(candidato.getId())).isEmpty();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void cadastrosSimultaneosNaoDuplicamDescricaoDoMesmoTipo() throws Exception {
        Estudo estudo = estudo(StatusEstudo.PLANEJAMENTO);
        var executor = Executors.newFixedThreadPool(2);
        List<Future<String>> resultados = new ArrayList<>();
        CountDownLatch iniciados = new CountDownLatch(2);
        try {
            new TransactionTemplate(transactionManager).execute(estado -> {
                estudoRepository.buscarComBloqueio(estudo.getId()).orElseThrow();
                for (int i = 0; i < 2; i++) {
                    resultados.add(executor.submit(() -> {
                        iniciados.countDown();
                        try {
                            criterioService.criar(estudo.getId(),
                                    new CriterioRequest(TipoCriterio.INCLUSAO, null,
                                            "Condição fictícia concorrente", null, null));
                            return "CRIADO";
                        } catch (ConflitoRegraException erro) {
                            return erro.getCodigo();
                        }
                    }));
                }
                try {
                    assertThat(iniciados.await(5, TimeUnit.SECONDS)).isTrue();
                } catch (InterruptedException erro) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(erro);
                }
                for (Future<String> resultado : resultados) {
                    assertThatThrownBy(() -> resultado.get(250, TimeUnit.MILLISECONDS))
                            .isInstanceOf(TimeoutException.class);
                }
                return null;
            });
            assertThat(List.of(resultados.get(0).get(10, TimeUnit.SECONDS),
                            resultados.get(1).get(10, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder("CRIADO", "CRITERIO_DUPLICADO");
            assertThat(criterioService.listar(estudo.getId())).hasSize(1);
        } finally {
            executor.shutdownNow();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
            estudoRepository.deleteById(estudo.getId());
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void atividadeEsperaTransicaoERecusaEstadoQueDeixouDeSerPlanejamento() throws Exception {
        Estudo estudo = estudo(StatusEstudo.PLANEJAMENTO);
        criterioService.criar(estudo.getId(), new CriterioRequest(TipoCriterio.INCLUSAO, null,
                "Condição fictícia para avançar", null, null));
        UUID visita = visitaService.criar(estudo.getId(), new VisitaRequest("Inicial", 0, 0)).id();
        var executor = Executors.newSingleThreadExecutor();
        AtomicReference<Future<String>> resultado = new AtomicReference<>();
        CountDownLatch iniciado = new CountDownLatch(1);
        try {
            new TransactionTemplate(transactionManager).execute(estado -> {
                estudoRepository.buscarComBloqueio(estudo.getId()).orElseThrow();
                resultado.set(executor.submit(() -> {
                    iniciado.countDown();
                    try {
                        visitaService.criarAcao(visita, new AcaoRequest("Atividade concorrente", null,
                                CategoriaAcao.AVALIACAO_CLINICA));
                        return "CRIADA";
                    } catch (ConflitoRegraException erro) {
                        return erro.getCodigo();
                    }
                }));
                try {
                    assertThat(iniciado.await(5, TimeUnit.SECONDS)).isTrue();
                } catch (InterruptedException erro) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(erro);
                }
                assertThatThrownBy(() -> resultado.get().get(250, TimeUnit.MILLISECONDS))
                        .isInstanceOf(TimeoutException.class);
                transicaoService.avancar(estudo.getId(), StatusEstudo.RECRUTAMENTO,
                        usuarioRepository.findByEmail(TokenDeTeste.PESQUISADOR).orElseThrow().getId());
                return null;
            });
            assertThat(resultado.get().get(10, TimeUnit.SECONDS)).isEqualTo("ESTUDO_FORA_DE_PLANEJAMENTO");
            assertThat(visitaService.listar(estudo.getId()).get(0).acoes()).isEmpty();
        } finally {
            executor.shutdownNow();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
            estudoRepository.deleteById(estudo.getId());
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void consentimentosSimultaneosRetornamCriadoEConflitoPorHttp() throws Exception {
        Candidato candidato = candidato(StatusEstudo.RECRUTAMENTO, SituacaoCandidato.CADASTRADO);
        UUID estudoId = candidato.getEstudo().getId();
        var executor = Executors.newFixedThreadPool(2);
        List<Future<String>> resultados = new ArrayList<>();
        CountDownLatch iniciados = new CountDownLatch(2);
        try {
            new TransactionTemplate(transactionManager).execute(estado -> {
                estudoRepository.buscarComBloqueio(estudoId).orElseThrow();
                for (int i = 0; i < 2; i++) {
                    resultados.add(executor.submit(() -> {
                        iniciados.countDown();
                        var resposta = registrar(candidato, "{}").andReturn().getResponse();
                        if (resposta.getStatus() == 201) {
                            return "CRIADO";
                        }
                        assertThat(resposta.getStatus()).isEqualTo(409);
                        return JsonPath.<String>read(resposta.getContentAsString(), "$.codigoRegra");
                    }));
                }
                try {
                    assertThat(iniciados.await(5, TimeUnit.SECONDS)).isTrue();
                } catch (InterruptedException erro) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(erro);
                }
                for (Future<String> resultado : resultados) {
                    assertThatThrownBy(() -> resultado.get(250, TimeUnit.MILLISECONDS))
                            .isInstanceOf(TimeoutException.class);
                }
                return null;
            });
            assertThat(List.of(resultados.get(0).get(10, TimeUnit.SECONDS),
                            resultados.get(1).get(10, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder("CRIADO", "CONSENTIMENTO_DUPLICADO");
            assertThat(consentimentoRepository.findByCandidatoId(candidato.getId())).isPresent();
        } finally {
            executor.shutdownNow();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
            consentimentoRepository.findByCandidatoId(candidato.getId()).ifPresent(consentimentoRepository::delete);
            candidatoRepository.deleteById(candidato.getId());
            estudoRepository.deleteById(estudoId);
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void consentimentoEsperaTransicaoERecusaEstudoQueEntrouEmExecucao() throws Exception {
        Candidato candidato = candidato(StatusEstudo.RECRUTAMENTO, SituacaoCandidato.CADASTRADO);
        UUID estudoId = candidato.getEstudo().getId();
        var executor = Executors.newSingleThreadExecutor();
        AtomicReference<Future<String>> resultado = new AtomicReference<>();
        CountDownLatch iniciado = new CountDownLatch(1);
        try {
            new TransactionTemplate(transactionManager).execute(estado -> {
                estudoRepository.buscarComBloqueio(estudoId).orElseThrow();
                resultado.set(executor.submit(() -> {
                    iniciado.countDown();
                    return registrar(candidato, "{}").andExpect(status().isConflict())
                            .andReturn().getResponse().getContentAsString();
                }));
                try {
                    assertThat(iniciado.await(5, TimeUnit.SECONDS)).isTrue();
                } catch (InterruptedException erro) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(erro);
                }
                assertThatThrownBy(() -> resultado.get().get(250, TimeUnit.MILLISECONDS))
                        .isInstanceOf(TimeoutException.class);
                transicaoService.avancar(estudoId, StatusEstudo.EXECUCAO,
                        usuarioRepository.findByEmail(TokenDeTeste.PESQUISADOR).orElseThrow().getId());
                return null;
            });
            String resposta = resultado.get().get(10, TimeUnit.SECONDS);
            assertThat(JsonPath.<String>read(resposta, "$.codigoRegra"))
                    .isEqualTo("ESTUDO_FORA_DE_RECRUTAMENTO");
            assertThat(consentimentoRepository.findByCandidatoId(candidato.getId())).isEmpty();
        } finally {
            executor.shutdownNow();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
            consentimentoRepository.findByCandidatoId(candidato.getId()).ifPresent(consentimentoRepository::delete);
            candidatoRepository.deleteById(candidato.getId());
            estudoRepository.deleteById(estudoId);
        }
    }

    private Estudo estudo(StatusEstudo status) {
        Estudo estudo = new Estudo();
        estudo.setTitulo("Estudo fictício T48/T57");
        estudo.setObjetivo("Verificar visitas e consentimento");
        estudo.setResponsavel(usuarioRepository.findByEmail(TokenDeTeste.PESQUISADOR).orElseThrow());
        estudo.setLimiteParticipantes(10);
        estudo.setStatus(status);
        return estudoRepository.saveAndFlush(estudo);
    }

    private Candidato candidato(StatusEstudo status, SituacaoCandidato situacao) {
        Candidato candidato = new Candidato();
        candidato.setEstudo(estudo(status));
        candidato.setNomeFicticio("Candidato fictício T57");
        candidato.setCodigo("CAND-001");
        candidato.setDataNascimento(LocalDate.of(1990, 1, 1));
        candidato.setSituacao(situacao);
        return candidatoRepository.saveAndFlush(candidato);
    }

    private UUID criarVisita(Estudo estudo, int dia) throws Exception {
        String corpo = mockMvc.perform(post(visitas(estudo)).header("Authorization", pesquisador)
                        .contentType(MediaType.APPLICATION_JSON).content(dadosVisita(dia)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(corpo, "$.id"));
    }

    private UUID criarAcao(UUID visita) throws Exception {
        String corpo = mockMvc.perform(post(acoes(visita)).header("Authorization", pesquisador)
                        .contentType(MediaType.APPLICATION_JSON).content(dadosAcao("Avaliação", "AVALIACAO_CLINICA")))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(corpo, "$.id"));
    }

    private ResultActions registrar(Candidato candidato, String corpo) throws Exception {
        return mockMvc.perform(post(consentimento(candidato)).header("Authorization", coordenador)
                .contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private static String dadosVisita(int dia) {
        return "{\"nome\":\"Visita " + dia + "\",\"diaPrevisto\":" + dia + ",\"tolerancia\":0}";
    }

    private static String dadosAcao(String nome, String categoria) {
        return "{\"nome\":\"" + nome + "\",\"descricao\":\"Procedimento fictício\",\"categoria\":\"" + categoria + "\"}";
    }

    private static String visitas(Estudo estudo) { return "/api/estudos/" + estudo.getId() + "/visitas"; }
    private static String acoes(UUID visita) { return "/api/visitas/" + visita + "/acoes"; }
    private static String consentimento(Candidato candidato) { return "/api/candidatos/" + candidato.getId() + "/consentimento"; }
}
