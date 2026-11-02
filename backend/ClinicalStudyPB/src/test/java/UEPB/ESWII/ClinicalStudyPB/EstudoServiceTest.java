package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.EstudoRequest;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.PerfilUsuario;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.repository.CandidatoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.EstudoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.UsuarioRepository;
import UEPB.ESWII.ClinicalStudyPB.service.EstudoService;
import UEPB.ESWII.ClinicalStudyPB.service.RecursoNaoEncontradoException;
import UEPB.ESWII.ClinicalStudyPB.service.RegraNegocioException;

/** T30 (PBI-07 / RF04): regras de negócio do serviço de estudos, isoladas do banco. */
class EstudoServiceTest {

    private EstudoRepository estudoRepository;
    private UsuarioRepository usuarioRepository;
    private EstudoService service;
    private Usuario pesquisador;

    @BeforeEach
    void configurar() {
        estudoRepository = mock(EstudoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        service = new EstudoService(estudoRepository, usuarioRepository, mock(CandidatoRepository.class));

        pesquisador = new Usuario();
        pesquisador.setId(UUID.randomUUID());
        pesquisador.setNome("Pesquisadora Fictícia");
        pesquisador.setPerfil(PerfilUsuario.PESQUISADOR);
        when(usuarioRepository.findById(pesquisador.getId())).thenReturn(Optional.of(pesquisador));
        when(estudoRepository.save(any(Estudo.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    private EstudoRequest requisicao(LocalDate inicio, LocalDate fim) {
        return new EstudoRequest("Estudo", "Objetivo", pesquisador.getId(), inicio, fim, 20, StatusEstudo.PLANEJAMENTO);
    }

    @Test
    @DisplayName("Cria estudo preenchendo todos os campos e o responsável")
    void criaEstudoComTodosOsCampos() {
        Estudo criado = service.criar(requisicao(LocalDate.of(2026, 11, 1), LocalDate.of(2027, 1, 1)));

        assertThat(criado.getTitulo()).isEqualTo("Estudo");
        assertThat(criado.getObjetivo()).isEqualTo("Objetivo");
        assertThat(criado.getResponsavel()).isSameAs(pesquisador);
        assertThat(criado.getLimiteParticipantes()).isEqualTo(20);
        assertThat(criado.getStatus()).isEqualTo(StatusEstudo.PLANEJAMENTO);
    }

    @Test
    @DisplayName("Aceita data final igual à inicial e datas ausentes")
    void aceitaDatasIguaisOuAusentes() {
        LocalDate dia = LocalDate.of(2026, 11, 1);
        assertThat(service.criar(requisicao(dia, dia)).getDataFim()).isEqualTo(dia);
        assertThat(service.criar(requisicao(null, null)).getDataInicio()).isNull();
    }

    @Test
    @DisplayName("Rejeita data final anterior à data inicial sem salvar")
    void rejeitaDataFinalAnterior() {
        EstudoRequest dados = requisicao(LocalDate.of(2027, 1, 1), LocalDate.of(2026, 11, 1));

        assertThatThrownBy(() -> service.criar(dados))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("A data final não pode ser anterior à data inicial");
        verify(estudoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rejeita responsável inexistente")
    void rejeitaResponsavelInexistente() {
        UUID desconhecido = UUID.randomUUID();
        EstudoRequest dados = new EstudoRequest("Estudo", "Objetivo", desconhecido, null, null, 5, StatusEstudo.PLANEJAMENTO);

        assertThatThrownBy(() -> service.criar(dados))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining(desconhecido.toString());
    }

    @Test
    @DisplayName("Atualizar ou excluir estudo inexistente gera recurso não encontrado")
    void estudoInexistente() {
        UUID id = UUID.randomUUID();
        when(estudoRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(id, requisicao(null, null)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> service.excluir(id))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Atualiza um estudo existente com os novos dados")
    void atualizaEstudoExistente() {
        Estudo existente = new Estudo();
        existente.setId(UUID.randomUUID());
        when(estudoRepository.buscarComBloqueio(existente.getId())).thenReturn(Optional.of(existente));

        EstudoRequest dados = new EstudoRequest("Novo título", "Novo objetivo", pesquisador.getId(),
                null, null, 99, StatusEstudo.PLANEJAMENTO);
        Estudo atualizado = service.atualizar(existente.getId(), dados);

        assertThat(atualizado.getId()).isEqualTo(existente.getId());
        assertThat(atualizado.getTitulo()).isEqualTo("Novo título");
        assertThat(atualizado.getStatus()).isEqualTo(StatusEstudo.PLANEJAMENTO);
    }
}
