package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.AcaoRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.AcaoResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.VisitaRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.VisitaResponse;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.CategoriaAcao;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Acao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.VisitaPlanejada;
import UEPB.ESWII.ClinicalStudyPB.repository.AcaoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.EstudoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.VisitaPlanejadaRepository;
import UEPB.ESWII.ClinicalStudyPB.service.ConflitoRegraException;
import UEPB.ESWII.ClinicalStudyPB.service.EstudoAcesso;
import UEPB.ESWII.ClinicalStudyPB.service.RecursoNaoEncontradoException;
import UEPB.ESWII.ClinicalStudyPB.service.VisitaService;

/**
 * T50 (RF06): regras do serviço de visitas previstas e de suas ações, com repositórios simulados. Os limites
 * dos campos são do DTO e estão em {@code VisitaRequestValidacaoTest}.
 */
class VisitaServiceTest {

    private VisitaPlanejadaRepository visitaRepository;
    private AcaoRepository acaoRepository;
    private VisitaService service;
    private Estudo estudo;
    /** "Banco" em memória das visitas do estudo; o repositório simulado lê e grava aqui. */
    private List<VisitaPlanejada> banco;

    @BeforeEach
    void configurar() {
        visitaRepository = mock(VisitaPlanejadaRepository.class);
        acaoRepository = mock(AcaoRepository.class);
        EstudoRepository estudoRepository = mock(EstudoRepository.class);
        service = new VisitaService(visitaRepository, acaoRepository, new EstudoAcesso(estudoRepository));

        estudo = new Estudo();
        estudo.setId(UUID.randomUUID());
        estudo.setStatus(StatusEstudo.PLANEJAMENTO);
        when(estudoRepository.findById(estudo.getId())).thenReturn(Optional.of(estudo));
        // Escritas carregam o estudo com bloqueio de escrita (concorrência com a transição de estado)
        when(estudoRepository.buscarComBloqueio(estudo.getId())).thenReturn(Optional.of(estudo));

        banco = new ArrayList<>();
        when(visitaRepository.findByEstudoIdOrderByOrdemAsc(estudo.getId())).thenAnswer(i -> new ArrayList<>(banco));
        when(visitaRepository.existsByEstudoIdAndDiaPrevisto(any(), org.mockito.ArgumentMatchers.anyInt()))
                .thenAnswer(i -> banco.stream().anyMatch(v -> v.getDiaPrevisto() == (int) i.getArgument(1)));
        when(visitaRepository.existsByEstudoIdAndDiaPrevistoAndIdNot(any(), org.mockito.ArgumentMatchers.anyInt(), any()))
                .thenAnswer(i -> banco.stream().anyMatch(
                        v -> v.getDiaPrevisto() == (int) i.getArgument(1) && !v.getId().equals(i.getArgument(2))));
        when(visitaRepository.saveAndFlush(any(VisitaPlanejada.class))).thenAnswer(i -> {
            VisitaPlanejada v = i.getArgument(0);
            if (v.getId() == null) {
                v.setId(UUID.randomUUID());
            }
            if (!banco.contains(v)) {
                banco.add(v);
            }
            return v;
        });
        when(acaoRepository.saveAndFlush(any(Acao.class))).thenAnswer(i -> {
            Acao a = i.getArgument(0);
            if (a.getId() == null) {
                a.setId(UUID.randomUUID());
            }
            return a;
        });
    }

    private static VisitaRequest pedido(String nome, int dia) {
        return new VisitaRequest(nome, dia, 0);
    }

    /** Visita já "gravada" no banco simulado, com o id e a ordem informados. */
    private VisitaPlanejada existente(String nome, int dia) {
        VisitaPlanejada v = new VisitaPlanejada();
        v.setId(UUID.randomUUID());
        v.setEstudo(estudo);
        v.setNome(nome);
        v.setDiaPrevisto(dia);
        banco.add(v);
        when(visitaRepository.findById(v.getId())).thenReturn(Optional.of(v));
        when(visitaRepository.buscarEstudoId(v.getId())).thenReturn(Optional.of(estudo.getId()));
        return v;
    }

    private static AcaoRequest pedidoAcao(String nome, String descricao) {
        return new AcaoRequest(nome, descricao, CategoriaAcao.EXAME_LABORATORIAL);
    }

    // ---------- visitas

    @Test
    @DisplayName("Cria a visita com o nome aparado e a ordem 1 quando é a primeira")
    void criaPrimeiraVisita() {
        VisitaResponse criada = service.criar(estudo.getId(), pedido("  Triagem  ", 0));

        assertThat(criada.nome()).isEqualTo("Triagem");
        assertThat(criada.diaPrevisto()).isZero();
        assertThat(criada.ordem()).isEqualTo(1);
        assertThat(criada.acoes()).isEmpty();
    }

    @Test
    @DisplayName("A ordem segue o dia previsto, não a ordem de cadastro")
    void ordemSegueODia() {
        VisitaResponse tardia = service.criar(estudo.getId(), pedido("Retorno", 30));
        VisitaResponse inicial = service.criar(estudo.getId(), pedido("Triagem", 0));
        VisitaResponse meio = service.criar(estudo.getId(), pedido("Seguimento", 14));

        assertThat(inicial.ordem()).isEqualTo(1);
        assertThat(meio.ordem()).isEqualTo(2);
        assertThat(banco).extracting(VisitaPlanejada::getNome, VisitaPlanejada::getOrdem)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("Triagem", 1),
                        org.assertj.core.groups.Tuple.tuple("Seguimento", 2),
                        org.assertj.core.groups.Tuple.tuple("Retorno", 3));
        assertThat(tardia.id()).isNotNull();
    }

    @Test
    @DisplayName("Dois dias previstos iguais no mesmo estudo são recusados (409) e nada é gravado")
    void diaRepetidoNaCriacao() {
        service.criar(estudo.getId(), pedido("Triagem", 7));

        assertThatThrownBy(() -> service.criar(estudo.getId(), pedido("Outra", 7)))
                .isInstanceOfSatisfying(ConflitoRegraException.class,
                        erro -> assertThat(erro.getCodigo()).isEqualTo("VISITA_DIA_DUPLICADO"));
        assertThat(banco).hasSize(1);
    }

    @Test
    @DisplayName("Editar a visita mantendo o próprio dia não é conflito; usar o dia de outra visita é")
    void diaRepetidoNaEdicao() {
        VisitaPlanejada a = existente("A", 0);
        VisitaPlanejada b = existente("B", 10);

        VisitaResponse editada = service.atualizar(estudo.getId(), b.getId(), pedido("B renomeada", 10));
        assertThat(editada.nome()).isEqualTo("B renomeada");

        assertThatThrownBy(() -> service.atualizar(estudo.getId(), b.getId(), pedido("B", 0)))
                .isInstanceOfSatisfying(ConflitoRegraException.class,
                        erro -> assertThat(erro.getCodigo()).isEqualTo("VISITA_DIA_DUPLICADO"));
        assertThat(a.getDiaPrevisto()).isZero();
    }

    @Test
    @DisplayName("Mudar o dia reordena as visitas")
    void editarODiaReordena() {
        VisitaPlanejada a = existente("A", 0);
        VisitaPlanejada b = existente("B", 10);
        a.setOrdem(1);
        b.setOrdem(2);

        service.atualizar(estudo.getId(), a.getId(), pedido("A", 20));

        assertThat(b.getOrdem()).isEqualTo(1);
        assertThat(a.getOrdem()).isEqualTo(2);
    }

    @Test
    @DisplayName("Excluir remove a visita e reordena as que ficaram")
    void excluirReordena() {
        VisitaPlanejada a = existente("A", 0);
        VisitaPlanejada b = existente("B", 10);
        VisitaPlanejada c = existente("C", 20);
        a.setOrdem(1);
        b.setOrdem(2);
        c.setOrdem(3);
        org.mockito.Mockito.doAnswer(i -> banco.remove(i.<VisitaPlanejada>getArgument(0)))
                .when(visitaRepository).delete(any(VisitaPlanejada.class));

        service.excluir(estudo.getId(), b.getId());

        verify(visitaRepository).delete(b);
        assertThat(banco).containsExactly(a, c);
        assertThat(c.getOrdem()).isEqualTo(2);
    }

    @Test
    @DisplayName("A visita carrega as suas ações e elas saem na resposta (remover a visita as leva junto, por cascata)")
    void acoesDaVisita() {
        VisitaPlanejada visita = existente("Triagem", 0);
        service.criarAcao(visita.getId(), pedidoAcao("Hemograma", "Coleta em jejum"));

        VisitaResponse listada = service.listar(estudo.getId()).get(0);

        assertThat(listada.acoes()).extracting(AcaoResponse::nome).containsExactly("Hemograma");
        assertThat(visita.getAcoes()).hasSize(1);
    }

    // ---------- estado bloqueado

    @ParameterizedTest(name = "estudo em {0}")
    @EnumSource(value = StatusEstudo.class, names = "PLANEJAMENTO", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("Criar, editar e excluir visitas e ações fora de planejamento é conflito, em qualquer outro estado")
    void foraDePlanejamento(StatusEstudo estado) {
        VisitaPlanejada visita = existente("Triagem", 0);
        Acao acao = new Acao();
        acao.setId(UUID.randomUUID());
        acao.setVisita(visita);
        visita.getAcoes().add(acao);
        when(acaoRepository.findById(acao.getId())).thenReturn(Optional.of(acao));
        estudo.setStatus(estado);

        List<Runnable> operacoes = List.of(
                () -> service.criar(estudo.getId(), pedido("Nova", 5)),
                () -> service.atualizar(estudo.getId(), visita.getId(), pedido("Triagem", 0)),
                () -> service.excluir(estudo.getId(), visita.getId()),
                () -> service.criarAcao(visita.getId(), pedidoAcao("Exame", null)),
                () -> service.atualizarAcao(visita.getId(), acao.getId(), pedidoAcao("Exame", null)),
                () -> service.excluirAcao(visita.getId(), acao.getId()));
        for (Runnable operacao : operacoes) {
            assertThatThrownBy(operacao::run).isInstanceOfSatisfying(ConflitoRegraException.class,
                    erro -> assertThat(erro.getCodigo()).isEqualTo("ESTUDO_FORA_DE_PLANEJAMENTO"));
        }
        verify(visitaRepository, never()).delete(any(VisitaPlanejada.class));
        verify(acaoRepository, never()).delete(any(Acao.class));
        assertThat(banco).hasSize(1);
    }

    @Test
    @DisplayName("Listar continua permitido fora de planejamento")
    void listarForaDePlanejamento() {
        existente("Triagem", 0);
        estudo.setStatus(StatusEstudo.ENCERRADO);

        assertThat(service.listar(estudo.getId())).hasSize(1);
    }

    // ---------- ações

    @Test
    @DisplayName("Cria a ação com nome aparado e descrição em branco virando nula")
    void criaAcao() {
        VisitaPlanejada visita = existente("Triagem", 0);

        AcaoResponse comTexto = service.criarAcao(visita.getId(), pedidoAcao("  Hemograma ", "  Em jejum  "));
        AcaoResponse semTexto = service.criarAcao(visita.getId(), pedidoAcao("Questionário", "   "));

        assertThat(comTexto.nome()).isEqualTo("Hemograma");
        assertThat(comTexto.descricao()).isEqualTo("Em jejum");
        assertThat(comTexto.categoria()).isEqualTo(CategoriaAcao.EXAME_LABORATORIAL);
        assertThat(semTexto.descricao()).isNull();
    }

    @Test
    @DisplayName("Atualiza e exclui a ação da própria visita")
    void atualizaEExcluiAcao() {
        VisitaPlanejada visita = existente("Triagem", 0);
        AcaoResponse criada = service.criarAcao(visita.getId(), pedidoAcao("Hemograma", null));
        Acao entidade = visita.getAcoes().get(0);
        when(acaoRepository.findById(criada.id())).thenReturn(Optional.of(entidade));

        AcaoResponse editada = service.atualizarAcao(visita.getId(), criada.id(), pedidoAcao("Glicemia", "Jejum"));
        assertThat(editada.nome()).isEqualTo("Glicemia");

        service.excluirAcao(visita.getId(), criada.id());
        verify(acaoRepository).delete(entidade);
        assertThat(visita.getAcoes()).isEmpty();
    }

    // ---------- não encontrados

    @Test
    @DisplayName("Estudo inexistente, visita inexistente ou de outro estudo, e ação de outra visita: recurso não encontrado")
    void naoEncontrados() {
        UUID inexistente = UUID.randomUUID();
        assertThatThrownBy(() -> service.listar(inexistente)).isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> service.criar(inexistente, pedido("X", 0)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> service.atualizar(estudo.getId(), inexistente, pedido("X", 0)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> service.excluir(estudo.getId(), inexistente))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> service.criarAcao(inexistente, pedidoAcao("X", null)))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        Estudo outro = new Estudo();
        outro.setId(UUID.randomUUID());
        VisitaPlanejada deOutro = new VisitaPlanejada();
        deOutro.setId(UUID.randomUUID());
        deOutro.setEstudo(outro);
        when(visitaRepository.findById(deOutro.getId())).thenReturn(Optional.of(deOutro));
        assertThatThrownBy(() -> service.excluir(estudo.getId(), deOutro.getId()))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        VisitaPlanejada minha = existente("Minha", 0);
        Acao acaoDeOutra = new Acao();
        acaoDeOutra.setId(UUID.randomUUID());
        acaoDeOutra.setVisita(deOutro);
        when(acaoRepository.findById(acaoDeOutra.getId())).thenReturn(Optional.of(acaoDeOutra));
        assertThatThrownBy(() -> service.excluirAcao(minha.getId(), acaoDeOutra.getId()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> service.atualizarAcao(minha.getId(), UUID.randomUUID(), pedidoAcao("X", null)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
