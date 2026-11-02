package UEPB.ESWII.ClinicalStudyPB.controller;

import java.util.Map;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import UEPB.ESWII.ClinicalStudyPB.service.ConflitoRegraException;
import UEPB.ESWII.ClinicalStudyPB.service.RecursoNaoEncontradoException;
import UEPB.ESWII.ClinicalStudyPB.service.RegraNegocioException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> tratarCredenciaisInvalidas(BadCredentialsException ex) {
        return resposta(HttpStatus.UNAUTHORIZED, "CREDENCIAIS_INVALIDAS", ex.getMessage(), List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> tratarAcessoNegado(AccessDeniedException ex) {
        return resposta(HttpStatus.FORBIDDEN, "ACESSO_NEGADO", "Acesso negado para o seu perfil", List.of());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> tratarNaoEncontrado(RecursoNaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, "RECURSO_NAO_ENCONTRADO", ex.getMessage(), List.of());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Map<String, Object>> tratarRegraNegocio(RegraNegocioException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", ex.getMessage(), List.of());
    }

    // Regra de recrutamento violada: 409 com um código estável para a tela explicar o motivo
    @ExceptionHandler(ConflitoRegraException.class)
    public ResponseEntity<Map<String, Object>> tratarConflito(ConflitoRegraException ex) {
        return resposta(HttpStatus.CONFLICT, ex.getCodigo(), ex.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> tratarValidacao(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .findFirst().map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .orElse("Dados inválidos");
        List<Map<String, String>> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> Map.of("campo", erro.getField(), "erro",
                        erro.getDefaultMessage() == null ? "Valor inválido" : erro.getDefaultMessage()))
                .toList();
        return resposta(HttpStatus.BAD_REQUEST, "VALIDACAO_CAMPOS", mensagem, campos);
    }

    // JSON malformado, enum inválido (perfil, status), data fora do formato ISO...
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> tratarCorpoIlegivel(HttpMessageNotReadableException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "CORPO_INVALIDO", "Corpo da requisição inválido ou malformado", List.of());
    }

    // Ex.: UUID inválido no caminho da URL
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> tratarParametroInvalido(MethodArgumentTypeMismatchException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "PARAMETRO_INVALIDO", "Valor inválido para o parâmetro " + ex.getName(), List.of());
    }

    private ResponseEntity<Map<String, Object>> resposta(HttpStatus status, String codigo, String mensagem,
            List<Map<String, String>> campos) {
        // Campos antigos preservados para a web; contrato da Sprint 2 também expõe erros por campo.
        return ResponseEntity.status(status).body(Map.of("status", status.value(),
                "codigoRegra", codigo, "mensagem", mensagem, "errosCampo", campos,
                "erro", mensagem, "codigo", codigo));
    }
}
