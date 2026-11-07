# T65 · Infraestrutura de testes E2E (Playwright)

> **Sprint 2** · PBI-17 · Qualidade, integração e dados de demonstração  
> **Responsável:** I7 · **Estimativa:** 3h · **Prazo:** Sex 30/10  
> **Depende de:** Sprint 1 (login web, T21)  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Ter testes de ponta a ponta no navegador rodando localmente e no CI, com sessão por perfil reaproveitada, page objects e o primeiro conjunto de testes (login válido, inválido e logout).

## 2. Contexto: o que já existe e deve ser reaproveitado

- Documentos anteriores: `docs/sprint2/T65 — Requisitos e critérios de aceite (E2E com Playwright).md` (16 requisitos) e `docs/sprint2/T65 — Implementação da infraestrutura E2E.md`.
- A seção "Testes E2E" do `README.md` documenta os comandos.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/playwright.config.ts` | Existente | Chromium, URL base por variável, retries só no CI, trace na primeira repetição; projeto `setup` e projetos de teste que dependem dele |
| `web/ClinicalStudyPB/e2e/setup/auth.setup.ts` | Existente | Cria os usuários de teste e salva a sessão de cada perfil em `e2e/.auth/` a cada execução |
| `web/ClinicalStudyPB/e2e/support/env.ts, sessao.ts` | Existente | Variáveis (`.env.e2e`) e caminho da sessão por perfil |
| `web/ClinicalStudyPB/e2e/fixtures/index.ts` | Existente | Fixtures: `paginaLogin` e a lista de perfis `perfisE2E` |
| `web/ClinicalStudyPB/e2e/pages/LoginPage.ts, InicioPage.ts, BarraNavegacao.ts` | Existente | Page objects com `getByRole`/`getByLabel` |
| `web/ClinicalStudyPB/e2e/tests/login.spec.ts, sessao.spec.ts` | Existente | Login válido por perfil, senha errada, e-mail inexistente, logout e rota protegida |
| `web/ClinicalStudyPB/.env.e2e.example` | Existente | Valores fictícios; `.env.e2e` e `e2e/.auth/` ficam fora do Git |
| `.github/workflows/ci.yml` | Existente | Job `e2e`: PostgreSQL, backend, web (`vite preview`), Chromium e relatório como artefato `playwright-report` |

## 4. O que fazer em cada arquivo

### Requisitos (resumo dos 16 do documento de requisitos)

- RQ-01 a RQ-03: Playwright com versão fixa, `playwright.config.ts` e configuração por variáveis de ambiente.
- RQ-04 e RQ-05: usuários de teste reproduzíveis e login por API com sessão por perfil.
- RQ-06 a RQ-10: page object do login e testes de login válido, inválido, logout, estáveis e independentes (sem `waitForTimeout`).
- RQ-11 e RQ-12: job no GitHub Actions e artefatos (relatório e traces).
- RQ-13 a RQ-15: comando documentado no README, qualidade e segurança (sem credencial real), estrutura reaproveitável pela T66.

### Como usar

- `cd web/ClinicalStudyPB`; `cp .env.e2e.example .env.e2e`; subir backend e web; `npm run test:e2e` (todos), `npx playwright test e2e/tests/login.spec.ts` (um), `npm run test:e2e:headed`, `npm run test:e2e:report`.

### O que ainda falta fora do código

- Ver o job `e2e` verde no GitHub, quebrar um teste de propósito para ver o job vermelho e o trace, revisão do PR, alguém rodar só pelo README e atualizar o Azure Boards.

### Verificação de 07/11/2026

- A suíte E2E foi executada localmente contra o backend real (H2 em memória) e a web: 25 passam e 4 ficam ignorados (pendentes de T58/T61), em duas execuções seguidas e com `--repeat-each=5` (117 passam, nenhuma falha).
- Foi acrescentada a verificação de ambiente `e2e/setup/ambiente.setup.ts` (backend responde, web abre, MSW desligado), que roda junto do setup de sessões.

## 6. Testes

- 10 testes E2E passam localmente (`login.spec.ts` e `sessao.spec.ts`).

## 7. Critérios de aceite

- [ ] O teste roda no CI com um comando documentado.
- [ ] Falha forçada gera relatório e trace baixáveis.

## 8. Cuidados e fora de escopo

- `e2e/.auth/*.json` guarda tokens: não versionar (o `.gitignore` já ignora).

---
Parte do conjunto de documentos de design da Sprint 2: [índice](README.md).
