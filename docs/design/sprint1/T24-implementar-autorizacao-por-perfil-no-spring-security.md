# T24 · Implementar autorização por perfil no Spring Security

> **Sprint 1** · PBI-06 · RF03 Controle de acesso por perfil (web)  
> **Responsável:** I4 · **Estimativa:** 5h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T19, T23  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Aplicar no backend a matriz de permissões: cada endpoint declara quais perfis o acessam, e quem não tem permissão recebe 403.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Matriz RBAC: `docs/sprint1/arquitetura.md`, seção 4.
- Na Sprint 2 as expressões foram reunidas em `security/Permissoes`.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/security/SecurityConfig.java` | Existente | `@EnableMethodSecurity` habilita o `@PreAuthorize` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/security/Permissoes.java` | Existente | Constantes `LEITURA`, `ESCRITA_PLANEJAMENTO` e `ESCRITA_RECRUTAMENTO` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/UsuarioController.java` | Existente | Só `ADMINISTRADOR` |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/EstudoController.java` | Existente | Leitura para pesquisador e coordenador; escrita só para pesquisador |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/controller/ApiExceptionHandler.java` | Existente | `AccessDeniedException` → `403 {"erro": "Acesso negado para o seu perfil"}` |

## 4. O que fazer em cada arquivo

### Como aplicar a um endpoint novo

- Anotar o método com `@PreAuthorize(Permissoes.X)`; nunca confiar só em esconder o botão na web.
- Pesquisador escreve planejamento (critérios, visitas, estado); coordenador escreve recrutamento (candidatos, avaliação, consentimento, inclusão); ambos leem; administrador não acessa o recrutamento nem os estudos.

## 6. Testes

- `PermissaoAcessoIntegrationTest` (T26).

## 7. Critérios de aceite

- [ ] Perfil sem permissão recebe 403 em JSON.
- [ ] Toda rota de `/api/**` tem uma regra explícita.

## 8. Cuidados e fora de escopo

- Ao criar um controller novo, acrescentar a linha correspondente em `PermissaoAcessoIntegrationTest`.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
