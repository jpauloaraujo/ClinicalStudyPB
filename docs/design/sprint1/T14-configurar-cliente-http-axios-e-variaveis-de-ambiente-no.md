# T14 · Configurar cliente HTTP Axios e variáveis de ambiente no React

> **Sprint 1** · PBI-03 · Inicialização das aplicações web e mobile  
> **Responsável:** I5 · **Estimativa:** 2h · **Prazo:** Sprint 1 (13/10 a 26/10/2026)  
> **Depende de:** T11  
> **Situação:** ✅ Implementada (este documento descreve o que existe, para manter a coerência)

## 1. Objetivo

Ter um único cliente HTTP configurado, com a URL da API vinda de variável de ambiente, o token enviado em toda requisição e a sessão encerrada quando a API responde 401.

## 2. Contexto: o que já existe e deve ser reaproveitado

- Todas as chamadas da web (`src/services/recursos.ts` e `src/api/*.ts`) usam este cliente.

## 3. Onde implementar

| Arquivo | Ação | Responsabilidade |
|---|---|---|
| `web/ClinicalStudyPB/src/services/api.ts` | Existente | Instância Axios, leitura/gravação do token e interceptors |
| `web/ClinicalStudyPB/.env.example` | Existente | Modelo com `VITE_API_URL` |
| `web/ClinicalStudyPB/src/vite-env.d.ts` | Existente | Tipos das variáveis `VITE_*` |

## 4. O que fazer em cada arquivo

### src/services/api.ts

- `URL_API = import.meta.env.VITE_API_URL ?? "http://localhost:8081"`.
- Funções para ler, gravar e remover o token no `localStorage` (chave `CHAVE_TOKEN`).
- Interceptor de requisição: acrescenta `Authorization: Bearer <token>` quando há token.
- Interceptor de resposta: em `401` fora da tela de login, chama o callback de sessão expirada (registrado pelo `SessaoProvider`).

### .env.example

- Documentar `VITE_API_URL` e, desde a T40, `VITE_USE_MOCKS`; cada pessoa copia para `.env` (que não vai ao Git).

## 6. Testes

- Coberto indiretamente pelos testes de componente e pelo E2E de sessão (`e2e/tests/sessao.spec.ts`).

## 7. Critérios de aceite

- [ ] Mudar `VITE_API_URL` redireciona todas as chamadas.
- [ ] Requisições autenticadas levam o cabeçalho `Authorization`.
- [ ] Um `401` fora do login derruba a sessão e leva ao login.

## 8. Cuidados e fora de escopo

- Não colocar a URL da API fixa em nenhuma tela.
- Variáveis do Vite só são lidas na inicialização: reiniciar o `npm run dev` depois de mudar o `.env`.

---
Parte do conjunto de documentos de design da Sprint 1: [índice](README.md).
