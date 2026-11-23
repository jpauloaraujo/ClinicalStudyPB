package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;
import UEPB.ESWII.ClinicalStudyPB.repository.ParticipacaoRepository;
import UEPB.ESWII.ClinicalStudyPB.service.AcessoParticipante;
import UEPB.ESWII.ClinicalStudyPB.service.RecursoNaoEncontradoException;

/**
 * T109 (RQ-T109-04 a 07): teste de unidade do {@link AcessoParticipante} com o repositório simulado. A ordenação em
 * si é do repositório e é conferida no teste de fumaça dos endpoints ({@code MeEndpointTest}).
 */
class AcessoParticipanteTest {

    private static final UUID USUARIO = UUID.randomUUID();
    private static final UUID ESTUDO = UUID.randomUUID();
    private static final UUID PARTICIPACAO = UUID.randomUUID();

    private ParticipacaoRepository repositorio;
    private AcessoParticipante acesso;

    @BeforeEach
    void preparar() {
        repositorio = mock(ParticipacaoRepository.class);
        acesso = new AcessoParticipante(repositorio);
    }

    @Test
    @DisplayName("participacoesDe devolve as participações da conta na ordem do repositório")
    void participacoesDaConta() {
        Participacao a = new Participacao();
        Participacao b = new Participacao();
        when(repositorio.findByCandidatoUsuarioIdOrderByDataInclusaoDesc(USUARIO)).thenReturn(List.of(a, b));

        assertThat(acesso.participacoesDe(USUARIO)).containsExactly(a, b);
    }

    @Test
    @DisplayName("RQ-05: conta sem candidato vinculado devolve lista vazia, não erro")
    void contaSemVinculoDevolveListaVazia() {
        when(repositorio.findByCandidatoUsuarioIdOrderByDataInclusaoDesc(USUARIO)).thenReturn(List.of());

        assertThat(acesso.participacoesDe(USUARIO)).isEmpty();
    }

    @Test
    @DisplayName("usuário nulo (sem token) devolve lista vazia e nem consulta o banco")
    void usuarioNuloNaoConsultaOBanco() {
        assertThat(acesso.participacoesDe(null)).isEmpty();
        verifyNoInteractions(repositorio);
    }

    @Test
    @DisplayName("participacaoDoEstudo devolve a participação da conta no estudo")
    void participacaoDoEstudoDaConta() {
        Participacao participacao = new Participacao();
        when(repositorio.findByCandidatoUsuarioIdAndEstudoId(USUARIO, ESTUDO)).thenReturn(Optional.of(participacao));

        assertThat(acesso.participacaoDoEstudo(USUARIO, ESTUDO)).isSameAs(participacao);
    }

    @Test
    @DisplayName("RQ-04: estudo que não é da conta lança 404 (RecursoNaoEncontradoException), nunca 403")
    void estudoDeOutroLanca404() {
        when(repositorio.findByCandidatoUsuarioIdAndEstudoId(USUARIO, ESTUDO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> acesso.participacaoDoEstudo(USUARIO, ESTUDO))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("participacaoPorId devolve a participação quando é da conta")
    void participacaoPorIdDaConta() {
        Participacao participacao = new Participacao();
        when(repositorio.findByIdAndCandidatoUsuarioId(PARTICIPACAO, USUARIO)).thenReturn(Optional.of(participacao));

        assertThat(acesso.participacaoPorId(USUARIO, PARTICIPACAO)).isSameAs(participacao);
    }

    @Test
    @DisplayName("RQ-04: participação de outra conta lança 404 (RecursoNaoEncontradoException)")
    void participacaoDeOutraContaLanca404() {
        when(repositorio.findByIdAndCandidatoUsuarioId(PARTICIPACAO, USUARIO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> acesso.participacaoPorId(USUARIO, PARTICIPACAO))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("ids nulos lançam 404 sem consultar o banco")
    void idsNulosLancam404() {
        assertThatThrownBy(() -> acesso.participacaoDoEstudo(null, ESTUDO))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> acesso.participacaoDoEstudo(USUARIO, null))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> acesso.participacaoPorId(USUARIO, null))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verifyNoInteractions(repositorio);
    }
}
