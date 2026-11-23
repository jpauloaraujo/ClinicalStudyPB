package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.HistoricoMinhasVisitasResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.MinhaVisitaResponse;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaPlanejada;
import UEPB.ESWII.ClinicalStudyPB.repository.AcaoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaAgendadaRepository;
import UEPB.ESWII.ClinicalStudyPB.service.AcessoParticipante;
import UEPB.ESWII.ClinicalStudyPB.service.MinhasVisitasService;
import UEPB.ESWII.ClinicalStudyPB.service.ValidacaoRegraException;

/**
 * T120 (RQ-T120-01 a 04, 06 e 07): a montagem das consultas do app (filtro por estudo, relógio, páginas, resumo,
 * atividades e fuso) com os repositórios simulados e o {@link Clock} fixo. A consulta de verdade, com o banco e os
 * tokens reais, está em {@link MinhasVisitasEndpointTest}.
 */
class MinhasVisitasServiceTest {

    private static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-11-23T12:00:00Z"), ZoneId.of("America/Recife"));

    private AcessoParticipante acesso;
    private VisitaAgendadaRepository visitas;
    private AcaoRepository acoes;
    private MinhasVisitasService service;

    private final UUID usuarioId = UUID.randomUUID();
    private Participacao participacaoA;
    private Participacao participacaoB;

    @BeforeEach
    void preparar() {
        acesso = mock(AcessoParticipante.class);
        visitas = mock(VisitaAgendadaRepository.class);
        acoes = mock(AcaoRepository.class);
        service = new MinhasVisitasService(acesso, visitas, acoes, RELOGIO);

        participacaoA = participacao("Estudo A");
        participacaoB = participacao("Estudo B");
        when(acesso.participacoesDe(usuarioId)).thenReturn(List.of(participacaoA, participacaoB));
        when(acoes.nomesDasAtividades(anyCollection())).thenReturn(List.of());
    }

    private Participacao participacao(String tituloDoEstudo) {
        Estudo estudo = new Estudo();
        estudo.setId(UUID.randomUUID());
        estudo.setTitulo(tituloDoEstudo);
        Participacao participacao = new Participacao();
        participacao.setId(UUID.randomUUID());
        participacao.setEstudo(estudo);
        return participacao;
    }

    private VisitaAgendada visita(Participacao participacao, String nome, SituacaoVisita situacao,
            LocalDateTime quando) {
        VisitaPlanejada prevista = new VisitaPlanejada();
        prevista.setId(UUID.randomUUID());
        prevista.setNome(nome);
        VisitaAgendada visita = new VisitaAgendada();
        visita.setId(UUID.randomUUID());
        visita.setParticipacao(participacao);
        visita.setVisitaPlanejada(prevista);
        visita.setSituacao(situacao);
        visita.setDataHoraAgendada(quando);
        return visita;
    }

    private static VisitaAgendadaRepository.ContagemPorSituacao contagem(SituacaoVisita situacao, long total) {
        VisitaAgendadaRepository.ContagemPorSituacao c = mock(VisitaAgendadaRepository.ContagemPorSituacao.class);
        when(c.getSituacao()).thenReturn(situacao);
        when(c.getTotal()).thenReturn(total);
        return c;
    }

    private static AcaoRepository.NomeDaAtividade atividade(UUID visitaPlanejadaId, String nome) {
        AcaoRepository.NomeDaAtividade a = mock(AcaoRepository.NomeDaAtividade.class);
        when(a.getVisitaId()).thenReturn(visitaPlanejadaId);
        when(a.getNome()).thenReturn(nome);
        return a;
    }

    @SuppressWarnings("unchecked")
    private Collection<UUID> participacoesConsultadas() {
        ArgumentCaptor<Collection<UUID>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(visitas).proximasDasParticipacoes(captor.capture(), any(), any());
        return captor.getValue();
    }

    // ---- próximas ----

    @Test
    @DisplayName("Próximas: consulta só AGENDADA a partir de agora, no relógio do sistema, e devolve a ordem do banco")
    void proximasConsultaAgendadasAPartirDeAgora() {
        VisitaAgendada primeira = visita(participacaoA, "Retorno 30 dias", SituacaoVisita.AGENDADA,
                LocalDateTime.of(2026, 11, 24, 9, 30));
        VisitaAgendada segunda = visita(participacaoB, "Retorno 60 dias", SituacaoVisita.AGENDADA,
                LocalDateTime.of(2026, 12, 22, 14, 0));
        when(visitas.proximasDasParticipacoes(anyCollection(), eq(SituacaoVisita.AGENDADA),
                eq(LocalDateTime.of(2026, 11, 23, 9, 0)))).thenReturn(List.of(primeira, segunda));

        List<MinhaVisitaResponse> proximas = service.proximas(usuarioId, null);

        assertThat(proximas).extracting(MinhaVisitaResponse::visitaNome)
                .containsExactly("Retorno 30 dias", "Retorno 60 dias");
        assertThat(proximas).extracting(MinhaVisitaResponse::situacao).containsOnly(SituacaoVisita.AGENDADA);
    }

    @Test
    @DisplayName("Próximas: parte das participações da conta; com estudoId, só a do estudo (nunca um id do cliente)")
    void proximasFiltraPorEstudoDaConta() {
        when(visitas.proximasDasParticipacoes(anyCollection(), any(), any())).thenReturn(List.of());

        service.proximas(usuarioId, participacaoB.getEstudo().getId());

        assertThat(participacoesConsultadas()).containsExactly(participacaoB.getId());
    }

    @Test
    @DisplayName("Próximas: estudoId que não é da conta devolve lista vazia sem consultar visitas")
    void proximasDeEstudoDeOutroVazio() {
        assertThat(service.proximas(usuarioId, UUID.randomUUID())).isEmpty();

        verifyNoInteractions(visitas);
    }

    @Test
    @DisplayName("Próximas: conta sem participação devolve lista vazia sem consultar visitas")
    void proximasSemVinculoVazio() {
        when(acesso.participacoesDe(usuarioId)).thenReturn(List.of());

        assertThat(service.proximas(usuarioId, null)).isEmpty();

        verifyNoInteractions(visitas);
    }

    @Test
    @DisplayName("As atividades vêm só pelo nome, em ordem alfabética, e a data sai com o deslocamento do fuso")
    void atividadesOrdenadasEDataComFuso() {
        VisitaAgendada agendada = visita(participacaoA, "Retorno 30 dias", SituacaoVisita.AGENDADA,
                LocalDateTime.of(2026, 11, 24, 9, 30));
        when(visitas.proximasDasParticipacoes(anyCollection(), any(), any())).thenReturn(List.of(agendada));
        UUID prevista = agendada.getVisitaPlanejada().getId();
        List<AcaoRepository.NomeDaAtividade> nomes = List.of(atividade(prevista, "Peso"),
                atividade(prevista, "coleta de sangue"), atividade(prevista, "Altura"));
        when(acoes.nomesDasAtividades(anyCollection())).thenReturn(nomes);

        MinhaVisitaResponse resposta = service.proximas(usuarioId, null).get(0);

        assertThat(resposta.atividades()).containsExactly("Altura", "coleta de sangue", "Peso");
        assertThat(resposta.dataHora().toString()).isEqualTo("2026-11-24T09:30-03:00");
        assertThat(resposta.estudoTitulo()).isEqualTo("Estudo A");
        assertThat(resposta.dataHoraRealizada()).isNull();
        assertThat(resposta.motivo()).isNull();
    }

    // ---- histórico ----

    @Test
    @DisplayName("Histórico: consulta REALIZADA, AUSENTE e CANCELADA, nunca AGENDADA, com a página pedida")
    void historicoConsultaSoSituacoesDoHistorico() {
        when(visitas.historicoDasParticipacoes(anyCollection(), anyCollection(), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(visitas.contarPorSituacao(anyCollection(), anyCollection())).thenReturn(List.of());

        service.historico(usuarioId, null, 2, 15);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<SituacaoVisita>> situacoes = ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<Pageable> pagina = ArgumentCaptor.forClass(Pageable.class);
        verify(visitas).historicoDasParticipacoes(anyCollection(), situacoes.capture(), pagina.capture());
        assertThat(situacoes.getValue()).containsExactlyInAnyOrder(SituacaoVisita.REALIZADA, SituacaoVisita.AUSENTE,
                SituacaoVisita.CANCELADA);
        assertThat(pagina.getValue().getPageNumber()).isEqualTo(2);
        assertThat(pagina.getValue().getPageSize()).isEqualTo(15);
    }

    @Test
    @DisplayName("Histórico: o resumo conta realizadas, ausentes e canceladas à parte, independente da página")
    void historicoResumoIndependenteDaPagina() {
        VisitaAgendada realizada = visita(participacaoA, "Triagem", SituacaoVisita.REALIZADA,
                LocalDateTime.of(2026, 10, 30, 9, 0));
        realizada.setDataHoraRealizada(LocalDateTime.of(2026, 10, 30, 9, 10));
        VisitaAgendada cancelada = visita(participacaoA, "Retorno 45 dias", SituacaoVisita.CANCELADA,
                LocalDateTime.of(2026, 10, 10, 10, 0));
        cancelada.setMotivo("retirada do estudo");
        Page<VisitaAgendada> segundaPagina = new PageImpl<>(List.of(realizada, cancelada), PageRequest.of(1, 2), 6);
        when(visitas.historicoDasParticipacoes(anyCollection(), anyCollection(), any(Pageable.class)))
                .thenReturn(segundaPagina);
        List<VisitaAgendadaRepository.ContagemPorSituacao> totais = List.of(contagem(SituacaoVisita.REALIZADA, 3),
                contagem(SituacaoVisita.AUSENTE, 1), contagem(SituacaoVisita.CANCELADA, 2));
        when(visitas.contarPorSituacao(anyCollection(), anyCollection())).thenReturn(totais);

        HistoricoMinhasVisitasResponse resposta = service.historico(usuarioId, null, 1, 2);

        assertThat(resposta.resumo().realizadas()).isEqualTo(3);
        assertThat(resposta.resumo().ausentes()).isEqualTo(1);
        assertThat(resposta.resumo().canceladas()).isEqualTo(2);
        assertThat(resposta.pagina()).isEqualTo(1);
        assertThat(resposta.tamanho()).isEqualTo(2);
        assertThat(resposta.totalElementos()).isEqualTo(6);
        assertThat(resposta.totalPaginas()).isEqualTo(3);
        assertThat(resposta.conteudo()).hasSize(2);
    }

    @Test
    @DisplayName("Histórico: a realizada traz a data real e a cancelada traz o motivo; os demais campos vêm nulos")
    void historicoMotivoSoNasCanceladasEDataRealNasRealizadas() {
        VisitaAgendada realizada = visita(participacaoA, "Triagem", SituacaoVisita.REALIZADA,
                LocalDateTime.of(2026, 10, 30, 9, 0));
        realizada.setDataHoraRealizada(LocalDateTime.of(2026, 10, 30, 9, 10));
        realizada.setMotivo("texto que nao deve aparecer");
        VisitaAgendada cancelada = visita(participacaoA, "Retorno 45 dias", SituacaoVisita.CANCELADA,
                LocalDateTime.of(2026, 10, 10, 10, 0));
        cancelada.setMotivo("retirada do estudo");
        when(visitas.historicoDasParticipacoes(anyCollection(), anyCollection(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(realizada, cancelada)));
        when(visitas.contarPorSituacao(anyCollection(), anyCollection())).thenReturn(List.of());

        List<MinhaVisitaResponse> itens = service.historico(usuarioId, null, 0, 20).conteudo();

        assertThat(itens.get(0).dataHoraRealizada().toString()).isEqualTo("2026-10-30T09:10-03:00");
        assertThat(itens.get(0).motivo()).isNull();
        assertThat(itens.get(1).motivo()).isEqualTo("retirada do estudo");
        assertThat(itens.get(1).dataHoraRealizada()).isNull();
    }

    @Test
    @DisplayName("Histórico: sem visitas nas situações, o resumo vem zerado")
    void historicoSemContagemResumoZerado() {
        when(visitas.historicoDasParticipacoes(anyCollection(), anyCollection(), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(visitas.contarPorSituacao(anyCollection(), anyCollection())).thenReturn(List.of());

        HistoricoMinhasVisitasResponse resposta = service.historico(usuarioId, null, 0, 20);

        assertThat(resposta.resumo().realizadas()).isZero();
        assertThat(resposta.resumo().ausentes()).isZero();
        assertThat(resposta.resumo().canceladas()).isZero();
        assertThat(resposta.conteudo()).isEmpty();
    }

    @Test
    @DisplayName("Histórico: sem vínculo ou com estudo de outro devolve vazio e resumo zerado, sem consultar visitas")
    void historicoSemVinculoOuEstudoDeOutroVazio() {
        HistoricoMinhasVisitasResponse deOutro = service.historico(usuarioId, UUID.randomUUID(), 0, 20);
        when(acesso.participacoesDe(usuarioId)).thenReturn(List.of());
        HistoricoMinhasVisitasResponse semVinculo = service.historico(usuarioId, null, 0, 20);

        for (HistoricoMinhasVisitasResponse resposta : List.of(deOutro, semVinculo)) {
            assertThat(resposta.conteudo()).isEmpty();
            assertThat(resposta.resumo().realizadas()).isZero();
            assertThat(resposta.totalElementos()).isZero();
        }
        verifyNoInteractions(visitas);
    }

    @Test
    @DisplayName("Histórico: tamanho 100 é aceito; página negativa, tamanho 0 e tamanho 101 dão PARAMETRO_INVALIDO")
    void historicoValidaPaginacao() {
        when(visitas.historicoDasParticipacoes(anyCollection(), anyCollection(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 100), 0));
        when(visitas.contarPorSituacao(anyCollection(), anyCollection())).thenReturn(List.of());

        assertThat(service.historico(usuarioId, null, 0, 100).tamanho()).isEqualTo(100);
        for (int[] invalido : new int[][] {{-1, 20}, {0, 0}, {0, 101}}) {
            assertThatThrownBy(() -> service.historico(usuarioId, null, invalido[0], invalido[1]))
                    .isInstanceOf(ValidacaoRegraException.class);
        }
        verify(visitas, never()).historicoDasParticipacoes(anyCollection(), anyCollection(),
                eq(PageRequest.of(0, 101)));
    }
}
