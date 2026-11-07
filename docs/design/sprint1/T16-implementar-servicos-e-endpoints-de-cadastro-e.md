# T16 · Implementar serviços e endpoints de cadastro e gerenciamento de usuários

> **Sprint 1** · PBI-04 · RF01 Cadastro e gerenciamento de usuários  
> **Responsável:** I4 · **Estimativa:** 6h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T15, T08  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Permitir que o administrador crie, liste, consulte, edite e exclua usuários dos quatro perfis, com as regras de conta que protegem o sistema (por exemplo, não excluir o último administrador).

## 2. Contexto: o que já existe e deve ser reaproveitado

- DTOs e validações: T15 (`docs/sprint1/task-dtos-validacoes-usuarios.md`).
- Entidade e repositório: T08 (`domain/model/Usuario.java`, `repository/UsuarioRepository.java`).
- Senha com BCrypt (`PasswordEncoder` da T19).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/UsuarioController.java` | Existente | `/api/usuarios`, com `@PreAuthorize("hasRole('ADMINISTRADOR')")` na classe |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/UsuarioService.java` | Existente | Regras de negócio e persistência |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/repository/UsuarioRepository.java` | Existente | Consultas por e-mail e contagem de administradores ativos |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/ApiExceptionHandler.java` | Existente | Converte exceções em 400, 401, 403, 404 e 409 |

## 4. O que fazer em cada arquivo

### UsuarioController

- `GET /api/usuarios`, `GET /api/usuarios/{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`.
- Criação responde `201` com `Location`; exclusão responde `204`.

### UsuarioService

- Normalizar o e-mail para minúsculas antes de gravar e de comparar; e-mail repetido é recusado.
- Gravar a senha com o `PasswordEncoder`; nunca devolver a senha (o `UsuarioResponse` não a tem).
- O administrador não pode excluir, desativar nem rebaixar a própria conta.
- O último administrador ativo não pode deixar de ser ativo.
- Não excluir usuário que é responsável por estudo (a FK é `ON DELETE RESTRICT`): responder 400 orientando a desativar.

## 5. Contrato, regras e códigos de erro

- Códigos: `400` validação ou regra de conta, `401` sem token, `403` perfil diferente de administrador, `404` usuário inexistente.
- Corpo de erro: `{"erro": "mensagem"}`.

## 6. Testes

- `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/UsuarioIntegrationTest.java`, `RegrasDeContaIntegrationTest.java` e `UsuarioDtoValidationTest.java` (T18).

## 7. Critérios de aceite

- [ ] O administrador cria, consulta, edita e exclui usuários dos quatro perfis.
- [ ] Os demais perfis recebem 403.
- [ ] Regras de conta cobertas por teste.

## 8. Cuidados e fora de escopo

- Qualquer nova regra de conta entra no `UsuarioService`, nunca no controller.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
