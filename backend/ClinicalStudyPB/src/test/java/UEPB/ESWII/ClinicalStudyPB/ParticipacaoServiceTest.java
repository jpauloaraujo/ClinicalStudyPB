package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.ParticipacaoResponse;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.ResultadoAvaliacao;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoCandidato;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.AvaliacaoElegibilidade;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Candidato;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.repository.AvaliacaoElegibilidadeRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.ConsentimentoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.EstudoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.ParticipacaoRepository;
import UEPB.ESWII.ClinicalStudyPB.service.CandidatoService;
import UEPB.ESWII.ClinicalStudyPB.service.ConflitoRegraException;
import UEPB.ESWII.ClinicalStudyPB.service.EstudoAcesso;
import UEPB.ESWII.ClinicalStudyPB.service.ParticipacaoService;
import UEPB.ESWII.ClinicalStudyPB.service.RecursoNaoEncontradoException;
import UEPB.ESWII.ClinicalStudyPB.service.UsuarioAutenticado;

/**
 * T60: regras da inclusão de participantes com repositórios simulados, uma pré-condição por vez e
 * o limite exato de vagas (a última vaga é aceita, a seguinte é recusada).
 */
class ParticipacaoServiceTest {

    private ParticipacaoRepository participacaoRepository;
    private AvaliacaoElegibilidadeRepository avaliacaoRepository;
    private ConsentimentoRepository consentimentoRepository;
    private CandidatoService candidatoService;
    private ParticipacaoService service;

    private Estudo estudo;
    private Candidato candidato;
    private Usuario coordenador;

    @BeforeEach
    void configurar() {
        participacaoRepository = mock(ParticipacaoRepository.class);
        avaliacaoRepository = mock(AvaliacaoElegibilidadeRepository.class);
        consentimentoRepository = mock(ConsentimentoRepository.class);
        candidatoService = mock(CandidatoService.class);
        UsuarioAutenticado usuarioAutenticado = mock(UsuarioAutenticado.class);
        EstudoAcesso estudoAcesso = spy(new EstudoAcesso(mock(EstudoRepository.class)));
        service = new ParticipacaoService(participacaoRepository, avaliacaoRepository, consentimentoRepository,
                candidatoService, estudoAcesso, usuarioAutenticado);

        estudo = new Estudo();
        estudo.setId(UUID.randomUUID());
        estudo.setStatus(StatusEstudo.RECRUTAMENTO);
        estudo.setLimiteParticipantes(2);

        candidato = new Candidato();
        candidato.setId(UUID.randomUUID());
        candidato.setCodigo("CAND-001");
        candidato.setNomeFicticio("J. S.");
        candidato.setEstudo(estudo);
        candidato.setSituacao(SituacaoCandidato.CONSENTIDO);

        coordenador = new Usuario();
        coordenador.setId(UUID.randomUUID());
        coordenador.setNome("Coordenador");

        doReturn(estudo).when(estudoAcesso).buscarComBloqueio(estudo.getId());
        when(candidatoService.buscarEntidade(candidato.getId())).thenReturn(candidato);
        when(usuarioAutenticado.buscar(coordenador.getId())).thenReturn(coordenador);
        when(participacaoRepository.countByEstudoId(estudo.getId())).thenReturn(0L);
        when(participacaoRepository.save(any(Participacao.class))).thenAnswer(i -> i.getArgument(0));
        aprovado(ResultadoAvaliacao.APROVADO);
        when(consentimentoRepository.existsByCandidatoId(candidato.getId())).thenReturn(true);
    }

    private void aprovado(ResultadoAvaliacao resultado) {
        AvaliacaoElegibilidade avaliacao = new AvaliacaoElegibilidade();
        avaliacao.setResultado(resultado);
        when(avaliacaoRepository.findByCandidatoIdAndVigenteTrue(candidato.getId())).thenReturn(Optional.of(avaliacao));
    }

    private ParticipacaoResponse incluir() {
        return service.incluir(estudo.getId(), candidato.getId(), coordenador.getId());
    }

    private void esperaConflito(String codigo) {
        assertThatThrownBy(this::incluir).isInstanceOf(ConflitoRegraException.class)
                .extracting("codigo").isEqualTo(codigo);
        verify(participacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Inclusão válida grava a participação com a data de hoje e muda a situação para INCLUIDO")
    void inclusaoValida() {
        ParticipacaoResponse resposta = incluir();

        assertThat(resposta.codigo()).isEqualTo("CAND-001");
        assertThat(resposta.incluidoPor()).isEqualTo("Coordenador");
        assertThat(resposta.dataInclusao()).isNotNull();
        assertThat(candidato.getSituacao()).isEqualTo(SituacaoCandidato.INCLUIDO);
    }

    @Test
    @DisplayName("Estudo fora de recrutamento é recusado")
    void estudoForaDeRecrutamento() {
        estudo.setStatus(StatusEstudo.EXECUCAO);

        esperaConflito("ESTUDO_FORA_DE_RECRUTAMENTO");
    }

    @Test
    @DisplayName("Candidato de outro estudo não é encontrado (404)")
    void candidatoDeOutroEstudo() {
        Estudo outro = new Estudo();
        outro.setId(UUID.randomUUID());
        candidato.setEstudo(outro);

        assertThatThrownBy(this::incluir).isInstanceOf(RecursoNaoEncontradoException.class);
        verify(participacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Candidato já incluído é recusado")
    void candidatoJaIncluido() {
        candidato.setSituacao(SituacaoCandidato.INCLUIDO);

        esperaConflito("CANDIDATO_JA_INCLUIDO");
    }

    @Test
    @DisplayName("Sem avaliação vigente ou com avaliação reprovada o candidato não é elegível")
    void candidatoNaoElegivel() {
        aprovado(ResultadoAvaliacao.REPROVADO);
        esperaConflito("CANDIDATO_NAO_ELEGIVEL");

        when(avaliacaoRepository.findByCandidatoIdAndVigenteTrue(candidato.getId())).thenReturn(Optional.empty());
        esperaConflito("CANDIDATO_NAO_ELEGIVEL");
    }

    @Test
    @DisplayName("Sem consentimento a inclusão é recusada")
    void semConsentimento() {
        when(consentimentoRepository.existsByCandidatoId(candidato.getId())).thenReturn(false);

        esperaConflito("CONSENTIMENTO_AUSENTE");
    }

    @Test
    @DisplayName("Limite exato: a última vaga é aceita e a seguinte é recusada")
    void limiteExatoDeVagas() {
        when(participacaoRepository.countByEstudoId(estudo.getId())).thenReturn(1L);
        assertThat(incluir().codigo()).isEqualTo("CAND-001");

        candidato.setSituacao(SituacaoCandidato.CONSENTIDO);
        clearInvocations(participacaoRepository);
        when(participacaoRepository.countByEstudoId(estudo.getId())).thenReturn(2L);
        esperaConflito("VAGAS_ESGOTADAS");
    }

    @Test
    @DisplayName("As verificações seguem a ordem: elegibilidade antes de consentimento antes de vagas")
    void ordemDasVerificacoes() {
        aprovado(ResultadoAvaliacao.REPROVADO);
        when(consentimentoRepository.existsByCandidatoId(candidato.getId())).thenReturn(false);
        when(participacaoRepository.countByEstudoId(estudo.getId())).thenReturn(2L);
        esperaConflito("CANDIDATO_NAO_ELEGIVEL");

        aprovado(ResultadoAvaliacao.APROVADO);
        esperaConflito("CONSENTIMENTO_AUSENTE");

        when(consentimentoRepository.existsByCandidatoId(candidato.getId())).thenReturn(true);
        esperaConflito("VAGAS_ESGOTADAS");
    }
}
