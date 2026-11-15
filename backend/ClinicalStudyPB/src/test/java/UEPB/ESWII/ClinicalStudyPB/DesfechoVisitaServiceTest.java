package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.dao.OptimisticLockingFailureException;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.TipoDesfecho;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaAgendadaRepository;
import UEPB.ESWII.ClinicalStudyPB.service.ConflitoRegraException;
import UEPB.ESWII.ClinicalStudyPB.service.DesfechoVisitaService;
import UEPB.ESWII.ClinicalStudyPB.service.DesfechoVisitaService.ComandoDesfecho;
import UEPB.ESWII.ClinicalStudyPB.service.RecursoNaoEncontradoException;
import UEPB.ESWII.ClinicalStudyPB.service.UsuarioAutenticado;
import UEPB.ESWII.ClinicalStudyPB.service.ValidacaoRegraException;

/**
 * T81 (RF13): regras do serviço de desfecho com repositório simulado e {@code Clock} fixo em
 * 16/11/2026 10:00 (fuso do centro). A matriz completa, os endpoints e a concorrência real são da T85.
 */
class DesfechoVisitaServiceTest {

    private static final ZoneId FUSO = ZoneId.of("America/Fortaleza");
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 16, 10, 0);

    private VisitaAgendadaRepository repositorio;
    private DesfechoVisitaService service;
    private Estudo estudo;
    private VisitaAgendada visita;
    private Usuario coordenador;
    private UUID visitaId;

    @BeforeEach
    void configurar() {
        repositorio = mock(VisitaAgendadaRepository.class);
        UsuarioAutenticado usuarioAutenticado = mock(UsuarioAutenticado.class);
        Clock relogio = Clock.fixed(AGORA.atZone(FUSO).toInstant(), FUSO);
        service = new DesfechoVisitaService(repositorio, usuarioAutenticado, relogio);

        estudo = new Estudo();
        estudo.setId(UUID.randomUUID());
        estudo.setStatus(StatusEstudo.EXECUCAO);
        Participacao participacao = new Participacao();
        participacao.setId(UUID.randomUUID());
        participacao.setEstudo(estudo);

        coordenador = new Usuario();
        coordenador.setId(UUID.randomUUID());

        visitaId = UUID.randomUUID();
        visita = new VisitaAgendada();
        visita.setId(visitaId);
        visita.setParticipacao(participacao);
        visita.setSituacao(SituacaoVisita.AGENDADA);
        visita.setDataHoraAgendada(AGORA.minusDays(1));
        visita.setCriadoPor(coordenador);
        visita.setAlteradoPor(coordenador);
        visita.setCriadoEm(AGORA.minusDays(5));
        visita.setAlteradoEm(AGORA.minusDays(5));

        when(repositorio.buscarComBloqueio(visitaId)).thenReturn(Optional.of(visita));
        when(repositorio.saveAndFlush(any(VisitaAgendada.class))).thenAnswer(i -> i.getArgument(0));
        when(usuarioAutenticado.buscar(coordenador.getId())).thenReturn(coordenador);
    }

    private VisitaAgendada registrar(ComandoDesfecho comando) {
        return service.registrar(visitaId, comando, coordenador.getId());
    }

    private static ComandoDesfecho comando(TipoDesfecho tipo, LocalDateTime dataHora, String motivo, String obs) {
        return new ComandoDesfecho(tipo, dataHora, motivo, obs);
    }

    private static String codigoDe(Throwable erro) {
        return erro instanceof ConflitoRegraException c ? c.getCodigo()
                : ((ValidacaoRegraException) erro).getCodigo();
    }

    // ---------------------------------------------------------------- gravação por tipo

    @Test
    @DisplayName("Realizada grava situação, data e hora informadas, observação aparada, autor e data de alteração")
    void realizadaGravaOsCampos() {
        LocalDateTime real = AGORA.minusHours(1);

        VisitaAgendada resultado = registrar(comando(TipoDesfecho.REALIZADA, real, "ignorado", "  Tudo certo "));

        assertThat(resultado.getSituacao()).isEqualTo(SituacaoVisita.REALIZADA);
        assertThat(resultado.getDataHoraRealizada()).isEqualTo(real);
        assertThat(resultado.getMotivo()).isNull();
        assertThat(resultado.getObservacao()).isEqualTo("Tudo certo");
        assertThat(resultado.getAlteradoPor()).isSameAs(coordenador);
        assertThat(resultado.getAlteradoEm()).isEqualTo(AGORA);
        assertThat(resultado.getCriadoEm()).isEqualTo(AGORA.minusDays(5));
        verify(repositorio).saveAndFlush(visita);
    }

    @Test
    @DisplayName("Realizada sem data informada assume o instante do relógio")
    void realizadaSemDataAssumeAgora() {
        assertThat(registrar(comando(TipoDesfecho.REALIZADA, null, null, null)).getDataHoraRealizada())
                .isEqualTo(AGORA);
    }

    @Test
    @DisplayName("Ausente não guarda data real nem motivo; guarda a observação")
    void ausenteGravaSoAObservacao() {
        VisitaAgendada resultado = registrar(comando(TipoDesfecho.AUSENTE, AGORA, "ignorado", "Não atendeu"));

        assertThat(resultado.getSituacao()).isEqualTo(SituacaoVisita.AUSENTE);
        assertThat(resultado.getDataHoraRealizada()).isNull();
        assertThat(resultado.getMotivo()).isNull();
        assertThat(resultado.getObservacao()).isEqualTo("Não atendeu");
    }

    @Test
    @DisplayName("Cancelada grava o motivo aparado e não guarda data real")
    void canceladaGravaOMotivo() {
        VisitaAgendada resultado = registrar(comando(TipoDesfecho.CANCELADA, AGORA, "  Participante em viagem ", "  "));

        assertThat(resultado.getSituacao()).isEqualTo(SituacaoVisita.CANCELADA);
        assertThat(resultado.getMotivo()).isEqualTo("Participante em viagem");
        assertThat(resultado.getObservacao()).isNull();
        assertThat(resultado.getDataHoraRealizada()).isNull();
    }

    // ---------------------------------------------------------------- verificações, na ordem

    @Test
    @DisplayName("Visita inexistente: 404, sem gravar")
    void visitaInexistente() {
        UUID outra = UUID.randomUUID();
        when(repositorio.buscarComBloqueio(outra)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrar(outra, comando(TipoDesfecho.AUSENTE, null, null, null),
                coordenador.getId())).isInstanceOf(RecursoNaoEncontradoException.class);
        verify(repositorio, never()).saveAndFlush(any());
    }

    @ParameterizedTest(name = "Estudo em {0} recusa o desfecho")
    @EnumSource(value = StatusEstudo.class, names = {"PLANEJAMENTO", "ENCERRADO"})
    @DisplayName("Estudo fora de recrutamento ou execução: 409 ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO")
    void estudoForaDoEstado(StatusEstudo status) {
        estudo.setStatus(status);

        assertThatThrownBy(() -> registrar(comando(TipoDesfecho.CANCELADA, null, "Motivo válido", null)))
                .isInstanceOfSatisfying(ConflitoRegraException.class,
                        e -> assertThat(e.getCodigo()).isEqualTo("ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO"));
        verify(repositorio, never()).saveAndFlush(any());
    }

    @ParameterizedTest(name = "Estudo em {0} aceita o desfecho")
    @EnumSource(value = StatusEstudo.class, names = {"RECRUTAMENTO", "EXECUCAO"})
    @DisplayName("Estudo em recrutamento ou execução aceita o desfecho")
    void estudoNoEstadoCerto(StatusEstudo status) {
        estudo.setStatus(status);

        assertThat(registrar(comando(TipoDesfecho.AUSENTE, null, null, null)).getSituacao())
                .isEqualTo(SituacaoVisita.AUSENTE);
    }

    @ParameterizedTest(name = "Visita {0} não recebe outro desfecho")
    @EnumSource(value = SituacaoVisita.class, names = {"REALIZADA", "AUSENTE", "CANCELADA"})
    @DisplayName("Visita já finalizada: 409 TRANSICAO_VISITA_INVALIDA, sem gravar")
    void visitaFinalizadaRecusa(SituacaoVisita situacao) {
        visita.setSituacao(situacao);

        assertThatThrownBy(() -> registrar(comando(TipoDesfecho.AUSENTE, null, null, null)))
                .isInstanceOfSatisfying(ConflitoRegraException.class,
                        e -> assertThat(e.getCodigo()).isEqualTo("TRANSICAO_VISITA_INVALIDA"));
        verify(repositorio, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Data real futura: 400 DATA_REALIZACAO_FUTURA")
    void dataRealFutura() {
        assertThatThrownBy(() -> registrar(comando(TipoDesfecho.REALIZADA, AGORA.plusMinutes(1), null, null)))
                .isInstanceOfSatisfying(ValidacaoRegraException.class,
                        e -> assertThat(e.getCodigo()).isEqualTo("DATA_REALIZACAO_FUTURA"));
        verify(repositorio, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Cancelada com motivo de 4 caracteres: 400 MOTIVO_OBRIGATORIO")
    void canceladaSemMotivo() {
        assertThatThrownBy(() -> registrar(comando(TipoDesfecho.CANCELADA, null, " abcd ", null)))
                .isInstanceOfSatisfying(ValidacaoRegraException.class,
                        e -> assertThat(e.getCodigo()).isEqualTo("MOTIVO_OBRIGATORIO"));
        verify(repositorio, never()).saveAndFlush(any());
    }

    @ParameterizedTest(name = "Visita agendada para amanhã: {0} é recusada")
    @EnumSource(value = TipoDesfecho.class, names = {"REALIZADA", "AUSENTE"})
    @DisplayName("Realizada e ausente com data agendada futura: 409 VISITA_AINDA_NAO_OCORREU")
    void dataAgendadaFutura(TipoDesfecho tipo) {
        visita.setDataHoraAgendada(AGORA.plusDays(1));

        assertThatThrownBy(() -> registrar(comando(tipo, null, null, null)))
                .isInstanceOfSatisfying(ConflitoRegraException.class,
                        e -> assertThat(e.getCodigo()).isEqualTo("VISITA_AINDA_NAO_OCORREU"));
        verify(repositorio, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Cancelar vale a qualquer momento, inclusive com a data agendada no futuro")
    void canceladaComDataFutura() {
        visita.setDataHoraAgendada(AGORA.plusDays(30));

        assertThat(registrar(comando(TipoDesfecho.CANCELADA, null, "Participante em viagem", null)).getSituacao())
                .isEqualTo(SituacaoVisita.CANCELADA);
    }

    @ParameterizedTest(name = "Visita de hoje, depois do horário marcado: {0} é aceita")
    @EnumSource(TipoDesfecho.class)
    @DisplayName("Data agendada de hoje aceita os três desfechos, mesmo antes do horário marcado")
    void dataAgendadaDeHoje(TipoDesfecho tipo) {
        visita.setDataHoraAgendada(AGORA.withHour(18));

        assertThat(registrar(comando(tipo, null, "Participante em viagem", null)).getSituacao())
                .isEqualTo(MaquinaEstadosVisitaAcesso.destino(tipo));
    }

    @Test
    @DisplayName("Ordem: estudo encerrado vence a transição inválida")
    void estudoVenceATransicao() {
        estudo.setStatus(StatusEstudo.ENCERRADO);
        visita.setSituacao(SituacaoVisita.REALIZADA);

        assertThatThrownBy(() -> registrar(comando(TipoDesfecho.AUSENTE, null, null, null)))
                .satisfies(e -> assertThat(codigoDe(e)).isEqualTo("ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO"));
    }

    @Test
    @DisplayName("Ordem: transição inválida vence a data futura")
    void transicaoVenceADataFutura() {
        visita.setSituacao(SituacaoVisita.AUSENTE);

        assertThatThrownBy(() -> registrar(comando(TipoDesfecho.REALIZADA, AGORA.plusDays(1), null, null)))
                .satisfies(e -> assertThat(codigoDe(e)).isEqualTo("TRANSICAO_VISITA_INVALIDA"));
    }

    // ---------------------------------------------------------------- concorrência

    @Test
    @DisplayName("Versão otimista violada no saveAndFlush vira 409 VISITA_ALTERADA_CONCORRENTEMENTE")
    void versaoViolada() {
        when(repositorio.saveAndFlush(any(VisitaAgendada.class)))
                .thenThrow(new OptimisticLockingFailureException("versão divergente"));

        assertThatThrownBy(() -> registrar(comando(TipoDesfecho.AUSENTE, null, null, null)))
                .isInstanceOfSatisfying(ConflitoRegraException.class,
                        e -> assertThat(e.getCodigo()).isEqualTo("VISITA_ALTERADA_CONCORRENTEMENTE"));
    }

    @Test
    @DisplayName("A visita é carregada com bloqueio de escrita, nunca por findById simples")
    void carregaComBloqueio() {
        registrar(comando(TipoDesfecho.AUSENTE, null, null, null));

        verify(repositorio).buscarComBloqueio(visitaId);
        verify(repositorio, never()).findById(any());
    }

    /** Evita importar a máquina só para o destino esperado do teste parametrizado. */
    private static final class MaquinaEstadosVisitaAcesso {
        static SituacaoVisita destino(TipoDesfecho tipo) {
            return SituacaoVisita.valueOf(tipo.name());
        }
    }
}
