package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.PerfilUsuario;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.HistoricoStatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.domain.regra.MaquinaEstadosEstudo;
import UEPB.ESWII.ClinicalStudyPB.repository.CriterioRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.EstudoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.HistoricoStatusEstudoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.UsuarioRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaPlanejadaRepository;
import UEPB.ESWII.ClinicalStudyPB.service.ConflitoRegraException;
import UEPB.ESWII.ClinicalStudyPB.service.EstudoAcesso;
import UEPB.ESWII.ClinicalStudyPB.service.EstudoTransicaoService;
import UEPB.ESWII.ClinicalStudyPB.service.RecursoNaoEncontradoException;
import UEPB.ESWII.ClinicalStudyPB.service.RegraNegocioException;
import UEPB.ESWII.ClinicalStudyPB.service.UsuarioAutenticado;

/** T41 (RF07): serviço que avança o estado do estudo e grava o histórico, com repositórios simulados. */
class EstudoTransicaoServiceTest {

    private EstudoRepository estudoRepository;
    private CriterioRepository criterioRepository;
    private VisitaPlanejadaRepository visitaRepository;
    private HistoricoStatusEstudoRepository historicoRepository;
    private EstudoTransicaoService service;

    private Estudo estudo;
    private Usuario pesquisador;

    @BeforeEach
    void configurar() {
        estudoRepository = mock(EstudoRepository.class);
        criterioRepository = mock(CriterioRepository.class);
        visitaRepository = mock(VisitaPlanejadaRepository.class);
        historicoRepository = mock(HistoricoStatusEstudoRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        service = new EstudoTransicaoService(new EstudoAcesso(estudoRepository), estudoRepository,
                criterioRepository, visitaRepository, historicoRepository, new UsuarioAutenticado(usuarioRepository));

        pesquisador = new Usuario();
        pesquisador.setId(UUID.randomUUID());
        pesquisador.setNome("Pesquisadora Fictícia");
        pesquisador.setPerfil(PerfilUsuario.PESQUISADOR);
        when(usuarioRepository.findById(pesquisador.getId())).thenReturn(Optional.of(pesquisador));

        estudo = new Estudo();
        estudo.setId(UUID.randomUUID());
        estudo.setResponsavel(pesquisador);
        estudo.setStatus(StatusEstudo.PLANEJAMENTO);
        when(estudoRepository.buscarComBloqueio(estudo.getId())).thenReturn(Optional.of(estudo));
        when(estudoRepository.save(any(Estudo.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    private void comCriteriosEVisitas(long criterios, long visitas) {
        when(criterioRepository.contarInclusaoPorEstudo(estudo.getId())).thenReturn(criterios);
        when(visitaRepository.countByEstudoId(estudo.getId())).thenReturn(visitas);
    }

    @Test
    @DisplayName("Avança de planejamento para recrutamento e grava o histórico com quem avançou")
    void avancaEGravaHistorico() {
        comCriteriosEVisitas(1, 1);

        Estudo resultado = service.avancar(estudo.getId(), StatusEstudo.RECRUTAMENTO, pesquisador.getId());

        assertThat(resultado.getStatus()).isEqualTo(StatusEstudo.RECRUTAMENTO);
        ArgumentCaptor<HistoricoStatusEstudo> captor = ArgumentCaptor.forClass(HistoricoStatusEstudo.class);
        verify(historicoRepository).save(captor.capture());
        HistoricoStatusEstudo registro = captor.getValue();
        assertThat(registro.getEstudo()).isSameAs(estudo);
        assertThat(registro.getEstadoAnterior()).isEqualTo(StatusEstudo.PLANEJAMENTO);
        assertThat(registro.getEstadoNovo()).isEqualTo(StatusEstudo.RECRUTAMENTO);
        assertThat(registro.getUsuario()).isSameAs(pesquisador);
        assertThat(registro.getDataHora()).isNotNull();
    }

    @Test
    @DisplayName("Percorre recrutamento, execução e encerrado sem exigir critérios nem visitas")
    void avancaDeRecrutamentoAteEncerrado() {
        comCriteriosEVisitas(0, 0);
        estudo.setStatus(StatusEstudo.RECRUTAMENTO);

        service.avancar(estudo.getId(), StatusEstudo.EXECUCAO, pesquisador.getId());
        service.avancar(estudo.getId(), StatusEstudo.ENCERRADO, pesquisador.getId());

        assertThat(estudo.getStatus()).isEqualTo(StatusEstudo.ENCERRADO);
        verify(historicoRepository, org.mockito.Mockito.times(2)).save(any(HistoricoStatusEstudo.class));
    }

    @Test
    @DisplayName("Sem critério de inclusão não sai de planejamento e nada é gravado")
    void semCriterioNaoAvanca() {
        comCriteriosEVisitas(0, 2);

        assertThatThrownBy(() -> service.avancar(estudo.getId(), StatusEstudo.RECRUTAMENTO, pesquisador.getId()))
                .isInstanceOfSatisfying(ConflitoRegraException.class,
                        erro -> assertThat(erro.getCodigo()).isEqualTo(MaquinaEstadosEstudo.SEM_CRITERIO_INCLUSAO));

        assertThat(estudo.getStatus()).isEqualTo(StatusEstudo.PLANEJAMENTO);
        verify(estudoRepository, never()).save(any());
        verifyNoInteractions(historicoRepository);
    }

    @Test
    @DisplayName("Sem visita prevista não sai de planejamento e nada é gravado")
    void semVisitaNaoAvanca() {
        comCriteriosEVisitas(1, 0);

        assertThatThrownBy(() -> service.avancar(estudo.getId(), StatusEstudo.RECRUTAMENTO, pesquisador.getId()))
                .isInstanceOfSatisfying(ConflitoRegraException.class,
                        erro -> assertThat(erro.getCodigo()).isEqualTo(MaquinaEstadosEstudo.SEM_VISITA_PREVISTA));

        assertThat(estudo.getStatus()).isEqualTo(StatusEstudo.PLANEJAMENTO);
        verifyNoInteractions(historicoRepository);
    }

    @Test
    @DisplayName("Pular, voltar ou repetir o estado é recusado sem alterar o estudo nem gravar histórico")
    void transicaoInvalidaNaoAlteraNada() {
        comCriteriosEVisitas(1, 1);

        for (StatusEstudo destino : new StatusEstudo[] {StatusEstudo.EXECUCAO, StatusEstudo.ENCERRADO,
                StatusEstudo.PLANEJAMENTO}) {
            assertThatThrownBy(() -> service.avancar(estudo.getId(), destino, pesquisador.getId()))
                    .isInstanceOfSatisfying(ConflitoRegraException.class,
                            erro -> assertThat(erro.getCodigo()).isEqualTo(MaquinaEstadosEstudo.TRANSICAO_INVALIDA));
        }

        assertThat(estudo.getStatus()).isEqualTo(StatusEstudo.PLANEJAMENTO);
        verify(estudoRepository, never()).save(any());
        verifyNoInteractions(historicoRepository);
    }

    @Test
    @DisplayName("Estudo inexistente gera recurso não encontrado")
    void estudoInexistente() {
        UUID desconhecido = UUID.randomUUID();
        when(estudoRepository.buscarComBloqueio(desconhecido)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.avancar(desconhecido, StatusEstudo.RECRUTAMENTO, pesquisador.getId()))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining(desconhecido.toString());
    }

    @Test
    @DisplayName("Usuário ausente ou inexistente não avança o estudo")
    void usuarioInvalido() {
        comCriteriosEVisitas(1, 1);

        assertThatThrownBy(() -> service.avancar(estudo.getId(), StatusEstudo.RECRUTAMENTO, null))
                .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> service.avancar(estudo.getId(), StatusEstudo.RECRUTAMENTO, UUID.randomUUID()))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        assertThat(estudo.getStatus()).isEqualTo(StatusEstudo.PLANEJAMENTO);
        verifyNoInteractions(historicoRepository);
    }
}
