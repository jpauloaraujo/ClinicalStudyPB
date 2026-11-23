package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoParticipacao;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SolicitanteRetirada;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.RetiradaParticipacao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;
import UEPB.ESWII.ClinicalStudyPB.repository.ParticipacaoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.RetiradaParticipacaoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaAgendadaRepository;
import UEPB.ESWII.ClinicalStudyPB.service.ConflitoRegraException;
import UEPB.ESWII.ClinicalStudyPB.service.RecursoNaoEncontradoException;
import UEPB.ESWII.ClinicalStudyPB.service.RetiradaService;
import UEPB.ESWII.ClinicalStudyPB.service.RetiradaService.ComandoRetirada;
import UEPB.ESWII.ClinicalStudyPB.service.RetiradaService.ResultadoRetirada;
import UEPB.ESWII.ClinicalStudyPB.service.UsuarioAutenticado;
import UEPB.ESWII.ClinicalStudyPB.service.ValidacaoRegraException;

/**
 * T102 (RF16): regras de domínio da retirada de participante com repositórios simulados e {@code Clock} fixo.
 */
class RetiradaServiceTest {

    private static final ZoneId FUSO = ZoneId.of("America/Fortaleza");
    private static final LocalDate HOJE = LocalDate.of(2026, 11, 18);
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 18, 14, 30);

    private ParticipacaoRepository participacoes;
    private RetiradaParticipacaoRepository retiradas;
    private VisitaAgendadaRepository visitasAgendadas;
    private UsuarioAutenticado usuarioAutenticado;
    private RetiradaService service;

    private Estudo estudo;
    private Participacao participacao;
    private Usuario coordenador;
    private UUID participacaoId;

    @BeforeEach
    void preparar() {
        participacoes = mock(ParticipacaoRepository.class);
        retiradas = mock(RetiradaParticipacaoRepository.class);
        visitasAgendadas = mock(VisitaAgendadaRepository.class);
        usuarioAutenticado = mock(UsuarioAutenticado.class);
        Clock relogio = Clock.fixed(AGORA.atZone(FUSO).toInstant(), FUSO);
        service = new RetiradaService(participacoes, retiradas, visitasAgendadas, usuarioAutenticado, relogio);

        estudo = new Estudo();
        estudo.setId(UUID.randomUUID());
        estudo.setStatus(StatusEstudo.EXECUCAO);

        participacaoId = UUID.randomUUID();
        participacao = new Participacao();
        participacao.setId(participacaoId);
        participacao.setEstudo(estudo);
        participacao.setDataInclusao(HOJE.minusDays(20));

        coordenador = new Usuario();
        coordenador.setId(UUID.randomUUID());
        coordenador.setNome("Coordenador Demo (Ficticio)");

        when(participacoes.buscarComBloqueio(participacaoId)).thenReturn(Optional.of(participacao));
        when(participacoes.existsById(participacaoId)).thenReturn(true);
        when(participacoes.saveAndFlush(any(Participacao.class))).thenAnswer(i -> i.getArgument(0));
        when(retiradas.saveAndFlush(any(RetiradaParticipacao.class))).thenAnswer(i -> i.getArgument(0));
        when(visitasAgendadas.listarFuturasPorSituacao(participacaoId, SituacaoVisita.AGENDADA, AGORA))
                .thenReturn(List.of());
        when(visitasAgendadas.saveAndFlush(any(VisitaAgendada.class))).thenAnswer(i -> i.getArgument(0));
        when(usuarioAutenticado.buscar(coordenador.getId())).thenReturn(coordenador);
    }

    private ComandoRetirada comandoValido() {
        return new ComandoRetirada("Mudança de cidade", SolicitanteRetirada.PARTICIPANTE, HOJE);
    }

    private VisitaAgendada visitaAgendada(SituacaoVisita situacao, LocalDateTime dataHora) {
        VisitaAgendada v = new VisitaAgendada();
        v.setId(UUID.randomUUID());
        v.setParticipacao(participacao);
        v.setSituacao(situacao);
        v.setDataHoraAgendada(dataHora);
        v.setCriadoPor(coordenador);
        v.setAlteradoPor(coordenador);
        v.setCriadoEm(AGORA.minusDays(5));
        v.setAlteradoEm(AGORA.minusDays(5));
        return v;
    }

    // ---------------------------------------------------------------- registro e cascata

    @ParameterizedTest(name = "Estudo em {0} aceita a retirada")
    @EnumSource(value = StatusEstudo.class, names = {"RECRUTAMENTO", "EXECUCAO"})
    @DisplayName("Registra retirada com motivo aparado, solicitante, data, autor, marca RETIRADO e usa bloqueio")
    void registraRetiradaComSucesso(StatusEstudo status) {
        estudo.setStatus(status);
        ComandoRetirada comando = new ComandoRetirada("  Mudança de cidade  ", SolicitanteRetirada.PARTICIPANTE,
                HOJE.minusDays(1));

        ResultadoRetirada resultado = service.registrar(participacaoId, comando, coordenador.getId());

        assertThat(participacao.getSituacao()).isEqualTo(SituacaoParticipacao.RETIRADO);
        assertThat(participacao.estaRetirada()).isTrue();
        assertThat(resultado.visitasCanceladas()).isZero();
        RetiradaParticipacao salva = resultado.retirada();
        assertThat(salva.getParticipacao()).isSameAs(participacao);
        assertThat(salva.getMotivo()).isEqualTo("Mudança de cidade");
        assertThat(salva.getSolicitante()).isEqualTo(SolicitanteRetirada.PARTICIPANTE);
        assertThat(salva.getDataRetirada()).isEqualTo(HOJE.minusDays(1));
        assertThat(salva.getRegistradoPor()).isSameAs(coordenador);
        assertThat(salva.getRegistradoEm()).isEqualTo(AGORA);
        verify(participacoes).buscarComBloqueio(participacaoId);
        verify(participacoes, never()).findById(any());
        verify(retiradas).saveAndFlush(any(RetiradaParticipacao.class));
        verify(participacoes).saveAndFlush(participacao);
    }

    @Test
    @DisplayName("Cancela visitas futuras AGENDADA com motivo 'retirada do estudo' e atualiza auditoria")
    void cancelaVisitasFuturasEmCascata() {
        VisitaAgendada futura1 = visitaAgendada(SituacaoVisita.AGENDADA, AGORA.plusDays(3));
        VisitaAgendada futura2 = visitaAgendada(SituacaoVisita.AGENDADA, AGORA.plusDays(15));
        futura2.setObservacao("Observação prévia");
        when(visitasAgendadas.listarFuturasPorSituacao(participacaoId, SituacaoVisita.AGENDADA, AGORA))
                .thenReturn(List.of(futura1, futura2));

        ResultadoRetirada resultado = service.registrar(participacaoId, comandoValido(), coordenador.getId());

        assertThat(resultado.visitasCanceladas()).isEqualTo(2);
        for (VisitaAgendada v : List.of(futura1, futura2)) {
            assertThat(v.getSituacao()).isEqualTo(SituacaoVisita.CANCELADA);
            assertThat(v.getMotivo()).isEqualTo(RetiradaService.MOTIVO_CANCELAMENTO_RETIRADA);
            assertThat(v.getDataHoraRealizada()).isNull();
            assertThat(v.getAlteradoPor()).isSameAs(coordenador);
            assertThat(v.getAlteradoEm()).isEqualTo(AGORA);
            verify(visitasAgendadas).saveAndFlush(v);
        }
        assertThat(futura2.getObservacao()).isEqualTo("Observação prévia");
    }

    @Test
    @DisplayName("Consulta apenas visitas futuras AGENDADA (> agora), preservando vencidas e finalizadas")
    void consultaSomenteFuturasAgendadas() {
        service.registrar(participacaoId, comandoValido(), coordenador.getId());

        verify(visitasAgendadas).listarFuturasPorSituacao(participacaoId, SituacaoVisita.AGENDADA, AGORA);
        verify(visitasAgendadas, never()).saveAndFlush(any());
    }

    // ---------------------------------------------------------------- pré-condições e ordem

    @Test
    @DisplayName("Participação inexistente: 404 sem gravar")
    void participacaoInexistente() {
        UUID inexistente = UUID.randomUUID();
        when(participacoes.buscarComBloqueio(inexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrar(inexistente, comandoValido(), coordenador.getId()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(retiradas, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Segunda retirada do mesmo participante: 409 PARTICIPANTE_JA_RETIRADO (irreversível)")
    void segundaRetiradaRecusada() {
        participacao.retirar();

        assertThatThrownBy(() -> service.registrar(participacaoId, comandoValido(), coordenador.getId()))
                .isInstanceOfSatisfying(ConflitoRegraException.class,
                        e -> assertThat(e.getCodigo()).isEqualTo(RetiradaService.PARTICIPANTE_JA_RETIRADO));
        verify(retiradas, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Estudo encerrado: 409 ESTUDO_ENCERRADO")
    void estudoEncerradoRecusado() {
        estudo.setStatus(StatusEstudo.ENCERRADO);

        assertThatThrownBy(() -> service.registrar(participacaoId, comandoValido(), coordenador.getId()))
                .isInstanceOfSatisfying(ConflitoRegraException.class,
                        e -> assertThat(e.getCodigo()).isEqualTo(RetiradaService.ESTUDO_ENCERRADO));
        verify(retiradas, never()).saveAndFlush(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "abcd", "  ab  "})
    @DisplayName("Motivo vazio ou com menos de 5 caracteres após trim: 400 MOTIVO_OBRIGATORIO")
    void motivoCurtoRecusado(String motivo) {
        ComandoRetirada comando = new ComandoRetirada(motivo, SolicitanteRetirada.COORDENACAO, HOJE);

        assertThatThrownBy(() -> service.registrar(participacaoId, comando, coordenador.getId()))
                .isInstanceOfSatisfying(ValidacaoRegraException.class,
                        e -> assertThat(e.getCodigo()).isEqualTo(RetiradaService.MOTIVO_OBRIGATORIO));
        verify(retiradas, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Motivo nulo ou comando nulo: 400 MOTIVO_OBRIGATORIO; 5 caracteres aceito")
    void motivoNuloECincoCaracteres() {
        assertThatThrownBy(() -> service.registrar(participacaoId,
                new ComandoRetirada(null, SolicitanteRetirada.PARTICIPANTE, HOJE), coordenador.getId()))
                .isInstanceOfSatisfying(ValidacaoRegraException.class,
                        e -> assertThat(e.getCodigo()).isEqualTo(RetiradaService.MOTIVO_OBRIGATORIO));

        assertThatThrownBy(() -> service.registrar(participacaoId, null, coordenador.getId()))
                .isInstanceOfSatisfying(ValidacaoRegraException.class,
                        e -> assertThat(e.getCodigo()).isEqualTo(RetiradaService.MOTIVO_OBRIGATORIO));

        ResultadoRetirada ok = service.registrar(participacaoId,
                new ComandoRetirada("12345", SolicitanteRetirada.PESQUISADOR, HOJE), coordenador.getId());
        assertThat(ok.retirada().getMotivo()).isEqualTo("12345");
    }

    @Test
    @DisplayName("Data futura: 400 DATA_RETIRADA_FUTURA; data nula ou solicitante nulo: 400 VALIDACAO_CAMPOS")
    void dataFuturaECamposNulos() {
        assertThatThrownBy(() -> service.registrar(participacaoId,
                new ComandoRetirada("Mudança de cidade", SolicitanteRetirada.PARTICIPANTE, HOJE.plusDays(1)),
                coordenador.getId()))
                .isInstanceOfSatisfying(ValidacaoRegraException.class,
                        e -> assertThat(e.getCodigo()).isEqualTo(RetiradaService.DATA_RETIRADA_FUTURA));

        assertThatThrownBy(() -> service.registrar(participacaoId,
                new ComandoRetirada("Mudança de cidade", SolicitanteRetirada.PARTICIPANTE, null),
                coordenador.getId()))
                .isInstanceOfSatisfying(ValidacaoRegraException.class,
                        e -> assertThat(e.getCodigo()).isEqualTo("VALIDACAO_CAMPOS"));

        assertThatThrownBy(() -> service.registrar(participacaoId,
                new ComandoRetirada("Mudança de cidade", null, HOJE),
                coordenador.getId()))
                .isInstanceOfSatisfying(ValidacaoRegraException.class,
                        e -> assertThat(e.getCodigo()).isEqualTo("VALIDACAO_CAMPOS"));
        verify(retiradas, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Ordem das verificações: já retirado vence estudo encerrado; estudo encerrado vence motivo ausente")
    void ordemDasVerificacoes() {
        estudo.setStatus(StatusEstudo.ENCERRADO);
        ComandoRetirada invalido = new ComandoRetirada("", null, HOJE.plusDays(5));

        assertThatThrownBy(() -> service.registrar(participacaoId, invalido, coordenador.getId()))
                .isInstanceOfSatisfying(ConflitoRegraException.class,
                        e -> assertThat(e.getCodigo()).isEqualTo(RetiradaService.ESTUDO_ENCERRADO));

        participacao.retirar();
        assertThatThrownBy(() -> service.registrar(participacaoId, invalido, coordenador.getId()))
                .isInstanceOfSatisfying(ConflitoRegraException.class,
                        e -> assertThat(e.getCodigo()).isEqualTo(RetiradaService.PARTICIPANTE_JA_RETIRADO));
    }

    @Test
    @DisplayName("Não existe operação de desfazer retirada nem reativar participação no serviço")
    void semOperacaoDeDesfazer() {
        assertThat(Arrays.stream(RetiradaService.class.getDeclaredMethods()).map(Method::getName))
                .noneMatch(nome -> nome.toLowerCase().contains("desfazer")
                        || nome.toLowerCase().contains("reativar")
                        || nome.toLowerCase().contains("cancelarretirada"));
    }

    // ---------------------------------------------------------------- consulta

    @Test
    @DisplayName("buscar devolve a retirada e conta visitas canceladas pelo motivo 'retirada do estudo'")
    void buscarRetiradaExistente() {
        RetiradaParticipacao existente = new RetiradaParticipacao();
        existente.setId(UUID.randomUUID());
        existente.setParticipacao(participacao);
        existente.setMotivo("Mudança de cidade");
        existente.setSolicitante(SolicitanteRetirada.PARTICIPANTE);
        existente.setDataRetirada(HOJE);
        existente.setRegistradoPor(coordenador);
        existente.setRegistradoEm(AGORA);
        when(retiradas.findByParticipacaoId(participacaoId)).thenReturn(Optional.of(existente));
        when(visitasAgendadas.countByParticipacaoIdAndSituacaoAndMotivo(
                participacaoId, SituacaoVisita.CANCELADA, RetiradaService.MOTIVO_CANCELAMENTO_RETIRADA))
                .thenReturn(2L);

        ResultadoRetirada consulta = service.buscar(participacaoId);

        assertThat(consulta.retirada()).isSameAs(existente);
        assertThat(consulta.visitasCanceladas()).isEqualTo(2);
    }

    @Test
    @DisplayName("buscar devolve 404 para participação inexistente ou ainda ativa")
    void buscarInexistenteOuAtiva() {
        UUID inexistente = UUID.randomUUID();
        when(participacoes.existsById(inexistente)).thenReturn(false);
        assertThatThrownBy(() -> service.buscar(inexistente)).isInstanceOf(RecursoNaoEncontradoException.class);

        when(retiradas.findByParticipacaoId(participacaoId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscar(participacaoId)).isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
