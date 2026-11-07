package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.ConsentimentoRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.ConsentimentoResponse;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.PerfilUsuario;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoCandidato;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Candidato;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Consentimento;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.repository.ConsentimentoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.EstudoRepository;
import UEPB.ESWII.ClinicalStudyPB.service.CandidatoService;
import UEPB.ESWII.ClinicalStudyPB.service.ConflitoRegraException;
import UEPB.ESWII.ClinicalStudyPB.service.ConsentimentoService;
import UEPB.ESWII.ClinicalStudyPB.service.EstudoAcesso;
import UEPB.ESWII.ClinicalStudyPB.service.RecursoNaoEncontradoException;
import UEPB.ESWII.ClinicalStudyPB.service.RegraNegocioException;
import UEPB.ESWII.ClinicalStudyPB.service.UsuarioAutenticado;

/**
 * T59 (RF10): regras do serviço de consentimento simulado, com repositórios simulados. O fluxo completo com
 * banco e autenticação está em {@code ConsentimentoEndpointTest} e {@code RecrutamentoIntegrationTest}.
 */
class ConsentimentoServiceTest {

    private static final String VERSAO_PADRAO = "TCLE-SIMULADO v1";

    private ConsentimentoRepository consentimentoRepository;
    private CandidatoService candidatoService;
    private ConsentimentoService service;
    private Estudo estudo;
    private Candidato candidato;
    private Usuario coordenador;

    @BeforeEach
    void configurar() {
        consentimentoRepository = mock(ConsentimentoRepository.class);
        candidatoService = mock(CandidatoService.class);
        UsuarioAutenticado usuarioAutenticado = mock(UsuarioAutenticado.class);
        service = new ConsentimentoService(consentimentoRepository, candidatoService,
                new EstudoAcesso(mock(EstudoRepository.class)), usuarioAutenticado, VERSAO_PADRAO);

        estudo = new Estudo();
        estudo.setId(UUID.randomUUID());
        estudo.setStatus(StatusEstudo.RECRUTAMENTO);

        candidato = new Candidato();
        candidato.setId(UUID.randomUUID());
        candidato.setEstudo(estudo);
        candidato.setSituacao(SituacaoCandidato.CADASTRADO);
        when(candidatoService.buscarEntidade(candidato.getId())).thenReturn(candidato);

        coordenador = new Usuario();
        coordenador.setId(UUID.randomUUID());
        coordenador.setNome("Coordenadora Fictícia");
        coordenador.setPerfil(PerfilUsuario.COORDENADOR);
        when(usuarioAutenticado.buscar(coordenador.getId())).thenReturn(coordenador);
    }

    private Consentimento gravado() {
        ArgumentCaptor<Consentimento> captor = ArgumentCaptor.forClass(Consentimento.class);
        verify(consentimentoRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("Sem dados no corpo, registra agora, com a versão padrão do termo e o usuário autenticado como responsável")
    void registraComPadroes() {
        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);

        ConsentimentoResponse resposta = service.registrar(candidato.getId(), new ConsentimentoRequest(null, null),
                coordenador.getId());

        Consentimento salvo = gravado();
        assertThat(salvo.getCandidato()).isSameAs(candidato);
        assertThat(salvo.getResponsavel()).isSameAs(coordenador);
        assertThat(salvo.getVersaoTermo()).isEqualTo(VERSAO_PADRAO);
        assertThat(salvo.getDataHora()).isBetween(antes, LocalDateTime.now().plusSeconds(1));
        assertThat(resposta.responsavel()).isEqualTo("Coordenadora Fictícia");
        assertThat(resposta.versaoTermo()).isEqualTo(VERSAO_PADRAO);
    }

    @Test
    @DisplayName("Aceita data e hora no passado e uma versão própria do termo, aparando espaços")
    void aceitaDataPassadaEVersaoPropria() {
        LocalDateTime ontem = LocalDateTime.now().minusDays(1).withNano(0);

        service.registrar(candidato.getId(), new ConsentimentoRequest(ontem, "  TCLE-SIMULADO v2  "),
                coordenador.getId());

        Consentimento salvo = gravado();
        assertThat(salvo.getDataHora()).isEqualTo(ontem);
        assertThat(salvo.getVersaoTermo()).isEqualTo("TCLE-SIMULADO v2");
    }

    @Test
    @DisplayName("Versão em branco usa a versão padrão")
    void versaoEmBrancoUsaAPadrao() {
        service.registrar(candidato.getId(), new ConsentimentoRequest(null, "   "), coordenador.getId());

        assertThat(gravado().getVersaoTermo()).isEqualTo(VERSAO_PADRAO);
    }

    @Test
    @DisplayName("Data e hora futuras são recusadas (400) e nada é gravado")
    void dataFuturaRecusada() {
        LocalDateTime amanha = LocalDateTime.now().plusDays(1);

        assertThatThrownBy(() -> service.registrar(candidato.getId(), new ConsentimentoRequest(amanha, null),
                coordenador.getId())).isInstanceOf(RegraNegocioException.class);
        verify(consentimentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Segundo consentimento do mesmo candidato é conflito (409 CONSENTIMENTO_DUPLICADO)")
    void duplicidade() {
        when(consentimentoRepository.existsByCandidatoId(candidato.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.registrar(candidato.getId(), new ConsentimentoRequest(null, null),
                coordenador.getId())).isInstanceOfSatisfying(ConflitoRegraException.class,
                        erro -> assertThat(erro.getCodigo()).isEqualTo("CONSENTIMENTO_DUPLICADO"));
        verify(consentimentoRepository, never()).save(any());
    }

    @ParameterizedTest(name = "estudo em {0}")
    @EnumSource(value = StatusEstudo.class, names = "RECRUTAMENTO", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("Fora de recrutamento é conflito (409 ESTUDO_FORA_DE_RECRUTAMENTO), em qualquer outro estado")
    void foraDeRecrutamento(StatusEstudo estado) {
        estudo.setStatus(estado);

        assertThatThrownBy(() -> service.registrar(candidato.getId(), new ConsentimentoRequest(null, null),
                coordenador.getId())).isInstanceOfSatisfying(ConflitoRegraException.class,
                        erro -> assertThat(erro.getCodigo()).isEqualTo("ESTUDO_FORA_DE_RECRUTAMENTO"));
        verify(consentimentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Candidato já incluído não recebe consentimento (409 CANDIDATO_JA_INCLUIDO)")
    void candidatoIncluido() {
        candidato.setSituacao(SituacaoCandidato.INCLUIDO);

        assertThatThrownBy(() -> service.registrar(candidato.getId(), new ConsentimentoRequest(null, null),
                coordenador.getId())).isInstanceOfSatisfying(ConflitoRegraException.class,
                        erro -> assertThat(erro.getCodigo()).isEqualTo("CANDIDATO_JA_INCLUIDO"));
        verify(consentimentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Candidato elegível passa a consentido; os demais mantêm a situação")
    void situacaoDoCandidato() {
        candidato.setSituacao(SituacaoCandidato.ELEGIVEL);
        service.registrar(candidato.getId(), new ConsentimentoRequest(null, null), coordenador.getId());
        assertThat(candidato.getSituacao()).isEqualTo(SituacaoCandidato.CONSENTIDO);
    }

    @ParameterizedTest(name = "situação {0}")
    @EnumSource(value = SituacaoCandidato.class, names = { "CADASTRADO", "EM_AVALIACAO", "NAO_ELEGIVEL" })
    @DisplayName("O consentimento independe da avaliação: quem ainda não é elegível mantém a situação")
    void consentimentoAntesDaAvaliacao(SituacaoCandidato situacao) {
        candidato.setSituacao(situacao);

        service.registrar(candidato.getId(), new ConsentimentoRequest(null, null), coordenador.getId());

        assertThat(candidato.getSituacao()).isEqualTo(situacao);
        verify(consentimentoRepository).save(any(Consentimento.class));
    }

    @Test
    @DisplayName("Candidato inexistente: recurso não encontrado")
    void candidatoInexistente() {
        UUID inexistente = UUID.randomUUID();
        when(candidatoService.buscarEntidade(inexistente)).thenThrow(new RecursoNaoEncontradoException("Candidato"));

        assertThatThrownBy(() -> service.registrar(inexistente, new ConsentimentoRequest(null, null),
                coordenador.getId())).isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> service.buscar(inexistente)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Buscar sem consentimento registrado: recurso não encontrado; com registro, devolve o comprovante")
    void buscar() {
        assertThatThrownBy(() -> service.buscar(candidato.getId())).isInstanceOf(RecursoNaoEncontradoException.class);

        Consentimento registrado = new Consentimento();
        registrado.setId(UUID.randomUUID());
        registrado.setCandidato(candidato);
        registrado.setResponsavel(coordenador);
        registrado.setDataHora(LocalDateTime.of(2026, 11, 3, 9, 30));
        registrado.setVersaoTermo(VERSAO_PADRAO);
        when(consentimentoRepository.findByCandidatoId(candidato.getId())).thenReturn(Optional.of(registrado));

        ConsentimentoResponse comprovante = service.buscar(candidato.getId());

        assertThat(comprovante.candidatoId()).isEqualTo(candidato.getId());
        assertThat(comprovante.dataHora()).isEqualTo(LocalDateTime.of(2026, 11, 3, 9, 30));
        assertThat(comprovante.responsavel()).isEqualTo("Coordenadora Fictícia");
        assertThat(comprovante.versaoTermo()).isEqualTo(VERSAO_PADRAO);
    }
}
