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
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoCandidato;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Candidato;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaPlanejada;
import UEPB.ESWII.ClinicalStudyPB.domain.regra.JanelaDeAgendamento;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaAgendadaRepository;
import UEPB.ESWII.ClinicalStudyPB.service.AgendamentoVisitaService;
import UEPB.ESWII.ClinicalStudyPB.service.ConflitoRegraException;

class AgendamentoVisitaServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 12);
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 12, 12, 0);

    private VisitaAgendadaRepository repository;
    private AgendamentoVisitaService service;
    private Participacao participacao;
    private VisitaPlanejada visita;
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        repository = mock(VisitaAgendadaRepository.class);
        when(repository.save(any(VisitaAgendada.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
        service = new AgendamentoVisitaService(repository,
                Clock.fixed(Instant.parse("2026-11-12T12:00:00Z"), ZoneOffset.UTC));

        Estudo estudo = estudo(StatusEstudo.RECRUTAMENTO);
        Candidato candidato = new Candidato();
        candidato.setSituacao(SituacaoCandidato.INCLUIDO);
        participacao = new Participacao();
        participacao.setId(UUID.randomUUID());
        participacao.setCandidato(candidato);
        participacao.setEstudo(estudo);
        participacao.setDataInclusao(HOJE.minusDays(30));

        visita = new VisitaPlanejada();
        visita.setId(UUID.randomUUID());
        visita.setEstudo(estudo);
        visita.setDiaPrevisto(30);
        visita.setTolerancia(2);

        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Rejeita primeiro uma participação inativa")
    void rejeitaParticipacaoInativaAntesDasDemaisRegras() {
        participacao.getCandidato().setSituacao(SituacaoCandidato.CADASTRADO);
        participacao.getEstudo().setStatus(StatusEstudo.PLANEJAMENTO);

        esperaConflito("PARTICIPANTE_RETIRADO",
                () -> service.agendarVisita(participacao, visita, HOJE.minusDays(1).atStartOfDay(), usuario));
    }

    @Test
    @DisplayName("Rejeita estudos fora de recrutamento ou execução")
    void validaEstadoDoEstudo() {
        participacao.getEstudo().setStatus(StatusEstudo.ENCERRADO);

        esperaConflito("ESTUDO_INVALIDO",
                () -> service.agendarVisita(participacao, visita, HOJE.atStartOfDay(), usuario));
    }

    @Test
    @DisplayName("Aceita estudos em execução")
    void aceitaEstudoEmExecucao() {
        participacao.getEstudo().setStatus(StatusEstudo.EXECUCAO);

        assertThat(service.agendarVisita(participacao, visita, HOJE.atStartOfDay(), usuario).estaAgendada()).isTrue();
    }

    @Test
    @DisplayName("Rejeita visita prevista pertencente a outro estudo")
    void validaEstudoDaVisita() {
        visita.setEstudo(estudo(StatusEstudo.RECRUTAMENTO));

        esperaConflito("VISITA_INCOMPATIVEL",
                () -> service.agendarVisita(participacao, visita, HOJE.atStartOfDay(), usuario));
    }

    @Test
    @DisplayName("Rejeita datas passadas antes de verificar a janela")
    void validaDataPassadaAntesDaJanela() {
        esperaConflito("DATA_PASSADA",
                () -> service.agendarVisita(participacao, visita, HOJE.minusDays(1).atStartOfDay(), usuario));
    }

    @Test
    @DisplayName("A janela inclui seus limites e rejeita datas fora dela")
    void validaLimitesInclusivosDaJanela() {
        participacao.setDataInclusao(HOJE.minusDays(25));
        assertThat(service.agendarVisita(participacao, visita, HOJE.plusDays(3).atStartOfDay(), usuario).dataAgendada())
                .isEqualTo(HOJE.plusDays(3));
        assertThat(service.agendarVisita(participacao, visita, HOJE.plusDays(7).atStartOfDay(), usuario).dataAgendada())
                  .isEqualTo(HOJE.plusDays(7));

        esperaConflito("DATA_FORA_DA_JANELA",
                () -> service.agendarVisita(participacao, visita, HOJE.plusDays(2).atStartOfDay(), usuario));

        esperaConflito("DATA_FORA_DA_JANELA",
                () -> service.agendarVisita(participacao, visita, HOJE.plusDays(8).atStartOfDay(), usuario));
    }

    @Test
    @DisplayName("Recusa agendamento anterior da mesma visita prevista")
    void rejeitaAgendamentoDuplicado() {
        when(repository.existsByParticipacaoIdAndVisitaPlanejadaId(
                participacao.getId(), visita.getId())).thenReturn(true);

        esperaConflito("AGENDAMENTO_DUPLICADO",
                () -> service.agendarVisita(participacao, visita, HOJE.atStartOfDay(), usuario));
    }

    @Test
    @DisplayName("Registra agendamento, autor e instantes de criação e alteração")
    void registraAgendamentoEAuditoria() {
        VisitaAgendada salva = service.agendarVisita(participacao, visita, HOJE.atStartOfDay(), usuario);

        assertThat(salva.getSituacao()).isEqualTo(SituacaoVisita.AGENDADA);
        assertThat(salva.getDataHoraAgendada()).isEqualTo(HOJE.atStartOfDay());
        assertThat(salva.getCriadoPor()).isSameAs(usuario);
        assertThat(salva.getAlteradoPor()).isSameAs(usuario);
        assertThat(salva.getCriadoEm()).isEqualTo(AGORA);
        assertThat(salva.getAlteradoEm()).isEqualTo(AGORA);
    }

    @Test
    @DisplayName("Reagenda somente visita AGENDADA e mantém validação da mesma janela")
    void reagendaVisitaAgendadaNaMesmaJanela() {
        VisitaAgendada agendada = visitaAgendada(SituacaoVisita.AGENDADA);

        VisitaAgendada salva = service.reagendarVisita(agendada, HOJE.plusDays(1).atStartOfDay(), usuario);

        assertThat(salva.getDataHoraAgendada()).isEqualTo(HOJE.plusDays(1).atStartOfDay());
        assertThat(salva.getAlteradoPor()).isSameAs(usuario);
        assertThat(salva.getAlteradoEm()).isEqualTo(AGORA);

        esperaConflito("DATA_FORA_DA_JANELA",
                () -> service.reagendarVisita(agendada, HOJE.plusDays(3).atStartOfDay(), usuario));
        esperaConflito("REAGENDAMENTO_INVALIDO",
                () -> service.reagendarVisita(visitaAgendada(SituacaoVisita.REALIZADA), HOJE.atStartOfDay(), usuario));
    }

    @Test
    @DisplayName("Calcula dia previsto e início e fim da janela")
    void calculaJanelaSugerida() {
        JanelaDeAgendamento janela = service.calcularJanelaSugerida(participacao, visita);

        assertThat(janela.dataBase()).isEqualTo(HOJE);
        assertThat(janela.dataInicio()).isEqualTo(HOJE.minusDays(2));
        assertThat(janela.dataFim()).isEqualTo(HOJE.plusDays(2));
    }

    private VisitaAgendada visitaAgendada(SituacaoVisita situacao) {
        VisitaAgendada agendada = new VisitaAgendada();
        agendada.setParticipacao(participacao);
        agendada.setVisitaPlanejada(visita);
        agendada.setSituacao(situacao);
        agendada.setDataHoraAgendada(HOJE.atStartOfDay());
        agendada.setCriadoPor(usuario);
        return agendada;
    }

    private static Estudo estudo(StatusEstudo status) {
        Estudo estudo = new Estudo();
        estudo.setId(UUID.randomUUID());
        estudo.setStatus(status);
        return estudo;
    }

    private static void esperaConflito(String codigo, ThrowingCallable acao) {
        assertThatThrownBy(acao).isInstanceOfSatisfying(ConflitoRegraException.class,
                erro -> assertThat(erro.getCodigo()).isEqualTo(codigo));
    }
}
