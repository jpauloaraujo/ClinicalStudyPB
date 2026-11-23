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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.HistoricoParticipanteResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.HistoricoParticipanteResponse.ResumoDoHistorico;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.HistoricoParticipanteResponse.VisitaDoHistorico;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoNoHistorico;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoParticipacao;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SolicitanteRetirada;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Acao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Candidato;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.ResultadoAtividade;
import UEPB.ESWII.ClinicalStudyPB.domain.model.RetiradaParticipacao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaPlanejada;
import UEPB.ESWII.ClinicalStudyPB.repository.ParticipacaoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.ResultadoAtividadeRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.RetiradaParticipacaoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaAgendadaRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaPlanejadaRepository;
import UEPB.ESWII.ClinicalStudyPB.service.HistoricoParticipanteService;
import UEPB.ESWII.ClinicalStudyPB.service.RecursoNaoEncontradoException;

/**
 * T96: a montagem do histórico (junção em memória, situação, resultados ordenados, fuso e resumo) com os repositórios
 * simulados. A consulta de verdade, com o banco, e a contagem de consultas ficam em
 * {@link HistoricoParticipanteEndpointTest}.
 */
class HistoricoParticipanteServiceTest {

    private static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-11-12T12:00:00Z"),
            ZoneId.of("America/Recife"));
    private static final LocalDate INCLUSAO = LocalDate.of(2026, 10, 20);

    private ParticipacaoRepository participacoes;
    private VisitaPlanejadaRepository previstas;
    private VisitaAgendadaRepository agendadas;
    private ResultadoAtividadeRepository resultados;
    private RetiradaParticipacaoRepository retiradas;
    private HistoricoParticipanteService service;
    private Participacao participacao;
    private UUID estudoId;

    @BeforeEach
    void preparar() {
        participacoes = mock(ParticipacaoRepository.class);
        previstas = mock(VisitaPlanejadaRepository.class);
        agendadas = mock(VisitaAgendadaRepository.class);
        resultados = mock(ResultadoAtividadeRepository.class);
        retiradas = mock(RetiradaParticipacaoRepository.class);
        service = new HistoricoParticipanteService(participacoes, previstas, agendadas, resultados, retiradas, RELOGIO);

        estudoId = UUID.randomUUID();
        Estudo estudo = new Estudo();
        estudo.setId(estudoId);
        Candidato candidato = new Candidato();
        candidato.setId(UUID.randomUUID());
        candidato.setCodigo("CAND-001");
        candidato.setNomeFicticio("Pessoa Teta (Ficticia)");
        participacao = new Participacao();
        participacao.setId(UUID.randomUUID());
        participacao.setCandidato(candidato);
        participacao.setEstudo(estudo);
        participacao.setDataInclusao(INCLUSAO);

        when(participacoes.buscarComCandidato(participacao.getId())).thenReturn(Optional.of(participacao));
        when(previstas.findByEstudoIdOrderByDiaPrevistoAscOrdemAsc(estudoId)).thenReturn(List.of());
        when(agendadas.listarComVisitaPlanejada(participacao.getId())).thenReturn(List.of());
        when(resultados.listarDaParticipacaoComAtividade(participacao.getId())).thenReturn(List.of());
    }

    private VisitaPlanejada prevista(String nome, int ordem, int dia) {
        VisitaPlanejada visita = new VisitaPlanejada();
        visita.setId(UUID.randomUUID());
        visita.setNome(nome);
        visita.setOrdem(ordem);
        visita.setDiaPrevisto(dia);
        visita.setTolerancia(3);
        return visita;
    }

    private VisitaAgendada agendada(VisitaPlanejada visita, SituacaoVisita situacao) {
        VisitaAgendada agendada = new VisitaAgendada();
        agendada.setId(UUID.randomUUID());
        agendada.setParticipacao(participacao);
        agendada.setVisitaPlanejada(visita);
        agendada.setSituacao(situacao);
        agendada.setDataHoraAgendada(LocalDateTime.of(2026, 11, 20, 9, 30));
        return agendada;
    }

    private ResultadoAtividade resultado(VisitaAgendada visita, String atividade, String valor) {
        Acao acao = new Acao();
        acao.setId(UUID.randomUUID());
        acao.setNome(atividade);
        ResultadoAtividade resultado = new ResultadoAtividade();
        resultado.setId(UUID.randomUUID());
        resultado.setVisitaAgendada(visita);
        resultado.setAcao(acao);
        resultado.setValor(valor);
        resultado.setUnidade("un");
        resultado.setObservacao("obs " + atividade);
        return resultado;
    }

    private void cadastrar(List<VisitaPlanejada> visitas, List<VisitaAgendada> doParticipante,
            List<ResultadoAtividade> doResultado) {
        when(previstas.findByEstudoIdOrderByDiaPrevistoAscOrdemAsc(estudoId)).thenReturn(visitas);
        when(agendadas.listarComVisitaPlanejada(participacao.getId())).thenReturn(doParticipante);
        when(resultados.listarDaParticipacaoComAtividade(participacao.getId())).thenReturn(doResultado);
    }

    @Test
    @DisplayName("Traz os dados do participante e a data prevista de cada visita (inclusão mais dia previsto)")
    void trazParticipanteEDataSugerida() {
        cadastrar(List.of(prevista("Triagem", 1, 0), prevista("Retorno 30 dias", 2, 30)), List.of(), List.of());

        HistoricoParticipanteResponse historico = service.historico(participacao.getId());

        assertThat(historico.participante().participacaoId()).isEqualTo(participacao.getId());
        assertThat(historico.participante().candidatoId()).isEqualTo(participacao.getCandidato().getId());
        assertThat(historico.participante().codigo()).isEqualTo("CAND-001");
        assertThat(historico.participante().nomeFicticio()).isEqualTo("Pessoa Teta (Ficticia)");
        assertThat(historico.participante().dataInclusao()).isEqualTo(INCLUSAO);
        assertThat(historico.participante().situacao()).isEqualTo(SituacaoParticipacao.ATIVO);
        assertThat(historico.participante().retirada()).isNull();
        verify(retiradas, never()).findByParticipacaoId(any());
        assertThat(historico.visitas()).extracting(VisitaDoHistorico::dataSugerida)
                .containsExactly(INCLUSAO, INCLUSAO.plusDays(30));
    }

    @Test
    @DisplayName("T104: participante retirado traz situacao RETIRADO e os dados da retirada no histórico")
    void participanteRetiradoTrazDadosDaRetirada() {
        participacao.retirar();
        RetiradaParticipacao retirada = new RetiradaParticipacao();
        retirada.setParticipacao(participacao);
        retirada.setDataRetirada(LocalDate.of(2026, 11, 10));
        retirada.setMotivo("Mudança de cidade");
        retirada.setSolicitante(SolicitanteRetirada.PARTICIPANTE);
        when(retiradas.findByParticipacaoId(participacao.getId())).thenReturn(Optional.of(retirada));

        HistoricoParticipanteResponse historico = service.historico(participacao.getId());

        assertThat(historico.participante().situacao()).isEqualTo(SituacaoParticipacao.RETIRADO);
        assertThat(historico.participante().retirada()).isNotNull();
        assertThat(historico.participante().retirada().dataRetirada()).isEqualTo(LocalDate.of(2026, 11, 10));
        assertThat(historico.participante().retirada().motivo()).isEqualTo("Mudança de cidade");
        assertThat(historico.participante().retirada().solicitante()).isEqualTo(SolicitanteRetirada.PARTICIPANTE);
    }

    @Test
    @DisplayName("Uma linha para cada visita prevista, na ordem da consulta, e a sem agendamento vira NAO_AGENDADA com tudo nulo")
    void umItemParaCadaVisitaPrevista() {
        VisitaPlanejada triagem = prevista("Triagem", 1, 0);
        VisitaPlanejada retorno = prevista("Retorno 30 dias", 2, 30);
        VisitaPlanejada ultimo = prevista("Retorno 60 dias", 3, 60);
        cadastrar(List.of(triagem, retorno, ultimo), List.of(agendada(retorno, SituacaoVisita.AGENDADA)), List.of());

        List<VisitaDoHistorico> visitas = service.historico(participacao.getId()).visitas();

        assertThat(visitas).extracting(VisitaDoHistorico::nome)
                .containsExactly("Triagem", "Retorno 30 dias", "Retorno 60 dias");
        assertThat(visitas).extracting(VisitaDoHistorico::situacao).containsExactly(
                SituacaoNoHistorico.NAO_AGENDADA, SituacaoNoHistorico.AGENDADA, SituacaoNoHistorico.NAO_AGENDADA);
        VisitaDoHistorico semAgendamento = visitas.get(0);
        assertThat(semAgendamento.visitaAgendadaId()).isNull();
        assertThat(semAgendamento.dataHoraAgendada()).isNull();
        assertThat(semAgendamento.dataHoraRealizada()).isNull();
        assertThat(semAgendamento.motivo()).isNull();
        assertThat(semAgendamento.observacao()).isNull();
        assertThat(semAgendamento.resultados()).isEmpty();
    }

    @Test
    @DisplayName("A visita agendada traz id, situação, motivo, observação e as datas com o fuso do centro (-03:00)")
    void visitaAgendadaTrazOsCamposDoAgendamento() {
        VisitaPlanejada triagem = prevista("Triagem", 1, 0);
        VisitaAgendada realizada = agendada(triagem, SituacaoVisita.REALIZADA);
        realizada.setDataHoraRealizada(LocalDateTime.of(2026, 11, 20, 10, 5));
        realizada.setObservacao("Tudo certo");
        VisitaPlanejada retorno = prevista("Retorno 30 dias", 2, 30);
        VisitaAgendada cancelada = agendada(retorno, SituacaoVisita.CANCELADA);
        cancelada.setMotivo("Participante em viagem");
        cadastrar(List.of(triagem, retorno), List.of(cancelada, realizada), List.of());

        List<VisitaDoHistorico> visitas = service.historico(participacao.getId()).visitas();

        VisitaDoHistorico primeira = visitas.get(0);
        assertThat(primeira.situacao()).isEqualTo(SituacaoNoHistorico.REALIZADA);
        assertThat(primeira.visitaAgendadaId()).isEqualTo(realizada.getId());
        assertThat(primeira.dataHoraAgendada().toString()).isEqualTo("2026-11-20T09:30-03:00");
        assertThat(primeira.dataHoraRealizada().toString()).isEqualTo("2026-11-20T10:05-03:00");
        assertThat(primeira.observacao()).isEqualTo("Tudo certo");
        VisitaDoHistorico segunda = visitas.get(1);
        assertThat(segunda.situacao()).isEqualTo(SituacaoNoHistorico.CANCELADA);
        assertThat(segunda.motivo()).isEqualTo("Participante em viagem");
        assertThat(segunda.dataHoraRealizada()).isNull();
    }

    @Test
    @DisplayName("Os resultados de cada visita saem ordenados pelo nome da atividade; visita sem resultado devolve lista vazia")
    void resultadosOrdenadosPeloNomeDaAtividade() {
        VisitaPlanejada triagem = prevista("Triagem", 1, 0);
        VisitaPlanejada retorno = prevista("Retorno 30 dias", 2, 30);
        VisitaAgendada daTriagem = agendada(triagem, SituacaoVisita.REALIZADA);
        VisitaAgendada doRetorno = agendada(retorno, SituacaoVisita.AGENDADA);
        cadastrar(List.of(triagem, retorno), List.of(daTriagem, doRetorno), List.of(
                resultado(daTriagem, "Escala de dor", "3"),
                resultado(daTriagem, "aferir pressão arterial", "120/80"),
                resultado(daTriagem, "Balança", "70")));

        List<VisitaDoHistorico> visitas = service.historico(participacao.getId()).visitas();

        assertThat(visitas.get(0).resultados()).extracting(r -> r.nome())
                .containsExactly("aferir pressão arterial", "Balança", "Escala de dor");
        assertThat(visitas.get(0).resultados().get(0).valor()).isEqualTo("120/80");
        assertThat(visitas.get(0).resultados().get(0).unidade()).isEqualTo("un");
        assertThat(visitas.get(0).resultados().get(0).observacao()).isEqualTo("obs aferir pressão arterial");
        assertThat(visitas.get(1).resultados()).isEmpty();
    }

    @Test
    @DisplayName("Resumo: conta cada situação e pendentes = agendadas + não agendadas")
    void resumoConta() {
        VisitaPlanejada v1 = prevista("V1", 1, 0);
        VisitaPlanejada v2 = prevista("V2", 2, 10);
        VisitaPlanejada v3 = prevista("V3", 3, 20);
        VisitaPlanejada v4 = prevista("V4", 4, 30);
        VisitaPlanejada v5 = prevista("V5", 5, 40);
        VisitaPlanejada v6 = prevista("V6", 6, 50);
        cadastrar(List.of(v1, v2, v3, v4, v5, v6), List.of(agendada(v1, SituacaoVisita.REALIZADA),
                agendada(v2, SituacaoVisita.AUSENTE), agendada(v3, SituacaoVisita.CANCELADA),
                agendada(v4, SituacaoVisita.AGENDADA)), List.of());

        ResumoDoHistorico resumo = service.historico(participacao.getId()).resumo();

        assertThat(resumo).isEqualTo(new ResumoDoHistorico(6, 1, 1, 1, 1, 2, 3));
    }

    @Test
    @DisplayName("Sem visitas previstas o histórico vem vazio e o resumo todo zero")
    void semVisitasPrevistas() {
        HistoricoParticipanteResponse historico = service.historico(participacao.getId());

        assertThat(historico.visitas()).isEmpty();
        assertThat(historico.resumo()).isEqualTo(new ResumoDoHistorico(0, 0, 0, 0, 0, 0, 0));
    }

    @Test
    @DisplayName("Invariante: previstas = realizadas + ausentes + canceladas + agendadas + não agendadas, para qualquer combinação")
    void invarianteParaQualquerCombinacao() {
        SituacaoNoHistorico[] situacoes = SituacaoNoHistorico.values();
        int visitasPorCombinacao = 4;
        int combinacoes = (int) Math.pow(situacoes.length, visitasPorCombinacao);
        for (int combinacao = 0; combinacao < combinacoes; combinacao++) {
            List<VisitaPlanejada> visitas = new ArrayList<>();
            List<VisitaAgendada> doParticipante = new ArrayList<>();
            int resto = combinacao;
            for (int i = 0; i < visitasPorCombinacao; i++) {
                SituacaoNoHistorico situacao = situacoes[resto % situacoes.length];
                resto /= situacoes.length;
                VisitaPlanejada visita = prevista("V" + i, i + 1, i * 10);
                visitas.add(visita);
                if (situacao != SituacaoNoHistorico.NAO_AGENDADA) {
                    doParticipante.add(agendada(visita, SituacaoVisita.valueOf(situacao.name())));
                }
            }
            cadastrar(visitas, doParticipante, List.of());

            ResumoDoHistorico resumo = service.historico(participacao.getId()).resumo();

            assertThat(resumo.previstas()).isEqualTo(visitasPorCombinacao);
            assertThat(resumo.realizadas() + resumo.ausentes() + resumo.canceladas() + resumo.agendadas()
                    + resumo.naoAgendadas()).as("combinação %d", combinacao).isEqualTo(resumo.previstas());
            assertThat(resumo.pendentes()).isEqualTo(resumo.agendadas() + resumo.naoAgendadas());
        }
    }

    @Test
    @DisplayName("Participação inexistente: 404 (RecursoNaoEncontrado) sem consultar visitas nem resultados")
    void participacaoInexistente() {
        UUID inexistente = UUID.randomUUID();
        when(participacoes.buscarComCandidato(inexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.historico(inexistente))
                .isInstanceOf(RecursoNaoEncontradoException.class).hasMessage("Participação não encontrada.");

        verify(previstas, never()).findByEstudoIdOrderByDiaPrevistoAscOrdemAsc(any());
        verify(agendadas, never()).listarComVisitaPlanejada(any());
        verify(resultados, never()).listarDaParticipacaoComAtividade(any());
    }

    @Test
    @DisplayName("Só lê: o serviço nunca grava nem apaga")
    void naoGravaNada() {
        VisitaPlanejada triagem = prevista("Triagem", 1, 0);
        VisitaAgendada realizada = agendada(triagem, SituacaoVisita.REALIZADA);
        cadastrar(List.of(triagem), List.of(realizada), List.of(resultado(realizada, "Escala de dor", "3")));

        service.historico(participacao.getId());

        verify(agendadas, never()).save(any());
        verify(agendadas, never()).delete(any());
        verify(resultados, never()).save(any());
        verify(resultados, never()).delete(any());
        verify(participacoes, never()).save(any());
        verify(previstas, never()).save(any());
    }
}
