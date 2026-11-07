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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.CandidatoRequest;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.SituacaoCandidato;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.StatusEstudo;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Candidato;
import UEPB.ESWII.ClinicalStudyPB.domain.model.Estudo;
import UEPB.ESWII.ClinicalStudyPB.repository.AvaliacaoElegibilidadeRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.CandidatoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.ConsentimentoRepository;
import UEPB.ESWII.ClinicalStudyPB.repository.EstudoRepository;
import UEPB.ESWII.ClinicalStudyPB.service.CandidatoService;
import UEPB.ESWII.ClinicalStudyPB.service.ConflitoRegraException;
import UEPB.ESWII.ClinicalStudyPB.service.EstudoAcesso;
import UEPB.ESWII.ClinicalStudyPB.service.RecursoNaoEncontradoException;
import jakarta.validation.Validation;

/** T53: regras de candidatos isoladas de HTTP e banco de dados. */
class CandidatoServiceTest {

    private static final LocalDate NASCIMENTO = LocalDate.of(1990, 4, 12);
    private CandidatoRepository candidatoRepository;
    private EstudoRepository estudoRepository;
    private CandidatoService service;
    private Estudo estudo;

    @BeforeEach
    void configurar() {
        candidatoRepository = mock(CandidatoRepository.class);
        estudoRepository = mock(EstudoRepository.class);
        service = new CandidatoService(candidatoRepository, mock(AvaliacaoElegibilidadeRepository.class),
                mock(ConsentimentoRepository.class), new EstudoAcesso(estudoRepository));
        estudo = new Estudo();
        estudo.setId(UUID.randomUUID());
        estudo.setStatus(StatusEstudo.RECRUTAMENTO);
        when(estudoRepository.buscarComBloqueio(estudo.getId())).thenReturn(Optional.of(estudo));
    }

    @Test
    void recusaDuplicidadeSemSalvar() {
        when(candidatoRepository.existsByEstudoIdAndNomeFicticioIgnoreCaseAndDataNascimento(
                estudo.getId(), "Candidato fictício", NASCIMENTO)).thenReturn(true);

        assertThatThrownBy(() -> service.criar(estudo.getId(), dados(" Candidato fictício ")))
                .isInstanceOfSatisfying(ConflitoRegraException.class,
                        erro -> assertThat(erro.getCodigo()).isEqualTo("CANDIDATO_DUPLICADO"));
        verify(candidatoRepository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = StatusEstudo.class, names = {"PLANEJAMENTO", "EXECUCAO", "ENCERRADO"})
    void naoCadastraForaDeRecrutamento(StatusEstudo estado) {
        estudo.setStatus(estado);

        assertThatThrownBy(() -> service.criar(estudo.getId(), dados("Candidato fictício")))
                .isInstanceOfSatisfying(ConflitoRegraException.class,
                        erro -> assertThat(erro.getCodigo()).isEqualTo("ESTUDO_FORA_DE_RECRUTAMENTO"));
        verify(candidatoRepository, never()).save(any());
    }

    @Test
    void naoEditaCandidatoIncluidoNemAlteraSeusDados() {
        Candidato candidato = candidato("Nome original", SituacaoCandidato.INCLUIDO);
        when(candidatoRepository.findById(candidato.getId())).thenReturn(Optional.of(candidato));

        assertThatThrownBy(() -> service.atualizar(candidato.getId(), dados("Nome alterado")))
                .isInstanceOfSatisfying(ConflitoRegraException.class,
                        erro -> assertThat(erro.getCodigo()).isEqualTo("CANDIDATO_JA_INCLUIDO"));
        assertThat(candidato.getNomeFicticio()).isEqualTo("Nome original");
        verify(candidatoRepository, never()).save(any());
    }

    @Test
    void naoEditaParaDuplicarOutroCandidato() {
        Candidato candidato = candidato("Nome original", SituacaoCandidato.CADASTRADO);
        when(candidatoRepository.findById(candidato.getId())).thenReturn(Optional.of(candidato));
        when(candidatoRepository.existsByEstudoIdAndNomeFicticioIgnoreCaseAndDataNascimentoAndIdNot(
                estudo.getId(), "Nome repetido", NASCIMENTO, candidato.getId())).thenReturn(true);

        assertThatThrownBy(() -> service.atualizar(candidato.getId(), dados("Nome repetido")))
                .isInstanceOfSatisfying(ConflitoRegraException.class,
                        erro -> assertThat(erro.getCodigo()).isEqualTo("CANDIDATO_DUPLICADO"));
        assertThat(candidato.getNomeFicticio()).isEqualTo("Nome original");
        verify(candidatoRepository, never()).save(any());
    }

    @Test
    void atualizaAntesDaInclusaoPreservandoCodigoEEstudo() {
        Candidato candidato = candidato("Nome original", SituacaoCandidato.ELEGIVEL);
        when(candidatoRepository.findById(candidato.getId())).thenReturn(Optional.of(candidato));
        when(candidatoRepository.save(candidato)).thenReturn(candidato);

        var resposta = service.atualizar(candidato.getId(), dados(" Novo pseudônimo "));

        assertThat(resposta.nomeFicticio()).isEqualTo("Novo pseudônimo");
        assertThat(resposta.codigo()).isEqualTo("CAND-001");
        assertThat(resposta.estudoId()).isEqualTo(estudo.getId());
        assertThat(resposta.situacao()).isEqualTo(SituacaoCandidato.ELEGIVEL);
    }

    @Test
    void candidatoInexistenteRetornaNaoEncontrado() {
        UUID desconhecido = UUID.randomUUID();
        assertThatThrownBy(() -> service.buscar(desconhecido)).isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> service.atualizar(desconhecido, dados("Candidato fictício")))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void validacaoDeEntradaRecusaNascimentoHojeOuNoFuturo() {
        // A data é validada por Bean Validation no DTO antes de o controller chamar o serviço.
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            for (LocalDate data : new LocalDate[] {LocalDate.now(), LocalDate.now().plusDays(1)}) {
                var erros = validator.validate(new CandidatoRequest("Candidato fictício", data, null));
                assertThat(erros).anySatisfy(erro -> {
                    assertThat(erro.getPropertyPath().toString()).isEqualTo("dataNascimento");
                    assertThat(erro.getMessage()).isEqualTo("A data de nascimento deve estar no passado");
                });
            }
            assertThat(validator.validate(dados("Candidato fictício"))).isEmpty();
        }
    }

    private Candidato candidato(String nome, SituacaoCandidato situacao) {
        Candidato candidato = new Candidato();
        candidato.setId(UUID.randomUUID());
        candidato.setEstudo(estudo);
        candidato.setNomeFicticio(nome);
        candidato.setDataNascimento(NASCIMENTO);
        candidato.setCodigo("CAND-001");
        candidato.setSituacao(situacao);
        return candidato;
    }

    private static CandidatoRequest dados(String nome) {
        return new CandidatoRequest(nome, NASCIMENTO, null);
    }
}
