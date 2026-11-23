package UEPB.ESWII.ClinicalStudyPB.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.ContaParticipanteResponse;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.ContaVinculadaResponse;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.PerfilUsuario;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Candidato;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Participacao;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Usuario;
import UEPB.ESWII.ClinicalStudyPB.repository.CandidatoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.ParticipacaoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.UsuarioRepository;

/**
 * T111 (RF17): regras do vínculo da conta do participante com repositórios simulados, uma violação por vez e a
 * <b>ordem</b> das verificações do RQ-T111-03. Fica no pacote do serviço para alcançar o método {@code participacaoAtiva}
 * (ponto único da regra "participação ativa", que só muda quando a retirada da T101 entrar).
 */
class VinculoContaServiceTest {

    private ParticipacaoRepository participacaoRepository;
    private UsuarioRepository usuarioRepository;
    private CandidatoRepository candidatoRepository;
    private VinculoContaService service;

    private Participacao participacao;
    private Candidato candidato;
    private Usuario conta;

    @BeforeEach
    void configurar() {
        participacaoRepository = mock(ParticipacaoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        candidatoRepository = mock(CandidatoRepository.class);
        service = spy(new VinculoContaService(participacaoRepository, usuarioRepository, candidatoRepository));

        candidato = new Candidato();
        candidato.setId(UUID.randomUUID());
        participacao = new Participacao();
        participacao.setId(UUID.randomUUID());
        participacao.setCandidato(candidato);
        conta = new Usuario();
        conta.setId(UUID.randomUUID());
        conta.setNome("Participante (Ficticio)");
        conta.setEmail("participante@clinicalstudy.test");
        conta.setPerfil(PerfilUsuario.PARTICIPANTE);
        conta.setAtivo(true);

        when(participacaoRepository.buscarComCandidato(participacao.getId())).thenReturn(Optional.of(participacao));
        when(usuarioRepository.findById(conta.getId())).thenReturn(Optional.of(conta));
        when(candidatoRepository.existsByUsuarioId(conta.getId())).thenReturn(false);
        when(candidatoRepository.vincularConta(candidato.getId(), conta)).thenReturn(1);
    }

    private ContaVinculadaResponse vincular() {
        return service.vincular(participacao.getId(), conta.getId());
    }

    private void esperaConflito(String codigo) {
        assertThatThrownBy(this::vincular).isInstanceOf(ConflitoRegraException.class)
                .extracting("codigo").isEqualTo(codigo);
        verify(candidatoRepository, never()).vincularConta(any(), any());
    }

    @Test
    @DisplayName("Vínculo válido grava a conta no candidato e devolve só identificação (sem senha nem perfil)")
    void vinculoValido() {
        ContaVinculadaResponse resposta = vincular();

        assertThat(resposta).isEqualTo(new ContaVinculadaResponse(participacao.getId(), conta.getId(),
                "Participante (Ficticio)", "participante@clinicalstudy.test"));
        verify(candidatoRepository).vincularConta(candidato.getId(), conta);
        assertThat(ContaVinculadaResponse.class.getRecordComponents()).extracting("name")
                .containsExactly("participacaoId", "usuarioId", "nome", "email");
    }

    @Test
    @DisplayName("404: participação inexistente")
    void participacaoInexistente() {
        UUID outra = UUID.randomUUID();
        when(participacaoRepository.buscarComCandidato(outra)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.vincular(outra, conta.getId()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(candidatoRepository, never()).vincularConta(any(), any());
    }

    @Test
    @DisplayName("404: conta inexistente")
    void contaInexistente() {
        UUID outra = UUID.randomUUID();
        when(usuarioRepository.findById(outra)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.vincular(participacao.getId(), outra))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(candidatoRepository, never()).vincularConta(any(), any());
    }

    @Test
    @DisplayName("409 PARTICIPANTE_RETIRADO: participação que não está ativa (a regra fica em participacaoAtiva)")
    void participanteRetirado() {
        doReturn(false).when(service).participacaoAtiva(participacao);

        esperaConflito("PARTICIPANTE_RETIRADO");
    }

    @Test
    @DisplayName("T101: participação ATIVO conta como ativa e a RETIRADO não (pela situação real da participação)")
    void participacaoAtivaSegueASituacao() {
        assertThat(service.participacaoAtiva(participacao)).isTrue();

        participacao.retirar();

        assertThat(service.participacaoAtiva(participacao)).isFalse();
        esperaConflito("PARTICIPANTE_RETIRADO");
    }

    @Test
    @DisplayName("409 CONTA_NAO_E_PARTICIPANTE: conta de outro perfil")
    void contaDeOutroPerfil() {
        conta.setPerfil(PerfilUsuario.COORDENADOR);

        esperaConflito("CONTA_NAO_E_PARTICIPANTE");
    }

    @Test
    @DisplayName("409 CONTA_INATIVA: conta desativada")
    void contaInativa() {
        conta.setAtivo(false);

        esperaConflito("CONTA_INATIVA");
    }

    @Test
    @DisplayName("409 PARTICIPACAO_JA_TEM_CONTA: o participante já tem uma conta (inclusive a mesma conta reenviada)")
    void participacaoJaTemConta() {
        candidato.setUsuario(conta);

        esperaConflito("PARTICIPACAO_JA_TEM_CONTA");
    }

    @Test
    @DisplayName("409 CONTA_JA_VINCULADA: a conta já pertence a outro participante")
    void contaJaVinculada() {
        when(candidatoRepository.existsByUsuarioId(conta.getId())).thenReturn(true);

        esperaConflito("CONTA_JA_VINCULADA");
    }

    @Test
    @DisplayName("Corrida: o UPDATE condicional com 0 linhas vira 409 PARTICIPACAO_JA_TEM_CONTA, nunca sucesso")
    void atualizacaoSemLinhasAfetadas() {
        when(candidatoRepository.vincularConta(candidato.getId(), conta)).thenReturn(0);

        assertThatThrownBy(this::vincular).isInstanceOf(ConflitoRegraException.class)
                .extracting("codigo").isEqualTo("PARTICIPACAO_JA_TEM_CONTA");
    }

    @Test
    @DisplayName("Corrida: a violação do UNIQUE dentro do UPDATE vira 409 CONTA_JA_VINCULADA, nunca 500")
    void violacaoDoUniqueVira409() {
        when(candidatoRepository.vincularConta(candidato.getId(), conta))
                .thenThrow(new DataIntegrityViolationException("uk_candidatos_usuario"));

        assertThatThrownBy(this::vincular).isInstanceOf(ConflitoRegraException.class)
                .extracting("codigo").isEqualTo("CONTA_JA_VINCULADA");
    }

    // ---- a ordem das verificações do RQ-T111-03 ----

    @Test
    @DisplayName("Ordem 1: retirado vem antes de perfil e de conta inativa")
    void ordemRetiradoAntesDeTudo() {
        doReturn(false).when(service).participacaoAtiva(participacao);
        conta.setPerfil(PerfilUsuario.PESQUISADOR);
        conta.setAtivo(false);
        candidato.setUsuario(conta);

        esperaConflito("PARTICIPANTE_RETIRADO");
    }

    @Test
    @DisplayName("Ordem 2: perfil errado vem antes de conta inativa")
    void ordemPerfilAntesDeInativa() {
        conta.setPerfil(PerfilUsuario.ADMINISTRADOR);
        conta.setAtivo(false);

        esperaConflito("CONTA_NAO_E_PARTICIPANTE");
    }

    @Test
    @DisplayName("Ordem 3: conta inativa vem antes de 'participação já tem conta'")
    void ordemInativaAntesDeJaTemConta() {
        conta.setAtivo(false);
        candidato.setUsuario(new Usuario());

        esperaConflito("CONTA_INATIVA");
    }

    @Test
    @DisplayName("Ordem 4: 'participação já tem conta' vem antes de 'conta já vinculada'")
    void ordemJaTemContaAntesDeContaVinculada() {
        candidato.setUsuario(new Usuario());
        when(candidatoRepository.existsByUsuarioId(conta.getId())).thenReturn(true);

        esperaConflito("PARTICIPACAO_JA_TEM_CONTA");
    }

    // ---- consulta e contas disponíveis ----

    @Test
    @DisplayName("Consulta devolve a conta vinculada, sem dados de segurança")
    void consultaComConta() {
        candidato.setUsuario(conta);

        assertThat(service.consultar(participacao.getId())).isEqualTo(new ContaVinculadaResponse(participacao.getId(),
                conta.getId(), "Participante (Ficticio)", "participante@clinicalstudy.test"));
    }

    @Test
    @DisplayName("Consulta sem conta vinculada ou de participação inexistente dá 404")
    void consultaSemConta() {
        assertThatThrownBy(() -> service.consultar(participacao.getId()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        UUID outra = UUID.randomUUID();
        when(participacaoRepository.buscarComCandidato(outra)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.consultar(outra)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Contas disponíveis vêm do repositório, já filtradas e ordenadas")
    void contasDisponiveis() {
        List<ContaParticipanteResponse> lista = List.of(
                new ContaParticipanteResponse(UUID.randomUUID(), "Ana", "ana@clinicalstudy.test"));
        when(usuarioRepository.listarContasDisponiveis()).thenReturn(lista);

        assertThat(service.contasDisponiveis()).isEqualTo(lista);
    }
}
