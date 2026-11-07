# T20 · Implementar endpoint de login e validação de credenciais

> **Sprint 1** · PBI-05 · RF02 Autenticação e logout (web)  
> **Responsável:** I4 · **Estimativa:** 4h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T19  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Receber e-mail e senha, validar e devolver o token; e oferecer `GET /api/auth/me` para a web e o mobile montarem a sessão (a resposta do login só traz o token).

## 2. Contexto: o que já existe e deve ser reaproveitado

- O `LoginResponse` não traz o perfil, e o teste de T22 garante isso.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/AuthController.java` | Existente | `POST /api/auth/login` e `GET /api/auth/me` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/service/AuthService.java` | Existente | Valida credenciais e emite o token |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/LoginRequest.java` | Existente | E-mail válido e senha obrigatórios |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/dto/LoginResponse.java` | Existente | `token` e `tipo` ("Bearer") |

## 4. O que fazer em cada arquivo

### AuthService

- Buscar o usuário pelo e-mail em minúsculas.
- E-mail inexistente, senha incorreta e usuário inativo devolvem a **mesma** `BadCredentialsException("Credenciais inválidas")`, para não revelar se o e-mail existe.

### AuthController

- `login` devolve 200 com o token; `usuarioAutenticado` lê o `sub` do JWT (`@AuthenticationPrincipal Jwt`) e devolve `UsuarioResponse`.

## 5. Contrato, regras e códigos de erro

- `POST /api/auth/login` → `200 {token, tipo}`, `400` corpo inválido, `401 {"erro": "Credenciais inválidas"}`.
- `GET /api/auth/me` → `200` com nome, e-mail e perfil; `401` sem token.

## 6. Testes

- `AuthenticationIntegrationTest` (13 testes).

## 7. Critérios de aceite

- [ ] Credenciais válidas devolvem token; inválidas, 401 com mensagem genérica.
- [ ] Usuário desativado não entra.

## 8. Cuidados e fora de escopo

- Nunca registrar a senha em log.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
