# T22 · Criar testes de login válido, inválido e acesso não autenticado

> **Sprint 1** · PBI-05 · RF02 Autenticação e logout (web)  
> **Responsável:** I7 · **Estimativa:** 3h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T20  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Garantir por teste que o login funciona, que credenciais erradas são recusadas sem vazar informação e que rotas protegidas exigem token.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/AuthenticationIntegrationTest.java` | Existente | 13 testes de integração do fluxo de autenticação |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/support/TokenDeTeste.java` | Existente | Login real nos testes de outros módulos |

## 4. O que fazer em cada arquivo

### Cenários cobertos

- Login válido para cada perfil devolve token e `tipo`.
- E-mail inexistente, senha errada e usuário inativo: 401 com a mesma mensagem.
- Corpo inválido: 400. Rota protegida sem token: 401. Token adulterado: 401.
- `GET /api/auth/me` devolve o dono do token.

## 6. Testes

- `./mvnw test`.

## 7. Critérios de aceite

- [ ] Os 13 testes passam.
- [ ] A resposta do login não contém o perfil nem a senha.

## 8. Cuidados e fora de escopo

- Não usar senha real; o seed V2 usa `senha-de-teste`.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
