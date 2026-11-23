package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.dao.DataIntegrityViolationException;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.VisitaDoParticipanteResponse;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoCandidato;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Candidato;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaPlanejada;
import UEPB.ESWII.ClinicalStudyPB.repository.ParticipacaoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaAgendadaRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaPlanejadaRepository;
import UEPB.ESWII.ClinicalStudyPB.service.AgendamentoService;
import UEPB.ESWII.ClinicalStudyPB.service.AgendamentoVisitaService;
import UEPB.ESWII.ClinicalStudyPB.service.ConflitoRegraException;
import UEPB.ESWII.ClinicalStudyPB.service.RecursoNaoEncontradoException;
import UEPB.ESWII.ClinicalStudyPB.service.UsuarioAutenticado;
import UEPB.ESWII.ClinicalStudyPB.service.ValidacaoRegraException;

/**
 * T78 (RF12, RQ-T78-01 a 07): a matriz das regras do agendamento pelo serviço de entrada ({@link AgendamentoService}),
 * com os repositórios simulados e o {@link Clock} fixo. Complementa o {@link AgendamentoVisitaServiceTest}, que testa
 * as regras isoladas; aqui valem a busca dos objetos (404), a ordem das verificações, o autor e a data gravados e a
 * sugestão de datas. Os endpoints, com tokens reais, estão em AgendamentoRegrasEndpointTest.
 */
class AgendamentoServiceTest {

    /** Relógio fixo: "hoje" e "agora" do teste saem dele. */
    private static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-11-12T12:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate HOJE = LocalDate.now(RELOGIO);
    private static final LocalDateTime AGORA = LocalDateTime.now(RELOGIO);

    private ParticipacaoRepository participacoes;
    private VisitaPlanejadaRepository previstas;
    private VisitaAgendadaRepository agendadas;
    private UsuarioAutenticado autenticado;
    private AgendamentoService service;

    private Estudo estudo;
    private Participacao participacao;
    /** Janela de HOJE + 3 a HOJE + 7 (dia 5, tolerância 2; participante incluído hoje). */
    private VisitaPlanejada retorno;
    /** Janela de um dia só: hoje. */
    private VisitaPlanejada triagem;
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        participacoes = mock(ParticipacaoRepository.class);
        previstas = mock(VisitaPlanejadaRepository.class);
        agendadas = mock(VisitaAgendadaRepository.class);
        autenticado = mock(UsuarioAutenticado.class);
        when(agendadas.saveAndFlush(any(VisitaAgendada.class))).thenAnswer(i -> i.getArgument(0));
        when(agendadas.save(any(VisitaAgendada.class))).thenAnswer(i -> i.getArgument(0));
        service = new AgendamentoService(new AgendamentoVisitaService(agendadas, RELOGIO), participacoes, previstas,
                agendadas, autenticado, RELOGIO);

        estudo = new Estudo();
        estudo.setId(UUID.randomUUID());
        estudo.setStatus(StatusEstudo.RECRUTAMENTO);
        Candidato candidato = new Candidato();
        candidato.setId(UUID.randomUUID());
        candidato.setCodigo("CAND-001");
        candidato.setNomeFicticio("Pessoa Teta (Ficticia)");
        candidato.setSituacao(SituacaoCandidato.INCLUIDO);
        participacao = new Participacao();
        participacao.setId(UUID.randomUUID());
        participacao.setCandidato(candidato);
        participacao.setEstudo(estudo);
        participacao.setDataInclusao(HOJE);
        retorno = prevista("Retorno", 1, 5, 2);
        triagem = prevista("Triagem", 0, 0, 0);

        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNome("Coordenadora (Ficticia)");

        when(participacoes.buscarComCandidato(participacao.getId())).thenReturn(Optional.of(participacao));
        when(previstas.findById(retorno.getId())).thenReturn(Optional.of(retorno));
        when(previstas.findById(triagem.getId())).thenReturn(Optional.of(triagem));
        when(autenticado.buscar(usuario.getId())).thenReturn(usuario);
    }

    private VisitaPlanejada prevista(String nome, int ordem, int dia, int tolerancia) {
        VisitaPlanejada visita = new VisitaPlanejada();
        visita.setId(UUID.randomUUID());
        visita.setEstudo(estudo);
        visita.setNome(nome);
        visita.setOrdem(ordem);
        visita.setDiaPrevisto(dia);
        visita.setTolerancia(tolerancia);
        return visita;
    }

    private VisitaAgendada agendada(VisitaPlanejada visita, SituacaoVisita situacao, LocalDateTime quando) {
        VisitaAgendada agendada = new VisitaAgendada();
        agendada.setId(UUID.randomUUID());
        agendada.setParticipacao(participacao);
        agendada.setVisitaPlanejada(visita);
        agendada.setSituacao(situacao);
        agendada.setDataHoraAgendada(quando);
        agendada.setCriadoPor(usuario);
        agendada.setCriadoEm(AGORA.minusDays(1));
        agendada.setAlteradoPor(usuario);
        agendada.setAlteradoEm(AGORA.minusDays(1));
        return agendada;
    }

    private VisitaAgendada agendar(VisitaPlanejada visita, LocalDateTime quando) {
        return service.agendar(participacao.getId(), visita.getId(), quando, usuario.getId());
    }

    private static void esperaConflito(String codigo, ThrowingCallable acao) {
        assertThatThrownBy(acao).isInstanceOfSatisfying(ConflitoRegraException.class,
                e -> assertThat(e.getCodigo()).isEqualTo(codigo));
    }

    private static void esperaValidacao(String codigo, ThrowingCallable acao) {
        assertThatThrownBy(acao).isInstanceOfSatisfying(ValidacaoRegraException.class,
                e -> assertThat(e.getCodigo()).isEqualTo(codigo));
    }

    // ---- RQ-T78-01: estado do estudo ----

    @ParameterizedTest(name = "estudo {0}: recusa o agendamento")
    @EnumSource(value = StatusEstudo.class, names = { "PLANEJAMENTO", "ENCERRADO" })
    @DisplayName("RQ-T78-01: estudo em planejamento ou encerrado recusa o agendamento")
    void recusaEstudoForaDeRecrutamentoOuExecucao(StatusEstudo status) {
        estudo.setStatus(status);

        esperaConflito("ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO", () -> agendar(retorno, HOJE.plusDays(5).atStartOfDay()));
    }

    @ParameterizedTest(name = "estudo {0}: aceita o agendamento")
    @EnumSource(value = StatusEstudo.class, names = { "RECRUTAMENTO", "EXECUCAO" })
    @DisplayName("RQ-T78-01: estudo em recrutamento ou execução aceita o agendamento")
    void aceitaEstudoEmRecrutamentoOuExecucao(StatusEstudo status) {
        estudo.setStatus(status);

        assertThat(agendar(retorno, HOJE.plusDays(5).atStartOfDay()).estaAgendada()).isTrue();
    }

    // ---- RQ-T78-02: datas e janela ----

    @Test
    @DisplayName("RQ-T78-02: ontem é DATA_NO_PASSADO; hoje é aceito quando está na janela")
    void ontemRecusadoEHojeAceito() {
        esperaValidacao("DATA_NO_PASSADO", () -> agendar(triagem, HOJE.minusDays(1).atStartOfDay()));

        assertThat(agendar(triagem, HOJE.atTime(9, 30)).getDataHoraAgendada()).isEqualTo(HOJE.atTime(9, 30));
    }

    @Test
    @DisplayName("RQ-T78-02: um dia antes e um dia depois da janela são DATA_FORA_DA_JANELA; o primeiro e o último dia passam")
    void limitesDaJanela() {
        esperaConflito("DATA_FORA_DA_JANELA", () -> agendar(retorno, HOJE.plusDays(2).atStartOfDay()));
        esperaConflito("DATA_FORA_DA_JANELA", () -> agendar(retorno, HOJE.plusDays(8).atStartOfDay()));

        assertThat(agendar(retorno, HOJE.plusDays(3).atStartOfDay()).getDataHoraAgendada())
                .isEqualTo(HOJE.plusDays(3).atStartOfDay());
        assertThat(agendar(retorno, HOJE.plusDays(7).atTime(23, 59)).getDataHoraAgendada())
                .isEqualTo(HOJE.plusDays(7).atTime(23, 59));
    }

    @Test
    @DisplayName("RQ-T78-02: a mensagem do erro de janela traz a janela válida")
    void mensagemTrazAJanela() {
        assertThatThrownBy(() -> agendar(retorno, HOJE.plusDays(10).atStartOfDay()))
                .isInstanceOfSatisfying(ConflitoRegraException.class, e -> assertThat(e.getMessage())
                        .contains("15/11/2026").contains("19/11/2026"));
    }

    // ---- RQ-T78-03: recursos inexistentes ----

    @Test
    @DisplayName("RQ-T78-03: participação inexistente e visita prevista inexistente viram 404")
    void participacaoOuVisitaInexistente() {
        UUID outra = UUID.randomUUID();
        when(participacoes.buscarComCandidato(outra)).thenReturn(Optional.empty());
        when(previstas.findById(outra)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.agendar(outra, retorno.getId(), AGORA.plusDays(5), usuario.getId()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> service.agendar(participacao.getId(), outra, AGORA.plusDays(5), usuario.getId()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("RQ-T78-03: visita prevista de outro estudo vira 404")
    void visitaDeOutroEstudo() {
        Estudo outroEstudo = new Estudo();
        outroEstudo.setId(UUID.randomUUID());
        VisitaPlanejada deOutro = prevista("Retorno de outro", 1, 5, 2);
        deOutro.setEstudo(outroEstudo);
        when(previstas.findById(deOutro.getId())).thenReturn(Optional.of(deOutro));

        assertThatThrownBy(() -> agendar(deOutro, HOJE.plusDays(5).atStartOfDay()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("RQ-T78-03: reagendar uma visita agendada inexistente vira 404")
    void reagendarInexistente() {
        UUID inexistente = UUID.randomUUID();
        when(agendadas.buscarParaReagendar(inexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reagendar(inexistente, AGORA.plusDays(5), usuario.getId()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    // ---- RQ-T78-04: duplicidade ----

    @Test
    @DisplayName("RQ-T78-04: agendar duas vezes o mesmo par é VISITA_JA_AGENDADA; visita cancelada também conta")
    void agendarDuasVezesOMesmoPar() {
        when(agendadas.existsByParticipacaoIdAndVisitaPlanejadaId(participacao.getId(), retorno.getId()))
                .thenReturn(true);

        esperaConflito("VISITA_JA_AGENDADA", () -> agendar(retorno, HOJE.plusDays(5).atStartOfDay()));
    }

    @Test
    @DisplayName("RQ-T78-04: a restrição única do banco (corrida) também vira VISITA_JA_AGENDADA")
    void restricaoUnicaViraConflito() {
        when(agendadas.saveAndFlush(any(VisitaAgendada.class))).thenThrow(new DataIntegrityViolationException("uk"));

        esperaConflito("VISITA_JA_AGENDADA", () -> agendar(retorno, HOJE.plusDays(5).atStartOfDay()));
    }

    // ---- RQ-T78-05: reagendamento ----

    @ParameterizedTest(name = "reagendar visita {0}: VISITA_NAO_ESTA_AGENDADA")
    @EnumSource(value = SituacaoVisita.class, names = { "REALIZADA", "AUSENTE", "CANCELADA" })
    @DisplayName("RQ-T78-05: só a visita AGENDADA pode ser reagendada")
    void reagendarVisitaSemSerAgendada(SituacaoVisita situacao) {
        VisitaAgendada visita = agendada(retorno, situacao, HOJE.plusDays(5).atStartOfDay());
        when(agendadas.buscarParaReagendar(visita.getId())).thenReturn(Optional.of(visita));

        esperaConflito("VISITA_NAO_ESTA_AGENDADA",
                () -> service.reagendar(visita.getId(), HOJE.plusDays(6).atStartOfDay(), usuario.getId()));
    }

    @Test
    @DisplayName("RQ-T78-05: reagendar fora da janela e para o passado dão os erros certos")
    void reagendarForaDaJanelaEParaOPassado() {
        VisitaAgendada visita = agendada(retorno, SituacaoVisita.AGENDADA, HOJE.plusDays(5).atStartOfDay());
        when(agendadas.buscarParaReagendar(visita.getId())).thenReturn(Optional.of(visita));

        esperaConflito("DATA_FORA_DA_JANELA",
                () -> service.reagendar(visita.getId(), HOJE.plusDays(9).atStartOfDay(), usuario.getId()));
        esperaValidacao("DATA_NO_PASSADO",
                () -> service.reagendar(visita.getId(), HOJE.minusDays(1).atStartOfDay(), usuario.getId()));
    }

    @Test
    @DisplayName("RQ-T78-05: reagendar para a mesma data é aceito e atualiza o instante da alteração")
    void reagendarParaAMesmaData() {
        LocalDateTime mesma = HOJE.plusDays(5).atTime(9, 30);
        VisitaAgendada visita = agendada(retorno, SituacaoVisita.AGENDADA, mesma);
        when(agendadas.buscarParaReagendar(visita.getId())).thenReturn(Optional.of(visita));

        VisitaAgendada salva = service.reagendar(visita.getId(), mesma, usuario.getId());

        assertThat(salva.getDataHoraAgendada()).isEqualTo(mesma);
        assertThat(salva.getAlteradoEm()).isEqualTo(AGORA);
        assertThat(salva.getAlteradoPor()).isSameAs(usuario);
    }

    // ---- RQ-T78-06: ordem das verificações ----

    @Test
    @DisplayName("RQ-T78-06: com estudo encerrado e data passada, vence o erro do estudo")
    void erroDoEstudoVenceODaData() {
        estudo.setStatus(StatusEstudo.ENCERRADO);

        esperaConflito("ESTUDO_FORA_DE_RECRUTAMENTO_OU_EXECUCAO", () -> agendar(retorno, HOJE.minusDays(3).atStartOfDay()));
    }

    @Test
    @DisplayName("RQ-T78-06: a data passada vence o erro da janela")
    void dataPassadaVenceAJanela() {
        esperaValidacao("DATA_NO_PASSADO", () -> agendar(retorno, HOJE.minusDays(3).atStartOfDay()));
    }

    // ---- RQ-T78-07: autor, data e sugestão ----

    @Test
    @DisplayName("RQ-T78-07: o agendamento grava o autor e o instante do Clock nos campos de criação e alteração")
    void gravaAutorEData() {
        VisitaAgendada salva = agendar(retorno, HOJE.plusDays(5).atTime(10, 0));

        assertThat(salva.getSituacao()).isEqualTo(SituacaoVisita.AGENDADA);
        assertThat(salva.getCriadoPor()).isSameAs(usuario);
        assertThat(salva.getAlteradoPor()).isSameAs(usuario);
        assertThat(salva.getCriadoEm()).isEqualTo(AGORA);
        assertThat(salva.getAlteradoEm()).isEqualTo(AGORA);
    }

    @Test
    @DisplayName("RQ-T78-07: as visitas do participante trazem a data sugerida e a janela de todas as visitas previstas")
    void sugestaoParaTodasAsVisitasPrevistas() {
        when(previstas.findByEstudoIdOrderByOrdemAsc(estudo.getId())).thenReturn(List.of(triagem, retorno));
        VisitaAgendada agendadaDoRetorno = agendada(retorno, SituacaoVisita.AGENDADA, HOJE.plusDays(5).atTime(9, 0));
        when(agendadas.findByParticipacaoIdOrderByVisitaPlanejadaOrdemAsc(participacao.getId()))
                .thenReturn(List.of(agendadaDoRetorno));

        List<VisitaDoParticipanteResponse> visitas = service.visitasDoParticipante(participacao.getId());

        assertThat(visitas).hasSize(2);
        VisitaDoParticipanteResponse t = visitas.get(0);
        assertThat(t.nome()).isEqualTo("Triagem");
        assertThat(t.dataSugerida()).isEqualTo(HOJE);
        assertThat(t.janelaInicio()).isEqualTo(HOJE);
        assertThat(t.janelaFim()).isEqualTo(HOJE);
        assertThat(t.agendamento()).isNull();
        VisitaDoParticipanteResponse r = visitas.get(1);
        assertThat(r.dataSugerida()).isEqualTo(HOJE.plusDays(5));
        assertThat(r.janelaInicio()).isEqualTo(HOJE.plusDays(3));
        assertThat(r.janelaFim()).isEqualTo(HOJE.plusDays(7));
        assertThat(r.agendamento()).isNotNull();
        assertThat(r.agendamento().id()).isEqualTo(agendadaDoRetorno.getId());
    }

    @Test
    @DisplayName("RQ-T78-07: as visitas de uma participação inexistente viram 404")
    void visitasDeParticipacaoInexistente() {
        UUID outra = UUID.randomUUID();
        when(participacoes.buscarComCandidato(outra)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.visitasDoParticipante(outra)).isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
