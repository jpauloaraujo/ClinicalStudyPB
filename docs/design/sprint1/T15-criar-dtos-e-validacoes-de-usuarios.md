# T15 · Criar DTOs e validações de usuários

> **Sprint 1** · PBI-04 · RF01 Cadastro e gerenciamento de usuários  
> **Responsável:** I3 · **Estimativa:** 3h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T08  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Definir os objetos de entrada e saída da API de usuários, com validações, sem nunca expor a entidade JPA nem a senha.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Documento anterior: `docs/sprint1/task-dtos-validacoes-usuarios.md`. O tratamento de erros é do `ApiExceptionHandler` (400 com `{"erro": "campo: mensagem"}`).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/UsuarioCriacaoRequest.java` | Existente | Entrada do cadastro |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/UsuarioAtualizacaoRequest.java` | Existente | Entrada da atualização (senha opcional, `ativo` obrigatório) |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/UsuarioResponse.java` | Existente | Saída: id, nome, e-mail, perfil e ativo; nunca a senha |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/UsuarioController.java` | Existente | Recebe os DTOs com `@Valid` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/UsuarioService.java` | Existente | Converte DTO em entidade, checa e-mail duplicado e grava a senha com BCrypt |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/UsuarioDtoValidationTest.java` | Existente | Casos válidos e inválidos |

## 4. O que fazer em cada arquivo

### Regras de validação

- `nome`: obrigatório, até 120.
- `email`: obrigatório, formato válido, até 180, único.
- `senha` no cadastro: obrigatória, 8 a 100; na atualização: opcional e, se enviada, 8 a 100.
- `perfil`: obrigatório, um valor de `PerfilUsuario`. `ativo`: obrigatório na atualização.

### Exemplos

- `POST /api/usuarios` com `{nome, email, senha, perfil}`.
- `PUT /api/usuarios/{id}` com `{nome, email, senha: null, perfil, ativo}`; `senha: null` mantém a senha atual.

### Como testar

- `./mvnw -Dtest=UsuarioDtoValidationTest test` e depois `./mvnw test`.

## 6. Testes

- `UsuarioDtoValidationTest`.

## 7. Critérios de aceite

- [ ] Dados inválidos respondem 400 com a mensagem do campo.
- [ ] A resposta nunca contém a senha ou o hash.

## 8. Cuidados e fora de escopo

- Mensagens de validação em português, como as dos demais DTOs.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
