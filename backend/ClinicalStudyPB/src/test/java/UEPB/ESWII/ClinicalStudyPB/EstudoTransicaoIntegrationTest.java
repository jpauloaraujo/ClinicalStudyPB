package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.CriterioExclusao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.CriterioInclusao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.HistoricoStatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaPlanejada;
import UEPB.ESWII.ClinicalStudyPB.domain.regra.MaquinaEstadosEstudo;
import UEPB.ESWII.ClinicalStudyPB.repository.CriterioRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.EstudoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.HistoricoStatusEstudoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.UsuarioRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaPlanejadaRepository;
import UEPB.ESWII.ClinicalStudyPB.service.ConflitoRegraException;
import UEPB.ESWII.ClinicalStudyPB.service.EstudoTransicaoService;

/**
 * T38 / T41: a máquina de estados sobre o banco de testes (H2 com as migrations do Flyway), o que
 * também prova que a entidade do histórico bate com a tabela {@code historico_status_estudo}.
 */
@SpringBootTest
@Transactional
class EstudoTransicaoIntegrationTest {

    @Autowired
    private EstudoTransicaoService service;
    @Autowired
    private EstudoRepository estudoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private CriterioRepository criterioRepository;
    @Autowired
    private VisitaPlanejadaRepository visitaRepository;
    @Autowired
    private HistoricoStatusEstudoRepository historicoRepository;

    private Usuario pesquisador;

    @BeforeEach
    void carregarPesquisador() {
        pesquisador = usuarioRepository.findByEmail("pesquisador@clinicalstudy.test").orElseThrow();
    }

    private Estudo novoEstudo() {
        Estudo estudo = new Estudo();
        estudo.setTitulo("Estudo de Transição");
        estudo.setObjetivo("Objetivo fictício");
        estudo.setResponsavel(pesquisador);
        estudo.setLimiteParticipantes(10);
        estudo.setStatus(StatusEstudo.PLANEJAMENTO);
        return estudoRepository.save(estudo);
    }

    private void adicionarCriterioDeInclusao(Estudo estudo) {
        CriterioInclusao criterio = new CriterioInclusao();
        criterio.setEstudo(estudo);
        criterio.setTitulo("Idade adulta");
        criterio.setDescricao("Ter 18 anos ou mais");
        criterio.setPeso(1f);
        criterio.setDeterminante(true);
        criterioRepository.save(criterio);
    }

    private void adicionarCriterioDeExclusao(Estudo estudo) {
        CriterioExclusao criterio = new CriterioExclusao();
        criterio.setEstudo(estudo);
        criterio.setTitulo("Gestante");
        criterio.setDescricao("Gestação em curso");
        criterioRepository.save(criterio);
    }

    private void adicionarVisita(Estudo estudo) {
        VisitaPlanejada visita = new VisitaPlanejada();
        visita.setEstudo(estudo);
        visita.setNome("Triagem");
        visita.setDiaPrevisto(0);
        visita.setTolerancia(0);
        visita.setOrdem(1);
        visitaRepository.save(visita);
    }

    @Test
    @DisplayName("Percorre o ciclo completo e registra uma linha de histórico por mudança")
    void percorreOCicloCompleto() {
        Estudo estudo = novoEstudo();
        adicionarCriterioDeInclusao(estudo);
        adicionarVisita(estudo);

        service.avancar(estudo.getId(), StatusEstudo.RECRUTAMENTO, pesquisador.getId());
        service.avancar(estudo.getId(), StatusEstudo.EXECUCAO, pesquisador.getId());
        Estudo encerrado = service.avancar(estudo.getId(), StatusEstudo.ENCERRADO, pesquisador.getId());

        assertThat(encerrado.getStatus()).isEqualTo(StatusEstudo.ENCERRADO);
        assertThat(encerrado.getResponsavel().getNome()).isNotBlank();
        List<HistoricoStatusEstudo> historico = historicoRepository.findByEstudoIdOrderByDataHoraAsc(estudo.getId());
        assertThat(historico).hasSize(3)
                .extracting(HistoricoStatusEstudo::getEstadoNovo)
                .containsExactlyInAnyOrder(StatusEstudo.RECRUTAMENTO, StatusEstudo.EXECUCAO, StatusEstudo.ENCERRADO);
        assertThat(historico).allSatisfy(registro -> {
            assertThat(registro.getUsuario().getId()).isEqualTo(pesquisador.getId());
            assertThat(registro.getDataHora()).isNotNull();
        });
    }

    @Test
    @DisplayName("Critério de exclusão sozinho não basta para sair de planejamento")
    void criterioDeExclusaoNaoContaComoInclusao() {
        Estudo estudo = novoEstudo();
        adicionarCriterioDeExclusao(estudo);
        adicionarVisita(estudo);

        assertThatThrownBy(() -> service.avancar(estudo.getId(), StatusEstudo.RECRUTAMENTO, pesquisador.getId()))
                .isInstanceOfSatisfying(ConflitoRegraException.class,
                        erro -> assertThat(erro.getCodigo()).isEqualTo(MaquinaEstadosEstudo.SEM_CRITERIO_INCLUSAO));
        assertThat(historicoRepository.findByEstudoIdOrderByDataHoraAsc(estudo.getId())).isEmpty();
    }

    @Test
    @DisplayName("Sem visita prevista não sai de planejamento")
    void semVisitaNaoSaiDePlanejamento() {
        Estudo estudo = novoEstudo();
        adicionarCriterioDeInclusao(estudo);

        assertThatThrownBy(() -> service.avancar(estudo.getId(), StatusEstudo.RECRUTAMENTO, pesquisador.getId()))
                .isInstanceOfSatisfying(ConflitoRegraException.class,
                        erro -> assertThat(erro.getCodigo()).isEqualTo(MaquinaEstadosEstudo.SEM_VISITA_PREVISTA));
        assertThat(estudoRepository.findById(estudo.getId()).orElseThrow().getStatus())
                .isEqualTo(StatusEstudo.PLANEJAMENTO);
    }

    @Test
    @DisplayName("Não pula nem volta de estado")
    void naoPulaNemVolta() {
        Estudo estudo = novoEstudo();
        adicionarCriterioDeInclusao(estudo);
        adicionarVisita(estudo);
        UUID id = estudo.getId();

        assertThatThrownBy(() -> service.avancar(id, StatusEstudo.EXECUCAO, pesquisador.getId()))
                .isInstanceOfSatisfying(ConflitoRegraException.class,
                        erro -> assertThat(erro.getCodigo()).isEqualTo(MaquinaEstadosEstudo.TRANSICAO_INVALIDA));

        service.avancar(id, StatusEstudo.RECRUTAMENTO, pesquisador.getId());
        assertThatThrownBy(() -> service.avancar(id, StatusEstudo.PLANEJAMENTO, pesquisador.getId()))
                .isInstanceOf(ConflitoRegraException.class);
        assertThat(historicoRepository.findByEstudoIdOrderByDataHoraAsc(id)).hasSize(1);
    }
}
