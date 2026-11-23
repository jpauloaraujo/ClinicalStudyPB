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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import UEPB.ESWII.ClinicalStudyPB.domain.enums.BloqueioResultados;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.CategoriaAcao;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoVisita;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Acao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.ResultadoAtividade;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaAgendada;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaPlanejada;
import UEPB.ESWII.ClinicalStudyPB.repository.AcaoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.ResultadoAtividadeRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.UsuarioRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaAgendadaRepository;
import UEPB.ESWII.ClinicalStudyPB.service.ConflitoRegraException;
import UEPB.ESWII.ClinicalStudyPB.service.RecursoNaoEncontradoException;
import UEPB.ESWII.ClinicalStudyPB.service.ResultadoAtividadeService;
import UEPB.ESWII.ClinicalStudyPB.service.ResultadoAtividadeService.ComandoResultado;
import UEPB.ESWII.ClinicalStudyPB.service.ResultadoAtividadeService.ResultadoRegistrado;
import UEPB.ESWII.ClinicalStudyPB.service.ResultadoAtividadeService.ResultadosDaVisita;
import UEPB.ESWII.ClinicalStudyPB.service.ValidacaoRegraException;

/**
 * T89: regras do serviço de resultados fictícios das avaliações, com repositórios simulados e relógio fixo. A matriz
 * completa por endpoint fica na T92.
 */
class ResultadoAtividadeServiceTest {

    private static final ZoneId FUSO = ZoneId.of("America/Recife");
    /** 14/11/2026 10:00 no fuso do centro. */
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 14, 10, 0);

    private final VisitaAgendadaRepository visitasAgendadas = mock(VisitaAgendadaRepository.class);
    private final AcaoRepository acoes = mock(AcaoRepository.class);
    private final ResultadoAtividadeRepository resultados = mock(ResultadoAtividadeRepository.class);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);

    private ResultadoAtividadeService service;
    private Estudo estudo;
    private VisitaPlanejada triagem;
    private VisitaAgendada visita;
    private Acao pressao;
    private Acao dor;
    private Usuario pesquisador;
    private Usuario outroPesquisador;

    @BeforeEach
    void preparar() {
        Clock clock = Clock.fixed(AGORA.atZone(FUSO).toInstant(), FUSO);
        service = new ResultadoAtividadeService(visitasAgendadas, acoes, resultados, usuarios, clock);

        estudo = new Estudo();
        estudo.setId(UUID.randomUUID());
        estudo.setStatus(StatusEstudo.EXECUCAO);
        triagem = visitaPlanejada(estudo, "Triagem");

        visita = new VisitaAgendada();
        visita.setId(UUID.randomUUID());
        visita.setVisitaPlanejada(triagem);
        visita.setSituacao(SituacaoVisita.REALIZADA);
        when(visitasAgendadas.findById(visita.getId())).thenReturn(Optional.of(visita));

        pressao = acao(triagem, "Aferir pressão arterial", CategoriaAcao.AVALIACAO_CLINICA);
        dor = acao(triagem, "Escala de dor", CategoriaAcao.AVALIACAO_CLINICA);

        pesquisador = usuario("Pesquisador Demo (Ficticio)");
        outroPesquisador = usuario("Outro Pesquisador (Ficticio)");

        when(resultados.findByVisitaAgendadaIdAndAcaoId(any(), any())).thenReturn(Optional.empty());
        when(resultados.save(any(ResultadoAtividade.class))).thenAnswer(chamada -> chamada.getArgument(0));
    }

    private static VisitaPlanejada visitaPlanejada(Estudo estudo, String nome) {
        VisitaPlanejada v = new VisitaPlanejada();
        v.setId(UUID.randomUUID());
        v.setEstudo(estudo);
        v.setNome(nome);
        return v;
    }

    private Acao acao(VisitaPlanejada visitaPlanejada, String nome, CategoriaAcao categoria) {
        Acao a = new Acao();
        a.setId(UUID.randomUUID());
        a.setVisita(visitaPlanejada);
        a.setNome(nome);
        a.setCategoria(categoria);
        when(acoes.findById(a.getId())).thenReturn(Optional.of(a));
        return a;
    }

    private Usuario usuario(String nome) {
        Usuario u = new Usuario();
        u.setId(UUID.randomUUID());
        u.setNome(nome);
        when(usuarios.getReferenceById(u.getId())).thenReturn(u);
        return u;
    }

    private ResultadoRegistrado registrar(Acao acao, String valor) {
        return service.registrar(visita.getId(), acao.getId(), new ComandoResultado(valor, "mmHg", null),
                pesquisador.getId());
    }

    private ResultadoAtividade resultadoExistente(Acao acao, String valor, Usuario autor, LocalDateTime quando) {
        ResultadoAtividade r = new ResultadoAtividade();
        r.setId(UUID.randomUUID());
        r.setVisitaAgendada(visita);
        r.setAcao(acao);
        r.setValor(valor);
        r.setRegistradoPor(autor);
        r.setRegistradoEm(quando);
        r.setAlteradoPor(autor);
        r.setAlteradoEm(quando);
        return r;
    }

    private void esperaConflito(String codigo, ThrowingCallable chamada) {
        assertThatThrownBy(chamada).isInstanceOf(ConflitoRegraException.class)
                .extracting(e -> ((ConflitoRegraException) e).getCodigo()).isEqualTo(codigo);
        verify(resultados, never()).save(any());
    }

    // ---------------------------------------------------------------- registrar: primeiro registro e edição

    @Test
    @DisplayName("O primeiro registro cria o resultado com autor e datas de registro e alteração iguais (201)")
    void primeiroRegistroCriaComAutorEDatas() {
        ResultadoRegistrado salvo = registrar(pressao, "120/80");

        assertThat(salvo.criado()).isTrue();
        ResultadoAtividade r = salvo.resultado();
        assertThat(r.getVisitaAgendada()).isSameAs(visita);
        assertThat(r.getAcao()).isSameAs(pressao);
        assertThat(r.getValor()).isEqualTo("120/80");
        assertThat(r.getUnidade()).isEqualTo("mmHg");
        assertThat(r.getRegistradoPor()).isSameAs(pesquisador);
        assertThat(r.getAlteradoPor()).isSameAs(pesquisador);
        assertThat(r.getRegistradoEm()).isEqualTo(AGORA);
        assertThat(r.getAlteradoEm()).isEqualTo(AGORA);
    }

    @Test
    @DisplayName("A edição troca o valor e só o autor e a data da última alteração; o registro original não muda (200)")
    void edicaoAtualizaSoAAlteracao() {
        LocalDateTime ontem = AGORA.minusDays(1);
        ResultadoAtividade existente = resultadoExistente(pressao, "120/80", outroPesquisador, ontem);
        when(resultados.findByVisitaAgendadaIdAndAcaoId(visita.getId(), pressao.getId()))
                .thenReturn(Optional.of(existente));

        ResultadoRegistrado salvo = registrar(pressao, "130/85");

        assertThat(salvo.criado()).isFalse();
        assertThat(salvo.resultado()).isSameAs(existente);
        assertThat(existente.getValor()).isEqualTo("130/85");
        assertThat(existente.getRegistradoPor()).isSameAs(outroPesquisador);
        assertThat(existente.getRegistradoEm()).isEqualTo(ontem);
        assertThat(existente.getAlteradoPor()).isSameAs(pesquisador);
        assertThat(existente.getAlteradoEm()).isEqualTo(AGORA);
    }

    @Test
    @DisplayName("Valor, unidade e observação são gravados com trim; unidade e observação em branco viram nulos")
    void gravaComTrimEBrancosViramNulos() {
        ResultadoAtividade r = service.registrar(visita.getId(), pressao.getId(),
                new ComandoResultado("  120/80  ", "   ", "  "), pesquisador.getId()).resultado();

        assertThat(r.getValor()).isEqualTo("120/80");
        assertThat(r.getUnidade()).isNull();
        assertThat(r.getObservacao()).isNull();

        ResultadoAtividade outro = service.registrar(visita.getId(), dor.getId(),
                new ComandoResultado("3", " pontos ", " Relatada pelo participante "), pesquisador.getId()).resultado();
        assertThat(outro.getUnidade()).isEqualTo("pontos");
        assertThat(outro.getObservacao()).isEqualTo("Relatada pelo participante");
    }

    @Test
    @DisplayName("Resultado parcial: salvar uma atividade não exige as outras")
    void resultadoParcialSalvaUmaAtividadePorVez() {
        registrar(pressao, "120/80");

        verify(resultados).save(any(ResultadoAtividade.class));
        verify(resultados, never()).findByVisitaAgendadaIdAndAcaoId(visita.getId(), dor.getId());
    }

    // ---------------------------------------------------------------- registrar: verificações na ordem

    @Test
    @DisplayName("(1) Visita agendada inexistente: 404")
    void visitaInexistente() {
        UUID inexistente = UUID.randomUUID();
        when(visitasAgendadas.findById(inexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrar(inexistente, pressao.getId(),
                new ComandoResultado("1", null, null), pesquisador.getId()))
                .isInstanceOf(RecursoNaoEncontradoException.class).hasMessageContaining("Visita agendada");
    }

    @Test
    @DisplayName("(1) Atividade inexistente: 404")
    void atividadeInexistente() {
        UUID inexistente = UUID.randomUUID();
        when(acoes.findById(inexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrar(visita.getId(), inexistente,
                new ComandoResultado("1", null, null), pesquisador.getId()))
                .isInstanceOf(RecursoNaoEncontradoException.class).hasMessageContaining("Atividade");
    }

    @Test
    @DisplayName("(2) Visita agendada, ausente ou cancelada: 409 VISITA_NAO_REALIZADA")
    void visitaNaoRealizada() {
        for (SituacaoVisita situacao : List.of(SituacaoVisita.AGENDADA, SituacaoVisita.AUSENTE,
                SituacaoVisita.CANCELADA)) {
            visita.setSituacao(situacao);
            esperaConflito("VISITA_NAO_REALIZADA", () -> registrar(pressao, "120/80"));
        }
    }

    @Test
    @DisplayName("(3) Atividade de outra visita prevista: 409 ATIVIDADE_NAO_PERTENCE_A_VISITA")
    void atividadeDeOutraVisita() {
        Acao deOutraVisita = acao(visitaPlanejada(estudo, "Retorno 30 dias"), "Exame físico",
                CategoriaAcao.AVALIACAO_CLINICA);

        esperaConflito("ATIVIDADE_NAO_PERTENCE_A_VISITA", () -> registrar(deOutraVisita, "Normal"));
    }

    @Test
    @DisplayName("(4) Atividade que não é de avaliação clínica: 409 ATIVIDADE_FORA_DA_CATEGORIA_AVALIACAO")
    void atividadeForaDaCategoria() {
        Acao coleta = acao(triagem, "Coletar sangue", CategoriaAcao.EXAME_LABORATORIAL);

        esperaConflito("ATIVIDADE_FORA_DA_CATEGORIA_AVALIACAO", () -> registrar(coleta, "Coletado"));
    }

    @Test
    @DisplayName("(5) Estudo encerrado: 409 ESTUDO_ENCERRADO, também para editar")
    void estudoEncerrado() {
        estudo.setStatus(StatusEstudo.ENCERRADO);
        when(resultados.findByVisitaAgendadaIdAndAcaoId(visita.getId(), pressao.getId()))
                .thenReturn(Optional.of(resultadoExistente(pressao, "120/80", pesquisador, AGORA.minusDays(1))));

        esperaConflito("ESTUDO_ENCERRADO", () -> registrar(pressao, "130/85"));
    }

    @Test
    @DisplayName("(6) Valor vazio, só com espaços ou ausente: 400 VALOR_OBRIGATORIO")
    void valorObrigatorio() {
        for (String valor : new String[] {null, "", "   "}) {
            assertThatThrownBy(() -> registrar(pressao, valor)).isInstanceOf(ValidacaoRegraException.class)
                    .extracting(e -> ((ValidacaoRegraException) e).getCodigo()).isEqualTo("VALOR_OBRIGATORIO");
        }
        assertThatThrownBy(() -> service.registrar(visita.getId(), pressao.getId(), null, pesquisador.getId()))
                .isInstanceOf(ValidacaoRegraException.class);
        verify(resultados, never()).save(any());
    }

    @Test
    @DisplayName("A ordem vale: visita não realizada vem antes da atividade de outra categoria e do estudo encerrado")
    void ordemDasVerificacoes() {
        Acao coleta = acao(triagem, "Coletar sangue", CategoriaAcao.EXAME_LABORATORIAL);
        Acao deOutraVisita = acao(visitaPlanejada(estudo, "Retorno 30 dias"), "Exame físico",
                CategoriaAcao.EXAME_LABORATORIAL);
        estudo.setStatus(StatusEstudo.ENCERRADO);

        visita.setSituacao(SituacaoVisita.AGENDADA);
        esperaConflito("VISITA_NAO_REALIZADA", () -> registrar(coleta, ""));

        visita.setSituacao(SituacaoVisita.REALIZADA);
        esperaConflito("ATIVIDADE_NAO_PERTENCE_A_VISITA", () -> registrar(deOutraVisita, ""));
        esperaConflito("ATIVIDADE_FORA_DA_CATEGORIA_AVALIACAO", () -> registrar(coleta, ""));
        esperaConflito("ESTUDO_ENCERRADO", () -> registrar(pressao, ""));
    }

    // ---------------------------------------------------------------- consultar

    @Test
    @DisplayName("A consulta lista as atividades de avaliação com resultado ou pendentes e as contagens")
    void consultaListaAtividadesEContagens() {
        when(acoes.findByVisitaIdAndCategoriaOrderByNomeAsc(triagem.getId(), CategoriaAcao.AVALIACAO_CLINICA))
                .thenReturn(List.of(pressao, dor));
        ResultadoAtividade daPressao = resultadoExistente(pressao, "120/80", pesquisador, AGORA);
        when(resultados.findByVisitaAgendadaId(visita.getId())).thenReturn(List.of(daPressao));

        ResultadosDaVisita consulta = service.consultar(visita.getId());

        assertThat(consulta.visitaAgendada()).isSameAs(visita);
        assertThat(consulta.bloqueio()).isNull();
        assertThat(consulta.atividades()).extracting(a -> a.acao().getNome())
                .containsExactly("Aferir pressão arterial", "Escala de dor");
        assertThat(consulta.atividades().get(0).resultado()).isSameAs(daPressao);
        assertThat(consulta.atividades().get(1).resultado()).isNull();
        assertThat(consulta.total()).isEqualTo(2);
        assertThat(consulta.registrados()).isEqualTo(1);
        assertThat(consulta.pendentes()).isEqualTo(1);
    }

    @Test
    @DisplayName("A consulta carrega atividades e resultados em duas consultas, sem consulta por atividade")
    void consultaSemNMaisUm() {
        when(acoes.findByVisitaIdAndCategoriaOrderByNomeAsc(triagem.getId(), CategoriaAcao.AVALIACAO_CLINICA))
                .thenReturn(List.of(pressao, dor));
        when(resultados.findByVisitaAgendadaId(visita.getId())).thenReturn(List.of());

        service.consultar(visita.getId());

        verify(resultados, never()).findByVisitaAgendadaIdAndAcaoId(any(), any());
        verify(acoes, never()).findById(any());
    }

    @Test
    @DisplayName("Visita não realizada: a consulta responde com bloqueio VISITA_NAO_REALIZADA, sem recusar")
    void consultaDeVisitaNaoRealizada() {
        visita.setSituacao(SituacaoVisita.AGENDADA);
        estudo.setStatus(StatusEstudo.ENCERRADO);
        when(acoes.findByVisitaIdAndCategoriaOrderByNomeAsc(triagem.getId(), CategoriaAcao.AVALIACAO_CLINICA))
                .thenReturn(List.of(pressao));
        when(resultados.findByVisitaAgendadaId(visita.getId())).thenReturn(List.of());

        ResultadosDaVisita consulta = service.consultar(visita.getId());

        assertThat(consulta.bloqueio()).isEqualTo(BloqueioResultados.VISITA_NAO_REALIZADA);
        assertThat(consulta.total()).isEqualTo(1);
        assertThat(consulta.pendentes()).isEqualTo(1);
    }

    @Test
    @DisplayName("Estudo encerrado: a consulta responde com bloqueio ESTUDO_ENCERRADO e mostra os resultados")
    void consultaDeEstudoEncerrado() {
        estudo.setStatus(StatusEstudo.ENCERRADO);
        when(acoes.findByVisitaIdAndCategoriaOrderByNomeAsc(triagem.getId(), CategoriaAcao.AVALIACAO_CLINICA))
                .thenReturn(List.of(pressao));
        when(resultados.findByVisitaAgendadaId(visita.getId()))
                .thenReturn(List.of(resultadoExistente(pressao, "120/80", pesquisador, AGORA)));

        ResultadosDaVisita consulta = service.consultar(visita.getId());

        assertThat(consulta.bloqueio()).isEqualTo(BloqueioResultados.ESTUDO_ENCERRADO);
        assertThat(consulta.registrados()).isEqualTo(1);
    }

    @Test
    @DisplayName("T102: participante retirado recusa registro e edição (409 PARTICIPANTE_RETIRADO) e consulta devolve bloqueio")
    void participanteRetiradoBloqueiaResultados() {
        Participacao participacao = new Participacao();
        participacao.setEstudo(estudo);
        participacao.retirar();
        visita.setParticipacao(participacao);

        esperaConflito("PARTICIPANTE_RETIRADO", () -> registrar(pressao, "120/80"));

        when(acoes.findByVisitaIdAndCategoriaOrderByNomeAsc(triagem.getId(), CategoriaAcao.AVALIACAO_CLINICA))
                .thenReturn(List.of(pressao));
        when(resultados.findByVisitaAgendadaId(visita.getId())).thenReturn(List.of());
        assertThat(service.consultar(visita.getId()).bloqueio()).isEqualTo(BloqueioResultados.PARTICIPANTE_RETIRADO);

        // Prioridade: estudo encerrado vence participante retirado, tanto na escrita quanto na consulta
        estudo.setStatus(StatusEstudo.ENCERRADO);
        esperaConflito("ESTUDO_ENCERRADO", () -> registrar(pressao, "120/80"));
        assertThat(service.consultar(visita.getId()).bloqueio()).isEqualTo(BloqueioResultados.ESTUDO_ENCERRADO);
    }

    @Test
    @DisplayName("Consulta de visita agendada inexistente: 404")
    void consultaDeVisitaInexistente() {
        UUID inexistente = UUID.randomUUID();
        when(visitasAgendadas.findById(inexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consultar(inexistente)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Sem relógio solto: o horário gravado vem do Clock da aplicação")
    void horarioVemDoClock() {
        Instant outroInstante = Instant.parse("2026-12-01T15:30:00Z");
        ResultadoAtividadeService comOutroRelogio = new ResultadoAtividadeService(visitasAgendadas, acoes,
                resultados, usuarios, Clock.fixed(outroInstante, FUSO));

        ResultadoAtividade r = comOutroRelogio.registrar(visita.getId(), pressao.getId(),
                new ComandoResultado("1", null, null), pesquisador.getId()).resultado();

        assertThat(r.getRegistradoEm()).isEqualTo(LocalDateTime.ofInstant(outroInstante, FUSO));
    }
}
