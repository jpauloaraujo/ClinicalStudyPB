package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.PerfilUsuario;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.HistoricoStatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.repository.CriterioRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.EstudoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.HistoricoStatusEstudoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.UsuarioRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaPlanejadaRepository;
import UEPB.ESWII.ClinicalStudyPB.service.ConflitoRegraException;
import UEPB.ESWII.ClinicalStudyPB.service.EstudoAcesso;
import UEPB.ESWII.ClinicalStudyPB.service.EstudoTransicaoService;
import UEPB.ESWII.ClinicalStudyPB.service.UsuarioAutenticado;

/**
 * T44 (RF07): o serviço de transição percorrendo as 16 combinações de origem e destino com Mockito.
 * Só 3 avançam e gravam histórico; as outras 13 recusam sem alterar o estudo nem gravar nada.
 * Complementa {@code EstudoTransicaoServiceTest} (T41), que cobre as pré-condições e os usuários inválidos.
 */
class EstudoTransicaoCombinacoesServiceTest {

    private EstudoRepository estudoRepository;
    private HistoricoStatusEstudoRepository historicoRepository;
    private EstudoTransicaoService service;
    private Estudo estudo;
    private Usuario pesquisador;

    @BeforeEach
    void configurar() {
        estudoRepository = mock(EstudoRepository.class);
        CriterioRepository criterioRepository = mock(CriterioRepository.class);
        VisitaPlanejadaRepository visitaRepository = mock(VisitaPlanejadaRepository.class);
        historicoRepository = mock(HistoricoStatusEstudoRepository.class);
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        service = new EstudoTransicaoService(new EstudoAcesso(estudoRepository), estudoRepository,
                criterioRepository, visitaRepository, historicoRepository, new UsuarioAutenticado(usuarioRepository));

        pesquisador = new Usuario();
        pesquisador.setId(UUID.randomUUID());
        pesquisador.setPerfil(PerfilUsuario.PESQUISADOR);
        when(usuarioRepository.findById(pesquisador.getId())).thenReturn(Optional.of(pesquisador));

        estudo = new Estudo();
        estudo.setId(UUID.randomUUID());
        estudo.setResponsavel(pesquisador);
        when(estudoRepository.buscarComBloqueio(estudo.getId())).thenReturn(Optional.of(estudo));
        when(estudoRepository.save(any(Estudo.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
        // Pré-condições atendidas: um critério de inclusão e uma visita, para isolar a regra da sequência
        when(criterioRepository.contarInclusaoPorEstudo(estudo.getId())).thenReturn(1L);
        when(visitaRepository.countByEstudoId(estudo.getId())).thenReturn(1L);
    }

    static Stream<Arguments> combinacoes() {
        return Stream.of(StatusEstudo.values()).flatMap(origem -> Stream.of(StatusEstudo.values())
                .map(destino -> Arguments.of(origem, destino, destino.ordinal() == origem.ordinal() + 1)));
    }

    @ParameterizedTest(name = "{0} -> {1}: permitida = {2}")
    @MethodSource("combinacoes")
    @DisplayName("Nas 16 combinações, o serviço só avança e grava histórico nas 3 permitidas")
    void servicoNasDezesseisCombinacoes(StatusEstudo origem, StatusEstudo destino, boolean permitida) {
        estudo.setStatus(origem);

        if (permitida) {
            Estudo resultado = service.avancar(estudo.getId(), destino, pesquisador.getId());

            assertThat(resultado.getStatus()).isEqualTo(destino);
            ArgumentCaptor<HistoricoStatusEstudo> captor = ArgumentCaptor.forClass(HistoricoStatusEstudo.class);
            verify(historicoRepository).save(captor.capture());
            assertThat(captor.getValue().getEstadoAnterior()).isEqualTo(origem);
            assertThat(captor.getValue().getEstadoNovo()).isEqualTo(destino);
            assertThat(captor.getValue().getUsuario()).isSameAs(pesquisador);
        } else {
            assertThatThrownBy(() -> service.avancar(estudo.getId(), destino, pesquisador.getId()))
                    .isInstanceOf(ConflitoRegraException.class);

            assertThat(estudo.getStatus()).isEqualTo(origem);
            verify(historicoRepository, never()).save(any());
            verify(estudoRepository, never()).save(any());
        }
    }
}
