package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.CriterioRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.CriterioResponse;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.TipoCriterio;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Criterio;
import UEPB.ESWII.ClinicalStudyPB.domain.model.CriterioExclusao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.CriterioInclusao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.repository.CriterioRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.EstudoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.RespostaAvaliacaoRepository;
import UEPB.ESWII.ClinicalStudyPB.service.ConflitoRegraException;
import UEPB.ESWII.ClinicalStudyPB.service.CriterioService;
import UEPB.ESWII.ClinicalStudyPB.service.EstudoAcesso;
import UEPB.ESWII.ClinicalStudyPB.service.RecursoNaoEncontradoException;
import UEPB.ESWII.ClinicalStudyPB.service.RegraNegocioException;

/**
 * T47 (RF05): regras do serviço de critérios com repositórios simulados. Os limites de tamanho da descrição
 * e o tipo obrigatório são regras do DTO e estão em {@code CriterioRequestValidacaoTest}.
 */
class CriterioServiceTest {

    private CriterioRepository criterioRepository;
    private RespostaAvaliacaoRepository respostaRepository;
    private CriterioService service;
    private Estudo estudo;

    @BeforeEach
    void configurar() {
        criterioRepository = mock(CriterioRepository.class);
        respostaRepository = mock(RespostaAvaliacaoRepository.class);
        EstudoRepository estudoRepository = mock(EstudoRepository.class);
        service = new CriterioService(criterioRepository, respostaRepository, new EstudoAcesso(estudoRepository));

        estudo = new Estudo();
        estudo.setId(UUID.randomUUID());
        estudo.setStatus(StatusEstudo.PLANEJAMENTO);
        when(estudoRepository.findById(estudo.getId())).thenReturn(Optional.of(estudo));
        when(estudoRepository.buscarComBloqueio(estudo.getId())).thenReturn(Optional.of(estudo));
        when(criterioRepository.findByEstudoIdOrderByTituloAsc(estudo.getId())).thenReturn(List.of());
        when(criterioRepository.save(any(Criterio.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    private static CriterioRequest pedido(TipoCriterio tipo, String titulo, String descricao) {
        return new CriterioRequest(tipo, titulo, descricao, null, null);
    }

    private CriterioInclusao existente(String titulo, String descricao) {
        CriterioInclusao criterio = new CriterioInclusao();
        criterio.setId(UUID.randomUUID());
        criterio.setEstudo(estudo);
        criterio.setTitulo(titulo);
        criterio.setDescricao(descricao);
        return criterio;
    }

    @Test
    @DisplayName("Cria um critério de inclusão com peso 1 e determinante por padrão, aparando espaços")
    void criaInclusaoComPadroes() {
        CriterioResponse criado = service.criar(estudo.getId(), pedido(TipoCriterio.INCLUSAO, "  Idade  ", "  Maior de 18 anos "));

        assertThat(criado.tipo()).isEqualTo(TipoCriterio.INCLUSAO);
        assertThat(criado.titulo()).isEqualTo("Idade");
        assertThat(criado.descricao()).isEqualTo("Maior de 18 anos");
        assertThat(criado.peso()).isEqualTo(1f);
        assertThat(criado.determinante()).isTrue();
    }

    @Test
    @DisplayName("Cria um critério de exclusão sem peso nem flag determinante")
    void criaExclusaoSemPeso() {
        CriterioResponse criado = service.criar(estudo.getId(),
                new CriterioRequest(TipoCriterio.EXCLUSAO, "Gestante", "Em gestação", 5f, false));

        assertThat(criado.tipo()).isEqualTo(TipoCriterio.EXCLUSAO);
        assertThat(criado.peso()).isNull();
        assertThat(criado.determinante()).isNull();
    }

    @Test
    @DisplayName("Descrição repetida no mesmo tipo (ignorando maiúsculas e espaços) é conflito e nada é gravado")
    void descricaoRepetida() {
        when(criterioRepository.findByEstudoIdOrderByTituloAsc(estudo.getId()))
                .thenReturn(List.of(existente("Idade", "Maior de 18 anos")));

        assertThatThrownBy(() -> service.criar(estudo.getId(), pedido(TipoCriterio.INCLUSAO, "Adulto", "  MAIOR DE 18 ANOS ")))
                .isInstanceOfSatisfying(ConflitoRegraException.class,
                        erro -> assertThat(erro.getCodigo()).isEqualTo("CRITERIO_DUPLICADO"));
        verify(criterioRepository, never()).save(any());
    }

    @Test
    @DisplayName("A mesma descrição em outro tipo é aceita")
    void mesmaDescricaoEmOutroTipo() {
        when(criterioRepository.findByEstudoIdOrderByTituloAsc(estudo.getId()))
                .thenReturn(List.of(existente("Idade", "Maior de 18 anos")));

        CriterioResponse criado = service.criar(estudo.getId(), pedido(TipoCriterio.EXCLUSAO, "Adulto", "Maior de 18 anos"));

        assertThat(criado.tipo()).isEqualTo(TipoCriterio.EXCLUSAO);
    }

    @Test
    @DisplayName("Título repetido no mesmo tipo é conflito")
    void tituloRepetido() {
        when(criterioRepository.findByEstudoIdOrderByTituloAsc(estudo.getId()))
                .thenReturn(List.of(existente("Idade", "Maior de 18 anos")));

        assertThatThrownBy(() -> service.criar(estudo.getId(), pedido(TipoCriterio.INCLUSAO, "idade", "Outra descrição")))
                .isInstanceOfSatisfying(ConflitoRegraException.class,
                        erro -> assertThat(erro.getCodigo()).isEqualTo("CRITERIO_DUPLICADO"));
    }

    @Test
    @DisplayName("Ao editar, o próprio critério não conta como repetido")
    void edicaoIgnoraOProprioCriterio() {
        CriterioInclusao atual = existente("Idade", "Maior de 18 anos");
        when(criterioRepository.findByEstudoIdOrderByTituloAsc(estudo.getId())).thenReturn(List.of(atual));
        when(criterioRepository.findById(atual.getId())).thenReturn(Optional.of(atual));

        CriterioResponse editado = service.atualizar(estudo.getId(), atual.getId(),
                pedido(TipoCriterio.INCLUSAO, "Idade", "Maior de 18 anos"));

        assertThat(editado.titulo()).isEqualTo("Idade");
    }

    @ParameterizedTest(name = "estudo em {0}")
    @EnumSource(value = StatusEstudo.class, names = "PLANEJAMENTO", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("Criar, editar e excluir fora de planejamento é conflito, em qualquer outro estado")
    void foraDePlanejamento(StatusEstudo estado) {
        estudo.setStatus(estado);
        CriterioInclusao atual = existente("Idade", "Maior de 18 anos");
        when(criterioRepository.findById(atual.getId())).thenReturn(Optional.of(atual));

        assertThatThrownBy(() -> service.criar(estudo.getId(), pedido(TipoCriterio.INCLUSAO, "Novo", "Descrição nova")))
                .isInstanceOfSatisfying(ConflitoRegraException.class,
                        erro -> assertThat(erro.getCodigo()).isEqualTo("ESTUDO_FORA_DE_PLANEJAMENTO"));
        assertThatThrownBy(() -> service.atualizar(estudo.getId(), atual.getId(), pedido(TipoCriterio.INCLUSAO, "Idade", "Maior de 18 anos")))
                .isInstanceOf(ConflitoRegraException.class);
        assertThatThrownBy(() -> service.excluir(estudo.getId(), atual.getId()))
                .isInstanceOf(ConflitoRegraException.class);
        verify(criterioRepository, never()).save(any());
        verify(criterioRepository, never()).delete(any());
    }

    @Test
    @DisplayName("O tipo de um critério não pode ser trocado na edição")
    void tipoNaoMuda() {
        CriterioInclusao atual = existente("Idade", "Maior de 18 anos");
        when(criterioRepository.findById(atual.getId())).thenReturn(Optional.of(atual));

        assertThatThrownBy(() -> service.atualizar(estudo.getId(), atual.getId(),
                pedido(TipoCriterio.EXCLUSAO, "Idade", "Maior de 18 anos")))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("Estudo inexistente e critério de outro estudo geram recurso não encontrado")
    void naoEncontrados() {
        UUID inexistente = UUID.randomUUID();
        assertThatThrownBy(() -> service.listar(inexistente)).isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> service.criar(inexistente, pedido(TipoCriterio.INCLUSAO, "Idade", "Maior de 18 anos")))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        Estudo outro = new Estudo();
        outro.setId(UUID.randomUUID());
        CriterioExclusao deOutro = new CriterioExclusao();
        deOutro.setId(UUID.randomUUID());
        deOutro.setEstudo(outro);
        when(criterioRepository.findById(deOutro.getId())).thenReturn(Optional.of(deOutro));
        assertThatThrownBy(() -> service.excluir(estudo.getId(), deOutro.getId()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> service.excluir(estudo.getId(), UUID.randomUUID()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Critério já usado em avaliações não pode ser excluído")
    void criterioEmUso() {
        CriterioInclusao atual = existente("Idade", "Maior de 18 anos");
        when(criterioRepository.findById(atual.getId())).thenReturn(Optional.of(atual));
        when(respostaRepository.existsByCriterioId(atual.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.excluir(estudo.getId(), atual.getId()))
                .isInstanceOfSatisfying(ConflitoRegraException.class,
                        erro -> assertThat(erro.getCodigo()).isEqualTo("CRITERIO_EM_USO"));
        verify(criterioRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Exclui o critério que não foi usado")
    void excluiCriterioLivre() {
        CriterioInclusao atual = existente("Idade", "Maior de 18 anos");
        when(criterioRepository.findById(atual.getId())).thenReturn(Optional.of(atual));

        service.excluir(estudo.getId(), atual.getId());

        verify(criterioRepository).delete(atual);
    }
}
