package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.AvaliacaoRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.AvaliacaoResponse;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.ResultadoAvaliacao;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoCandidato;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.AvaliacaoElegibilidade;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Candidato;
import UEPB.ESWII.ClinicalStudyPB.domain.model.CriterioExclusao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.CriterioInclusao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.repository.AvaliacaoElegibilidadeRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.ConsentimentoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.CriterioRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.EstudoRepository;
import UEPB.ESWII.ClinicalStudyPB.service.AvaliacaoService;
import UEPB.ESWII.ClinicalStudyPB.service.CandidatoService;
import UEPB.ESWII.ClinicalStudyPB.service.ConflitoRegraException;
import UEPB.ESWII.ClinicalStudyPB.service.EstudoAcesso;
import UEPB.ESWII.ClinicalStudyPB.service.RegraNegocioException;
import UEPB.ESWII.ClinicalStudyPB.service.UsuarioAutenticado;

/**
 * T54: regras do serviço de avaliação de elegibilidade, isoladas do banco. A regra pura de
 * elegibilidade em si está em {@code DominioRecrutamentoTest}.
 */
class AvaliacaoServiceTest {

    private AvaliacaoElegibilidadeRepository avaliacaoRepository;
    private CriterioRepository criterioRepository;
    private ConsentimentoRepository consentimentoRepository;
    private CandidatoService candidatoService;
    private AvaliacaoService service;

    private Estudo estudo;
    private Candidato candidato;
    private Usuario avaliador;
    private CriterioInclusao inclusao;
    private CriterioExclusao exclusao;

    @BeforeEach
    void configurar() {
        avaliacaoRepository = mock(AvaliacaoElegibilidadeRepository.class);
        criterioRepository = mock(CriterioRepository.class);
        consentimentoRepository = mock(ConsentimentoRepository.class);
        candidatoService = mock(CandidatoService.class);
        UsuarioAutenticado usuarioAutenticado = mock(UsuarioAutenticado.class);
        service = new AvaliacaoService(avaliacaoRepository, criterioRepository, consentimentoRepository,
                candidatoService, new EstudoAcesso(mock(EstudoRepository.class)), usuarioAutenticado);

        estudo = new Estudo();
        estudo.setId(UUID.randomUUID());
        estudo.setStatus(StatusEstudo.RECRUTAMENTO);

        candidato = new Candidato();
        candidato.setId(UUID.randomUUID());
        candidato.setEstudo(estudo);
        candidato.setSituacao(SituacaoCandidato.CADASTRADO);

        avaliador = new Usuario();
        avaliador.setId(UUID.randomUUID());
        avaliador.setNome("Coordenador");

        inclusao = new CriterioInclusao();
        inclusao.setId(UUID.randomUUID());
        inclusao.setEstudo(estudo);
        inclusao.setTitulo("Idade adulta");
        inclusao.setPeso(1f);
        inclusao.setDeterminante(true);

        exclusao = new CriterioExclusao();
        exclusao.setId(UUID.randomUUID());
        exclusao.setEstudo(estudo);
        exclusao.setTitulo("Gestante");

        when(candidatoService.buscarEntidade(candidato.getId())).thenReturn(candidato);
        when(criterioRepository.findByEstudoIdOrderByTituloAsc(estudo.getId())).thenReturn(List.of(inclusao, exclusao));
        when(usuarioAutenticado.buscar(avaliador.getId())).thenReturn(avaliador);
        when(avaliacaoRepository.findByCandidatoIdAndVigenteTrue(candidato.getId())).thenReturn(Optional.empty());
        when(avaliacaoRepository.save(any(AvaliacaoElegibilidade.class))).thenAnswer(i -> i.getArgument(0));
    }

    private static AvaliacaoRequest.Resposta resposta(UUID criterio, boolean aplica) {
        return new AvaliacaoRequest.Resposta(criterio, aplica, null);
    }

    private AvaliacaoResponse avaliar(boolean aplicaInclusao, boolean aplicaExclusao) {
        return service.avaliar(candidato.getId(), new AvaliacaoRequest(
                List.of(resposta(inclusao.getId(), aplicaInclusao), resposta(exclusao.getId(), aplicaExclusao))),
                avaliador.getId());
    }

    @Test
    @DisplayName("Resposta faltando é recusada (400) e nada é gravado")
    void respostaFaltandoERecusada() {
        var pedido = new AvaliacaoRequest(List.of(resposta(inclusao.getId(), true)));

        assertThatThrownBy(() -> service.avaliar(candidato.getId(), pedido, avaliador.getId()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("todos os critérios");
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Resposta de critério de outro estudo é recusada (400)")
    void criterioDeOutroEstudoERecusado() {
        var pedido = new AvaliacaoRequest(List.of(resposta(inclusao.getId(), true), resposta(exclusao.getId(), false),
                resposta(UUID.randomUUID(), true)));

        assertThatThrownBy(() -> service.avaliar(candidato.getId(), pedido, avaliador.getId()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não pertence a este estudo");
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Resposta repetida para o mesmo critério é recusada (400)")
    void respostaRepetidaERecusada() {
        var pedido = new AvaliacaoRequest(List.of(resposta(inclusao.getId(), true), resposta(inclusao.getId(), false)));

        assertThatThrownBy(() -> service.avaliar(candidato.getId(), pedido, avaliador.getId()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("mais de uma resposta");
    }

    @Test
    @DisplayName("Estudo fora de recrutamento resulta em 409")
    void estudoForaDeRecrutamento() {
        estudo.setStatus(StatusEstudo.PLANEJAMENTO);

        assertThatThrownBy(() -> avaliar(true, false)).isInstanceOf(ConflitoRegraException.class)
                .extracting("codigo").isEqualTo("ESTUDO_FORA_DE_RECRUTAMENTO");
    }

    @Test
    @DisplayName("Candidato já incluído resulta em 409")
    void candidatoJaIncluido() {
        candidato.setSituacao(SituacaoCandidato.INCLUIDO);

        assertThatThrownBy(() -> avaliar(true, false)).isInstanceOf(ConflitoRegraException.class)
                .extracting("codigo").isEqualTo("CANDIDATO_JA_INCLUIDO");
    }

    @Test
    @DisplayName("Aprovado sem consentimento fica ELEGIVEL; com consentimento fica CONSENTIDO")
    void aprovadoAtualizaASituacao() {
        assertThat(avaliar(true, false).resultado()).isEqualTo(ResultadoAvaliacao.APROVADO);
        assertThat(candidato.getSituacao()).isEqualTo(SituacaoCandidato.ELEGIVEL);

        when(consentimentoRepository.existsByCandidatoId(candidato.getId())).thenReturn(true);
        avaliar(true, false);
        assertThat(candidato.getSituacao()).isEqualTo(SituacaoCandidato.CONSENTIDO);
    }

    @Test
    @DisplayName("Reprovado vira NAO_ELEGIVEL e informa os critérios reprovadores")
    void reprovadoAtualizaASituacao() {
        AvaliacaoResponse resposta = avaliar(true, true);

        assertThat(resposta.resultado()).isEqualTo(ResultadoAvaliacao.REPROVADO);
        assertThat(resposta.criteriosReprovadores()).extracting(AvaliacaoResponse.CriterioFalho::titulo)
                .containsExactly("Gestante");
        assertThat(candidato.getSituacao()).isEqualTo(SituacaoCandidato.NAO_ELEGIVEL);
    }

    @Test
    @DisplayName("Reavaliação marca a anterior como não vigente e a nova como vigente")
    void reavaliacaoPreservaOHistorico() {
        AvaliacaoElegibilidade anterior = new AvaliacaoElegibilidade();
        anterior.setVigente(true);
        when(avaliacaoRepository.findByCandidatoIdAndVigenteTrue(candidato.getId())).thenReturn(Optional.of(anterior));

        AvaliacaoResponse nova = avaliar(true, false);

        assertThat(anterior.isVigente()).isFalse();
        assertThat(nova.vigente()).isTrue();
        verify(avaliacaoRepository).saveAndFlush(anterior);
    }
}
