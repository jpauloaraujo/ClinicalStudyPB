# T23 · Definir matriz de permissões dos quatro perfis

> **Sprint 1** · PBI-06 · RF03 Controle de acesso por perfil (web)  
> **Responsável:** I1 · **Estimativa:** 2h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T01  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Definir quem pode fazer o quê, para que o backend (T24) e a web (T25) apliquem a mesma regra.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Matriz completa de RF01 a RF22: `docs/sprint1/arquitetura.md`, seção 4.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `docs/sprint1/arquitetura.md` | Existente | Seção 4: matriz RBAC |
| `backend/ClinicalStudyPB/src/main/java/UEPB/ESWII/ClinicalStudyPB/security/Permissoes.java` | Existente | Expressões `@PreAuthorize` do recrutamento |
| `web/ClinicalStudyPB/src/routes/navegacao.ts` | Existente | Perfis de cada item de menu e rota |
| `web/ClinicalStudyPB/src/pages/estudos/detalhe/abas.ts` | Existente | Perfis de cada aba do estudo |

## 4. O que fazer em cada arquivo

### Resumo do que está aplicado (RF01 a RF11)

- Administrador: gerencia usuários (RF01); não acessa estudos nem recrutamento.
- Pesquisador: cadastra e edita estudos (RF04), critérios (RF05), visitas (RF06) e altera o estado (RF07); lê candidatos e participantes.
- Coordenador: lê estudos, critérios e visitas; cadastra candidatos, avalia, registra consentimento e inclui (RF08 a RF11).
- Participante: usa só o aplicativo mobile; recebe 403 nos endpoints de gestão.

### Onde cada regra é aplicada

- Backend: `@PreAuthorize` com `Permissoes.LEITURA`, `ESCRITA_PLANEJAMENTO` (pesquisador) e `ESCRITA_RECRUTAMENTO` (coordenador).
- Web: `RequerPerfil` com os perfis de `navegacao.ts` e `abas.ts`.

### Para uma funcionalidade nova

- Acrescentar a linha à matriz do DAS, definir a constante em `Permissoes` (se for nova), aplicar no controller e na rota, e cobrir com teste de 403.

## 7. Critérios de aceite

- [ ] Matriz aprovada pela equipe.
- [ ] Backend e web aplicam exatamente os mesmos perfis.

## 8. Cuidados e fora de escopo

- A matriz do DAS descreve o produto completo (RF01 a RF22); a aplicação cobre RF01 a RF11 nesta Release.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
