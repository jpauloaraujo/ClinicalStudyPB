# T18 · Implementar testes unitários e de integração do módulo de usuários

> **Sprint 1** · PBI-04 · RF01 Cadastro e gerenciamento de usuários  
> **Responsável:** I2 · **Estimativa:** 5h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T15, T16  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Provar com testes automáticos que o cadastro e o gerenciamento de usuários cumprem as validações e as regras de conta.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Helper de login real: `support/TokenDeTeste.java` (usa os usuários do seed V2; senha `senha-de-teste`).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/UsuarioDtoValidationTest.java` | Existente | Validação dos DTOs, sem Spring |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/UsuarioIntegrationTest.java` | Existente | CRUD completo pelo MockMvc com token de administrador |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/RegrasDeContaIntegrationTest.java` | Existente | Autoexclusão, último administrador, e-mail repetido e simultâneo |
| `backend/ClinicalStudyPB/src/test/java/UEPB/ESWII/ClinicalStudyPB/support/TokenDeTeste.java` | Existente | Obtém `Bearer` por `/api/auth/login` |

## 4. O que fazer em cada arquivo

### O que cada classe cobre

- `UsuarioDtoValidationTest`: nome, e-mail e senha nos limites.
- `UsuarioIntegrationTest`: cadastro sem expor a senha, listagem e consulta, atualização (com e sem nova senha), troca de senha, e-mail duplicado, senha obrigatória no cadastro e curta na atualização, exclusão, usuário responsável por estudo e 404.
- `RegrasDeContaIntegrationTest`: o administrador não se exclui, desativa nem rebaixa; o último administrador ativo é protegido.

## 6. Testes

- `./mvnw test` em `backend/ClinicalStudyPB`; rodam em H2 no CI e em PostgreSQL no job `backend-postgres`.

## 7. Critérios de aceite

- [ ] Todos os testes passam.
- [ ] Cada regra de conta tem um teste que falha se a regra for removida.

## 8. Cuidados e fora de escopo

- Testes de integração usam `@Transactional` para reverter os dados; não depender da ordem de execução.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
