package UEPB.ESWII.ClinicalStudyPB;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import UEPB.ESWII.ClinicalStudyPB.controller.dto.UsuarioAtualizacaoRequest;
import UEPB.ESWII.ClinicalStudyPB.controller.dto.UsuarioCriacaoRequest;
import UEPB.ESWII.ClinicalStudyPB.domain.enums.PerfilUsuario;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class UsuarioDtoValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void configurarValidador() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void fecharValidador() {
        factory.close();
    }

    @Test
    @DisplayName("Cadastro: aceita dados válidos")
    void aceitaDadosValidosNoCadastro() {
        UsuarioCriacaoRequest dados = new UsuarioCriacaoRequest(
                "Helena Costa", "helena@example.com", "senha-segura-123", PerfilUsuario.PESQUISADOR);

        assertThat(validator.validate(dados)).isEmpty();
    }

    @Test
    @DisplayName("Cadastro: rejeita nome vazio, e-mail inválido, senha curta e perfil ausente")
    void rejeitaDadosInvalidosNoCadastro() {
        UsuarioCriacaoRequest dados = new UsuarioCriacaoRequest(
                " ", "email-invalido", "123", null);

        Set<ConstraintViolation<UsuarioCriacaoRequest>> erros = validator.validate(dados);

        assertThat(erros).extracting(erro -> erro.getPropertyPath().toString())
                .contains("nome", "email", "senha", "perfil");
    }

    @Test
    @DisplayName("Atualização: permite omitir a senha")
    void permiteOmitirSenhaNaAtualizacao() {
        UsuarioAtualizacaoRequest dados = new UsuarioAtualizacaoRequest(
                "Helena Costa", "helena@example.com", null, PerfilUsuario.PESQUISADOR, true);

        assertThat(validator.validate(dados)).isEmpty();
    }

    @Test
    @DisplayName("Atualização: rejeita senha informada com menos de oito caracteres")
    void rejeitaSenhaCurtaNaAtualizacao() {
        UsuarioAtualizacaoRequest dados = new UsuarioAtualizacaoRequest(
                "Helena Costa", "helena@example.com", "123", PerfilUsuario.PESQUISADOR, true);

        assertThat(validator.validate(dados))
                .extracting(erro -> erro.getPropertyPath().toString())
                .contains("senha");
    }
}
