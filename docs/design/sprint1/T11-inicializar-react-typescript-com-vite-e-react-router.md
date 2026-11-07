# T11 · Inicializar React + TypeScript com Vite e React Router

> **Sprint 1** · PBI-03 · Inicialização das aplicações web e mobile  
> **Responsável:** I5 · **Estimativa:** 4h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T01  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Criar o projeto web do zero com Vite, React, TypeScript e React Router, com lint e build funcionando, para que as telas das demais tasks tenham onde ser encaixadas.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Projeto em `web/ClinicalStudyPB/`. Versões atuais: React 19, TypeScript 6, Vite 8, React Router 7 (ver `package.json`).
- Layout e navegação posteriores: T13 (`docs/sprint1/implementacao-web-layout-navegacao.md`).

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/package.json` | Existente | Dependências e scripts (`dev`, `build`, `lint`, `test`, `test:e2e`) |
| `web/ClinicalStudyPB/vite.config.ts` | Existente | Configuração do Vite e do Vitest |
| `web/ClinicalStudyPB/index.html` | Existente | Página única da SPA |
| `web/ClinicalStudyPB/src/main.tsx` | Existente | Ponto de entrada: monta o `RouterProvider` e o `SessaoProvider` |
| `web/ClinicalStudyPB/src/routes/router.tsx` | Existente | Árvore de rotas (exportada como `rotas` para os testes) |
| `web/ClinicalStudyPB/tsconfig*.json, eslint.config.js` | Existente | TypeScript estrito e regras de lint |

## 4. O que fazer em cada arquivo

### Criação do projeto

- `npm create vite@latest` com o template React + TypeScript dentro de `web/ClinicalStudyPB`.
- Instalar `react-router`; remover os arquivos de exemplo do template (ver lista em T13).

### Rotas

- Usar `createBrowserRouter` em `src/routes/router.tsx`; exportar o array `rotas` para os testes montarem a mesma árvore com `createMemoryRouter`.

### Scripts

- `npm run dev` (porta 5173, a que o CORS do backend libera), `npm run build`, `npm run lint`.

## 6. Testes

- `npm run lint` e `npm run build` sem erros (rodam no job `web` do CI).

## 7. Critérios de aceite

- [ ] `npm run dev` abre a aplicação em http://localhost:5173.
- [ ] Lint e build passam.

## 8. Cuidados e fora de escopo

- A porta 5173 é a única liberada no CORS do backend para a web; mudar exige alterar `SecurityConfig`.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
